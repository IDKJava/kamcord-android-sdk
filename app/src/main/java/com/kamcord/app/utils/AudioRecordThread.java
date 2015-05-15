package com.kamcord.app.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.kamcord.app.server.model.Game;

import java.io.IOException;
import java.util.List;

public class AudioRecordThread extends HandlerThread implements Handler.Callback {

    private MediaRecorder mRecorder = null;
    private Context mContext;
    private Handler mHandler;
    private int audioNumber = 0;
    private Game gameModel;
    private String mSessionFolderName;

    private ActivityManager activityManager;


    public AudioRecordThread(Game gameModel, Context context, FileManagement fileManagement) {
        super("dsdsd");
        this.gameModel = gameModel;
        this.mContext = context;

        this.activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mSessionFolderName = fileManagement.getGameName() + "/" + fileManagement.getUUIDString() + "/";
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {

        switch (msg.what) {
            case Message.RECORD_CLIP:
                Log.v("FindMe", "RECORD_CLIP");
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
                Log.v("FindMe", "STOP_RECORDING");
                break;
        }
        return false;
    }

    public void setHandler(Handler handler)
    {
        this.mHandler = handler;
    }

    private boolean isGameInForeground() {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfoList) {
            if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String pkgName : runningAppProcessInfo.pkgList) {
                    if (pkgName.equals(gameModel.play_store_id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void recordUntilBackground() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        String AudioPath = "/sdcard/Kamcord_Android/" + mSessionFolderName + "audio" + audioNumber + ".aac";
        mRecorder.setOutputFile(AudioPath);
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


    public enum RecordingState {
        IDLE,
        RECORDING,
        PAUSED,
    }

    public static class Message {
        public static final int RECORD_CLIP = 1;
        public static final int STOP_RECORDING = 2;
        public static final int POLL = 3;
    }
}
