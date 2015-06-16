package com.kamcord.app.analytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.kamcord.app.server.model.analytics.Event;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Created by pplunkett on 6/15/15.
 */
public class AnalyticsThread extends HandlerThread implements Handler.Callback, Application.ActivityLifecycleCallbacks {
    private static final int SEND_EVERY_MS = 300000;
    private static final String WHEN_KEY = "when";

    private Handler handler;

    private String appSessionId = null;
    private int foregroundActivityCount = 0;
    Map<Object, Event> pendingEvents = new WeakHashMap<>();
    private boolean sendingEvents = false;

    public AnalyticsThread(String name) {
        super(name);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean handleMessage(Message message) {

        Object who = message.obj;

        What what = What.UNKNOWN;
        try {
            what = What.values()[message.what];
        } catch( IndexOutOfBoundsException e ) {}

        Bundle data = message.getData();
        long whenMs = 0;
        if( data != null ) {
            whenMs = data.getLong(WHEN_KEY, 0);
        }

        if( appSessionId == null ) {
            appSessionId = UUID.randomUUID().toString();
        }

        boolean appForegrounded = false;
        boolean appBackgrounded = false;
        switch( what ) {
            case ACTIVITY_STARTED:
                if( foregroundActivityCount == 0 ) {
                    appForegrounded = true;
                    pendingEvents.put(who, new Event(Event.Name.KAMCORD_APP_LAUNCH, whenMs, appSessionId));
                }
                foregroundActivityCount++;
                break;

            case ACTIVITY_STOPPED:
                if( foregroundActivityCount == 1 ) {
                    appBackgrounded = true;
                    if( pendingEvents.containsKey(who) ) {
                        Event launchEvent = pendingEvents.get(who);
                        launchEvent.setDurationFromStopTime(whenMs);
                        KamcordAnalytics.addUnsentEvent(launchEvent);
                    } else {
                        Log.w(KamcordAnalytics.TAG, "No start session corresponding to Object " + who + "!");
                    }
                }
                foregroundActivityCount--;
                break;

            case UNKNOWN:
            default:
                break;
        }

        if( appForegrounded && KamcordAnalytics.isFirstLaunch() ) {
            KamcordAnalytics.writeFirstLaunch();
            KamcordAnalytics.addUnsentEvent(new Event(Event.Name.FIRST_APP_LAUNCH, whenMs, appSessionId));
        }

        if( (System.currentTimeMillis() - KamcordAnalytics.getLastSendTime() > SEND_EVERY_MS
                || appForegrounded || appBackgrounded) && !sendingEvents ) {
            sendEvents();
        }

        if( appBackgrounded ) {
            appSessionId = null;
        }

        return false;
    }

    private void sendEvents() {

    }

    private Message newMessage(Object who, What what) {
        Message msg = Message.obtain(handler, what.ordinal(), who);
        Bundle data = new Bundle();
        data.putLong(WHEN_KEY, System.currentTimeMillis());
        msg.setData(data);
        return msg;
    }

    private enum What {
        ACTIVITY_STOPPED,
        ACTIVITY_STARTED,
        UNKNOWN,
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        handler.sendMessage(newMessage(activity, What.ACTIVITY_STARTED));
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        handler.sendMessage(newMessage(activity, What.ACTIVITY_STOPPED));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
