package com.kamcord.app.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.util.concurrent.TimeUnit;

/**
 * Created by donliang1 on 5/12/15.
 */
public class VideoUtils {

    public static String getVideoDuration(String filePath) {
        int videoDuration = 0;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String time = "";
        long hours, mins, secs;
        try {
            retriever.setDataSource(filePath);
            time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoDuration = Integer.parseInt(time);
            hours = TimeUnit.MILLISECONDS.toHours(videoDuration);
            mins = TimeUnit.MILLISECONDS.toMinutes(videoDuration);
            secs = TimeUnit.MILLISECONDS.toSeconds(videoDuration);
            if(hours == 0) {
                time = String.format("%02d:%02d:%02d", hours, mins, secs);
            } else {
                time = String.format("%02d:%02d", mins, secs);
            }
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

    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(2000000);
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
        return bitmap;
    }
}
