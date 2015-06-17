package com.kamcord.app.testutils.analytics;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Mehmet on 6/16/15.
 */
//will be overriden while reading.
@DynamoDBTable(tableName = "external_share")
public class ExternalShare extends KamcordEvent{
    private String uiSessionId;
    private float requestDuration;
    private int isSuccess;
    private String failureReason;
    private String videoGlobalId;
    private String externalNetwork;

    @DynamoDBAttribute(attributeName = "ui_session_id")
    public String getUiSessionId() {
        return uiSessionId;
    }

    public void setUiSessionId(String uiSessionId) {
        this.uiSessionId = uiSessionId;
    }

    @DynamoDBAttribute(attributeName = "request_duration")
    public float getRequestDuration() {
        return requestDuration;
    }

    public void setRequestDuration(float requestDuration) {
        this.requestDuration = requestDuration;
    }

    @DynamoDBAttribute(attributeName = "is_success")
    public int getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(int isSuccess) {
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

    @DynamoDBAttribute(attributeName = "external_network")
    public String getExternalNetwork() {
        return externalNetwork;
    }

    public void setExternalNetwork(String externalNetwork) {
        this.externalNetwork = externalNetwork;
    }



}
