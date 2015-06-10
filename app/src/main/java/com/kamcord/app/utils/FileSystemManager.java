package com.kamcord.app.utils;

import android.os.Environment;
import android.util.Log;

import com.kamcord.app.model.RecordingSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by pplunkett on 5/13/15.
 */
public class FileSystemManager {

    public static final String VIDEO_CLIPLIST_FILENAME = "video_cliplist.txt";
    public static final String AUDIO_CLIPLIST_FILENAME = "audio_cliplist.txt";
    public static final String STITCHED_VIDEO_FILENAME = "video.mp4";
    public static final String STITCHED_AUDIO_FILENAME = "audio.mp4";
    public static final String MERGED_VIDEO_FILENAME = "merged.mp4";
    public static final String VIDEO_CLIP_REGEX = "video[0-9][0-9][0-9].mp4";
    public static final String AUDIO_CLIP_REGEX = "audio[0-9][0-9][0-9].mp4";

    public static File getCacheDirectory()
    {
        File cacheDirectory = new File(Environment.getExternalStorageDirectory(), "Kamcord_Android");
        cacheDirectory.mkdirs(); // Make sure we create our cache directory when we ask for it.

        // We also need to make sure that no other apps index our media files.
        makeNoMedia(cacheDirectory);

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

        // We also need to make sure that no other apps index our media files.
        makeNoMedia(cacheDirectory);

        return cacheDirectory;
    }

    public static File getRecordingSessionCacheDirectory(RecordingSession recordingSession)
    {
        File cacheDirectory = new File(getGameCacheDirectory(recordingSession), recordingSession.getUUID());
        cacheDirectory.mkdirs();

        // We also need to make sure that no other apps index our media files.
        makeNoMedia(cacheDirectory);

        return cacheDirectory;
    }

    public static void cleanRecordingSessionCacheDirectory(RecordingSession recordingSession)
    {
        File recordingSessionCacheDirectory = getRecordingSessionCacheDirectory(recordingSession);
        nukeDirectory(recordingSessionCacheDirectory);
    }


    private static void nukeDirectory(File file)
    {
        if(file.isDirectory()) {
            for(File child : file.listFiles()) {
                nukeDirectory(child);
            }
        }
        file.delete();
    }

    public static void deleteUnmerged(RecordingSession session)
    {
        File sessionCache = getRecordingSessionCacheDirectory(session);
        String clipRegex = "(" + VIDEO_CLIP_REGEX + "|" + AUDIO_CLIP_REGEX + ")";
        for( File file : sessionCache.listFiles() )
        {
            if( file.getName().matches(clipRegex) )
            {
                file.delete();
            }
        }

        // TODO: If we ever want to give the option to not upload audio, we shouldn't do the following.
        new File(sessionCache, STITCHED_VIDEO_FILENAME).delete();
        new File(sessionCache, STITCHED_AUDIO_FILENAME).delete();
    }

    public enum Mark {
        SHARED,
        UPLOADED,
    }

    public static void markRecordingSession(RecordingSession session, Mark mark) {
        markRecordingSession(session, mark, "");
    }

    public static void markRecordingSession(RecordingSession session, Mark mark, String info) {
        FileWriter fileWriter = null;
        try {
            File markFile = new File(getRecordingSessionCacheDirectory(session), "." + mark.name());
            markFile.createNewFile();
            if( info != null ) {
                fileWriter = new FileWriter(markFile, false);
                fileWriter.write(info);
            }
        } catch( IOException e ) {
            Log.w("Kamcord", "Unable to mark recording session with mark " + mark.name());
        }
        finally{
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch( Exception e ) {
                }
            }
        }
    }

    public static boolean directoryHasMark(File directory, Mark mark) {
        return new File(directory, "." + mark.name()).exists();
    }

    public static String getMarkInfo(File directory, Mark mark) {
        String info = null;

        if( directoryHasMark(directory, mark) ) {
            try {
                File markFile = new File(directory, "." + mark.name());
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(markFile)));
                String line;
                while( (line = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                info = stringBuilder.toString();
            } catch( FileNotFoundException e ) {
                info = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return info;
    }

    public static void removeOldRecordings() {
        File cacheDirectory = getCacheDirectory();
        for( File dir : cacheDirectory.listFiles(PACKAGE_FILENAME_FILTER) ) {
            for( File recordingCache : dir.listFiles(UUID_FILENAME_FILTER) ) {

                // If the recording session was not shared, or the recording session has completed upload,
                // we remove the recording cache.
                if( !directoryHasMark(recordingCache, Mark.SHARED) || directoryHasMark(recordingCache, Mark.UPLOADED) ) {
                    nukeDirectory(recordingCache);
                }
            }
        }
    }

    private static final FilenameFilter PACKAGE_FILENAME_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File parent, String filename) {
            return new File(parent, filename).isDirectory() && filename.matches("^[^\\.]*(\\.[^\\.]*)*$");
        }
    };

    private static final FilenameFilter UUID_FILENAME_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File parent, String filename) {
            try {
                UUID.fromString(filename);
                return new File(parent, filename).isDirectory();
            } catch( Exception e ) {
            }
            return false;
        }
    };

    private static void makeNoMedia(File directory)
    {
        if( directory.isDirectory() ) {
        try {
                File noMedia = new File(directory, ".nomedia");
                noMedia.createNewFile();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
