package com.kamcord.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.StringUtils;

import java.util.UUID;

/**
 * Created by pplunkett on 5/15/15.
 */
public class RecordingSession implements Parcelable {
    private String uuid;
    private String videoTitle;
    private String videoDescription;
    private String gameServerID;
    private String gameServerName;
    private String gamePackageName;

    public RecordingSession()
    {
    }

    public RecordingSession(Game game) {
        uuid = UUID.randomUUID().toString();
        gameServerName = game.game_primary_id;
        gameServerName = game.name;
        gamePackageName = game.play_store_id;
    }

    public String getUUID() {
        return uuid;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public String getGameServerID() {
        return gameServerID;
    }

    public String getGameServerName() {
        return gameServerName;
    }

    public String getGamePackageName() {
        return gamePackageName;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(uuid);
        parcel.writeString(videoTitle);
        parcel.writeString(videoDescription);
        parcel.writeString(gameServerID);
        parcel.writeString(gameServerName);
        parcel.writeString(gamePackageName);
    }

    public static final Parcelable.Creator<RecordingSession> CREATOR
            = new Parcelable.Creator<RecordingSession>() {
        public RecordingSession createFromParcel(Parcel in) {
            return new RecordingSession(in);
        }

        public RecordingSession[] newArray(int size) {
            return new RecordingSession[size];
        }
    };

    private RecordingSession(Parcel in) {
        uuid = in.readString();
        videoTitle = in.readString();
        videoDescription = in.readString();
        gameServerID = in.readString();
        gameServerName = in.readString();
        gamePackageName = in.readString();
    }

    @Override
    public boolean equals(Object other)
    {
        if( other == null || !(other instanceof RecordingSession) )
            return false;

        RecordingSession otherVideo = (RecordingSession) other;
        if( !StringUtils.compare(uuid, otherVideo.uuid) )
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
                .append("[")
                .append(uuid).append(",")
                .append(videoTitle).append(",")
                .append(videoDescription).append(",")
                .append(gameServerID).append(",")
                .append(gameServerName).append(",")
                .append(gamePackageName)
                .append("]").toString();
    }
}
