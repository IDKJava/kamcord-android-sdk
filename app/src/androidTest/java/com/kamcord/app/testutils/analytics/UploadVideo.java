package com.kamcord.app.testutils.analytics;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Mehmet on 6/16/15.
 */
//will be overriden while reading.
@DynamoDBTable(tableName = "upload_video")
public class UploadVideo {

    private String uiSessionId;
    private String requestDuration;
    private String isSuccess;
    private String failureReason;
    private String videoGlobalId;
    private String wasReplayed;

    @DynamoDBAttribute(attributeName = "ui_session_id")
    public String getUiSessionId() {
        return uiSessionId;
    }

    public void setUiSessionId(String uiSessionId) {
        this.uiSessionId = uiSessionId;
    }

    @DynamoDBAttribute(attributeName = "request_duration")
    public String getRequestDuration() {
        return requestDuration;
    }

    public void setRequestDuration(String requestDuration) {
        this.requestDuration = requestDuration;
    }

    @DynamoDBAttribute(attributeName = "is_success")
    public String getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(String isSuccess) {
        this.isSuccess = isSuccess;
    }

    @DynamoDBAttribute(attributeName = "failure_reason")
    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @DynamoDBAttribute(attributeName = "video_global_id")
    public String getVideoGlobalId() {
        return videoGlobalId;
    }

    public void setVideoGlobalId(String videoGlobalId) {
        this.videoGlobalId = videoGlobalId;
    }

    @DynamoDBAttribute(attributeName = "was_replayed")
    public String getWasReplayed() {
        return wasReplayed;
    }

    public void setWasReplayed(String wasReplayed) {
        this.wasReplayed = wasReplayed;
    }

}
