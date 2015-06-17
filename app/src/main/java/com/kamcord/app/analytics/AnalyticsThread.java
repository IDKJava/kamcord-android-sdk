package com.kamcord.app.analytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.kamcord.app.server.client.EventTrackerClient;
import com.kamcord.app.server.model.Account;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.server.model.analytics.TrackEventEntity;
import com.kamcord.app.server.model.analytics.WrappedResponse;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.DeviceManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pplunkett on 6/15/15.
 */
public class AnalyticsThread extends HandlerThread implements
        Handler.Callback,
        Application.ActivityLifecycleCallbacks {
    private static final int SEND_EVERY_MS = 300000;
    private static final int MAX_UNSENT_EVENTS = 100;
    private static final String WHEN_KEY = "when";

    private Handler handler;

    private String appSessionId = null;
    private int foregroundActivityCount = 0;
    private Object foregroundMarker = null;
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
        } catch (IndexOutOfBoundsException e) {
        }

        if( what == What.RESTORE_AFTER_FAILED_SEND ) {
            if( who instanceof Set ) {
                for( Object o : (Set) who ) {
                    if( o instanceof Event ) {
                        KamcordAnalytics.addUnsentEvent((Event) o);
                    }
                }
            }
            return false;
        }

        Bundle data = message.getData();
        long whenMs = 0;
        if (data != null) {
            whenMs = data.getLong(WHEN_KEY, 0);
        }

        if (appSessionId == null) {
            appSessionId = UUID.randomUUID().toString();
        }

        boolean appForegrounded = false;
        boolean appBackgrounded = false;
        switch (what) {
            case ACTIVITY_STARTED:
                if (foregroundActivityCount == 0) {
                    appForegrounded = true;
                    foregroundMarker = new Object();
                    pendingEvents.put(foregroundMarker, new Event(Event.Name.KAMCORD_APP_LAUNCH, whenMs, appSessionId));
                }
                foregroundActivityCount++;
                break;

            case ACTIVITY_STOPPED:
                if (foregroundActivityCount == 1) {
                    appBackgrounded = true;
                    if (pendingEvents.containsKey(foregroundMarker)) {
                        Event launchEvent = pendingEvents.remove(foregroundMarker);
                        foregroundMarker = null;
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

        if (appForegrounded && KamcordAnalytics.isFirstLaunch()) {
            KamcordAnalytics.writeFirstLaunch();
            KamcordAnalytics.addUnsentEvent(new Event(Event.Name.FIRST_KAMCORD_APP_LAUNCH, whenMs, appSessionId));
        }

        if ((System.currentTimeMillis() - KamcordAnalytics.getLastSendTime() > SEND_EVERY_MS
                || KamcordAnalytics.unsentEventCount() > MAX_UNSENT_EVENTS
                || appForegrounded || appBackgrounded)
                && !sendingEvents && KamcordAnalytics.unsentEventCount() > 0 ) {
            sendEvents();
        }

        if (appBackgrounded) {
            appSessionId = null;
        }

        return false;
    }

    private void sendEvents() {
        sendingEvents = true;
        String userRegistrationId = null;
        Account myAccount = AccountManager.getStoredAccount();
        if( myAccount != null ) {
            userRegistrationId = myAccount.id;
        }

        TrackEventEntity.Builder builder = new TrackEventEntity.Builder()
                .setAppDeviceId(DeviceManager.getDeviceToken())
                .setUserRegistrationId(userRegistrationId);
        Set<Event> unsentEvents = KamcordAnalytics.getUnsentEvents();
        for( Event event : unsentEvents ) {
            builder.addEvent(event);
        }

        TrackEventEntity entity = builder.build();
        EventTrackerClient.getInstance().trackEvent(entity, new TrackEventCallback(entity.event));
    }

    private Message newMessage(Object who, What what) {
        Message msg = Message.obtain(handler, what.ordinal(), who);
        Bundle data = new Bundle();
        long when = System.currentTimeMillis();
        data.putLong(WHEN_KEY, when);
        msg.setData(data);
        return msg;
    }

    private class TrackEventCallback implements Callback<WrappedResponse<?>> {

        private Set<Event> events;
        public TrackEventCallback(Set<Event> events) {
            this.events = events;
        }

        @Override
        public void success(WrappedResponse<?> wrappedResponse, Response response) {
            if( wrappedResponse == null || wrappedResponse.status_code != WrappedResponse.StatusCode.OK ) {
                handler.sendMessage(newMessage(events, What.RESTORE_AFTER_FAILED_SEND));
            } else {
                KamcordAnalytics.clearUnsentEvents();
                KamcordAnalytics.setLastSendTime(System.currentTimeMillis());
            }
            sendingEvents = false;
        }

        @Override
        public void failure(RetrofitError error) {
            handler.sendMessage(newMessage(events, What.RESTORE_AFTER_FAILED_SEND));
            sendingEvents = false;
        }
    }

    private enum What {
        ACTIVITY_STOPPED,
        ACTIVITY_STARTED,
        RESTORE_AFTER_FAILED_SEND,
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
