package com.kamcord.app.kamcord.activity.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.ArrayList;

public class StitchClipsThread extends Thread {

    private String ResultPath;
    private ArrayList<String> inputVideoClips;
    private Context FFmpegContext;
    private FFmpeg mFFmpeg;
    private String cmd;

    public StitchClipsThread(ArrayList<String> inputVideos, Context context) {
        this.inputVideoClips = inputVideos;
        this.FFmpegContext = context;

    }

    @Override
    public void run() {
        ResultPath = Environment.getExternalStorageDirectory().getParent() + "/" + Environment.getExternalStorageDirectory().getName() + "/Kamcord_Video";
        File ResultFolder = new File(ResultPath);
        if (!ResultFolder.exists() || !ResultFolder.isDirectory()) {
            ResultFolder.mkdir();
        }

        mFFmpeg = FFmpeg.getInstance(FFmpegContext);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d("FFmpeg","start");

                }

                @Override
                public void onFailure() {
                    Log.d("FFmpeg","fails");
                }

                @Override
                public void onSuccess() {
                    Log.d("loadBinary(success):", "yo");
                }

                @Override
                public void onFinish() {
                    Log.d("FFmpeg","finish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        cmd = "-i " + ResultPath+"/Clip1.mp4" + " " + ResultPath + "/video.avi";
        Log.d("cmd:", cmd);
        startStitching(cmd);
    }

    public void startStitching(String command) {
        try {


            // Execute "ffmpeg -version" command you just need to pass "-version"
            mFFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {

                }

                @Override
                public void onSuccess(String message) {
                    Log.d("execute:", "success");
                }

                @Override
                public void onFinish() {
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

}
