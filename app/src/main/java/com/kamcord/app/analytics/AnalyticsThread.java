package com.kamcord.app.analytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.kamcord.app.server.model.analytics.Event;

import java.util.HashMap;

/**
 * Created by pplunkett on 6/15/15.
 */
public class AnalyticsThread extends HandlerThread implements Handler.Callback, Application.ActivityLifecycleCallbacks {
    private static final int SEND_EVERY_MS = 300000;

    private long lastSendTime;
    private Handler handler;

    public AnalyticsThread(String name, long lastSendTime) {
        super(name);
        this.lastSendTime = lastSendTime;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean handleMessage(Message message) {

        What what = What.UNKNOWN;
        try {
            what = What.values()[message.what];
        } catch( IndexOutOfBoundsException e ) {}

        Log.v("FindMe", "say what again: " + what);
        Log.v("FindMe", "  with obj: " + message.obj);

        switch( what ) {
            case ACTIVITY_STARTED:
                break;

            case ACTIVITY_STOPPED:

                break;

            case UNKNOWN:

                break;
        }

        if( shouldSendEvents() ) {
            sendEvents();
        }

        return false;
    }

    private boolean shouldSendEvents() {
        long now = System.currentTimeMillis();
        if( now - lastSendTime > SEND_EVERY_MS ) {

        }
        return false;
    }

    private void sendEvents() {

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
        Message msg = Message.obtain(handler, What.ACTIVITY_STARTED.ordinal(), activity.getComponentName().toString());
        handler.sendMessage(msg);
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Message msg = Message.obtain(handler, What.ACTIVITY_STOPPED.ordinal(), activity.getComponentName().toString());
        handler.sendMessage(msg);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
