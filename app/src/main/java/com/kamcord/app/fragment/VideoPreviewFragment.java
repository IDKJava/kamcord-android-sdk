package com.kamcord.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.kamcord.app.R;

/**
 * Created by donliang1 on 5/13/15.
 */
public class VideoPreviewFragment extends Fragment {

    private VideoView mVideoView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_videopreview, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mVideoView = (VideoView) v.findViewById(R.id.video_preview);
        final String videoPath = getArguments().getString("videopath");

        mVideoView.setVideoPath(videoPath);
        mVideoView.start();
        mVideoView.requestFocus();
        return v;
    }
}
