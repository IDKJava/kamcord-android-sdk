package com.kamcord.app.testutils.analytics;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Mehmet on 6/16/15.
 */
//will be overriden while reading.
@DynamoDBTable(tableName = "kamcord_app_launch")
public class KamcordAppLaunch extends KamcordEvent {


    private String eventDuration;
    private String fromNotif;
    private String notifType;
    private String customNotifCategory;

    @DynamoDBAttribute(attributeName = "event_duration")
    public String getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(String eventDuration) {
        this.eventDuration = eventDuration;
    }

    @DynamoDBAttribute(attributeName = "from_notif")
    public String getFromNotif() {
        return fromNotif;
    }

    public void setFromNotif(String fromNotif) {
        this.fromNotif = fromNotif;
    }

    @DynamoDBAttribute(attributeName = "notif_type")
    public String getNotifType() {
        return notifType;
    }

    public void setNotifType(String notifType) {
        this.notifType = notifType;
    }

    @DynamoDBAttribute(attributeName = "custom_notif_category")
    public String getCustomNotifCategory() {
        return customNotifCategory;
    }

    public void setCustomNotifCategory(String customNotifCategory) {
        this.customNotifCategory = customNotifCategory;
    }




}
