package com.kamcord.app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.activity.VideoPreviewActivity;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.service.UploadService;
import com.kamcord.app.thread.StitchClipsThread;
import com.kamcord.app.thread.StitchClipsThread.StitchSuccessListener;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.KeyboardUtils;
import com.kamcord.app.utils.VideoUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTouch;
import rx.Observable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

public class ShareFragment extends Fragment {
    public static final String ARG_RECORDING_SESSION = "recording_session";

    @InjectView(R.id.share_scrollview)
    ScrollView scrollView;
    @InjectView(R.id.thumbnailImageView)
    ImageView thumbnailImageView;
    @InjectView(R.id.playImageView)
    ImageView playImageView;
    @InjectView(R.id.titleEditText)
    EditText titleEditText;
    @InjectView(R.id.videoDurationTextView)
    TextView videoDurationTextView;
    @InjectView(R.id.processingProgressBarContainer)
    ViewGroup processingProgressBarContainer;
    @InjectView(R.id.share_toolbar)
    Toolbar mToolbar;

    private Menu menu;
    private MenuItem shareMenuItem;
    private String videoPath;
    private RecordingSession recordingSession;
    private StitchSuccessListener stitchSuccessListener = new StitchSuccessListener() {
        @Override
        public void onVideoStitchSuccess(RecordingSession recordingSession) {

        }

        @Override
        public void onVideoStitchFailure(RecordingSession recordingSession) {

        }

        @Override
        public void onAudioStitchSuccess(RecordingSession recordingSession) {

        }

        @Override
        public void onAudioStitchFailure(RecordingSession recordingSession) {

        }

        @Override
        public void onMergeSuccess(RecordingSession recordingSession) {
            videoPrepared(new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                    FileSystemManager.MERGED_VIDEO_FILENAME));
            FileSystemManager.deleteUnmerged(recordingSession);
        }

        @Override
        public void onMergeFailure(RecordingSession recordingSession) {
            // TODO: show the user something about failing to process the video.
        }
    };
    private StitchClipsThread stitchClipsThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_share, container, false);
        ButterKnife.inject(this, root);
        RecordActivity activity = ((RecordActivity) getActivity());
        setHasOptionsMenu(true);
        activity.setSupportActionBar(mToolbar);
        ActionBar actionbar = activity.getSupportActionBar();
        actionbar.setTitle(getResources().getString(R.string.fragmentShare));
        actionbar.setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha, null);
        upArrow.setColorFilter(getResources().getColor(R.color.ColorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        actionbar.setHomeAsUpIndicator(upArrow);

        recordingSession = getArguments().getParcelable(ARG_RECORDING_SESSION);

        File videoFile = new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                FileSystemManager.MERGED_VIDEO_FILENAME);
        if (videoFile.exists()) {
            videoPrepared(videoFile);
        } else {
            processingProgressBarContainer.setVisibility(View.VISIBLE);
            playImageView.setVisibility(View.GONE);
            stitchClipsThread = new StitchClipsThread(recordingSession,
                    getActivity().getApplicationContext(),
                    stitchSuccessListener);
            stitchClipsThread.start();
        }
        return root;
    }

    @OnTouch(R.id.titleEditText)
    public boolean scrollToBottom() {
        scrollView.smoothScrollTo(0, scrollView.getBottom());
        KeyboardUtils.hideSoftKeyboard(titleEditText, getActivity().getApplicationContext());

        Observable<OnTextChangeEvent> editTextObservable = WidgetObservable.text(titleEditText);
        editTextObservable
                .map(new Func1<OnTextChangeEvent, Integer>() {
                    @Override
                    public Integer call(OnTextChangeEvent onTextChangeEvent) {
                        return onTextChangeEvent.text().length();
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer textLength) {
                        if (textLength > 0) {
                            if (menu != null && shareMenuItem != null) {
//                                ActionMenuDecorator.setMenuItemColor(shareMenuItem, getResources().getColor(R.color.kamcordGreen));
                                shareMenuItem.setEnabled(true);
                            }
                        } else {
//                            ActionMenuDecorator.setMenuItemColor(shareMenuItem, getResources().getColor(R.color.ColorPrimaryDark));
                            shareMenuItem.setEnabled(false);
                        }
                    }
                });

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if (stitchClipsThread != null) {
            stitchClipsThread.cancelStitching();
        }
    }

    @OnClick(R.id.thumbnailImageView)
    public void showVideoPreviewActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(VideoPreviewActivity.ARG_VIDEO_PATH,
                new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                        FileSystemManager.MERGED_VIDEO_FILENAME).getAbsolutePath());
        Intent intent = new Intent(getActivity().getApplicationContext(), VideoPreviewActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void videoPrepared(File videoFile) {
        videoPath = videoFile.getAbsolutePath();
        Bitmap bitmap = VideoUtils.getVideoThumbnail(videoPath);
        if (thumbnailImageView != null && bitmap != null) {
            thumbnailImageView.setImageBitmap(bitmap);
        }
        String videoDurationStr = VideoUtils.getVideoDuration(videoPath);
        if (videoDurationTextView != null && videoDurationStr != null) {
            videoDurationTextView.setVisibility(View.VISIBLE);
            videoDurationTextView.setText(videoDurationStr);
        }
        if (processingProgressBarContainer != null) {
            processingProgressBarContainer.setVisibility(View.GONE);
        }
        if (playImageView != null) {
            playImageView.setVisibility(View.VISIBLE);
        }
    }

    private void videoProcessing() {
        processingProgressBarContainer.setVisibility(View.VISIBLE);
        playImageView.setVisibility(View.GONE);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        this.shareMenuItem = menu.findItem(R.id.action_share_upload);
//        ActionMenuDecorator.setMenuItemColor(shareMenuItem, getResources().getColor(R.color.ColorPrimaryDark));
        this.shareMenuItem.getActionView().setBackgroundColor(getResources().getColor(R.color.kamcordGreen));
        shareMenuItem.setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                KeyboardUtils.hideSoftKeyboard(titleEditText, getActivity().getApplicationContext());
                getActivity().onBackPressed();
                break;
            case R.id.action_share_upload: {
                if (AccountManager.isLoggedIn()) {
                    recordingSession.setVideoTitle(titleEditText.getEditableText().toString());
                    Intent uploadIntent = new Intent(getActivity(), UploadService.class);
                    uploadIntent.putExtra(UploadService.ARG_SESSION_TO_SHARE, recordingSession);
                    getActivity().startService(uploadIntent);
                    getActivity().onBackPressed();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.youMustBeLoggedIn), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(intent);
                }
                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

}
