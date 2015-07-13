package com.kamcord.app.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
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
import com.kamcord.app.player.ExtractorRendererBuilder;
import com.kamcord.app.player.HlsRendererBuilder;
import com.kamcord.app.player.Player;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.view.LiveMediaControls;
import com.kamcord.app.view.MediaControls;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class VideoViewActivity extends AppCompatActivity implements
        SurfaceHolder.Callback,
        Player.Listener,
        Player.TextListener,
        Player.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener {
    private static final String TAG = VideoViewActivity.class.getSimpleName();

    public static final String ARG_VIDEO = "video";
    public static final String ARG_STREAM = "stream";

    private static final float CAPTION_LINE_HEIGHT_RATIO = 0.0533f;

    @InjectView(R.id.surface_view)
    VideoSurfaceView surfaceView;
    @InjectView(R.id.shutter)
    View shutterView;
    @InjectView(R.id.subtitles)
    SubtitleView subtitleView;

    private Video video = null;
    private Stream stream = null;

    private Player player;
    private boolean playerNeedsPrepare;
    private float qualityMultiplier = 2f;

    private long playerPosition;

    private MediaControls mediaControls;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;

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
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
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
        }
    }

    // Internal methods

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

    // Player.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY) {
            mediaControls.show(playWhenReady ? 3000 : 0, true);
        }
    }

    @Override
    public void onError(Exception e) {
        playerNeedsPrepare = true;
        showControls();
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
        if (mediaControls.isShowing()) {
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
}
