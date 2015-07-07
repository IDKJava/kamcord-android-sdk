package com.kamcord.app.fragment;

import android.accounts.Account;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.activity.VideoViewActivity;
import com.kamcord.app.adapter.MainViewPagerAdapter;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.service.UploadService;
import com.kamcord.app.thread.StitchClipsThread;
import com.kamcord.app.thread.StitchClipsThread.StitchSuccessListener;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.ActiveRecordingSessionManager;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.KeyboardUtils;
import com.kamcord.app.utils.StringUtils;
import com.kamcord.app.utils.VideoUtils;
import com.kamcord.app.view.utils.OnBackPressedListener;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import butterknife.OnTouch;
import rx.Observable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;

public class ShareFragment extends Fragment implements OnBackPressedListener {
    public static final String TAG = ShareFragment.class.getSimpleName();
    public static final String ARG_RECORDING_SESSION = "recording_session";
    public static final int YOUTUBE_REQUEST_AUTHORIZATION_CODE = 0x0000fafa;
    public static final int YOUTUBE_CHOOSE_ACCOUNT_CODE = 0x0000fefe;

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
    @InjectView(R.id.share_button)
    Button shareButton;
    @InjectViews({R.id.share_twitterbutton, R.id.share_youtubebutton})
    List<FrameLayout> shareSourceButtonViews;
    @InjectView(R.id.twitterLoginButton)
    TwitterLoginButton twitterLoginButton;

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
            videoPrepared(recordingSession);
            FileSystemManager.deleteUnmerged(recordingSession);
        }

        @Override
        public void onMergeFailure(RecordingSession recordingSession) {
            // TODO: show the user something about failing to process the video.
        }
    };
    private StitchClipsThread stitchClipsThread;
    private HashMap<Integer, Boolean> shareSourceHashMap = new HashMap<>();
    private static final int TWITTER_INDEX = 0;
    private static final int YOUTUBE_INDEX = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_share, container, false);
        ButterKnife.inject(this, root);
        initShareSourceHashMap();

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha, null);
        upArrow.setColorFilter(getResources().getColor(R.color.ColorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationIcon(upArrow);
        mToolbar.setTitle(R.string.fragmentShare);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtils.hideSoftKeyboard(titleEditText, getActivity().getApplicationContext());
                getActivity().onBackPressed();
            }
        });

        recordingSession = new Gson().fromJson(getArguments().getString(ARG_RECORDING_SESSION), RecordingSession.class);

        File videoFile = new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                FileSystemManager.MERGED_VIDEO_FILENAME);
        if (videoFile.exists()) {
            videoPrepared(recordingSession);
        } else {
            processingProgressBarContainer.setVisibility(View.VISIBLE);
            playImageView.setVisibility(View.GONE);

            shareButton.setEnabled(false);
            thumbnailImageView.setEnabled(false);

            stitchClipsThread = new StitchClipsThread(recordingSession,
                    getActivity().getApplicationContext(),
                    stitchSuccessListener);
            stitchClipsThread.start();
        }


        titleEditText.setHint(StringUtils.defaultVideoTitle(getActivity(), recordingSession));

        twitterLoginButton.setCallback(
                new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {
                        if (shareSourceButtonViews.get(TWITTER_INDEX) != null) {
                            shareSourceButtonViews.get(TWITTER_INDEX).setSelected(true);
                        }
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        if (shareSourceButtonViews.get(TWITTER_INDEX) != null) {
                            shareSourceButtonViews.get(TWITTER_INDEX).setSelected(false);
                        }
                    }
                }
        );

        return root;
    }

    @OnTouch(R.id.titleEditText)
    public boolean scrollToBottom() {
        titleEditText.setHint("");
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
                        } else {
                            titleEditText.setHint(StringUtils.defaultVideoTitle(getActivity(), recordingSession));
                        }
                    }
                });
        return false;
    }



    @OnClick(R.id.share_button)
    public void click(View v) {
        if (AccountManager.isLoggedIn()) {
            if (titleEditText.getEditableText().length() > 0) {
                recordingSession.setVideoTitle(titleEditText.getEditableText().toString().trim());
            } else {
                recordingSession.setVideoTitle(StringUtils.defaultVideoTitle(getActivity(), recordingSession));
            }
            if (shareSourceHashMap.size() > 0) {
                recordingSession.setShareSources(shareSourceHashMap);
            }

            recordingSession.setState(RecordingSession.State.SHARED);
            recordingSession.setShareAppSessionId(KamcordAnalytics.getCurrentAppSessionId());
            ActiveRecordingSessionManager.updateActiveSession(recordingSession);

            KeyboardUtils.hideSoftKeyboard(titleEditText, getActivity().getApplicationContext());
            Intent uploadIntent = new Intent(getActivity(), UploadService.class);
            uploadIntent.putExtra(UploadService.ARG_SESSION_TO_SHARE, new Gson().toJson(recordingSession));
            getActivity().startService(uploadIntent);
            if (getActivity() instanceof RecordActivity) {
                ((RecordActivity) getActivity()).setCurrentItem(MainViewPagerAdapter.PROFILE_FRAGMENT_POSITION);
            }
            showDeleteDialogOnBack = false;
            getActivity().onBackPressed();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.youMustBeLoggedIn), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(intent);
        }

    }

    @OnClick({R.id.share_twitterbutton, R.id.share_youtubebutton})
    public void onClick(FrameLayout button) {

        if (button.isActivated()) {
            button.setActivated(false);
            shareSourceHashMap.put(button.getId(), false);

        } else {
            if (isLoggedInToExternalNetwork(button.getId())) {
                button.setActivated(true);
                shareSourceHashMap.put(button.getId(), true);

            } else {
                logInToExternalNetwork(button.getId());

            }
        }
    }

    private boolean isLoggedInToExternalNetwork(int networkId) {
        boolean isLoggedIn = false;

        switch (networkId) {
            case R.id.share_twitterbutton:
                isLoggedIn = Twitter.getSessionManager().getActiveSession() != null;
                break;

            case R.id.share_youtubebutton:
                isLoggedIn = AccountManager.YouTube.getStoredAuthorizationCode() != null
                        && AccountManager.YouTube.getStoredRefreshToken() != null;
                break;
        }

        return isLoggedIn;
    }

    private void logInToExternalNetwork(int networkId) {
        switch (networkId) {
            case R.id.share_twitterbutton:
                twitterLoginButton.callOnClick();
                break;

            case R.id.share_youtubebutton:
                android.accounts.Account youTubeAccount = AccountManager.YouTube.getStoredAccount();
                if (youTubeAccount != null) {
                    AccountManager.YouTube.fetchAuthorizationCode(getActivity(), YOUTUBE_REQUEST_AUTHORIZATION_CODE);
                } else {
                    Intent chooseAccountIntent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
                    startActivityForResult(chooseAccountIntent, YOUTUBE_CHOOSE_ACCOUNT_CODE);
                }
                break;
        }
    }

    public void initShareSourceHashMap() {
        for (FrameLayout shareSourceButton : shareSourceButtonViews) {
            shareSourceHashMap.put(shareSourceButton.getId(), false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mToolbar.setNavigationOnClickListener(null);
        ButterKnife.reset(this);
        if (stitchClipsThread != null) {
            stitchClipsThread.cancelStitching();
        }
    }

    @OnClick(R.id.thumbnailImageView)
    public void showVideoPreviewActivity() {
        Intent intent = new Intent(getActivity().getApplicationContext(), VideoViewActivity.class);

        Uri uri = Uri.parse(new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession)
                , FileSystemManager.MERGED_VIDEO_FILENAME).getAbsolutePath());
        intent.setData(uri);
        intent.putExtra(VideoViewActivity.ARG_VIDEO_TYPE, VideoViewActivity.VideoType.MP4);
        intent.putExtra(VideoViewActivity.ARG_IS_LIVE, false);
        startActivity(intent);

        recordingSession.setWasReplayed(true);
        ActiveRecordingSessionManager.updateActiveSession(recordingSession);
    }

    private void videoPrepared(RecordingSession recordingSession) {
        if( !this.recordingSession.equals(recordingSession) ) {
            // TODO: show the user something about this funky state we're in.
            return;
        }

        if (thumbnailImageView != null ) {
            File thumbnailFile = VideoUtils.getVideoThumbnailFile(recordingSession);
            Picasso.with(getActivity())
                    .load(thumbnailFile)
                    .into(thumbnailImageView);
        }

        String videoDurationStr = VideoUtils.getVideoDuration(recordingSession);
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

        shareButton.setEnabled(true);
        thumbnailImageView.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (twitterLoginButton != null) {
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == YOUTUBE_REQUEST_AUTHORIZATION_CODE) {
            if (shareSourceButtonViews != null && shareSourceButtonViews.get(YOUTUBE_INDEX) != null) {
                View youTubeButton = shareSourceButtonViews.get(YOUTUBE_INDEX);
                if (resultCode == Activity.RESULT_OK) {
                    youTubeButton.setActivated(true);
                    shareSourceHashMap.put(youTubeButton.getId(), true);
                    AccountManager.YouTube.fetchAuthorizationCode(getActivity(), -1);
                } else {
                    youTubeButton.setActivated(false);
                    shareSourceHashMap.put(youTubeButton.getId(), false);
                    AccountManager.YouTube.clearStoredAccount();
                }
            }
        }

        if (requestCode == YOUTUBE_CHOOSE_ACCOUNT_CODE) {
            if (shareSourceButtonViews != null && shareSourceButtonViews.get(YOUTUBE_INDEX) != null) {
                View youTubeButton = shareSourceButtonViews.get(YOUTUBE_INDEX);
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null && !accountName.isEmpty()) {
                        Account youTubeAccount = new Account(accountName, "com.google");
                        AccountManager.YouTube.setStoredAccount(youTubeAccount);
                        logInToExternalNetwork(youTubeButton.getId());
                    }
                } else {
                    youTubeButton.setActivated(false);
                    shareSourceHashMap.put(youTubeButton.getId(), false);
                }
            }
        }
    }

    private boolean showDeleteDialogOnBack = true;

    @Override
    public boolean onBackPressed() {
        if (showDeleteDialogOnBack) {
            showDeleteDialogOnBack = false;
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.areYouSure)
                    .setMessage(R.string.youWillLoseMonster)
                    .setPositiveButton(R.string.deleteVideo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FileSystemManager.cleanRecordingSessionCacheDirectory(recordingSession);
                            getActivity().getSupportFragmentManager().popBackStack();
                            showDeleteDialogOnBack = true;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            showDeleteDialogOnBack = true;
                        }
                    }).show();
            return true;
        }
        return false;
    }
}
