package com.kamcord.app.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kamcord.app.R;
import com.kamcord.app.activity.LoginActivity;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.player.Player;
import com.kamcord.app.server.callbacks.FollowCallback;
import com.kamcord.app.server.callbacks.UnfollowCallback;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.User;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.StringUtils;
import com.kamcord.app.utils.VideoUtils;
import com.kamcord.app.utils.ViewUtils;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by pplunkett on 7/6/15.
 */
public class LiveMediaControls implements MediaControls {
    private static final int FADE_DURATION_MS = 150;

    private MediaControls.ControlButtonClickListener controlButtonClickListener;

    private RelativeLayout root;

    private Video video;
    private Stream stream;
    private User owner;
    private MediaController.MediaPlayerControl playerControl;

    private boolean isScrubberTracking = false;
    private boolean isEnded = false;
    private int lastState = -1;

    @InjectView(R.id.live_indicator_textview)
    TextView liveIndicatorTextView;
    @InjectView(R.id.share_button)
    ImageButton shareButton;

    @InjectView(R.id.owner_container)
    ViewGroup ownerContainer;
    @InjectView(R.id.profile_letter_textview)
    TextView profileLetterTextView;
    @InjectView(R.id.avatar_imageview)
    ImageView avatarImageView;
    @InjectView(R.id.username_textview)
    TextView usernameTextView;
    @InjectView(R.id.follow_button)
    Button followButton;

    @InjectView(R.id.scrubber_container)
    ViewGroup scrubberContainer;
    @InjectView(R.id.scrubber_seekbar)
    SeekBar scrubberSeekBar;
    @InjectView(R.id.current_time_textview)
    TextView currentTimeTextView;
    @InjectView(R.id.total_time_textview)
    TextView totalTimeTextView;

    @InjectView(R.id.play_button_container)
    ViewGroup playButtonContainer;
    @InjectView(R.id.play_button)
    ImageButton playButton;
    @InjectView(R.id.buffering_progressbar)
    ProgressBar bufferingProgressBar;

    @InjectView(R.id.error_container)
    ViewGroup errorContainer;

    @InjectView(R.id.ended_container)
    ViewGroup endedContainer;
    @InjectView(R.id.stream_views)
    TextView streamViewsTextView;
    @InjectView(R.id.stream_length)
    TextView streamLengthTextView;
    @InjectView(R.id.stream_length_container)
    ViewGroup streamLengthContainer;
    @InjectView(R.id.stream_views_container)
    ViewGroup streamViewsContainer;

    public LiveMediaControls(Context context, Video video, Stream stream) {
        root = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.view_live_media_controls, null);
        ButterKnife.inject(this, root);
        this.video = video;
        this.stream = stream;
        if (video != null && video.user != null) {
            owner = video.user;
        } else if (stream != null && stream.user != null) {
            owner = stream.user;
        }
    }

    @Override
    public void setAnchorView(View anchorView) {
        if (anchorView instanceof ViewGroup) {
            ((ViewGroup) anchorView).addView(root);

            if (Build.VERSION.SDK_INT >= 21) {
                scrubberSeekBar.setSplitTrack(false);
            }

            if( owner != null && owner.id != null) {
                avatarImageView.setVisibility(View.GONE); // TODO: unhide this when we start receiving avatars from server

                usernameTextView.setText(owner.username);

                if (owner.username != null && owner.username.length() > 0) {
                    profileLetterTextView.setText(owner.username.substring(0, 1).toUpperCase());
                    CalligraphyUtils.applyFontToTextView(root.getContext(), profileLetterTextView, "fonts/proximanova_semibold.otf");
                }

                int profileColor = root.getResources().getColor(R.color.defaultProfileColor);
                try {
                    profileColor = Color.parseColor(owner.profile_color);
                } catch (Exception e) {
                }
                profileLetterTextView.getBackground().setColorFilter(profileColor, PorterDuff.Mode.MULTIPLY);
            }

            Account myAccount = AccountManager.getStoredAccount();
            if (owner != null && owner.id != null && !(myAccount != null && owner.id.equals(myAccount.id))) {
                if (owner.is_user_following == null)
                    owner.is_user_following = false;

                followButton.setActivated(owner.is_user_following);
                if(owner.is_user_following)
                {
                    followButton.setText(root.getContext().getResources().getString(R.string.videoFollowing));
                }
                else
                {
                    followButton.setText(root.getContext().getResources().getString(R.string.videoFollow));
                }
            }
            else {
                followButton.setVisibility(View.GONE);
            }

            if (stream != null) {
                scrubberContainer.setVisibility(View.GONE);
                playButtonContainer.setVisibility(View.GONE);
            } else {
                liveIndicatorTextView.setVisibility(View.GONE);

                scrubberSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    private boolean wasPlaying = false;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        isScrubberTracking = true;
                        if (playerControl != null) {
                            wasPlaying = playerControl.isPlaying();
                            if (playerControl.canPause()) {
                                playerControl.pause();
                            }
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        isScrubberTracking = false;
                        if (playerControl != null) {
                            float percent = (float) scrubberSeekBar.getProgress() / (float) scrubberSeekBar.getMax();
                            int position = (int) ((float) playerControl.getDuration() * percent);
                            playerControl.seekTo(position);
                            if (wasPlaying) {
                                playerControl.start();
                            }
                        }
                    }
                });
            }

            if( (video == null || video.video_site_watch_page == null) && (stream == null || stream.user == null || stream.user.username == null)) {
                shareButton.setVisibility(View.GONE);
            }
        }
    }

    public void setControlButtonClickListener(MediaControls.ControlButtonClickListener controlButtonClickListener) {
        this.controlButtonClickListener = controlButtonClickListener;
    }

    private void toggleFollowButton() {
        if (AccountManager.isLoggedIn()) {
            Callback<?> callback = null;
            if (owner.is_user_following) {
                owner.is_user_following = false;
                followButton.setActivated(false);
                followButton.setText(root.getContext().getResources().getString(R.string.videoFollow));
                callback = new UnfollowCallback(owner.id,
                        video != null ? video.video_id : null,
                        video != null ? Event.ViewSource.VIDEO_DETAIL_VIEW : Event.ViewSource.STREAM_DETAIL_VIEW);
                AppServerClient.getInstance().unfollow(owner.id, (UnfollowCallback) callback);
            } else {
                owner.is_user_following = true;
                followButton.setActivated(true);
                followButton.setText(root.getContext().getResources().getString(R.string.videoFollowing));
                callback = new FollowCallback(owner.id,
                        video != null ? video.video_id : null,
                        video != null ? Event.ViewSource.VIDEO_DETAIL_VIEW : Event.ViewSource.STREAM_DETAIL_VIEW);
                AppServerClient.getInstance().follow(owner.id, (FollowCallback) callback);
            }
            KamcordAnalytics.startSession(callback, Event.Name.FOLLOW_USER);
            ViewUtils.buttonCircularReveal(followButton);
        }
        else {
            Toast.makeText(root.getContext(), root.getContext().getResources().getString(R.string.youMustBeLoggedIn), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(root.getContext(), LoginActivity.class);
            Event.ViewSource source = video != null ?
                    (video.video_id != null ? Event.ViewSource.VIDEO_DETAIL_VIEW : Event.ViewSource.REPLAY_VIDEO_VIEW)
                    : Event.ViewSource.STREAM_DETAIL_VIEW;
            intent.putExtra(KamcordAnalytics.VIEW_SOURCE_KEY, source);
            if( source == Event.ViewSource.VIDEO_DETAIL_VIEW ) {
                intent.putExtra(KamcordAnalytics.VIDEO_ID_KEY, video.video_id);
            }
            intent.putExtra(KamcordAnalytics.INDUCING_ACTION_KEY, Event.InducingAction.FOLLOW_USER);
            root.getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.follow_button)
    public void onFollowButtonClick() {
        toggleFollowButton();
    }

    @OnClick(R.id.share_button)
    public void doExternalShare() {
        Bundle extras = new Bundle();
        if (video != null) {
            VideoUtils.doExternalShare(root.getContext(), video);
            extras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY, video.video_id != null ? Event.ViewSource.VIDEO_DETAIL_VIEW : Event.ViewSource.REPLAY_VIDEO_VIEW);
            extras.putString(KamcordAnalytics.VIDEO_ID_KEY, video.video_id);
        } else if (stream != null) {
            VideoUtils.doExternalShare(root.getContext(), stream);
            extras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.STREAM_DETAIL_VIEW);
            if( stream.user != null ) {
                extras.putString(KamcordAnalytics.STREAM_USER_ID_KEY, stream.user.id);
            }
        }
        KamcordAnalytics.fireEvent(Event.Name.EXTERNAL_RESHARE, extras);
    }

    @OnClick(R.id.play_button)
    public void onPlayButtonClick() {
        if( playerControl != null ) {
            if( isEnded ) {
                playerControl.seekTo(0);
                playerControl.start();
                if( controlButtonClickListener != null ) {
                    controlButtonClickListener.onReplayButtonClicked();
                }
            } else {
                if (playerControl.isPlaying() && playerControl.canPause()) {
                    playerControl.pause();
                    if( controlButtonClickListener != null ) {
                        controlButtonClickListener.onPauseButtonClicked();
                    }
                } else {
                    playerControl.start();
                    if( controlButtonClickListener != null ) {
                        controlButtonClickListener.onPlayButtonClicked();
                    }
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return root.dispatchKeyEvent(event);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
        this.playerControl = playerControl;
    }

    @Override
    public void setEnabled(boolean enabled) {
        root.setEnabled(enabled);
    }

    @Override
    public void show(int timeoutMs, boolean fade) {
        removeAllVisibilityCallbacks();
        root.post(fade ? fadeInRunnable : showRunnable);
        if (timeoutMs > 0) {
            root.postDelayed(fade ? fadeOutRunnable : hideRunnable, timeoutMs);
        }

        removeAllScrubberCallbacks();
        root.post(updateScrubberRunnable);
    }

    @Override
    public void hide(boolean fade) {
        removeAllVisibilityCallbacks();
        removeAllScrubberCallbacks();
        root.post(fade ? fadeOutRunnable : hideRunnable);
    }

    @Override
    public boolean isShowing() {
        return root.getAlpha() > 0f;
    }

    private void removeAllVisibilityCallbacks() {
        root.removeCallbacks(hideRunnable);
        root.removeCallbacks(showRunnable);
        root.removeCallbacks(fadeOutRunnable);
        root.removeCallbacks(fadeInRunnable);
    }

    private void removeAllScrubberCallbacks() {
        root.removeCallbacks(updateScrubberRunnable);
    }

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().cancel();
            root.setAlpha(0f);
        }
    };

    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().cancel();
            root.setAlpha(1f);
        }
    };

    private Runnable fadeInRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().alpha(1f).setDuration(FADE_DURATION_MS);
        }
    };

    private Runnable fadeOutRunnable = new Runnable() {
        @Override
        public void run() {
            root.animate().alpha(0f).setDuration(FADE_DURATION_MS);
        }
    };

    private Runnable updateScrubberRunnable = new Runnable() {
        private static final int UPDATE_FREQUENCY_MS = 200;
        @Override
        public void run() {
            if( playerControl != null ) {
                if( !isScrubberTracking ) {
                    int durationMs = playerControl.getDuration();
                    int currentPositionMs = playerControl.getCurrentPosition();

                    totalTimeTextView.setText(VideoUtils.videoDurationString(TimeUnit.MILLISECONDS, durationMs));
                    if( !isEnded ) {
                        currentTimeTextView.setText(VideoUtils.videoDurationString(TimeUnit.MILLISECONDS, currentPositionMs));

                        int progress = (int) ((float) scrubberSeekBar.getMax() * ((float) currentPositionMs / (float) durationMs));
                        scrubberSeekBar.setProgress(progress);
                    }
                }

                removeAllScrubberCallbacks();
                root.postDelayed(updateScrubberRunnable, UPDATE_FREQUENCY_MS);
            }
        }
    };

    // Player.Listener methods.
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        isEnded = false;
        if( playbackState == Player.STATE_READY ) {
            errorContainer.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
            bufferingProgressBar.setVisibility(View.GONE);
            playButton.setImageResource(playWhenReady ? R.drawable.pause_white : R.drawable.play_white);

        } else if( playbackState == Player.STATE_BUFFERING
                || playbackState == Player.STATE_PREPARING ) {
            playButton.setVisibility(View.GONE);
            bufferingProgressBar.setVisibility(View.VISIBLE);

        } else if( playbackState == Player.STATE_ENDED ) {
            isEnded = true;
            playButton.setVisibility(View.VISIBLE);
            bufferingProgressBar.setVisibility(View.GONE);
            currentTimeTextView.setText(VideoUtils.videoDurationString(TimeUnit.MILLISECONDS, playerControl.getDuration()));
            scrubberSeekBar.setProgress(scrubberSeekBar.getMax());
            playButton.setImageResource(R.drawable.replay_white);
            show(0, true);

        } else {
            playButton.setVisibility(View.GONE);
            bufferingProgressBar.setVisibility(View.GONE);
        }

        lastState = playbackState;
    }

    @Override
    public void onError(Exception e) {
        if( stream != null ) {
            errorContainer.setVisibility(View.VISIBLE);
        }
    }

    public void streamEnded(Stream stream) {
        endedContainer.setVisibility(View.VISIBLE);
        streamViewsTextView.setText(StringUtils.commatizedCount(stream.total_viewers_count));
        if( stream.ended_at != null && stream.started_at != null ) {
            streamLengthTextView.setText(VideoUtils.videoDurationString(TimeUnit.MILLISECONDS, stream.ended_at.getTime() - stream.started_at.getTime()));
        } else {
            streamLengthContainer.setVisibility(View.GONE);
        }
        show(0, true);
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {
    }
}
