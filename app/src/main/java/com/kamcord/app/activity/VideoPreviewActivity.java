package com.kamcord.app.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
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

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videopreview);
        initVideoPreview();
    }

    public void initVideoPreview() {
        mVideoView = (VideoView) findViewById(R.id.video_preview);
        replayImageBtn = (ImageButton) findViewById(R.id.replayButton);
        final String videoPath = getIntent().getExtras().getString(ARG_VIDEO_PATH);

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

        replayImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replayImageBtn.setVisibility(View.INVISIBLE);
                mVideoView.start();
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                replayImageBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {

    }
}
