package com.kamcord.app.analytics;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.kamcord.app.server.model.analytics.Event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by pplunkett on 6/15/15.
 */
public class KamcordAnalytics {
    static final String TAG = KamcordAnalytics.class.getSimpleName();
    public static final String SUCCESS_KEY = "success";
    public static final String VIDEO_ID_KEY = "video_id";
    public static final String FAILURE_REASON_KEY = "failure_reason";
    public static final String WAS_REPLAYED_KEY = "was_replayed";
    public static final String EXTERNAL_NETWORK_KEY = "external_network";
    public static final String APP_SESSION_ID_KEY = "app_session_id";
    public static final String IS_UPLOAD_RETRY_KEY = "is_upload_retry";
    public static final String GAME_ID_KEY = "game_id";
    public static final String VIDEO_LENGTH_KEY = "video_length";


    private static final String ANALYTICS_PREFS = "KAMCORD_ANALYTICS_PREFS";

    private static final String LAST_SEND_TIME_KEY = "LAST_SEND_TIME";
    private static final String FAILED_ATTEMPTS_IN_ROW_KEY = "FAILED_ATTEMPTS_IN_ROW";
    private static final String FIRST_LAUNCH_KEY = "FIRST_KAMCORD_APP_LAUNCH";
    private static final String UNSENT_EVENTS = "UNSENT_EVENTS";
    private static final int MAX_UNSENT_EVENT_COUNT = 1000;

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

    public static void startSession(Object who, Event.Name name) {
        analyticsThread.sendStartSession(who, name);
    }
    public static void endSession(Object who, Event.Name name) {
        endSession(who, name, null);
    }

    public static void endSession(Object who, Event.Name name, Bundle extras) {
        analyticsThread.sendEndSession(who, name, extras);
    }

    public static void fireEvent(Event.Name name) {
        fireEvent(name, null);
    }

    public static void fireEvent(Event.Name name, Bundle extras) {
        analyticsThread.sendFireEvent(name, extras);
    }

    public static String getCurrentAppSessionId() {
        if( analyticsThread != null ) {
            return analyticsThread.getCurrentAppSessionId();
        }
        return null;
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

    static void setFailedAttemptsInRow(int failedAttemptsInRow) {
        preferences.edit().putInt(FAILED_ATTEMPTS_IN_ROW_KEY, failedAttemptsInRow).commit();
    }
    static int getFailedAttemptsInRow() {
        return preferences.getInt(FAILED_ATTEMPTS_IN_ROW_KEY, 0);
    }


    static void addUnsentEvent(Event event) {
        event.convertTimes();
        if( unsentEventCount() > MAX_UNSENT_EVENT_COUNT ) {
            Iterator<Event> iterator = unsentEvents.iterator();
            Event minStartTimeEvent = null;
            while( iterator.hasNext() ) {
                Event e = iterator.next();
                if( minStartTimeEvent == null || e.start_time < minStartTimeEvent.start_time ) {
                    minStartTimeEvent = e;
                }
            }
            unsentEvents.remove(minStartTimeEvent);
        }
        boolean added = unsentEvents.add(event);
        if( added ) {
            saveEventSet(UNSENT_EVENTS, unsentEvents);
        }
    }
    static Set<Event> getUnsentEvents() {
        return unsentEvents;
    }
    static void clearSentEvents(Set<Event> sentEvents) {
        for( Event sentEvent : sentEvents ) {
            unsentEvents.remove(sentEvent);
        }
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
