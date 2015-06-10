package com.kamcord.app.testutils.analytics;


import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by Mehmet on 6/10/15.
 */
public class AnalyticsReader {
    private static String identityPoolId = "us-east-1:ffecbb18-df6f-40a4-baa9-2debbfe5fae8";

    public static CognitoCachingCredentialsProvider analyticsCredentialProvider = null;
    public static AmazonDynamoDBClient dynamoDBClient = null;

    public static AmazonDynamoDBClient getAnalyticsClient(Context context){
        if(dynamoDBClient == null) {
            if (analyticsCredentialProvider == null) {
                analyticsCredentialProvider =
                        new CognitoCachingCredentialsProvider(context, identityPoolId, Regions.US_EAST_1);
            }

            dynamoDBClient =
                    new AmazonDynamoDBClient(analyticsCredentialProvider);
        }
        return dynamoDBClient;
    }


}
