package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.StitchClipsThread;
import com.kamcord.app.utils.StitchClipsThread.StitchSuccessListener;
import com.kamcord.app.utils.VideoUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTouch;

public class ShareFragment extends Fragment {
    public static final String ARG_RECORDING_SESSION = "recording_session";

    @InjectView(R.id.share_scrollview) ScrollView scrollView;
    @InjectView(R.id.thumbnailImageView) ImageView thumbnailImageView;
    @InjectView(R.id.playImageView) ImageView playImageView;
    @InjectView(R.id.shareButton) Button shareButton;
    @InjectView(R.id.titleEditText) EditText titleEditText;
    @InjectView(R.id.descriptionEditText) EditText descriptionEditText;
    @InjectView(R.id.videoDurationTextView) TextView videoDurationTextView;
    @InjectView(R.id.processingProgressBarContainer) ViewGroup processingProgressBarContainer;
    @InjectView(R.id.share_toolbar) Toolbar mToolbar;

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
        }

        @Override
        public void onMergeFailure(RecordingSession recordingSession) {
            // TODO: show the user something about failing to process the video.
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_share, container, false);

        ButterKnife.inject(this, root);
        RecordActivity activity = ((RecordActivity)getActivity());
        activity.setSupportActionBar(mToolbar);
        ActionBar actionbar = activity.getSupportActionBar();
        actionbar.setTitle("");
        actionbar.setDisplayHomeAsUpEnabled(true);

        recordingSession = getArguments().getParcelable(ARG_RECORDING_SESSION);

        File videoFile = new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                FileSystemManager.MERGED_VIDEO_FILENAME);
        if (videoFile.exists()) {
            videoPrepared(videoFile);
        } else {
            processingProgressBarContainer.setVisibility(View.VISIBLE);
            playImageView.setVisibility(View.GONE);
            StitchClipsThread stitchClipsThread = new StitchClipsThread(recordingSession,
                    getActivity().getApplicationContext(),
                    stitchSuccessListener);
            stitchClipsThread.start();
        }
        return root;
    }

    @OnTouch({R.id.titleEditText, R.id.descriptionEditText})
    public boolean scrollToBottom() {
        scrollView.smoothScrollTo(0, scrollView.getBottom());
        return false;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        ButterKnife.reset(this);
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

    @OnClick(R.id.shareButton)
    public void share() {

        if(AccountManager.isLoggedIn()) {
            recordingSession.setVideoTitle(titleEditText.getEditableText().toString());
            recordingSession.setVideoDescription(descriptionEditText.getEditableText().toString());

            Intent uploadIntent = new Intent(getActivity(), UploadService.class);
            uploadIntent.putExtra(UploadService.ARG_SESSION_TO_SHARE, recordingSession);
            getActivity().startService(uploadIntent);
        }
        else
        {
            Toast.makeText(getActivity(), getResources().getString(R.string.youMustBeLoggedIn), Toast.LENGTH_SHORT);
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(intent);
        }
    }

    private void videoPrepared(File videoFile) {
        String videoPath = videoFile.getAbsolutePath();
        thumbnailImageView.setImageBitmap(VideoUtils.getVideoThumbnail(videoPath));
        String videoDurationStr = VideoUtils.getVideoDuration(videoPath);
        videoDurationTextView.setText(videoDurationStr);
        processingProgressBarContainer.setVisibility(View.GONE);
        playImageView.setVisibility(View.VISIBLE);
    }

    private void videoProcessing() {
        processingProgressBarContainer.setVisibility(View.VISIBLE);
        playImageView.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
