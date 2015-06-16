package com.kamcord.app.analytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * Created by pplunkett on 6/15/15.
 */
public class AnalyticsThread extends HandlerThread implements Handler.Callback, Application.ActivityLifecycleCallbacks {
    private static final int SEND_EVERY_MS = 300000;

    private Handler handler;
    private long lastSendTime;
    private boolean firstLaunch = false;

    private int foregroundActivityCount = 0;

    public AnalyticsThread(String name, long lastSendTime, boolean firstLaunch) {
        super(name);
        this.lastSendTime = lastSendTime;
        this.firstLaunch = firstLaunch;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void sendLaunchEvent() {
        Message msg = Message.obtain(handler, What.LAUNCH.ordinal());
        handler.sendMessage(msg);
    }

    @Override
    public boolean handleMessage(Message message) {

        What what = What.UNKNOWN;
        try {
            what = What.values()[message.what];
        } catch( IndexOutOfBoundsException e ) {}

        Log.v("FindMe", "say what again: " + what);
        Log.v("FindMe", "  with obj: " + message.obj);

        boolean backgroundedOrForegrounded = false;
        switch( what ) {
            case ACTIVITY_STARTED:
                if( foregroundActivityCount == 0 ) {
                    backgroundedOrForegrounded = true;
                }
                foregroundActivityCount++;
                break;

            case ACTIVITY_STOPPED:
                if( foregroundActivityCount == 1 ) {
                    backgroundedOrForegrounded = true;
                }
                foregroundActivityCount--;
                break;

            case LAUNCH:

                break;

            case UNKNOWN:
            default:
                break;
        }

        if( System.currentTimeMillis() - lastSendTime > SEND_EVERY_MS || backgroundedOrForegrounded ) {
            sendEvents();
        }

        return false;
    }

    private void sendEvents() {

    }

    private enum What {
        ACTIVITY_STOPPED,
        ACTIVITY_STARTED,
        LAUNCH,
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
