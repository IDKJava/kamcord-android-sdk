package com.kamcord.app.server.client;

import retrofit.http.POST;

/**
 * Created by pplunkett on 5/16/15.
 */
public class AmazonS3Client {

    public interface AmazonS3
    {
        @POST("?uploads")
        void startS3Upload();
    }
}
