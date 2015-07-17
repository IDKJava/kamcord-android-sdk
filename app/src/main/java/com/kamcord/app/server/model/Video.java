package com.kamcord.app.server.model;

import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.utils.FileSystemManager;

import java.io.File;
import java.util.Date;

/**
 * Created by pplunkett on 5/28/15.
 */
public class Video {
    public static class Thumbnails {
        public String small;
        public String regular;
    }

    public enum State {
        RESERVED,
        IN_PROCESSING,
        PROCESSED,
    }

    public Thumbnails thumbnails;
    public String title;
    public String video_id;
    public double duration;
    public int views;
    public int likes;
    public int comments;
    public int reshares;
    public boolean is_user_liking;
    public boolean is_user_resharing;
    public String game_primary_id;
    public Date upload_time;
    public String user_id;
    public String video_url;
    public String video_site_watch_page;
    public String username;
    public String game_name;
    public boolean featured;

    public State video_state;

    public Game game;
    public User user;
    public Source source;

    public static class Builder {
        Video video = new Video();

        public Builder fromRecordingSession(RecordingSession session) {
            video.video_url = new File(FileSystemManager.getRecordingSessionCacheDirectory(session),
                    FileSystemManager.MERGED_VIDEO_FILENAME).getAbsolutePath();
            video.title = session.getVideoTitle();
            return this;
        }

        public Video build() {
            return video;
        }
    }
}
