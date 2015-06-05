package com.kamcord.app.thread;

import android.app.ActivityManager;
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

import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.service.RecordingService;
import com.kamcord.app.utils.ApplicationStateUtils;
import com.kamcord.app.utils.FileSystemManager;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.CyclicBarrier;

public class AudioRecordThread extends HandlerThread implements Handler.Callback {

    private MediaCodec mAudioCodec = null;
    private AudioRecord mAudioRecord = null;
    private MediaMuxer mMediaMuxer = null;
    private CyclicBarrier clipStartBarrier = null;

    private boolean mMuxerStart = false;
    private boolean mMuxerWrite = false;
    private int mTrackIndex = -1;


    private Context mContext;
    private Handler mHandler;
    private int audioNumber = 0;

    private long presentationStartUs = -1;

    private ActivityManager activityManager;
    private RecordingSession mRecordingSession;

    private static class CodecSettings {
        public static final int SAMPLE_RATE = 44100;
        public static final int BIT_RATE = 64 * 1024;
    }

    public AudioRecordThread(Context context, RecordingSession recordingSession, CyclicBarrier clipStartBarrier) {
        super("dsdsd");
        this.mContext = context;

        this.activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
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
                break;

            case Message.POLL:
                if (!ApplicationStateUtils.isGameInForeground(mRecordingSession.getGamePackageName())) {
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

    private void recordUntilBackground() {

        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                CodecSettings.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(CodecSettings.SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT));

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
            mAudioCodec.start();

            try {
                clipStartBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
                releaseEncoder();
                return;
            }
            presentationStartUs = -1;
            mAudioRecord.startRecording();
            while(ApplicationStateUtils.isGameInForeground(mRecordingSession.getGamePackageName())) {
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
            MediaFormat format = MediaFormat.createAudioFormat("audio/mp4a-latm", CodecSettings.SAMPLE_RATE, 1);
            format.setInteger(MediaFormat.KEY_BIT_RATE, CodecSettings.BIT_RATE);
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
            mAudioCodec.queueInputBuffer(bufferIndex, 0, numBytesRead, System.nanoTime() / 1000, 0);
        }
    }

    private long lastPresentationTimeUs = -1;
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

            if (info.size != 0 && mMuxerStart ) {
                encodedData.position(info.offset);
                encodedData.limit(info.offset + info.size);
                if( presentationStartUs < 0 )
                {
                    presentationStartUs = info.presentationTimeUs;
                }
                if( info.presentationTimeUs > lastPresentationTimeUs ) {
                    mMediaMuxer.writeSampleData(mTrackIndex, encodedData, info);
                    lastPresentationTimeUs = info.presentationTimeUs;
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
            if (mMuxerStart) {
                mMediaMuxer.stop();
            }
            mMuxerStart = false;

            if( mMuxerWrite ) {
                audioNumber++;
            }
            mMuxerWrite = false;

            mMediaMuxer.release();
            mMediaMuxer = null;
        }
    }

    public static class Message {
        public static final int RECORD_CLIP = 1;
        public static final int STOP_RECORDING = 2;
        public static final int POLL = 3;
    }
}
