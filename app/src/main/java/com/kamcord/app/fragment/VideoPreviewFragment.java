package com.kamcord.app.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.VideoView;

import com.kamcord.app.R;

/**
 * Created by donliang1 on 5/13/15.
 */
public class VideoPreviewFragment extends Fragment {

    private VideoView mVideoView;
    private ImageButton replayImageBtn;
    private Boolean videoCompleted = false;
    private int currentStopPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_videopreview, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mVideoView = (VideoView) v.findViewById(R.id.video_preview);
        replayImageBtn = (ImageButton) v.findViewById(R.id.replayButton);
        final String videoPath = getArguments().getString("videopath");

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
                videoCompleted = true;
                replayImageBtn.setVisibility(View.VISIBLE);
            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (videoCompleted) {
                    mVideoView.start();
                }
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                }
                return true;
            }
        });

        return v;
    }
}
