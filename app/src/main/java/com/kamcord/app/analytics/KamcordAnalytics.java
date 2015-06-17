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
    static final String TAG = KamcordAnalytics.class.getSimpleName();
    private static final String ANALYTICS_PREFS = "KAMCORD_ANALYTICS_PREFS";

    private static final String LAST_SEND_TIME_KEY = "LAST_SEND_TIME";
    private static final String FIRST_LAUNCH_KEY = "FIRST_KAMCORD_APP_LAUNCH";
    private static final String UNSENT_EVENTS = "UNSENT_EVENTS";

    private static SharedPreferences preferences = null;
    private static AnalyticsThread analyticsThread;

    private static HashSet<Event> unsentEvents = new HashSet<>();

    public static void initializeWith(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(ANALYTICS_PREFS, Context.MODE_PRIVATE);

        loadEventSet(UNSENT_EVENTS, unsentEvents);

        if( analyticsThread == null ) {
            startAnalyticsThread(context);
        }
    }

    private static void startAnalyticsThread(Context context) {
        analyticsThread = new AnalyticsThread("Kamcord Analytics");

        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(analyticsThread);
        analyticsThread.start();
        analyticsThread.setHandler(new Handler(analyticsThread.getLooper(), analyticsThread));
    }

    static void writeFirstLaunch() {
        preferences.edit().putBoolean(FIRST_LAUNCH_KEY, false).commit();
    }
    static boolean isFirstLaunch() {
        return preferences.getBoolean(FIRST_LAUNCH_KEY, true);
    }

    static void setLastSendTime(long lastSendTime) {
        preferences.edit().putLong(LAST_SEND_TIME_KEY, lastSendTime).commit();
    }
    static long getLastSendTime() {
        return preferences.getLong(LAST_SEND_TIME_KEY, 0);
    }

    static void addUnsentEvent(Event event) {
        event.setTimes();
        boolean added = unsentEvents.add(event);
        if( added ) {
            saveEventSet(UNSENT_EVENTS, unsentEvents);
        }
    }
    static Set<Event> getUnsentEvents() {
        return unsentEvents;
    }
    static void clearUnsentEvents() {
        unsentEvents.clear();
        saveEventSet(UNSENT_EVENTS, unsentEvents);
    }
    static int unsentEventCount() {
        return unsentEvents.size();
    }

    private static void saveEventSet(String key, Set<Event> eventSet) {
        Set<String> serializedEventSet = new HashSet<>();
        for( Event event : eventSet ) {
            serializedEventSet.add(new Gson().toJson(event));
        }
        preferences.edit().putStringSet(key, serializedEventSet).commit();
    }

    private static void loadEventSet(String key, Set<Event> eventSet) {
        Set<String> serializedEventSet = preferences.getStringSet(key, new HashSet<String>());
        eventSet.clear();
        for( String serializedEvent : serializedEventSet ) {
            eventSet.add(new Gson().fromJson(serializedEvent, Event.class));
        }
    }
}
