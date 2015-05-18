package com.kamcord.app.server.model.builder;

import com.kamcord.app.server.model.VideoUploadedEntity;

import java.util.ArrayList;

/**
 * Created by pplunkett on 5/16/15.
 */
public class VideoUploadedEntityBuilder {
    private VideoUploadedEntity entity = new VideoUploadedEntity();

    public VideoUploadedEntityBuilder setVideoId(String videoId)
    {
        entity.video_id = videoId;
        return this;
    }

    public VideoUploadedEntityBuilder setVoiceEnabled(boolean voiceEnabled)
    {
        entity.voice_enabled = voiceEnabled;
        return this;
    }

    public VideoUploadedEntityBuilder addShare(VideoUploadedEntity.Share share)
    {
        if( entity.shares == null )
        {
            entity.shares = new ArrayList<VideoUploadedEntity.Share>();
        }
        entity.shares.add(share);
        return this;
    }

    public VideoUploadedEntity build()
    {
        return entity;
    }
}
