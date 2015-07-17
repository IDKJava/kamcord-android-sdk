package com.kamcord.app.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;

import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.metadata.GeobMetadata;
import com.google.android.exoplayer.metadata.PrivMetadata;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.SubtitleView;
import com.google.android.exoplayer.util.Util;
import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.player.ExtractorRendererBuilder;
import com.kamcord.app.player.HlsRendererBuilder;
import com.kamcord.app.player.Player;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.view.LiveMediaControls;
import com.kamcord.app.view.MediaControls;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class VideoViewActivity extends AppCompatActivity implements
        SurfaceHolder.Callback,
        Player.Listener,
        Player.TextListener,
        Player.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener,
        MediaControls.ControlButtonClickListener {
    private static final String TAG = VideoViewActivity.class.getSimpleName();

    public static final String ARG_VIDEO = "arg_video";
    public static final String ARG_STREAM = "arg_stream";
    public static final String ARG_USER_ID = "arg_user_id";
    public static final String ARG_FOLLOWED = "arg_followed";

    private static final float CAPTION_LINE_HEIGHT_RATIO = 0.0533f;
    private static final int MAX_RECONNECT_ATTEMPTS = 4;

    @InjectView(R.id.surface_view)
    VideoSurfaceView surfaceView;
    @InjectView(R.id.shutter)
    View shutterView;
    @InjectView(R.id.subtitles)
    SubtitleView subtitleView;

    private Video video = null;
    private Stream stream = null;
    private int position = -1;

    private Player player;
    private boolean playerNeedsPrepare;
    private float qualityMultiplier = 2f;
    private boolean playerError = false;

    private long playerPosition;

    private MediaControls mediaControls;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;

    private int reconnectAttemptCount = 0;

    // Analytics counters
    private long totalBufferingTimeMs = 0;
    private long lastBufferingStart = 0;
    private long totalPlayTimeMs = 0;
    private long lastPlayStart = 0;
    private int playStarts = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_view);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        if (intent.hasExtra(ARG_VIDEO)) {
            video = new Gson().fromJson(intent.getStringExtra(ARG_VIDEO), Video.class);
        }
        if (intent.hasExtra(ARG_STREAM)) {
            stream = new Gson().fromJson(intent.getStringExtra(ARG_STREAM), Stream.class);
        }

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getApplicationContext(), this);
        surfaceView.getHolder().addCallback(this);

        View root = findViewById(R.id.root);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    return mediaControls.dispatchKeyEvent(event);
                }
                return false;
            }
        });
        mediaControls = new LiveMediaControls(this, video, stream);
        mediaControls.hide(false);
        mediaControls.setAnchorView(root);
        mediaControls.setControlButtonClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onStart() {
        super.onStart();

        if( this.video != null ) {
            if( this.video.user != null ) {
                KamcordAnalytics.startSession(this, Event.Name.VIDEO_DETAIL_VIEW);
            } else {
                KamcordAnalytics.startSession(this, Event.Name.REPLAY_VIDEO_VIEW);
            }
        } else if( this.stream != null && this.stream.user != null ) {
            KamcordAnalytics.startSession(this, Event.Name.STREAM_DETAIL_VIEW);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Bundle extras = endSessionAnalyticsExtras();

        if( this.video != null ) {
            if( this.video.user != null ) {
                KamcordAnalytics.endSession(this, Event.Name.VIDEO_DETAIL_VIEW, extras);
            } else {
                KamcordAnalytics.endSession(this, Event.Name.REPLAY_VIDEO_VIEW, extras);
            }
        } else if( this.stream != null && this.stream.user != null ) {
            KamcordAnalytics.endSession(this, Event.Name.STREAM_DETAIL_VIEW, extras);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if ((video == null || video.video_url == null)
                && (stream == null || stream.play == null || stream.play.hls == null)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.errorPlayingVideo)
                    .setMessage(R.string.thereWasAnError)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            finish();
                        }
                    })
                    .show();
            return;
        }

        configureSubtitleView();

        if( player != null ) {
            shutterView.setVisibility(View.GONE);
        }

        // The player will be prepared on receiving audio capabilities.
        audioCapabilitiesReceiver.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            audioCapabilitiesReceiver.unregister();
            shutterView.setVisibility(View.VISIBLE);
            if( player.getPlayerControl().isPlaying() && player.getPlayerControl().canPause() ) {
                player.getPlayerControl().pause();
            }
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        if (video != null && video.user_id != null && video.user != null) {
            intent.putExtra(VideoViewActivity.ARG_USER_ID, video.user_id);
            intent.putExtra(VideoViewActivity.ARG_FOLLOWED, video.user.is_user_following);
        } else if (stream != null && stream.user_id != null && stream.user != null) {
            intent.putExtra(VideoViewActivity.ARG_USER_ID, stream.user_id);
            intent.putExtra(VideoViewActivity.ARG_FOLLOWED, stream.user.is_user_following);
        }

        setResult(Activity.RESULT_OK, intent);

        super.finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        boolean audioCapabilitiesChanged = !audioCapabilities.equals(this.audioCapabilities);
        if (player == null || audioCapabilitiesChanged) {
            this.audioCapabilities = audioCapabilities;
            releasePlayer();
            preparePlayer();
        } else if (player != null) {
            player.setBackgrounded(false);
            if( stream != null && stream.live ) {
                player.getPlayerControl().start();
            }
        }
    }

    // Internal methods

    private Bundle endSessionAnalyticsExtras() {
        Bundle extras = new Bundle();

        extras.putInt(KamcordAnalytics.NUM_PLAY_STARTS_KEY, playStarts);

        if( lastPlayStart > 0 ) {
            totalPlayTimeMs += System.currentTimeMillis() - lastPlayStart;
        }
        if( lastBufferingStart > 0 ) {
            totalBufferingTimeMs += System.currentTimeMillis() - lastBufferingStart;
        }
        extras.putFloat(KamcordAnalytics.BUFFERING_DURATION_KEY, (float) totalBufferingTimeMs / 1000f);
        extras.putFloat(KamcordAnalytics.VIDEO_LENGTH_WATCHED_KEY, (float) totalPlayTimeMs / 1000f);

        if( player != null ) {
            long videoLengthMs = player.getDuration();
            if( videoLengthMs > 0 ) {
                extras.putFloat(KamcordAnalytics.VIDEO_LENGTH_KEY, (float) videoLengthMs / 1000f);
            }
        }

        totalPlayTimeMs = 0;
        lastPlayStart = 0;
        totalBufferingTimeMs = 0;
        lastBufferingStart = 0;
        playStarts = 1;

        if( this.video != null && this.video.user != null ) {
            transferViewSourceExtras(extras);
            if( getIntent().getExtras().containsKey(KamcordAnalytics.PROFILE_USER_ID_KEY) ) {
                extras.putString(KamcordAnalytics.PROFILE_USER_ID_KEY,
                        getIntent().getExtras().getString(KamcordAnalytics.PROFILE_USER_ID_KEY));
            }
            extras.putString(KamcordAnalytics.VIDEO_ID_KEY, this.video.video_id);

        } else if( this.stream != null && this.stream.user != null ) {
            transferViewSourceExtras(extras);
            extras.putString(KamcordAnalytics.STREAM_USER_ID_KEY, this.stream.user.id);
            extras.putInt(KamcordAnalytics.IS_LIVE_KEY, this.stream.live ? 1 : 0);
            if( !this.stream.live ) {
                extras.putString(KamcordAnalytics.VIDEO_ID_KEY, this.stream.video_id);
            }
        }

        return extras;
    }

    private void transferViewSourceExtras(Bundle extras) {
        Bundle myExtras = getIntent().getExtras();
        if( myExtras.containsKey(KamcordAnalytics.VIEW_SOURCE_KEY) ) {
            extras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY,
                    myExtras.getSerializable(KamcordAnalytics.VIEW_SOURCE_KEY));
        }
        if( myExtras.containsKey(KamcordAnalytics.VIDEO_LIST_TYPE_KEY) ) {
            extras.putSerializable(KamcordAnalytics.VIDEO_LIST_TYPE_KEY,
                    myExtras.getSerializable(KamcordAnalytics.VIDEO_LIST_TYPE_KEY));
            if( myExtras.containsKey(KamcordAnalytics.VIDEO_LIST_ROW_KEY) ) {
                extras.putInt(KamcordAnalytics.VIDEO_LIST_ROW_KEY,
                        myExtras.getInt(KamcordAnalytics.VIDEO_LIST_ROW_KEY));
            }
            if( myExtras.containsKey(KamcordAnalytics.VIDEO_LIST_COL_KEY) ) {
                extras.putInt(KamcordAnalytics.VIDEO_LIST_COL_KEY,
                        myExtras.getInt(KamcordAnalytics.VIDEO_LIST_COL_KEY));
            }
        }
        if( myExtras.containsKey(KamcordAnalytics.FEED_ID_KEY) ) {
            extras.putString(KamcordAnalytics.FEED_ID_KEY,
                    myExtras.getString(KamcordAnalytics.FEED_ID_KEY));
        }
        if( myExtras.containsKey(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY) ) {
            extras.putString(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY,
                    myExtras.getString(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY));
        }
    }

    private Player.RendererBuilder getRendererBuilder() {
        Player.RendererBuilder rendererBuilder = null;
        String userAgent = Util.getUserAgent(this, getString(R.string.app_name));

        Uri videoUri = null;
        if (video != null && video.video_url != null) {
            videoUri = Uri.parse(video.video_url);
        } else if (stream != null && stream.play != null && stream.play.hls != null) {
            videoUri = Uri.parse(stream.play.hls);
        }

        if (videoUri != null) {
            if (videoUri.toString().endsWith(".m3u8")) {
                rendererBuilder = new HlsRendererBuilder(this, userAgent, videoUri, null,
                        audioCapabilities, qualityMultiplier);
            } else {
                rendererBuilder = new ExtractorRendererBuilder(this, userAgent, videoUri,
                        null, new Mp4Extractor());
            }
        }
        return rendererBuilder;
    }

    private void preparePlayer() {
        if (player == null) {
            player = new Player(getRendererBuilder());
            player.addListener(this);
            player.addListener(mediaControls);
            player.setTextListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaControls.setMediaPlayer(player.getPlayerControl());
            mediaControls.setEnabled(true);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(true);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    private void attemptReconnect() {
        reconnectAttemptCount++;

        if( reconnectAttemptCount > MAX_RECONNECT_ATTEMPTS ) {
            // If we exceed the maximum number of attempts, we give up and assume the stream has ended.
            // TODO: show the user the "stream ended" state
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppServerClient.getInstance().getStream(stream.stream_id, attemptStreamReconnectCallback);
                }
            }, (long) (Math.pow(2.0, reconnectAttemptCount - 1) * 1000));
        }
    }

    // Player.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY) {
            mediaControls.show(playWhenReady ? 3000 : 0, true);
        }

        // If we're not preparing or idle, we've successfully connected to the stream/video
        if( playbackState != Player.STATE_IDLE && playbackState != Player.STATE_PREPARING ) {
            reconnectAttemptCount = 0;
            playerError = false;
        }

        if( playbackState == Player.STATE_BUFFERING ) {
            lastBufferingStart = System.currentTimeMillis();
        } else if( lastBufferingStart > 0 ) {
            totalBufferingTimeMs += System.currentTimeMillis() - lastBufferingStart;
            lastBufferingStart = 0;
        }

        if( playbackState == Player.STATE_READY && playWhenReady ) {
            lastPlayStart = System.currentTimeMillis();
        } else if( lastPlayStart > 0 ) {
            totalPlayTimeMs += System.currentTimeMillis() - lastPlayStart;
            lastPlayStart = 0;
        }
    }

    @Override
    public void onError(Exception e) {
        playerNeedsPrepare = true;
        playerError = true;
        showControls();

        // Only attempt a reconnect if we're viewing a stream.
        if( stream != null ) {
            attemptReconnect();
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        shutterView.setVisibility(View.GONE);
        surfaceView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);

        int currentOrientation = getRequestedOrientation();
        int newOrientation;
        if (height <= width) {
            newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        if (newOrientation != currentOrientation) {
            setRequestedOrientation(newOrientation);
            mediaControls.show(3000, true);
        }
    }

    private void toggleControlsVisibility() {
        if (mediaControls.isShowing() && !playerError) {
            mediaControls.hide(true);
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaControls.show(0, true);
    }

    // Player.TextListener implementation

    @Override
    public void onText(String text) {
        if (TextUtils.isEmpty(text)) {
            subtitleView.setVisibility(View.INVISIBLE);
        } else {
            subtitleView.setVisibility(View.VISIBLE);
            subtitleView.setText(text);
        }
    }

    // Player.MetadataListener implementation

    @Override
    public void onId3Metadata(Map<String, Object> metadata) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (TxxxMetadata.TYPE.equals(entry.getKey())) {
                TxxxMetadata txxxMetadata = (TxxxMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s",
                        TxxxMetadata.TYPE, txxxMetadata.description, txxxMetadata.value));
            } else if (PrivMetadata.TYPE.equals(entry.getKey())) {
                PrivMetadata privMetadata = (PrivMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s",
                        PrivMetadata.TYPE, privMetadata.owner));
            } else if (GeobMetadata.TYPE.equals(entry.getKey())) {
                GeobMetadata geobMetadata = (GeobMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        GeobMetadata.TYPE, geobMetadata.mimeType, geobMetadata.filename,
                        geobMetadata.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", entry.getKey()));
            }
        }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    // LiveMediaControls.ControlButtonClickListener implementation

    @Override
    public void onPlayButtonClicked() {
    }

    @Override
    public void onPauseButtonClicked() {
    }

    @Override
    public void onReplayButtonClicked() {
        playStarts++;
    }

    private void configureSubtitleView() {
        CaptionStyleCompat captionStyle;
        float captionTextSize = getCaptionFontSize();
        if (Util.SDK_INT >= 19) {
            captionStyle = getUserCaptionStyleV19();
            captionTextSize *= getUserCaptionFontScaleV19();
        } else {
            captionStyle = CaptionStyleCompat.DEFAULT;
        }
        subtitleView.setStyle(captionStyle);
        subtitleView.setTextSize(captionTextSize);
    }

    private float getCaptionFontSize() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        return Math.max(getResources().getDimension(R.dimen.subtitle_minimum_font_size),
                CAPTION_LINE_HEIGHT_RATIO * Math.min(displaySize.x, displaySize.y));
    }

    @TargetApi(19)
    private float getUserCaptionFontScaleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        return captioningManager.getFontScale();
    }

    @TargetApi(19)
    private CaptionStyleCompat getUserCaptionStyleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
    }

    private Callback<GenericResponse<Stream>> attemptStreamReconnectCallback = new Callback<GenericResponse<Stream>>() {
        @Override
        public void success(GenericResponse<Stream> streamGenericResponse, Response response) {
            if( streamGenericResponse != null && streamGenericResponse.response != null ) {
                if( !streamGenericResponse.response.live ) {
                    // TODO: show the user the "stream ended" state
                } else {
                    // Try, try again
                    if( player != null && player.getPlaybackState() == Player.STATE_IDLE ) {
                        preparePlayer();
                    }
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            // Try, try again
            if( player != null && player.getPlaybackState() == Player.STATE_IDLE ) {
                preparePlayer();
            }
        }
    };
}
