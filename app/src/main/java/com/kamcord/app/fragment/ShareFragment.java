package com.kamcord.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.model.Video;
import com.kamcord.app.utils.VideoUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ShareFragment extends Fragment {

    @InjectView(R.id.thumbnailImageView) ImageView thumbnailImageView;
    @InjectView(R.id.playImageView) private ImageView playImageView;
    @InjectView(R.id.shareButton) private Button shareButton;
    @InjectView(R.id.videoDurationTextView) private TextView videoDurationTextView;

    private String videoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_share, container, false);

        ButterKnife.inject(this, root);
        videoPath = getArguments().getString("videopath");


        File videoFolder = new File(videoPath);
        if (videoFolder.exists()) {
            thumbnailImageView.setImageBitmap(VideoUtils.getVideoThumbnail(videoPath));
            String videoDurationStr = VideoUtils.getVideoDuration(videoPath);
            videoDurationTextView.setText(videoDurationStr);
        }
        return root;
    }

    @OnClick(R.id.thumbnailImageView)
    public void pushVideoPreviewFragment() {
        VideoPreviewFragment videoPreviewFragment = new VideoPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("videopath", videoPath);
        videoPreviewFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_layout, videoPreviewFragment)
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.shareButton)
    public void share()
    {
        Video videoToShare = new Video.Builder()
                .setVideoPath(videoPath)
                ;
    }
}
