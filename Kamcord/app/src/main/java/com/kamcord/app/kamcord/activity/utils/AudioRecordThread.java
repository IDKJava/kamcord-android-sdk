package com.kamcord.app.kamcord.activity.utils;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class AudioRecordThread extends HandlerThread implements Handler.Callback {

    private MediaRecorder mRecorder = null;

    public AudioRecordThread(String name) {
        super(name);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d("Start", "recording");
        startRecording();
        return true;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setOutputFile("/sdcard/Kamcord_Android/kamcord.aac");
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
