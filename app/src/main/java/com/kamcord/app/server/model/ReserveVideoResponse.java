package com.kamcord.app.server.model;

import java.util.Date;

/**
 * Created by pplunkett on 5/14/15.
 */
public class ReserveVideoResponse {
    public String video_id;
    public Credentials credentials;
    public Location video_location;
    public Location voice_location;

    public static class Credentials
    {
        public String access_key_id;
        public String secret_access_key;
        public String session_token;
        public Date expiration;
    }

    public static class Location
    {
        public String key;
        public String url;
        public String bucket;
    }
}
