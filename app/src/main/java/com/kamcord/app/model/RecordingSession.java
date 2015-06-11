package com.kamcord.app.model;

import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.StringUtils;

import java.util.UUID;

/**
 * Created by pplunkett on 5/15/15.
 */
public class RecordingSession {
    public static final float UPLOAD_FAILED_PROGRESS = Float.MAX_VALUE;
    public static final float UPLOAD_PROCESSING_PROGRESS = 2f;

    private String uuid;
    private String videoTitle;
    private String videoDescription;
    private String gameServerID;
    private String gameServerName;
    private String gamePackageName;
    private State state = State.STARTED;

    private transient boolean recordedFrames = false;
    private transient float uploadProgress = -1f;

    public RecordingSession() {}

    public RecordingSession(String uuid) {
        this.uuid = uuid;
    }

    public RecordingSession(Game game) {
        uuid = UUID.randomUUID().toString();
        gameServerID = game.game_primary_id;
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

    public void setUploadProgress(float uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public float getUploadProgress() {
        return uploadProgress;
    }

    public void setRecordedFrames(boolean recordedFrames)
    {
        this.recordedFrames = recordedFrames;
    }
    public boolean hasRecordedFrames()
    {
        return recordedFrames;
    }

    public void setState(State state) {
        this.state = state;
    }
    public State getState() {
        return state;
    }

    public enum State {
        STARTED,
        SHARED,
        UPLOADED,
        PROCESSED,
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
    public int hashCode() {
        return uuid.hashCode();
    }
}
