package com.kamcord.app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

    private ImageView thumbNailImageView;
    private ImageButton playImageButton;
    private Button shareButton;
    private String videoPath;
    private String videoDuraionStr;
    private TextView videoDuration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_recordshare, container, false);
        v.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        playImageButton = (ImageButton) v.findViewById(R.id.video_playbtn);
        shareButton = (Button) v.findViewById(R.id.video_uploadbtn);
        playImageButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);

        videoPath = "/sdcard/Kamcord_Android/clip1.mp4";
        File videoFolder = new File(videoPath);
        if (videoFolder.exists()) {
            videoFolder.mkdir();
            Log.d("video exists", "exists");
        } else {
            Log.d("video exists", "not exists");
        }
        thumbNailImageView = (ImageView) v.findViewById(R.id.videothumbnail_imageview);
        thumbNailImageView.setImageBitmap(VideoUtils.getVideoThumbnail(videoPath));

        videoDuration = (TextView) v.findViewById(R.id.previewduration_textview);
        videoDuraionStr = VideoUtils.getVideoDuration(videoPath);
        videoDuration.setText(videoDuraionStr);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_playbtn: {
                Intent playVideoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));
                playVideoIntent.setType("video/mp4");
                String title = getResources().getString(R.string.kamcordAppChooser);
                Intent chooser = Intent.createChooser(playVideoIntent, title);
                startActivity(chooser);
                break;
            }
            case R.id.video_uploadbtn: {
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
