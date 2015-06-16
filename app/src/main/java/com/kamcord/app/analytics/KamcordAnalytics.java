package com.kamcord.app.analytics;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by pplunkett on 6/15/15.
 */
public class KamcordAnalytics {
    private static final String ANALYTICS_PREFS = "KAMCORD_ANALYTICS_PREFS";

    private static SharedPreferences preferences = null;

    public static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(ANALYTICS_PREFS, Context.MODE_PRIVATE);
    }
}
