package com.kamcord.app.server.model;

import java.util.Date;

/**
 * Created by dennisqin on 6/30/15.
 */
public class Stream {
    public String stream_id;
    public String user_id;
    public String video_id;
    public Play play;
    public Thumbnails thumbnails;
    public boolean connected;
    public boolean live;
    public String game_id;
    public String name;
    public Date started_at;
    public Date ended_at;
    public int current_viewers_count;
    public int max_viewers_count;
    public int heart_count;
    public int message_count;
    public User user;
    public Game game;

    public class Play {
        public String rtmp;
        public String hls;
    }

    public class Thumbnails {
        public Thumbnail small;
        public Thumbnail medium;
        public Thumbnail large;

        public class Thumbnail {
            public String secure_url;
            public String unsecure_url;
        }
    }
}
