package com.kamcord.app.kamcord.activity.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.kamcord.app.kamcord.activity.model.RecordingMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecordHandlerThread extends HandlerThread implements Handler.Callback {

    private Context mContext;
    private Handler mHandler;
    private static RecordingMessage msgObject;

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
    private String packageString;
    private String[] packageList;
    private String selectedPackageName;
    private String gamefolder;

    public RecordHandlerThread(String name) {
        super(name);
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case 1:
                this.msgObject = (RecordingMessage) msg.obj;
                this.mMediaProjection = msgObject.getProjection();
                this.mContext = msgObject.getContext();
                this.recordFlag = msgObject.getRecordFlag();
                this.mHandler = msgObject.getHandler();
                this.selectedPackageName = msgObject.getPackageName();
                this.gamefolder = msgObject.getGameFolderString();

                startRecording();
                while (pollingGame()) {
                }
                Log.d("Polling has ", "finished.");
                Message resumeMsg = Message.obtain(this.mHandler, 1, msgObject);
                this.mHandler.sendMessage(resumeMsg);
                break;
            case 2:
                Log.d("Message: ", Integer.toString(msg.what));
                releaseEncoders();
                break;
        }
        return false;
    }

    private boolean pollingGame() {
        activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        runningAppProcessInfo = runningAppProcessInfoList.get(0);
        packageList = runningAppProcessInfo.pkgList;
        packageString = packageList[0];
        if (packageString.equals(selectedPackageName)) {
            return false;
        }
        return true;
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
                mMuxer = new MediaMuxer("/sdcard/Kamcord_Android/" + gamefolder + "/" + "Kamcord -" + fileNamePrefix + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException ioe) {
                throw new RuntimeException("Muxer failed.", ioe);
            }

            frameCount = 0;
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

        // Initialzation
        activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        runningAppProcessInfo = runningAppProcessInfoList.get(0);
        packageList = runningAppProcessInfo.pkgList;
        packageString = packageList[0];
        appImportance = runningAppProcessInfo.importance;

        while (this.recordFlag == true
                && packageString.equals(selectedPackageName)
                && appImportance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

            int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, 0);

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // No output available
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!mMuxerStart) {
                    mTrackIndex = mMuxer.addTrack(mVideoEncoder.getOutputFormat());
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
                        packageString = packageList[0];

                        encodedData.position(mVideoBufferInfo.offset);
                        encodedData.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
                        // Skip Frames from DummyActivity
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
            //mMediaProjection.stop();
            mMediaProjection = null;
        }
        mVideoBufferInfo = null;
    }
}
