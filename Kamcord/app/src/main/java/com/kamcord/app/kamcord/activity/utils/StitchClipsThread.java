package com.kamcord.app.kamcord.activity.utils;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StitchClipsThread extends Thread {

    private String gameSessionPath;
    private Context ffMpegContext;
    private FFmpeg mFFmpeg;
    private static String[] commandArray;

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

        writeClipFile();

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

        commandArray = new String[3];
        commandArray[0] = "-f concat -i " + gameSessionPath + "video_cliplist.txt -c copy " + gameSessionPath + "video.mp4";
        commandArray[1] = "-f concat -i " + gameSessionPath + "audio_cliplist.txt -c copy " + gameSessionPath + "audioUntrimmed.aac";
        commandArray[2] = "-i " + gameSessionPath + "video.mp4 -i " + gameSessionPath + "audioUnTrimmed.aac -vcodec copy -acodec copy -bsf:a aac_adtstoasc -strict -2 " + gameSessionPath + "out.mp4";
        startStitching();
    }

    public void startStitching() {

        for (int i = 0; i < 3; i++) {
            try {
                // Execute "ffmpeg -version" command you just need to pass "-version"
                mFFmpeg.execute(commandArray[i], new ExecuteBinaryResponseHandler() {
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

    private void writeClipFile() {
        try {
            File gameSessionFolder = new File(gameSessionPath);
            FileWriter fileWriter = new FileWriter(gameSessionPath + "/video_cliplist.txt");
            for (final File file : gameSessionFolder.listFiles())
            {
                if( file.getName().endsWith(".mp4") )
                {
                    fileWriter.write("file '" + file.getAbsolutePath() + "'\n");
                }
            }
            fileWriter.close();

            fileWriter = new FileWriter(gameSessionPath + "/audio_cliplist.txt");
            for (final File file : gameSessionFolder.listFiles())
            {
                if( file.getName().endsWith(".aac") )
                {
                    fileWriter.write("file '" + file.getAbsolutePath() + "'\n");
                }
            }
            fileWriter.close();
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }
}
