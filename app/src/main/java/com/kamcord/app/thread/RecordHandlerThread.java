package com.kamcord.app.thread;

import android.app.ActivityManager;
import android.app.Notification;
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
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.service.RecordingService;
import com.kamcord.app.utils.ApplicationStateUtils;
import com.kamcord.app.utils.FileSystemManager;
import com.kamcord.app.utils.NotificationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.CyclicBarrier;

public class RecordHandlerThread extends HandlerThread implements Handler.Callback {
    private static final String TAG = RecordHandlerThread.class.getSimpleName();

    private MediaProjection mMediaProjection;
    private Context mContext;
    private Handler mHandler;
    private Surface mSurface;
    private Notification.Builder notificationBuilder;

    private MediaMuxer mMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    private VirtualDisplay mVirtualDisplay;

    private CyclicBarrier clipStartBarrier = null;

    private boolean mMuxerStart = false;
    private boolean mMuxerWrite = false;
    private int mTrackIndex = -1;
    private static final String VIDEO_TYPE = "video/avc";

    private ActivityManager mActivityManager;
    private RecordingSession mRecordingSession;
    private int clipNumber = 0;
    private long presentationStartUs = -1;

    private static class CodecSettings
    {
        private static final int FRAME_RATE = 30;
        private static final int BIT_RATE = 4000000;
        private static final float RESOLUTION_MULTIPLIER = 0.5f;
    }

    private enum AspectRatio
    {
        INDETERMINATE,
        PORTRAIT,
        LANDSCAPE,
    }
    private AspectRatio aspectRatio = AspectRatio.INDETERMINATE;

    private static class Dimensions
    {
        public Dimensions(int width, int height)
        {
            this.width = width;
            this.height = height;
        }
        public int width;
        public int height;
    }
    private Dimensions codecDimensions = null;

    public RecordHandlerThread(MediaProjection mediaProjection, Context context, RecordingSession recordingSession, CyclicBarrier clipStartBarrier) {
        super("KamcordRecordingThread");
        this.mMediaProjection = mediaProjection;
        this.mContext = context;

        this.mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        this.mRecordingSession = recordingSession;
        this.clipStartBarrier = clipStartBarrier;
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {

        switch (msg.what) {
            case Message.RECORD_CLIP:
                try {
                    Thread.sleep(RecordingService.DROP_FIRST_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recordUntilBackground();
                mHandler.removeMessages(Message.POLL);
                mHandler.sendEmptyMessage(Message.POLL);
                NotificationUtils.updateNotification(mContext.getResources().getString(R.string.paused));
                break;

            case Message.POLL:
                if (!ApplicationStateUtils.isGameInForeground(mRecordingSession.getGamePackageName())) {
                    mHandler.removeMessages(Message.POLL);
                    mHandler.sendEmptyMessageDelayed(Message.POLL, 100);
                } else {
                    mHandler.removeMessages(Message.RECORD_CLIP);
                    mHandler.sendEmptyMessage(Message.RECORD_CLIP);
                    NotificationUtils.updateNotification(mContext.getResources().getString(R.string.recording));
                }
                break;

            case Message.STOP_RECORDING:
                mMediaProjection.stop();
                break;
        }
        return false;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
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
            int screenWidth = (int) (metrics.widthPixels * CodecSettings.RESOLUTION_MULTIPLIER);
            int screenHeight = (int) (metrics.heightPixels * CodecSettings.RESOLUTION_MULTIPLIER);
            int screenDensity = metrics.densityDpi;

            if( (aspectRatio == AspectRatio.PORTRAIT && screenWidth > screenHeight) || (aspectRatio == AspectRatio.LANDSCAPE && screenHeight > screenWidth) )
            {
                int tmp = screenWidth;
                screenWidth = screenHeight;
                screenHeight = tmp;
            }

            prepareMediaCodec(screenWidth, screenHeight);

            // RecordingSession Location
            try {

                File clipFile = new File(
                        FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                        String.format(Locale.ENGLISH, "video%03d.mp4", clipNumber));
                mMuxer = new MediaMuxer(clipFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException ioe) {
                throw new RuntimeException("Muxer failed.", ioe);
            }
            mVideoEncoder.start();

            try
            {
                clipStartBarrier.await();
                clipStartBarrier.reset();
            }
            catch(Exception e )
            {
                e.printStackTrace();
            }
            presentationStartUs = -1;
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("KamcordVirtualDisplay", screenWidth, screenHeight, screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);
            drainEncoder();
            releaseEncoders();
        }
    }

    private void prepareMediaCodec(int width, int height) {
        mVideoBufferInfo = new MediaCodec.BufferInfo();

        // Config a MediaCodec and get a surface which we want to record
        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_TYPE);
        } catch (IOException ioe) {
            releaseEncoders();
            return;
        }

        if( codecDimensions == null ) {
            MediaCodecInfo.VideoCapabilities videoCapabilities;

            MediaCodecInfo.CodecCapabilities codecCapabilities = mVideoEncoder.getCodecInfo().getCapabilitiesForType(VIDEO_TYPE);
            if (codecCapabilities != null) {
                videoCapabilities = codecCapabilities.getVideoCapabilities();

                // Round the dimensions to the nearest multiple that the codec supports.
                if (videoCapabilities != null) {
                    int widthAlignment = videoCapabilities.getWidthAlignment();
                    int heightAlignment = videoCapabilities.getHeightAlignment();

                    codecDimensions = new Dimensions(roundToNearest(width, widthAlignment), roundToNearest(height, heightAlignment));
                }
            }

            if (codecDimensions == null) {
                codecDimensions = new Dimensions(width, height);
            }
        }

        if( aspectRatio == AspectRatio.INDETERMINATE )
        {
            aspectRatio = codecDimensions.width > codecDimensions.height ? AspectRatio.LANDSCAPE : AspectRatio.PORTRAIT;
        }

        MediaFormat mMediaFormat = MediaFormat.createVideoFormat(VIDEO_TYPE, codecDimensions.width, codecDimensions.height);

        // Set format properties
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, CodecSettings.BIT_RATE);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, CodecSettings.FRAME_RATE);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        mVideoEncoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mVideoEncoder.createInputSurface();
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
        while (ApplicationStateUtils.isGameInForeground(mRecordingSession.getGamePackageName())) {

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

                if (mVideoBufferInfo.size != 0 && mMuxerStart ) {
                    encodedData.position(mVideoBufferInfo.offset);
                    encodedData.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
                    mMuxer.writeSampleData(mTrackIndex, encodedData, mVideoBufferInfo);
                    if( presentationStartUs < 0 )
                    {
                        presentationStartUs = mVideoBufferInfo.presentationTimeUs;
                    }
                    mMuxerWrite = true;
                    mRecordingSession.setRecordedFrames(true);
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
            if (mMuxerStart) {
                mMuxer.stop();
            }
            mMuxerStart = false;

            if( mMuxerWrite )
            {
                clipNumber++;
            }
            mMuxerWrite = false;

            mMuxer.release();
            mMuxer = null;
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
