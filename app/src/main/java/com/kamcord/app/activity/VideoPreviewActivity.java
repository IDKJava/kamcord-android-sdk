package com.kamcord.app.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by donliang1 on 5/18/15.
 */
public class VideoPreviewActivity extends Activity {
    public static final String ARG_VIDEO_PATH = "video_path";

    @InjectView(R.id.videoview_preview)
    VideoView mVideoView;
    @InjectView(R.id.replayButton)
    ImageButton replayImageBtn;
    private MediaController mediaController;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private int videoHeight;
    private int videoWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videopreview);
        initVideoPreview();
    }

    public void initVideoPreview() {
        ButterKnife.inject(this);
        final String videoPath = getIntent().getExtras().getString(ARG_VIDEO_PATH);

        // Determine videoview orientation
        mediaMetadataRetriever = new MediaMetadataRetriever();
        if (videoPath != null) {
            mediaMetadataRetriever.setDataSource(videoPath);
            videoHeight = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            videoWidth = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            if (videoHeight <= videoWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        if (mediaController == null) {
            mediaController = new MediaController(this);
            try {
                mVideoView.setMediaController(mediaController);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mVideoView.setVideoPath(videoPath);
        mVideoView.start();
        mVideoView.requestFocus();

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                replayImageBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.replayButton)
    public void replayVideo() {
        replayImageBtn.setVisibility(View.INVISIBLE);
        mVideoView.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
