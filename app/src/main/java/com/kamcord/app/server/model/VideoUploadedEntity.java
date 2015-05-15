package com.kamcord.app.server.model;

import java.util.List;

/**
 * Created by pplunkett on 5/14/15.
 */
public class VideoUploadedEntity {
    public String video_id;
    public boolean voice_enabled;
    public List<Share> shares;

    public static class Share
    {
        public ShareSource source;
        public String description;
        public String title;
        public String access_token;
        public String refresh_token;
    }

    public enum ShareSource
    {
        FACEBOOK,
        TWITTER,
        YOUTUBE,
        EMAIL,
        LINE,
        WECHAT,
        NICO_NICO,
    }
}
