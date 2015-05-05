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

    private String gameSessionPath;
    private Context ffMpegContext;
    private FFmpeg mFFmpeg;
    private static String cmd;

    public StitchClipsThread(String gameSessionFolder, Context context) {
        this.gameSessionPath = gameSessionFolder;
        this.ffMpegContext = context;
    }

    @Override
    public void run() {

        File ResultFolder = new File(gameSessionPath);
        if (!ResultFolder.exists() || !ResultFolder.isDirectory()) {
            ResultFolder.mkdir();
        }

        mFFmpeg = FFmpeg.getInstance(ffMpegContext);
        try {
            mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        cmd = "-f concat -i " + gameSessionPath + "cliplist.txt -c copy " + gameSessionPath + "video.mp4";
        startStitching();
    }

    public void startStitching() {
        try {

            // Execute "ffmpeg -version" command you just need to pass "-version"
            mFFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {
                    Log.d("progress:", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d("FFmpeg execute:", message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d("FFmpeg execute:", message);
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
