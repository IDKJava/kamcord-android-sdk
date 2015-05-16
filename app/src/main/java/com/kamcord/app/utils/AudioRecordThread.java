package com.kamcord.app.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;

import com.kamcord.app.model.RecordingSession;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AudioRecordThread extends HandlerThread implements Handler.Callback {

    private MediaRecorder mRecorder = null;
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
        File audioFile = new File(
                FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                String.format(Locale.ENGLISH, "audio%03d.aac", audioNumber));
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setOutputFile(audioFile.getAbsolutePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("Recorder", "prepare() failed");
        }

        mRecorder.start();

        while (isGameInForeground()) {
        }

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public static class Message {
        public static final int RECORD_CLIP = 1;
        public static final int STOP_RECORDING = 2;
        public static final int POLL = 3;
    }
}
