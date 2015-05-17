package com.kamcord.app.server.model.builder;

import com.kamcord.app.server.model.Metadata;
import com.kamcord.app.server.model.ReserveVideoEntity;

import java.util.ArrayList;

/**
 * Created by pplunkett on 5/15/15.
 */
public class ReserveVideoEntityBuilder {
    private ReserveVideoEntity reserveVideoEntity = new ReserveVideoEntity();

    public ReserveVideoEntityBuilder setGameId(String gameId)
    {
        reserveVideoEntity.game_id = gameId;
        return this;
    }

    public ReserveVideoEntityBuilder addKeyword(String keyword)
    {
        if( reserveVideoEntity.keywords == null )
        {
            reserveVideoEntity.keywords = new ArrayList<String>();
        }
        reserveVideoEntity.keywords.add(keyword);
        return this;
    }

    public ReserveVideoEntityBuilder setDescription(String description)
    {
        reserveVideoEntity.description = description;
        return this;
    }

    public ReserveVideoEntityBuilder setUserTitle(String userTitle)
    {
        reserveVideoEntity.user_title = userTitle;
        return this;
    }

    public ReserveVideoEntityBuilder setDefaultTitle(String defaultTitle)
    {
        reserveVideoEntity.default_title = defaultTitle;
        return this;
    }

    public ReserveVideoEntityBuilder addMetadata(Metadata metadata)
    {
        if( reserveVideoEntity.metadata == null )
        {
            reserveVideoEntity.metadata = new ArrayList<Metadata>();
        }
        reserveVideoEntity.metadata.add(metadata);
        return this;
    }

    public ReserveVideoEntity build()
    {
        return reserveVideoEntity;
    }
}
