package com.kamcord.app.kamcord.activity.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by donliang1 on 4/21/15.
 */
@TargetApi(21)
public class ScreenRecorder extends Thread {

    private Context mContext;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;

    private MediaMuxer mMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;

    private boolean mMuxerStart = false;
    private int mTrackIndex = -1;
    private int frameRate = 30;
    private static final String VIDEO_TYPE = "video/avc";
    private int delayFrame = 60;
    private int frameCount = 0;
    private String fileDateFormat = "yyyy-MM-dd HH:mm";

    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mScreenDensity;

    private boolean recordFlag;

    private ActivityManager activityManager;
    private List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList;
    private ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
    private int appImportance;

    public ScreenRecorder(MediaProjection mediaProjection, Context context, boolean recordFlag) {
        this.mMediaProjection = mediaProjection;
        this.mContext = context;
        this.recordFlag = recordFlag;
    }

    public void setFlag(boolean flag) {
        recordFlag = flag;
    }

    @Override
    public void run() {
        startRecording();
    }

    private void startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // Get specifications from DisplayMetrics Structure
            DisplayMetrics metrics = new DisplayMetrics();

            DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
            if (defaultDisplay == null) {
                throw new RuntimeException("No display available");
            }

            activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

            defaultDisplay.getMetrics(metrics);
            mDisplayWidth = metrics.widthPixels / 2;
            mDisplayHeight = metrics.heightPixels / 2;
            mScreenDensity = metrics.densityDpi;

            prepareMediaCodec();

            mVirtualDisplay = mMediaProjection.createVirtualDisplay("Recording", mDisplayWidth, mDisplayHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);

            // Video Location
            try {
                String fileNamePrefix = new SimpleDateFormat(fileDateFormat).format(new Date()).replaceAll("[\\s:]", "-");
                mMuxer = new MediaMuxer("/sdcard/Kamcord/" + "Kamcord-" + fileNamePrefix + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException ioe) {
                throw new RuntimeException("Muxer failed.", ioe);
            }

            // Start Media Encode
            drainEncoder();
            releaseEncoders();
        }
    }

    private void prepareMediaCodec() {
        mVideoBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat mMediaFormat = MediaFormat.createVideoFormat(VIDEO_TYPE, mDisplayWidth, mDisplayHeight);

        // Set format properties
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // Frequency of I frames expressed in secs between I frames

        // Config a MediaCodec and get a surface which we want to record
        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_TYPE);
            mVideoEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mVideoEncoder.createInputSurface();
            mVideoEncoder.start();
        } catch (IOException ioe) {
            releaseEncoders();
        }
    }

    private boolean drainEncoder() {

        runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        runningAppProcessInfo = runningAppProcessInfoList.get(0);
        String[] packageList = runningAppProcessInfo.pkgList;
        String str = packageList[0];
        Log.d("RunningAppProcessInfo: ", str);
        appImportance = runningAppProcessInfo.importance;
        Log.d("App Importance: ", Integer.toString(appImportance));

        while (this.recordFlag == true
                && str.equals("com.sgn.pandapop.gp")
                && appImportance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

            int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, 0);

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // No output available
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mTrackIndex >= 0) {
                    throw new RuntimeException("format changed twice");
                }
                mTrackIndex = mMuxer.addTrack(mVideoEncoder.getOutputFormat());
                if (!mMuxerStart && mTrackIndex >= 0) {
                    mMuxer.start();
                    mMuxerStart = true;
                }
            } else if (encoderStatus < 0) {
                // ignore it, but why?
            } else {
                ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(encoderStatus);
                if (encodedData == null) {
                    throw new RuntimeException("Could not fetch buffer." + encoderStatus);
                }

                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mVideoBufferInfo.size = 0;
                }

                if (mVideoBufferInfo.size != 0) {
                    if (mMuxerStart) {

                        runningAppProcessInfoList = activityManager.getRunningAppProcesses();
                        runningAppProcessInfo = runningAppProcessInfoList.get(0);
                        packageList = runningAppProcessInfo.pkgList;
                        str = packageList[0];
                        Log.d("RunningAppProcessInfo: ", str);

                        encodedData.position(mVideoBufferInfo.offset);
                        encodedData.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
                        if (frameCount >= delayFrame) {
                            mMuxer.writeSampleData(mTrackIndex, encodedData, mVideoBufferInfo);
                        }
                        frameCount++;
                        Log.d("frame count: ", "" + frameCount);
                    }
                }

                mVideoEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }

        }
        return false;
    }

    private void releaseEncoders() {
        if (mMuxer != null) {
            if (mMuxerStart) {
                mMuxer.stop();
            }
            mMuxer.release();
            mMuxer = null;
            mMuxerStart = false;
        }
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        mVideoBufferInfo = null;
    }
}
