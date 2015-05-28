package com.kamcord.app.server.model;

import java.util.Date;

/**
 * Created by pplunkett on 5/28/15.
 */
public class Video {
    public static class Thumbnails {
        public String small;
        public String regular;
    }

    public Thumbnails thumbnails;
    public String title;
    public String videoId;
    public double duration;
    public int views;
    public int likes;
    public int comments;
    public int reshares;
    public boolean isUserLiking;
    public boolean isUserResharing;
    public String gamePrimaryId;
    public Date uploadTime;
    public String userId;
    public String videoUrl;
    public String videoSiteWatchPage;
    public String username;
    public String gameName;
    public boolean featured;

    public Game game;
    public User user;
    public Source source;
}
