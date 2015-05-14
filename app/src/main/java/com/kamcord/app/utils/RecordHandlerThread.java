package com.kamcord.app.utils;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.kamcord.app.server.model.Game;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class RecordHandlerThread extends HandlerThread implements Handler.Callback {
    private static final String TAG = RecordHandlerThread.class.getSimpleName();

    private MediaProjection mMediaProjection;
    private Game mGameModel;
    private Context mContext;
    private Handler mHandler;
    private Surface mSurface;

    private MediaMuxer mMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    private VirtualDisplay mVirtualDisplay;

    private boolean mMuxerStart = false;
    private boolean mMuxerWrite = false;
    private int mTrackIndex = -1;
    private int frameRate = 30;
    private static final String VIDEO_TYPE = "video/avc";

    private ActivityManager mActivityManager;
    private String mSessionFolderName;
    private int clipNumber = 0;

    public RecordHandlerThread(MediaProjection mediaProjection, Game gameModel, Context context, FileManagement fileManagement) {
        super("KamcordRecordingThread");
        this.mMediaProjection = mediaProjection;
        this.mGameModel = gameModel;
        this.mContext = context;

        this.mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mSessionFolderName = fileManagement.getGameName() + "/" + fileManagement.getUUIDString() + "/";
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {

        switch (msg.what) {
            case Message.RECORD_CLIP:
                Log.v("FindMe", "RECORD_CLIP");
                clipNumber++;
                recordUntilBackground();
                mHandler.removeMessages(Message.POLL);
                mHandler.sendEmptyMessage(Message.POLL);
                break;

            case Message.POLL:
                Log.v("FindMe", "POLL");
                if (!isGameInForeground()) {
                    mHandler.removeMessages(Message.POLL);
                    mHandler.sendEmptyMessageDelayed(Message.POLL, 100);
                } else {
                    mHandler.removeMessages(Message.RECORD_CLIP);
                    mHandler.sendEmptyMessage(Message.RECORD_CLIP);
                }
                break;

            case Message.STOP_RECORDING:
                Log.v("FindMe", "STOP_RECORDING");
                mMediaProjection.stop();
                break;
        }
        return false;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public String getSessionFolderName() {
        return mSessionFolderName;
    }

    private boolean isGameInForeground() {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfoList) {
            if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String pkgName : runningAppProcessInfo.pkgList) {
                    if (pkgName.equals(mGameModel.play_store_id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void recordUntilBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // Get specifications from DisplayMetrics Structure
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            if (defaultDisplay == null) {
                throw new RuntimeException("No display available");
            }

            defaultDisplay.getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;
            int screenDensity = metrics.densityDpi;

            prepareMediaCodec(screenWidth, screenHeight);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("KamcordVirtualDisplay", screenWidth, screenHeight, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);

            // Video Location
            try {
                String clipPath = "/sdcard/Kamcord_Android/" + mSessionFolderName + "clip" + clipNumber + ".mp4";
                mMuxer = new MediaMuxer(clipPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException ioe) {
                throw new RuntimeException("Muxer failed.", ioe);
            }

            drainEncoder();
            releaseEncoders();
        }
    }

    private void prepareMediaCodec(int screenWidth, int screenHeight) {
        mVideoBufferInfo = new MediaCodec.BufferInfo();

        // Config a MediaCodec and get a surface which we want to record
        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_TYPE);
            MediaCodecInfo.VideoCapabilities videoCapabilities;

            MediaCodecInfo.CodecCapabilities codecCapabilities = mVideoEncoder.getCodecInfo().getCapabilitiesForType(VIDEO_TYPE);
            if( codecCapabilities != null )
            {
                videoCapabilities = codecCapabilities.getVideoCapabilities();

                // Round the dimensions to the nearest multiple that the codec supports.
                if( videoCapabilities != null )
                {
                    int widthAlignment = videoCapabilities.getWidthAlignment();
                    int heightAlignment = videoCapabilities.getHeightAlignment();

                    screenWidth = roundToNearest(screenWidth, widthAlignment);
                    screenHeight = roundToNearest(screenHeight, heightAlignment);
                }
            }

            MediaFormat mMediaFormat = MediaFormat.createVideoFormat(VIDEO_TYPE, screenWidth, screenHeight);

            // Set format properties
            mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1000000);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mVideoEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mVideoEncoder.createInputSurface();
            mVideoEncoder.start();
        } catch (IOException ioe) {
            releaseEncoders();
        }
    }

    private int roundToNearest(int intToRound, int modulus)
    {
        int rounded = intToRound;

        if( modulus > 0 )
        {
            int remainder = intToRound % modulus;
            if( remainder / 2 < modulus )
            {
                rounded -= remainder;
            }
            else
            {
                rounded += modulus - remainder;
            }
        }

        return rounded;
    }

    private boolean drainEncoder() {
        while (isGameInForeground()) {

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
                        encodedData.position(mVideoBufferInfo.offset);
                        encodedData.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
                        mMuxer.writeSampleData(mTrackIndex, encodedData, mVideoBufferInfo);
                        mMuxerWrite = true;
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
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMuxer != null) {
            if (mMuxerStart && mMuxerWrite) {
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
        mVideoBufferInfo = null;
    }

    public static class Message {
        public static final int RECORD_CLIP = 1;
        public static final int STOP_RECORDING = 2;
        public static final int POLL = 3;
    }
}
