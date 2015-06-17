package com.kamcord.app.testutils.analytics;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Mehmet on 6/16/15.
 */
//will be overriden while reading.
@DynamoDBTable(tableName = "external_share")
public class FirstKamcordAppLaunch extends KamcordEvent{

}
