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
    private static final int SEND_EVERY_MS = 0;//300000;
    private static final int MAX_UNSENT_EVENTS = 100;
    private static final int MAX_FAILED_SENDS = 4;
    private static final String WHEN_KEY = "when";
    private static final String NAME_KEY = "name";

    private static final String EXTRAS_KEY = "extras";

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

    public void sendStartSession(Object who, Event.Name name) {
        Message msg = newMessage(who, What.SESSION_STARTED, name);
        handler.sendMessage(msg);
    }

    public void sendEndSession(Object who, Event.Name name, Bundle extras) {
        Message msg = newMessage(who, What.SESSION_ENDED, name, extras);
        handler.sendMessage(msg);
    }

    public void sendFireEvent(Event.Name name, Bundle extras) {
        Message msg = newMessage(null, What.FIRE_EVENT, name, extras);
        handler.sendMessage(msg);
    }

    @Override
    public boolean handleMessage(Message message) {

        Object who = message.obj;

        What what = What.UNKNOWN;
        try {
            what = What.values()[message.what];
        } catch (IndexOutOfBoundsException e) {
        }

        if (what == What.RESTORE_AFTER_FAILED_SEND) {
            if (who instanceof Set) {
                for (Object o : (Set) who) {
                    if (o instanceof Event) {
                        KamcordAnalytics.addUnsentEvent((Event) o);
                    }
                }
            }

            // Increment the failed count, and cap at MAX_FAILED_SENDS.
            int failedSendsInARow = KamcordAnalytics.getFailedAttemptsInRow();
            failedSendsInARow = Math.min(failedSendsInARow + 1, MAX_FAILED_SENDS);
            KamcordAnalytics.setFailedAttemptsInRow(failedSendsInARow);

            // If there are n failed send attempts in a row, we make sure the next time we send is
            // at least 2^(4*(n-1) + 6) seconds in the future, by setting the last send time appropriately.
            long delayMs = (long) Math.pow(2.0, 4 * (failedSendsInARow - 1) + 6) * 1000;
            long newLastSendTime = System.currentTimeMillis() + delayMs - SEND_EVERY_MS;
            KamcordAnalytics.setLastSendTime(newLastSendTime);

            return false;
        }

        Bundle data = message.getData();
        long whenMs = 0;
        if (data != null) {
            whenMs = data.getLong(WHEN_KEY, 0);
        }

        boolean appForegrounded = false;
        boolean appBackgrounded = false;
        switch (what) {
            case ACTIVITY_STARTED: {
                if (foregroundActivityCount == 0) {
                    appForegrounded = true;
                    appSessionId = UUID.randomUUID().toString();
                    foregroundMarker = new Object();
                    pendingEvents.put(foregroundMarker, newEventFromData(data));
                }
                foregroundActivityCount++;
            }
            break;

            case ACTIVITY_STOPPED: {
                if (foregroundActivityCount == 1) {
                    appBackgrounded = true;
                    if (pendingEvents.containsKey(foregroundMarker)) {
                        Event launchEvent = pendingEvents.remove(foregroundMarker);
                        foregroundMarker = null;
                        completeEventFromData(launchEvent, data);
                        KamcordAnalytics.addUnsentEvent(launchEvent);
                    } else {
                        Log.w(KamcordAnalytics.TAG, "No start session corresponding to Object " + who + "!");
                    }
                }
                foregroundActivityCount--;
            }
            break;

            case SESSION_STARTED: {
                Event event = newEventFromData(data);
                pendingEvents.put(who, event);
            }
            break;

            case SESSION_ENDED: {
                if (pendingEvents.containsKey(who)) {
                    Event event = pendingEvents.remove(who);
                    completeEventFromData(event, data);
                    KamcordAnalytics.addUnsentEvent(event);
                } else {
                    Log.w(KamcordAnalytics.TAG, "No start session corresponding to Object " + who + "!");
                }
            }
            break;

            case FIRE_EVENT: {
                Event event = newEventFromData(data);
                completeEventFromData(event, data);
                KamcordAnalytics.addUnsentEvent(event);
            }
            break;

            case UNKNOWN:
            default:
                break;
        }

        if (appForegrounded && KamcordAnalytics.isFirstLaunch()) {
            KamcordAnalytics.writeFirstLaunch();
            KamcordAnalytics.addUnsentEvent(new Event(Event.Name.FIRST_KAMCORD_APP_LAUNCH, whenMs, appSessionId));
        }

        // Only send if we're foregrounding or backgrounding and we haven't sent in some time
        // OR there are a lot of unsent events and we haven't failed any sends lately.
        // AND only if we're not in the middle of sending events and there are events to actually send.
        if ( (((appForegrounded || appBackgrounded) && System.currentTimeMillis() - KamcordAnalytics.getLastSendTime() > SEND_EVERY_MS)
                || (KamcordAnalytics.unsentEventCount() > MAX_UNSENT_EVENTS && KamcordAnalytics.getFailedAttemptsInRow() == 0) )
                && !sendingEvents && KamcordAnalytics.unsentEventCount() > 0) {
            sendEvents();
        }

        if (appBackgrounded) {
            appSessionId = null;
        }

        return false;
    }

    public String getCurrentAppSessionId() {
        return appSessionId;
    }

    private void sendEvents() {
        sendingEvents = true;

        TrackEventEntity.Builder builder = new TrackEventEntity.Builder()
                .setAppDeviceId(DeviceManager.getDeviceToken());
        Set<Event> unsentEvents = KamcordAnalytics.getUnsentEvents();
        for (Event event : unsentEvents) {
            builder.addEvent(event);
        }

        TrackEventEntity entity = builder.build();
        EventTrackerClient.getInstance().trackEvent(entity, new TrackEventCallback(entity.event));
    }

    private Message newMessage(Object who, What what) {
        return newMessage(who, what, null);
    }

    private Message newMessage(Object who, What what, Event.Name name) {
        return newMessage(who, what, name, null);
    }

    private Message newMessage(Object who, What what, Event.Name name, Bundle extras) {
        Message msg = Message.obtain(handler, what.ordinal(), who);
        Bundle data = new Bundle();
        long when = System.currentTimeMillis();
        data.putLong(WHEN_KEY, when);
        if (name != null) {
            data.putString(NAME_KEY, name.name());
        }
        if (extras != null) {
            data.putBundle(EXTRAS_KEY, extras);
        }
        msg.setData(data);
        return msg;
    }

    private Event newEventFromData(Bundle data) {
        Event event = null;
        if (data.containsKey(NAME_KEY) && data.containsKey(WHEN_KEY)) {
            try {
                Event.Name name = Event.Name.valueOf(data.getString(NAME_KEY));
                long when = data.getLong(WHEN_KEY, 0);
                event = new Event(name, when, appSessionId);
            } catch (IllegalArgumentException e) {
                event = null;
            }
        }
        return event;
    }

    private void completeEventFromData(Event event, Bundle data) throws IllegalArgumentException {
        if (data.containsKey(NAME_KEY)
                && data.containsKey(WHEN_KEY)) {

            Event.Name name = Event.Name.valueOf(data.getString(NAME_KEY));
            long when = data.getLong(WHEN_KEY, 0);
            if (name != event.name) {
                throw new IllegalArgumentException("Mismatched event names when attempting to end session!");
            }
            Bundle extras = data.getBundle(EXTRAS_KEY);

            switch (name) {
                case UPLOAD_VIDEO: {
                    event.setRequestTimeFromStopTime(when);
                    if (extras != null) {
                        event.is_success = extras.getInt(KamcordAnalytics.SUCCESS_KEY, 0);
                        if (extras.containsKey(KamcordAnalytics.FAILURE_REASON_KEY)) {
                            event.failure_reason = Event.UploadFailureReason.valueOf(extras.getString(KamcordAnalytics.FAILURE_REASON_KEY));
                        }
                        event.video_global_id = extras.getString(KamcordAnalytics.VIDEO_ID_KEY, null);
                        event.was_replayed = extras.getInt(KamcordAnalytics.WAS_REPLAYED_KEY, 0);
                        event.is_retry = extras.getInt(KamcordAnalytics.IS_UPLOAD_RETRY_KEY, 0);
                    }
                }
                break;

                case KAMCORD_APP_LAUNCH: {
                    event.setDurationFromStopTime(when);
                }
                break;

                case EXTERNAL_SHARE: {
                    if (extras != null) {
                        if( extras.containsKey(KamcordAnalytics.EXTERNAL_NETWORK_KEY) ) {
                            event.external_network = Event.ExternalNetwork.valueOf(extras.getString(KamcordAnalytics.EXTERNAL_NETWORK_KEY));
                        }
                        event.video_global_id = extras.getString(KamcordAnalytics.VIDEO_ID_KEY, null);
                    }
                }
                break;

                case RECORD_VIDEO: {
                    if( extras != null ) {
                        event.event_duration = extras.getFloat(KamcordAnalytics.VIDEO_LENGTH_KEY, 0f);
                        event.game_id = extras.getString(KamcordAnalytics.GAME_ID_KEY, null);
                    }
                }
                break;

                case REPLAY_VIDEO_VIEW: {
                    completeVideoViewEventFrom(event, when, extras);
                }
                break;

                case VIDEO_DETAIL_VIEW: {
                    completeVideoViewEventFrom(event, when, extras);
                    completeVideoDetailViewEventFrom(event, extras);
                    if (extras != null) {
                        event.video_global_id = extras.getString(KamcordAnalytics.VIDEO_ID_KEY, null);
                        event.profile_user_id = extras.getString(KamcordAnalytics.PROFILE_USER_ID_KEY, null);
                    }
                }
                break;

                case STREAM_DETAIL_VIEW: {
                    completeVideoViewEventFrom(event, when, extras);
                    completeVideoDetailViewEventFrom(event, extras);
                    if (extras != null) {
                        event.stream_user_id = extras.getString(KamcordAnalytics.STREAM_USER_ID_KEY, null);
                        event.is_live = extras.getInt(KamcordAnalytics.IS_LIVE_KEY, 0);
                    }
                }
                break;

                case FOLLOW_USER: {
                    event.setRequestTimeFromStopTime(when);
                }
                break;

                case PROFILE_CREATION: {
                    event.setRequestTimeFromStopTime(when);
                }
            }

            // If they put the app_session_id in themselves, we assume that they know what they're doing.
            if( extras != null && extras.containsKey(KamcordAnalytics.APP_SESSION_ID_KEY) ) {
                event.app_session_id = extras.getString(KamcordAnalytics.APP_SESSION_ID_KEY);
            }
            Account myAccount = AccountManager.getStoredAccount();
            if( myAccount != null ) {
                event.user_registration_id = myAccount.id;
            }
        }
    }

    private void completeVideoViewEventFrom(Event event, long when, Bundle extras) {
        event.setDurationFromStopTime(when);
        if( extras != null ) {
            event.num_play_starts = extras.getInt(KamcordAnalytics.NUM_PLAY_STARTS_KEY, 1);
            event.num_replays = extras.getInt(KamcordAnalytics.NUM_PLAY_STARTS_KEY, 1);
            event.buffering_duration = extras.getFloat(KamcordAnalytics.BUFFERING_DURATION_KEY, 0f);
            event.video_length_watched = extras.getFloat(KamcordAnalytics.VIDEO_LENGTH_WATCHED_KEY, 0f);
            if( extras.containsKey(KamcordAnalytics.VIDEO_LENGTH_KEY) ) {
                event.video_length = extras.getFloat(KamcordAnalytics.VIDEO_LENGTH_KEY, 0f);
            }
        }
    }

    private void completeVideoDetailViewEventFrom(Event event, Bundle extras) {
        if (extras != null) {
            if( extras.containsKey(KamcordAnalytics.VIEW_SOURCE_KEY) ) {
                event.view_source = (Event.ViewSource) extras.getSerializable(KamcordAnalytics.VIEW_SOURCE_KEY);
            }
            if( extras.containsKey(KamcordAnalytics.VIDEO_LIST_TYPE_KEY) ) {
                event.video_list_type = (Event.ListType) extras.getSerializable(KamcordAnalytics.VIDEO_LIST_TYPE_KEY);
                if( extras.containsKey(KamcordAnalytics.VIDEO_LIST_ROW_KEY) ) {
                    event.video_list_row = extras.getInt(KamcordAnalytics.VIDEO_LIST_ROW_KEY);
                }
                if( extras.containsKey(KamcordAnalytics.VIDEO_LIST_COL_KEY) ) {
                    event.video_list_col = extras.getInt(KamcordAnalytics.VIDEO_LIST_COL_KEY);
                }
            }
            if( extras.containsKey(KamcordAnalytics.FEED_ID_KEY) ) {
                event.feed_id = extras.getString(KamcordAnalytics.FEED_ID_KEY);
            }
            if( extras.containsKey(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY) ) {
                event.notification_sent_id = extras.getString(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY);
            }
        }
    }

    private class TrackEventCallback implements Callback<WrappedResponse<?>> {

        private Set<Event> events;

        public TrackEventCallback(Set<Event> events) {
            this.events = events;
        }

        @Override
        public void success(WrappedResponse<?> wrappedResponse, Response response) {
            if (wrappedResponse == null || wrappedResponse.status_code != WrappedResponse.StatusCode.OK) {
                handler.sendMessageAtFrontOfQueue(newMessage(events, What.RESTORE_AFTER_FAILED_SEND));
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        KamcordAnalytics.setFailedAttemptsInRow(0);
                        KamcordAnalytics.clearSentEvents(events);
                        KamcordAnalytics.setLastSendTime(System.currentTimeMillis());
                    }
                });
            }
            sendingEvents = false;
        }

        @Override
        public void failure(RetrofitError error) {
            handler.sendMessageAtFrontOfQueue(newMessage(events, What.RESTORE_AFTER_FAILED_SEND));
            sendingEvents = false;
        }
    }

    private enum What {
        ACTIVITY_STOPPED,
        ACTIVITY_STARTED,
        SESSION_STARTED,
        SESSION_ENDED,
        FIRE_EVENT,
        RESTORE_AFTER_FAILED_SEND,
        UNKNOWN,
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.v("FindMe", "onActivityStarted(" + activity.getLocalClassName() + ")");
        handler.sendMessage(newMessage(activity, What.ACTIVITY_STARTED, Event.Name.KAMCORD_APP_LAUNCH));
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.v("FindMe", "onActivityStopped(" + activity.getLocalClassName() + ")");
        handler.sendMessage(newMessage(activity, What.ACTIVITY_STOPPED, Event.Name.KAMCORD_APP_LAUNCH));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
