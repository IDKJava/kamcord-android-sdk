package com.kamcord.app.kamcord.activity.utils;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

public class StitchClipsThread extends Thread {

    private String ResultPath;
    private String GameSessionPath;
    private Context FFmpegContext;
    private FFmpeg mFFmpeg;
    private static String cmd;

    public StitchClipsThread(String gameSessionFolder, Context context) {
        this.GameSessionPath = gameSessionFolder;
        this.FFmpegContext = context;
    }

    @Override
    public void run() {

        File ResultFolder = new File(GameSessionPath);
        if (!ResultFolder.exists() || !ResultFolder.isDirectory()) {
            ResultFolder.mkdir();
        }

        mFFmpeg = FFmpeg.getInstance(FFmpegContext);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d("FFmpeg load Binaray:", "start");

                }

                @Override
                public void onFailure() {
                    Log.d("FFmpeg load Binaray:", "fails");
                }

                @Override
                public void onSuccess() {
                    Log.d("FFmpeg load Binaray:", "success");
                }

                @Override
                public void onFinish() {
                    Log.d("FFmpeg load Binaray:", "finish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        cmd = "-f concat -i /sdcard/Kamcord_Android/cliplist.txt" + " -c copy " + GameSessionPath + "Kamcord.mp4";
        startStitching();
    }

    public void startStitching() {
        try {

            // Execute "ffmpeg -version" command you just need to pass "-version"
            mFFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                    Log.d("FFmpeg execute:", "start");
                }

                @Override
                public void onProgress(String message) {
                    Log.d("progress:", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d("FFmpeg execute:", "failure");
                }

                @Override
                public void onSuccess(String message) {
                    Log.d("FFmpeg execute:", "success");
                }

                @Override
                public void onFinish() {
                    Log.d("FFmpeg execute:", "finish");
                }
            });

        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

}
