package com.kamcord.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.service.UploadService;
import com.kamcord.app.utils.VideoUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ShareFragment extends Fragment {
    public static final String ARG_RECORDING_SESSION = "recording_session";

    @InjectView(R.id.thumbnailImageView) ImageView thumbnailImageView;
    @InjectView(R.id.playImageView) ImageView playImageView;
    @InjectView(R.id.shareButton) Button shareButton;
    @InjectView(R.id.titleEditText) EditText titleEditText;
    @InjectView(R.id.descriptionEditText) EditText descriptionEditText;
    @InjectView(R.id.videoDurationTextView) TextView videoDurationTextView;

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
        RecordingSession videoToShare = new RecordingSession.Builder()
                .setVideoPath(videoPath)
                .setTitle(titleEditText.getEditableText().toString())
                .setDescription(descriptionEditText.getEditableText().toString())
                .build();

        Intent uploadIntent = new Intent(getActivity(), UploadService.class);
        uploadIntent.putExtra(UploadService.ARG_VIDEO_TO_SHARE, videoToShare);
        getActivity().startService(uploadIntent);
    }
}
