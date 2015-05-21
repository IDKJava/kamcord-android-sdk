package com.kamcord.app.utils;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.kamcord.app.model.RecordingSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StitchClipsThread extends Thread {

    public interface StitchSuccessListener {
        void onVideoStitchSuccess(RecordingSession recordingSession);

        void onVideoStitchFailure(RecordingSession recordingSession);

        void onAudioStitchSuccess(RecordingSession recordingSession);

        void onAudioStitchFailure(RecordingSession recordingSession);

        void onMergeSuccess(RecordingSession recordingSession);

        void onMergeFailure(RecordingSession recordingSession);
    }

    private RecordingSession mRecordingSession;
    private Context ffMpegContext;
    private FFmpeg mFFmpeg;
    private StitchSuccessListener listener;

    public StitchClipsThread(RecordingSession recordingSession, Context context, StitchSuccessListener listener) {
        this.mRecordingSession = recordingSession;
        this.ffMpegContext = context;
        this.listener = listener;
    }

    @Override
    public void run() {
        mFFmpeg = FFmpeg.getInstance(ffMpegContext);
        try {
            mFFmpeg.loadBinary(null);
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        writeClipFiles();

        File recordingSessionCacheDirectory = FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession);
        File videoClipListFile = new File(recordingSessionCacheDirectory, FileSystemManager.VIDEO_CLIPLIST_FILENAME);
        File audioClipListFile = new File(recordingSessionCacheDirectory, FileSystemManager.AUDIO_CLIPLIST_FILENAME);
        File stitchedVideoFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_VIDEO_FILENAME);
        File stitchedAudioFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_AUDIO_FILENAME);
        File mergedFile = new File(recordingSessionCacheDirectory, FileSystemManager.MERGED_VIDEO_FILENAME);

        stitchClips(videoClipListFile, stitchedVideoFile, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {
                if (listener != null) {
                    listener.onVideoStitchSuccess(mRecordingSession);
                }
            }

            @Override
            public void onFailure(String message) {
                if (listener != null) {
                    listener.onVideoStitchFailure(mRecordingSession);
                }
            }
        });

        stitchClips(audioClipListFile, stitchedAudioFile, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {
                if (listener != null) {
                    listener.onAudioStitchSuccess(mRecordingSession);
                }
            }

            @Override
            public void onFailure(String message) {
                if (listener != null) {
                    listener.onAudioStitchFailure(mRecordingSession);
                }
            }
        });

        mergeVideoAndAudio(stitchedVideoFile, stitchedAudioFile, mergedFile, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {

            }

            @Override
            public void onFailure(String message) {
                if (listener != null) {
                    listener.onMergeFailure(mRecordingSession);
                }
            }

            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onMergeSuccess(mRecordingSession);
                }
            }
        });
    }

    private void stitchClips(File clipListFile, File result, ExecuteBinaryResponseHandler handler) {
        String command = "-f concat -i " + clipListFile.getAbsolutePath() + " -c copy " + result.getAbsolutePath();
        try {
            mFFmpeg.execute(command, handler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void mergeVideoAndAudio(File videoFile, File audioFile, File result, ExecuteBinaryResponseHandler handler) {
        String command = "-i " + videoFile.getAbsolutePath() + " -i " + audioFile.getAbsolutePath()
                + " -vcodec copy -acodec copy " + result.getAbsolutePath();
        try {
            mFFmpeg.execute(command, handler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
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

            writeFileNamesWithExtensionToFile(videoClipListFile, recordingSessionCacheDirectory, "video[0-9][0-9][0-9].mp4");
            writeFileNamesWithExtensionToFile(audioClipListFile, recordingSessionCacheDirectory, "audio[0-9][0-9][0-9].mp4");
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

    private void writeFileNamesWithExtensionToFile(File outFile, File directory, String regex) throws IOException {
        FileWriter fileWriter = new FileWriter(outFile);
        for (final File file : directory.listFiles()) {
            if (file.getName().matches(regex)) {
                fileWriter.write("file '" + file.getAbsolutePath() + "'\n");
            }
        }
        fileWriter.close();
    }
}
