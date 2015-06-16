package com.kamcord.app.analytics;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.google.gson.Gson;
import com.kamcord.app.server.model.analytics.Event;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by pplunkett on 6/15/15.
 */
public class KamcordAnalytics {
    private static final String ANALYTICS_PREFS = "KAMCORD_ANALYTICS_PREFS";

    private static final String LAST_SEND_TIME_KEY = "LAST_SEND_TIME";
    private static final String FIRST_LAUNCH_KEY = "FIRST_LAUNCH";
    private static final String COMPLETED_EVENTS_KEY = "COMPLETED_EVENTS";
    private static final String PENDING_EVENTS_KEY = "PENDING_EVENTS";

    private static SharedPreferences preferences = null;
    private static AnalyticsThread analyticsThread;

    private static HashSet<Event> completedEvents = new HashSet<>();
    private static HashSet<Event> pendingEvents = new HashSet<>();

    public static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(ANALYTICS_PREFS, Context.MODE_PRIVATE);

        Set<String> serializedCompletedEvents = preferences.getStringSet(COMPLETED_EVENTS_KEY, new HashSet<String>());
        for( String serializedCompletedEvent : serializedCompletedEvents ) {
            completedEvents.add(new Gson().fromJson(serializedCompletedEvent, Event.class));
        }
        Set<String> serializedPendingEvents = preferences.getStringSet(COMPLETED_EVENTS_KEY, new HashSet<String>());
        for( String serializedPendingEvent : serializedPendingEvents ) {
            pendingEvents.add(new Gson().fromJson(serializedPendingEvent, Event.class));
        }

        if( analyticsThread == null ) {
            startAnalyticsThread(context);
        }
    }

    private static void startAnalyticsThread(Context context) {
        long lastSendTime = preferences.getLong(LAST_SEND_TIME_KEY, 0);
        boolean firstLaunch = preferences.getBoolean(FIRST_LAUNCH_KEY, false);
        analyticsThread = new AnalyticsThread("Kamcord Analytics", lastSendTime, firstLaunch);

        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(analyticsThread);
        analyticsThread.start();
        analyticsThread.setHandler(new Handler(analyticsThread.getLooper(), analyticsThread));
    }

}
