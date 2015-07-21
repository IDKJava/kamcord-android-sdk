package com.kamcord.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by donliang1 on 5/12/15.
 */
public class VideoUtils {
    private static final int MAX_EXTERNAL_SHARE_TEXT_LENGTH = 140;

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

    public static String getStreamDurationString(Date date) {
        long duration;
        Date endDate = Calendar.getInstance().getTime();
        if (date != null && date.getTime() != 0L) {
            duration = endDate.getTime() - date.getTime();
        } else {
            duration = 0;
        }
        return VideoUtils.videoDurationString(TimeUnit.MILLISECONDS, duration);
    }

    public static String videoDurationString(TimeUnit unit, long duration) {
        String time;

        long hours = unit.toHours(duration);
        long mins = unit.toMinutes(duration) % 60;
        long secs = unit.toSeconds(duration) % 60;
        if (hours == 0) {
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
        if (!thumbnailFile.exists()) {
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

    public static void doExternalShare(Context context, Video video) {
        if (context instanceof Activity && video.video_id != null) {
            Activity activity = (Activity) context;
            String watchPageLink = "www.kamcord.com/v/" + video.video_id;


            String externalShareText = null;
            if (video.title != null) {
                externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareText),
                        video.title, watchPageLink);
                int diff = externalShareText.length() - MAX_EXTERNAL_SHARE_TEXT_LENGTH;
                if (diff > 0) {
                    String truncatedTitle = StringUtils.ellipsize(video.title, video.title.length() - diff);
                    externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareText),
                            truncatedTitle, video.video_site_watch_page);
                }
            } else {
                externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareTextNoTitle),
                        watchPageLink);
            }
            externalShareText = StringUtils.ellipsize(externalShareText, MAX_EXTERNAL_SHARE_TEXT_LENGTH);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, externalShareText);
            shareIntent.setType("text/plain");
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.shareTo)));
        }
    }

    public static void doExternalShare(Context context, Stream stream) {
        if (context instanceof Activity && stream.user != null && stream.user.username != null) {
            Activity activity = (Activity) context;
            String streamLink = "www.kamcord.com/live/" + stream.user.username;


            String externalShareText = null;
            if (stream.name != null) {
                externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareTextStream),
                        stream.name, streamLink);
                int diff = externalShareText.length() - MAX_EXTERNAL_SHARE_TEXT_LENGTH;
                if (diff > 0) {
                    String truncatedTitle = StringUtils.ellipsize(stream.name, stream.name.length() - diff);
                    externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareTextStream),
                            truncatedTitle, streamLink);
                }
            } else {
                externalShareText = String.format(Locale.ENGLISH, activity.getString(R.string.externalShareTextNoTitleStream),
                        streamLink);
            }
            externalShareText = StringUtils.ellipsize(externalShareText, MAX_EXTERNAL_SHARE_TEXT_LENGTH);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, externalShareText);
            shareIntent.setType("text/plain");
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.shareTo)));
        }
    }
}
