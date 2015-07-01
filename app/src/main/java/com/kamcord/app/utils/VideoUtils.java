package com.kamcord.app.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.kamcord.app.model.RecordingSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by donliang1 on 5/12/15.
 */
public class VideoUtils {

    public static String getVideoDuration(RecordingSession session) {
        String mergedVideoPath = new File(FileSystemManager.getRecordingSessionCacheDirectory(session),
                FileSystemManager.MERGED_VIDEO_FILENAME).getAbsolutePath();
        int videoDuration = 0;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String time = "";
        long hours, mins, secs;

        try {
            retriever.setDataSource(mergedVideoPath);
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoDuration = Integer.parseInt(time);
            time = videoDurationString(TimeUnit.MILLISECONDS, videoDuration);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        return time;
    }

    public static String videoDurationString(TimeUnit unit, long duration) {
        String time;

        long hours = unit.toHours(duration);
        long mins = unit.toMinutes(duration) % 60;
        long secs = unit.toSeconds(duration) % 60;
        if(hours == 0) {
            time = String.format("%02d:%02d", mins, secs);
        } else {
            time = String.format("%02d:%02d:%02d", hours, mins, secs);
        }

        return time;
    }

    public static File getVideoThumbnailFile(RecordingSession session) {
        File sessionCache = FileSystemManager.getRecordingSessionCacheDirectory(session);
        File thumbnailFile = new File(sessionCache, FileSystemManager.THUMBNAIL_FILENAME);

        // Lazily create the thumbnail.
        if( !thumbnailFile.exists() ) {
            File videoFile = new File(sessionCache, FileSystemManager.MERGED_VIDEO_FILENAME);
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(thumbnailFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return thumbnailFile;
    }

}
