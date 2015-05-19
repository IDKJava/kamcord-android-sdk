package com.kamcord.app.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;

import com.kamcord.app.model.RecordingSession;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;

public class AudioRecordThread extends HandlerThread implements Handler.Callback {

    private MediaCodec mAudioCodec = null;
    private AudioRecord mAudioRecord = null;
    private MediaMuxer mMediaMuxer = null;

    private long mCurrentTimestampUs = 0;
    private boolean mMuxerStart = false;
    private boolean mMuxerWrite = false;
    private int mTrackIndex = -1;


    private Context mContext;
    private Handler mHandler;
    private int audioNumber = 0;

    private ActivityManager activityManager;
    private RecordingSession mRecordingSession;


    public AudioRecordThread(Context context, RecordingSession recordingSession) {
        super("dsdsd");
        this.mContext = context;

        this.activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        this.mRecordingSession = recordingSession;
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {

        switch (msg.what) {
            case Message.RECORD_CLIP:
                audioNumber++;
                recordUntilBackground();
                mHandler.removeMessages(Message.POLL);
                mHandler.sendEmptyMessage(Message.POLL);
                break;

            case Message.POLL:
                if (!isGameInForeground()) {
                    mHandler.removeMessages(Message.POLL);
                    mHandler.sendEmptyMessageDelayed(Message.POLL, 100);
                } else {
                    mHandler.removeMessages(Message.RECORD_CLIP);
                    mHandler.sendEmptyMessage(Message.RECORD_CLIP);
                }
                break;

            case Message.STOP_RECORDING:
                break;
        }
        return false;
    }

    public void setHandler(Handler handler)
    {
        this.mHandler = handler;
    }

    private boolean isGameInForeground() {

        if( !((PowerManager) mContext.getSystemService(Context.POWER_SERVICE)).isInteractive() )
        {
            return false;
        }

        if( ((KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode() )
        {
            return false;
        }

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfoList) {
            if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String pkgName : runningAppProcessInfo.pkgList) {
                    if (pkgName.equals(mRecordingSession.getGamePackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void recordUntilBackground() {

        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT));

        prepareMediaCodec();

        try {
            File audioFile = new File(
                    FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                    String.format(Locale.ENGLISH, "audio%03d.mp4", audioNumber));
            mMediaMuxer = new MediaMuxer(audioFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch( IOException e)
        {
            mMediaMuxer = null;
        }

        if( mAudioCodec != null && mMediaMuxer != null)
        {

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            mCurrentTimestampUs = 0;
            mAudioRecord.startRecording();
            mAudioCodec.start();

            while(isGameInForeground()) {
                queueEncoder();
                drainEncoder(info);
            }

            releaseEncoder();
        }
    }

    private void prepareMediaCodec()
    {
        try {
            mAudioCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
            MediaFormat format = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 1);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 64 * 1024);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            mAudioCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch(IOException e)
        {
            mAudioCodec = null;
        }
    }

    private void queueEncoder() {
        int bufferIndex = mAudioCodec.dequeueInputBuffer(1000);
        if (bufferIndex >= 0)
        {
            ByteBuffer buffer = mAudioCodec.getInputBuffer(bufferIndex);
            int numBytesRead = mAudioRecord.read(buffer, buffer.capacity());
            mAudioCodec.queueInputBuffer(bufferIndex, 0, numBytesRead, mCurrentTimestampUs, 0);
            mCurrentTimestampUs += 1000000 * (numBytesRead / 2) / 44100;
        }
    }

    private void drainEncoder(MediaCodec.BufferInfo info) {
        int encoderStatus = mAudioCodec.dequeueOutputBuffer(info, 0);
        if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // No output available
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            if (!mMuxerStart) {
                mTrackIndex = mMediaMuxer.addTrack(mAudioCodec.getOutputFormat());
                mMediaMuxer.start();
                mMuxerStart = true;
            }
        } else if (encoderStatus < 0) {
            // ignore it, but why?
        } else {
            ByteBuffer encodedData = mAudioCodec.getOutputBuffer(encoderStatus);
            if (encodedData == null) {
                throw new RuntimeException("Could not fetch buffer." + encoderStatus);
            }

            if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                info.size = 0;
            }

            if (info.size != 0) {
                if (mMuxerStart) {
                    encodedData.position(info.offset);
                    encodedData.limit(info.offset + info.size);
                    mMediaMuxer.writeSampleData(mTrackIndex, encodedData, info);
                    mMuxerWrite = true;
                }
            }

            mAudioCodec.releaseOutputBuffer(encoderStatus, false);
        }
    }

    private void releaseEncoder()
    {
        if (mAudioCodec != null) {
            mAudioCodec.stop();
            mAudioCodec.release();
            mAudioCodec = null;
        }
        if( mAudioRecord != null )
        {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if (mMediaMuxer != null) {
            if (mMuxerStart && mMuxerWrite) {
                mMediaMuxer.stop();
            }
            mMediaMuxer.release();
            mMediaMuxer = null;
            mMuxerStart = false;
        }
    }

    public static class Message {
        public static final int RECORD_CLIP = 1;
        public static final int STOP_RECORDING = 2;
        public static final int POLL = 3;
    }
}
