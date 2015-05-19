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
import com.kamcord.app.activity.VideoPreviewActivity;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.service.UploadService;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.StitchClipsThread;
import com.kamcord.app.utils.StitchClipsThread.StitchSuccessListener;
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
    @InjectView(R.id.processingProgressBarContainer) ViewGroup processingProgressBarContainer;

    private RecordingSession recordingSession;
    private StitchSuccessListener stitchSuccessListener = new StitchSuccessListener() {
        @Override
        public void onVideoStitchSuccess(RecordingSession recordingSession) {

        }

        @Override
        public void onVideoStitchFailure(RecordingSession recordingSession) {

        }

        @Override
        public void onAudioStitchSuccess(RecordingSession recordingSession) {

        }

        @Override
        public void onAudioStitchFailure(RecordingSession recordingSession) {

        }

        @Override
        public void onMergeSuccess(RecordingSession recordingSession) {
            videoPrepared(new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                    FileSystemManager.MERGED_VIDEO_FILENAME));
        }

        @Override
        public void onMergeFailure(RecordingSession recordingSession) {
            // TODO: show the user something about failing to process the video.
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_share, container, false);

        ButterKnife.inject(this, root);
        recordingSession = getArguments().getParcelable(ARG_RECORDING_SESSION);

        File videoFile = new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                FileSystemManager.MERGED_VIDEO_FILENAME);
        if (videoFile.exists()) {
            videoPrepared(videoFile);
        }
        else
        {
            processingProgressBarContainer.setVisibility(View.VISIBLE);
            playImageView.setVisibility(View.GONE);
            StitchClipsThread stitchClipsThread = new StitchClipsThread(recordingSession,
                    getActivity().getApplicationContext(),
                    stitchSuccessListener );
            stitchClipsThread.start();
        }
        return root;
    }

    @OnClick(R.id.thumbnailImageView)
    public void showVideoPreviewActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(VideoPreviewActivity.ARG_VIDEO_PATH,
                new File(FileSystemManager.getRecordingSessionCacheDirectory(recordingSession),
                        FileSystemManager.MERGED_VIDEO_FILENAME).getAbsolutePath());
        Intent intent = new Intent(getActivity().getApplicationContext(), VideoPreviewActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.shareButton)
    public void share()
    {
        recordingSession.setVideoTitle(titleEditText.getEditableText().toString());
        recordingSession.setVideoDescription(descriptionEditText.getEditableText().toString());

        Intent uploadIntent = new Intent(getActivity(), UploadService.class);
        uploadIntent.putExtra(UploadService.ARG_SESSION_TO_SHARE, recordingSession);
        getActivity().startService(uploadIntent);
    }

    private void videoPrepared(File videoFile)
    {
        String videoPath = videoFile.getAbsolutePath();
        thumbnailImageView.setImageBitmap(VideoUtils.getVideoThumbnail(videoPath));
        String videoDurationStr = VideoUtils.getVideoDuration(videoPath);
        videoDurationTextView.setText(videoDurationStr);
        processingProgressBarContainer.setVisibility(View.GONE);
        playImageView.setVisibility(View.VISIBLE);
    }

    private void videoProcessing()
    {
        processingProgressBarContainer.setVisibility(View.VISIBLE);
        playImageView.setVisibility(View.GONE);
    }
}
