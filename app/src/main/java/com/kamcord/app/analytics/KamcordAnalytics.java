package com.kamcord.app.analytics;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.kamcord.app.server.model.analytics.Event;

import java.util.HashSet;

/**
 * Created by pplunkett on 6/15/15.
 */
public class KamcordAnalytics {
    private static final String ANALYTICS_PREFS = "KAMCORD_ANALYTICS_PREFS";

    private static final String LAST_SEND_TIME_KEY = "LAST_SEND_TIME";

    private static SharedPreferences preferences = null;
    private static AnalyticsThread analyticsThread;

    private static HashSet<Event> completedEvents = new HashSet<>();
    private static HashSet<Event> pendingEvents = new HashSet<>();

    public static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(ANALYTICS_PREFS, Context.MODE_PRIVATE);

        if( analyticsThread == null ) {
            startAnalyticsThread(context);
        }
    }

    private static void startAnalyticsThread(Context context) {
        long lastSendTime = preferences.getLong(LAST_SEND_TIME_KEY, 0);
        analyticsThread = new AnalyticsThread("Kamcord Analytics", lastSendTime);

        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(analyticsThread);
        analyticsThread.start();
        analyticsThread.setHandler(new Handler(analyticsThread.getLooper(), analyticsThread));
    }
}
