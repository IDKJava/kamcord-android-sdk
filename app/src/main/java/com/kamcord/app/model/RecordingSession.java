package com.kamcord.app.model;

import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.StringUtils;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by pplunkett on 5/15/15.
 */
public class RecordingSession {
    public static final float UPLOAD_FAILED_PROGRESS = Float.MAX_VALUE;
    public static final float UPLOAD_PROCESSING_PROGRESS = 2f;

    public enum State {
        STARTED,
        SHARED,
        UPLOADED,
        PROCESSED,
    }

    private String uuid;
    private String videoTitle;
    private String videoDescription;
    private String gameServerID;
    private String gameServerName;
    private String gamePackageName;
    private State state = State.STARTED;

    private String globalId = null;
    private HashMap<Integer, Boolean> shareSources = null;

    private boolean wasReplayed = false;

    private transient boolean recordedFrames = false;
    private transient float uploadProgress = -1f;
    private transient long durationUs = 0;

    public RecordingSession() {}

    public RecordingSession(String uuid, String gamePackageName) {
        this.uuid = uuid;
        this.gamePackageName = gamePackageName;
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

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }
    public String getGlobalId() {
        return globalId;
    }

    public void incrementDurationUs(long increment) {
        durationUs += increment;
    }
    public long getDurationUs() {
        return durationUs;
    }

    public HashMap<Integer, Boolean> getShareSources() {
        return shareSources;
    }

    public void setShareSources(HashMap<Integer, Boolean> shareSources) {
        this.shareSources = shareSources;
    }

    public boolean wasReplayed() {
        return wasReplayed;
    }
    public void setWasReplayed(boolean wasReplayed) {
        this.wasReplayed = wasReplayed;
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
