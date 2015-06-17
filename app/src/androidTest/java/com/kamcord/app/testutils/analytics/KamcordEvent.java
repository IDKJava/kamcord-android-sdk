package com.kamcord.app.testutils.analytics;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Mehmet on 6/16/15.
 */
//will be overriden while reading.
@DynamoDBTable(tableName = "kamcord_event")
public abstract class KamcordEvent {
    private String appDeviceId;
    private String analyticsDeviceId;
    private String userRegistrationId;
    private String clientName;
    private String clientVersion;
    private String osName;
    private String osVersion;
    private String deviceName;
    private String deviceModel;
    private String deviceBoard;
    private String language;
    private String country;
    //ts
    private String addedAt;
    //ts
    private String startTime;
    private String connectionType;
    private String appSessionId;
    private String eventId;

    @DynamoDBAttribute(attributeName = "app_device_id")
    public String getAppDeviceId() {
        return appDeviceId;
    }

    public void setAppDeviceId(String appDeviceId) {
        this.appDeviceId = appDeviceId;
    }

    @DynamoDBAttribute(attributeName = "analytics_device_id")
    public String getAnalyticsDeviceId() {
        return analyticsDeviceId;
    }

    public void setAnalyticsDeviceId(String analyticsDeviceId) {
        this.analyticsDeviceId = analyticsDeviceId;
    }

    @DynamoDBAttribute(attributeName = "user_registration_id")
    public String getUserRegistrationId() {
        return userRegistrationId;
    }

    public void setUserRegistrationId(String userRegistrationId) {
        this.userRegistrationId = userRegistrationId;
    }

    @DynamoDBAttribute(attributeName = "experimentId")
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @DynamoDBAttribute(attributeName = "client_version")
    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    @DynamoDBAttribute(attributeName = "os_name")
    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    @DynamoDBAttribute(attributeName = "os_version")
    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @DynamoDBAttribute(attributeName = "device_name")
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @DynamoDBAttribute(attributeName = "device_model")
    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    @DynamoDBAttribute(attributeName = "device_board")
    public String getDeviceBoard() {
        return deviceBoard;
    }

    public void setDeviceBoard(String deviceBoard) {
        this.deviceBoard = deviceBoard;
    }

    @DynamoDBAttribute(attributeName = "language")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @DynamoDBAttribute(attributeName = "country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @DynamoDBAttribute(attributeName = "added_at")
    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }

    @DynamoDBAttribute(attributeName = "start_time")
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @DynamoDBAttribute(attributeName = "connection_type")
    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    @DynamoDBAttribute(attributeName = "app_session_id")
    public String getAppSessionId() {
        return appSessionId;
    }

    public void setAppSessionId(String appSessionId) {
        this.appSessionId = appSessionId;
    }

    @DynamoDBHashKey(attributeName = "event_id")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
