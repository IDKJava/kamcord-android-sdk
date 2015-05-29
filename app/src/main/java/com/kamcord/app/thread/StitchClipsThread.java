package com.kamcord.app.thread;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.utils.FileSystemManager;

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

    private Runnable executionSteps[] = new Runnable[]{
            new Runnable() {
                @Override
                public void run() {
                    writeClipFiles();
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    stitchAudioClips();
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    stitchVideoClips();
                }
            },
            new Runnable() {
                @Override
                public void run() {
                    mergeAudioAndVideoClips();
                }
            }
    };

    @Override
    public void run() {
        mFFmpeg = FFmpeg.getInstance(ffMpegContext);
        try {
            mFFmpeg.loadBinary(null);
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < executionSteps.length && !cancelled; i++) {
            executionSteps[i].run();
        }

        handleCancelled();
    }

    private void stitchVideoClips()
    {
        File recordingSessionCacheDirectory = FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession);
        File videoClipListFile = new File(recordingSessionCacheDirectory, FileSystemManager.VIDEO_CLIPLIST_FILENAME);
        File stitchedVideoFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_VIDEO_FILENAME);
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
    }

    private void stitchAudioClips()
    {
        File recordingSessionCacheDirectory = FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession);
        File audioClipListFile = new File(recordingSessionCacheDirectory, FileSystemManager.AUDIO_CLIPLIST_FILENAME);
        File stitchedAudioFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_AUDIO_FILENAME);
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
    }

    private void mergeAudioAndVideoClips()
    {
        File recordingSessionCacheDirectory = FileSystemManager.getRecordingSessionCacheDirectory(mRecordingSession);
        File stitchedVideoFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_VIDEO_FILENAME);
        File stitchedAudioFile = new File(recordingSessionCacheDirectory, FileSystemManager.STITCHED_AUDIO_FILENAME);
        File mergedFile = new File(recordingSessionCacheDirectory, FileSystemManager.MERGED_VIDEO_FILENAME);
        mergeVideoAndAudio(stitchedVideoFile, stitchedAudioFile, mergedFile, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {
                if (listener != null) {
                    listener.onMergeSuccess(mRecordingSession);
                }
            }

            @Override
            public void onFailure(String message) {
                if (listener != null) {
                    listener.onMergeFailure(mRecordingSession);
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

            writeFileNamesWithExtensionToFile(videoClipListFile, recordingSessionCacheDirectory, FileSystemManager.VIDEO_CLIP_REGEX);
            writeFileNamesWithExtensionToFile(audioClipListFile, recordingSessionCacheDirectory, FileSystemManager.AUDIO_CLIP_REGEX);
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

    private volatile boolean cancelled = false;
    public void cancelStitching() {
        cancelled = true;
        listener = null;
        mFFmpeg.killRunningProcesses();
    }

    private void handleCancelled()
    {
        if( cancelled )
        {
            // If we were cancelled in the middle of execution, let's just nuke the recording session.
            FileSystemManager.cleanRecordingSessionCacheDirectory(mRecordingSession);
        }
    }
}
