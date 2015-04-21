package com.kamcord.app.kamcord.activity;

import android.annotation.TargetApi;
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
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by donliang1 on 4/21/15.
 */
@TargetApi(21)
public class ScreenRecorder extends Thread{

    private Context mContext;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;
    private SurfaceView mSurfaceView;

    private MediaMuxer mMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;

    private boolean mMuxerStart = false;
    private int mTrackIndex = -1;
    private int frameRate = 1;
    private static final String VIDEO_TYPE = "video/avc";

    private int frameCount = 0;
//    private static final int VIDEO_WIDTH = 720;
//    private static final int VIDEO_HEIGHT = 480;

    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mScreenDensity;

    public ScreenRecorder(MediaProjection mediaProjection, Context context)
    {
        this.mMediaProjection = mediaProjection;
        this.mContext = context;
    }

    @Override
    public void run() {
        Log.d("thread is ", "run");
        startRecording();
    }

    private void startRecording() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // Get specifications from DisplayMetrics Structure
            DisplayMetrics metrics = new DisplayMetrics();

            DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
            if(defaultDisplay == null) {
                throw new RuntimeException("No display available");
            }

            defaultDisplay.getMetrics(metrics);
            mDisplayWidth = metrics.widthPixels /2;
            mDisplayHeight = metrics.heightPixels /2;
            mScreenDensity = metrics.densityDpi;
//            Log.d("woohoo", "mDisplayWidth: " + mDisplayWidth);
//            Log.d("woohoo", "mDisplayHeight: " + mDisplayHeight);
//            Log.d("woohoo", "mScreenDensity: " + mScreenDensity);
//            if(true) return;

            prepareMediaCodec();

            mVirtualDisplay = mMediaProjection.createVirtualDisplay("Recording", mDisplayWidth, mDisplayHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);

            // Video Store Location
            try {
                mMuxer = new MediaMuxer("/sdcard/kamcord.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch(IOException ioe) {
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
//        mMediaFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate);
//        mMediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);  // Number of channels in an audio format
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // Frequency of I frames expressed in secs between I frames
//        mMediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / frameRate); /* */

        Log.d("MediaFormat: ", "" + mMediaFormat);
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
        while (true && frameCount <= 500) {
            int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, 0);
//            Log.d("encoderStatus", "" + encoderStatus);
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
            }
            else {
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
//        mDrainHandler.postDelayed(mDrainEncoderRunnable, 10);
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
