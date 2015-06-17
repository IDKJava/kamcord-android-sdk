package com.kamcord.app.testutils.analytics;


import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Mehmet on 6/10/15.
 */
public class AnalyticsReader {
    private static String identityPoolId = "us-east-1:ffecbb18-df6f-40a4-baa9-2debbfe5fae8";

    public static CognitoCachingCredentialsProvider analyticsCredentialProvider = null;
    public static AmazonDynamoDBClient dynamoDBClient = null;
    public static DynamoDBMapper dynamoDBMapper = null;

    public static AmazonDynamoDBClient getAnalyticsClient(Context context){
        try {
            if (dynamoDBClient == null) {
                if (analyticsCredentialProvider == null) {
                    analyticsCredentialProvider =
                            new CognitoCachingCredentialsProvider(context, identityPoolId, Regions.US_EAST_1);
                }

                dynamoDBClient =
                        new AmazonDynamoDBClient(analyticsCredentialProvider);
                dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return dynamoDBClient;
    }

    public static String getTableName(String eventName) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        int YYYY = cal.get(Calendar.YEAR);
        int MM = cal.get(Calendar.MONTH) + 1;
        int DD = cal.get(Calendar.DAY_OF_MONTH);
        int HH = cal.get(Calendar.HOUR_OF_DAY);

        return String.format("analytics_%4d_%2d_%2d_%2d_%s", YYYY, MM, DD, HH, "eventName").replace(" ", "0");
    }

    public static Object getTableRow(String eventName, Class<?> model, String eventId){
        String tableName = getTableName(eventName);
        Object obj = dynamoDBMapper.load(model,
                    eventId,
                new DynamoDBMapperConfig(
                        new DynamoDBMapperConfig.TableNameOverride(tableName)));
        return obj;
    }



}
