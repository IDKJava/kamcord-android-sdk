package com.kamcord.app.kamcord.activity.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.kamcord.app.kamcord.activity.model.RecordingMessage;

import java.io.IOException;
import java.util.List;

public class AudioRecordThread extends HandlerThread implements Handler.Callback {

    private MediaRecorder mRecorder = null;
    private RecordingMessage msgObject;
    private Context mContext;
    private boolean recordFlag;
    private Handler mHandler;
    private String selectedPackageName;
    private String gamefolder;
    private int AudioNumber = 1;

    private ActivityManager activityManager;
    private List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList;
    private ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
    private String[] packageList;
    private String packageString;

    public AudioRecordThread(String name) {
        super(name);
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case 1:
                this.msgObject = (RecordingMessage) msg.obj;
                this.mContext = msgObject.getContext();
                this.recordFlag = msgObject.getRecordFlag();
                this.mHandler = msgObject.getHandler();
                this.selectedPackageName = msgObject.getPackageName();
                this.gamefolder = msgObject.getGameFolderString();

                startRecording();
                while (pollingGame()) {
                }
                stopRecording();
                AudioNumber++;
                while (!pollingGame()) {
                }
                Log.d("Record", "Audio Again");
                Message resumeMsg = Message.obtain(this.mHandler, 1, this.msgObject);
                this.mHandler.sendMessage(resumeMsg);
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
            return true;
        }
        return false;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        String AudioPath = "/sdcard/Kamcord_Android/" + gamefolder + "audio" + AudioNumber + ".aac";
        mRecorder.setOutputFile(AudioPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("Recorder", "prepare() failed");
        }

        mRecorder.start();
    }

    public void stopRecording() {
        Log.d("Stop", "recording");
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
}
