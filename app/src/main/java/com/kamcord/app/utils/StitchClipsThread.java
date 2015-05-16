package com.kamcord.app.utils;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.kamcord.app.model.RecordingSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StitchClipsThread extends Thread {

    private RecordingSession mRecordingSession;
    private Context ffMpegContext;
    private FFmpeg mFFmpeg;
    private static String[] commandArray;
    private int commentAmount = 3;
    private ExecuteBinaryResponseHandler executeBinaryResponseHandler;

    public StitchClipsThread(RecordingSession recordingSession, Context context, ExecuteBinaryResponseHandler executeBinaryResponseHandler) {
        this.mRecordingSession = recordingSession;
        this.ffMpegContext = context;
        this.executeBinaryResponseHandler = executeBinaryResponseHandler;
    }

    @Override
    public void run() {
        writeClipFiles();

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

        File recordingSessionCacheDirectory = FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession);
        File videoClipListFile = new File(recordingSessionCacheDirectory, FileSystemManager.VIDEO_CLIPLIST_FILENAME);
        File audioClipListFile = new File(recordingSessionCacheDirectory, FileSystemManager.AUDIO_CLIPLIST_FILENAME);
        File stitchedVideoFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_VIDEO_FILENAME);
        File stitchedAudioFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_AUDIO_FILENAME);
        File mergedFile = new File(recordingSessionCacheDirectory, FileSystemManager.MERGED_VIDEO_FILENAME);
        commandArray = new String[commentAmount];
        commandArray[0] = "-f concat -i " + videoClipListFile.getAbsolutePath() + " -c copy " + stitchedVideoFile.getAbsolutePath();
        commandArray[1] = "-f concat -i " + audioClipListFile.getAbsolutePath() + " -c copy " + stitchedAudioFile.getAbsolutePath();
        commandArray[2] = "-i " + stitchedVideoFile.getAbsolutePath() + " -i " + stitchedAudioFile.getAbsolutePath()
                + " -vcodec copy -acodec copy -bsf:a aac_adtstoasc -strict -2 " + mergedFile.getAbsolutePath();
        startStitching();
    }

    public void startStitching() {

        for (int i = 0; i < commentAmount; i++) {
            try {
                // Execute "ffmpeg -version" command you just need to pass "-version"
                mFFmpeg.execute(commandArray[i], executeBinaryResponseHandler);
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }

    }

    private void writeClipFiles() {
        try {
            File recordingSessionCacheDirectory = FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession);
            File videoClipListFile = new File(
                    FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                    FileSystemManager.VIDEO_CLIPLIST_FILENAME);
            File audioClipListFile = new File(
                    FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession),
                    FileSystemManager.AUDIO_CLIPLIST_FILENAME);

            writeFileNamesWithExtensionToFile(videoClipListFile, recordingSessionCacheDirectory, ".mp4");
            writeFileNamesWithExtensionToFile(audioClipListFile, recordingSessionCacheDirectory, ".aac");
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

    private void writeFileNamesWithExtensionToFile(File outFile, File directory, String fileExtension) throws IOException
    {
        FileWriter fileWriter = new FileWriter(outFile);
        for (final File file : directory.listFiles()) {
            if (file.getName().endsWith(fileExtension)) {
                fileWriter.write("file '" + file.getAbsolutePath() + "'\n");
            }
        }
        fileWriter.close();
    }
}
