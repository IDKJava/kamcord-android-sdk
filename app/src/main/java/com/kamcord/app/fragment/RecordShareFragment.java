package com.kamcord.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.utils.VideoUtils;

import java.io.File;

public class RecordShareFragment extends Fragment implements View.OnClickListener {

    private ImageView thumbnailImageView;
    private ImageButton playImageButton;
    private Button shareButton;
    private String videoDurationStr;
    private TextView videoDuration;
    private String videoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_recordshare, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

//        videoPath = getArguments().getString("videopath");
        videoPath = "/sdcard/Kamcord_Android/out.mp4";

        thumbnailImageView = (ImageView) v.findViewById(R.id.videothumbnail_imageview);
        playImageButton = (ImageButton) v.findViewById(R.id.video_playbtn);
        shareButton = (Button) v.findViewById(R.id.video_uploadbtn);
        videoDuration = (TextView) v.findViewById(R.id.previewduration_textview);

        playImageButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);

        File videoFolder = new File(videoPath);
        if (videoFolder.exists()) {
            thumbnailImageView.setImageBitmap(VideoUtils.getVideoThumbnail(videoPath));
            videoDurationStr = VideoUtils.getVideoDuration(videoPath);
            videoDuration.setText(videoDurationStr);
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_playbtn: {
                VideoPreviewFragment videoPreviewFragment = new VideoPreviewFragment();
                Bundle bundle = new Bundle();
                bundle.putString("videopath", videoPath);
                videoPreviewFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(1, videoPreviewFragment)
                        .addToBackStack("TestFragment")
                        .commit();

//                VideoPreviewFragment videoPreviewFragment = new VideoPreviewFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString("videopath", videoPath);
//                videoPreviewFragment.setArguments(bundle);
//                getActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.main_activity_layout, videoPreviewFragment)
//                        .addToBackStack(null)
//                        .commit();
            }
            case R.id.video_uploadbtn: {
                // Logic for login
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
