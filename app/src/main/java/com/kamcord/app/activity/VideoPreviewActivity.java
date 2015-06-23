package com.kamcord.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.kamcord.app.R;
import com.kamcord.app.utils.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by donliang1 on 5/18/15.
 */
public class VideoPreviewActivity extends Activity {
    public static final String ARG_VIDEO_PATH = "video_path";

    @InjectView(R.id.videoview_preview)
    VideoView mVideoView;
    private MediaController mediaController;
    private int videoHeight;
    private int videoWidth;
    private int seekBarId;
    private int currentPlayTimeId;
    private SeekBar seekBar;
    private TextView currentPlayTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videopreview);
        initVideoPreview();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void initVideoPreview() {
        ButterKnife.inject(this);
        final String videoPath = getIntent().getExtras().getString(ARG_VIDEO_PATH);

        // Determine videoview orientation
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        if (videoPath != null) {
            try {
                mediaMetadataRetriever.setDataSource(videoPath);
                videoHeight = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                videoWidth = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                if (videoHeight <= videoWidth) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mediaMetadataRetriever.release();

        if (mediaController == null) {
            mediaController = new MediaController(this);
            try {
                mVideoView.setMediaController(mediaController);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (videoPath != null) {
            mVideoView.setVideoPath(videoPath);
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBarId = getResources().getIdentifier("mediacontroller_progress", "id", "android");
                    currentPlayTimeId = getResources().getIdentifier("time_current", "id", "android");
                    seekBar = (SeekBar) mediaController.findViewById(seekBarId);
                    currentPlayTime = (TextView) mediaController.findViewById(currentPlayTimeId);
                }
            });
            mVideoView.start();
            mVideoView.requestFocus();

        }

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                seekBar.setProgress(0);
//                seekBar.setMax(mVideoView.getDuration() / 1000);
//                seekBar.setProgress(mVideoView.getDuration() / 1000);
                currentPlayTime.setText(StringUtils.stringForTime(mVideoView.getDuration()));
                seekBar.post(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(mVideoView.getDuration() / 1000);
                    }
                });
                mediaController.show(0);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
