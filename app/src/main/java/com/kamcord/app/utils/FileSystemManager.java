package com.kamcord.app.utils;

import android.os.Environment;

import com.kamcord.app.model.RecordingSession;

import java.io.File;

/**
 * Created by pplunkett on 5/13/15.
 */
public class FileSystemManager {

    public static final String VIDEO_CLIPLIST_FILENAME = "video_cliplist.txt";
    public static final String AUDIO_CLIPLIST_FILENAME = "audio_cliplist.txt";
    public static final String STITCHED_VIDEO_FILENAME = "video.mp4";
    public static final String STITCHED_AUDIO_FILENAME = "audio.mp4";
    public static final String MERGED_VIDEO_FILENAME = "merged.mp4";

    public static File getCacheDirectory()
    {
        File cacheDirectory = new File(Environment.getExternalStorageDirectory(), "Kamcord_Android");
        cacheDirectory.mkdirs(); // Make sure we create our cache directory when we ask for it.
        return cacheDirectory;
    }

    public static File getSDKCacheDirectory()
    {
        return new File(Environment.getExternalStorageDirectory(), ".Kamcord");
    }

    public static File getOldSDKCacheDirectory()
    {
        return new File(Environment.getExternalStorageDirectory(), "Kamcord");
    }

    public static File getGameCacheDirectory(RecordingSession recordingSession)
    {
        File cacheDirectory = new File(getCacheDirectory(), recordingSession.getGamePackageName());
        cacheDirectory.mkdirs();
        return cacheDirectory;
    }

    public static File getRecordingSessionCacheDirectory(RecordingSession recordingSession)
    {
        File cacheDirectory = new File(getGameCacheDirectory(recordingSession), recordingSession.getUUID());
        cacheDirectory.mkdirs();
        return cacheDirectory;
    }

    public static void cleanCache() {
        try {
            clearCacheRecursive(getCacheDirectory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearCacheRecursive(File file)
    {
        if(file.isDirectory()) {
            for(File child : file.listFiles()) {
                clearCacheRecursive(child);
            }
        }
        file.delete();
    }
}
