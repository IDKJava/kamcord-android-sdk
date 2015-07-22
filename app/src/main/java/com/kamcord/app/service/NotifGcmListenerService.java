package com.kamcord.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.activity.RecordActivity;
import com.kamcord.app.activity.VideoViewActivity;
import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.Video;
import com.kamcord.app.server.model.analytics.Event;
import com.kamcord.app.utils.AccountManager;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class NotifGcmListenerService extends GcmListenerService {

    private static int NOTIFICATION_ID = 1;
    private static NotificationCompat.Builder notificationBuilder;
    private final static String STREAM_ID = "streamId";
    private final static String VIDEO_ID = "videoId";
    private final static String NOTIF_TEXT = "text";
    private final static String SERVER_NOTIF_ID = "notificationId";
    private final static String NOTIF_CATEGORY = "category";
    private final static String NOTIF_USER_ID = "userId";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String streamID = "";
        String videoID = "";
        String serverNotifId = null;
        String notifText = "";
        String userID = "";
        if (data != null) {
            userID = data.getString(NOTIF_USER_ID);
            streamID = data.getString(STREAM_ID);
            videoID = data.getString(VIDEO_ID);
            notifText = data.getString(NOTIF_TEXT);
            serverNotifId = data.getString(SERVER_NOTIF_ID);
        }
        if (streamID != null
                && AccountManager.isLoggedIn()
                && AccountManager.getStoredAccount() != null
                && AccountManager.getStoredAccount().id.equals(userID)) {

            AppServerClient.getInstance().getStream(streamID, new NotificationStreamCallBack(notifText, serverNotifId));
        } else if (videoID != null
                && AccountManager.isLoggedIn()
                && AccountManager.getStoredAccount() != null
                && AccountManager.getStoredAccount().id.equals(userID)) {
            AppServerClient.getInstance().getVideoInfo(videoID, new NotificationVideoCallback(notifText, serverNotifId));
        }
    }

    /* Notification Specification */
    /* 1. To display each notification seperately, use different Notification ID */
    public void sendNotification(Object object, String text, String serverNotifId) {
        notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.notifTitle))
                .setContentText(text)
                .setSmallIcon(R.drawable.notif_logo_small)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.app_icon))
                .setDefaults(~Notification.DEFAULT_VIBRATE);

        Intent resultIntent = new Intent(this, VideoViewActivity.class);
        if (object instanceof Stream) {
            resultIntent.putExtra(VideoViewActivity.ARG_STREAM, new Gson().toJson(object));
        } else if (object instanceof Video) {
            resultIntent.putExtra(VideoViewActivity.ARG_VIDEO, new Gson().toJson(object));
        }
        resultIntent.putExtra(VideoViewActivity.ARG_NOTIF_ID, NOTIFICATION_ID);
        resultIntent.putExtra(KamcordAnalytics.VIEW_SOURCE_KEY, Event.ViewSource.PUSH_NOTIFICATION);
        resultIntent.putExtra(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY, serverNotifId);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RecordActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        Notification notif = notificationBuilder.build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID++, notif);
    }

    private class NotificationStreamCallBack implements Callback<GenericResponse<Stream>> {

        String notifText = "";
        String serverNotifId = null;

        NotificationStreamCallBack(String notifMessage, String serverNotifId) {
            this.notifText = notifMessage;
            this.serverNotifId = serverNotifId;
        }

        @Override
        public void success(GenericResponse<Stream> streamGenericResponse, Response response) {
            if (streamGenericResponse != null && streamGenericResponse.response != null) {
                if (streamGenericResponse.response.live) {
                    Stream stream = streamGenericResponse.response;
                    sendNotification(stream, notifText, serverNotifId);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
        }
    }

    private class NotificationVideoCallback implements Callback<GenericResponse<Video>> {

        String notifText = "";
        String serverNotifId = null;

        NotificationVideoCallback(String notifMessage, String serverNotifId) {
            this.notifText = notifMessage;
            this.serverNotifId = serverNotifId;
        }

        @Override
        public void success(GenericResponse<Video> streamGenericResponse, Response response) {
            if (streamGenericResponse != null && streamGenericResponse.response != null) {
                Video video = streamGenericResponse.response;
                sendNotification(video, notifText, serverNotifId);
            }
        }

        @Override
        public void failure(RetrofitError error) {
        }
    }
}
