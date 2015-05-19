package com.kamcord.app.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.kamcord.app.R;

/**
 * Created by donliang1 on 5/18/15.
 */
public class VideoPreviewActivity extends Activity {
    public static final String ARG_VIDEO_PATH = "video_path";

    private VideoView mVideoView;
    private ImageButton replayImageBtn;
    private MediaController mediaController;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private int videoHeight;
    private int videoWidth;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videopreview);
        initVideoPreview();
    }

    public void initVideoPreview() {
        mVideoView = (VideoView) findViewById(R.id.videoview_preview);
        replayImageBtn = (ImageButton) findViewById(R.id.replayButton);
        final String videoPath = getIntent().getExtras().getString(ARG_VIDEO_PATH);
        Log.d("VideoPath", videoPath);

        // Determine videoview orientation
        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoPath);
        videoHeight = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        videoWidth = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        if(videoHeight <= videoWidth) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        replayImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replayImageBtn.setVisibility(View.INVISIBLE);
                mVideoView.start();
            }
        });

        if(mediaController == null) {
            mediaController = new MediaController(getApplicationContext());
        }
        try {
            mVideoView.setMediaController(mediaController);
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
