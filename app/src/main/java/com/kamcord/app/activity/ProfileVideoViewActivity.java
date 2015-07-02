package com.kamcord.app.activity;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.kamcord.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileVideoViewActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    public static final String ARG_VIDEO_PATH = "video_path";

    @InjectView(R.id.profile_videoview) VideoView myVideoView;
    private String url;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_video_view);
        ButterKnife.inject(this);

        url = getIntent().getExtras().getString(ARG_VIDEO_PATH);
        if(mediaController == null) {
            mediaController = new MediaController(this);
            try {
                myVideoView.setMediaController(mediaController);
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

        if(url != null) {
            myVideoView.setVideoURI(Uri.parse(url));
            myVideoView.setOnPreparedListener(this);
            myVideoView.start();
            myVideoView.requestFocus();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        if (videoHeight <= videoWidth) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        myVideoView.suspend();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
