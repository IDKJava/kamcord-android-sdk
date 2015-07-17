package com.kamcord.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.kamcord.app.R;
import com.kamcord.app.activity.VideoViewActivity;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.Stream;
import com.kamcord.app.server.model.Video;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class NotifGcmListenerService extends GcmListenerService {

    private static int NOTIFICATION_ID = 3141592;
    private static Notification.Builder notificationBuilder;
    private final static String STREAM_ID = "streamId";
    private final static String VIDEO_ID = "videoId";
    private final static String NOTIF_TEXT = "text";
    private final static String NOTIF_TITLE = "Kamcord";
    private final static String NOTIF_CATEGORY = "category";
    private final static String NOTIF_USER_ID = "userId";
    private String streamID = "";
    private String videoID = "";
    private String notifText = "";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (data != null) {
            streamID = data.getString(STREAM_ID);
            videoID = data.getString(VIDEO_ID);
            notifText = data.getString(NOTIF_TEXT);
        }
        if (streamID != null) {
            AppServerClient.getInstance().getStream(streamID, new notificationStreamCallBack());
        } else {
            AppServerClient.getInstance().getVideoInfo(videoID, new notificationVideoCallback());
        }
    }

    public void sendNotification(Object object, String text) {
        notificationBuilder = new Notification.Builder(this)
                .setContentTitle(NOTIF_TITLE)
                .setContentText(text)
                .setSmallIcon(R.drawable.notif_logo_small)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        Intent resultIntent = new Intent(this, VideoViewActivity.class);
        if (object instanceof Stream) {
            resultIntent.putExtra(VideoViewActivity.ARG_STREAM, new Gson().toJson(object));
        } else if (object instanceof Video) {
            resultIntent.putExtra(VideoViewActivity.ARG_VIDEO, new Gson().toJson(object));
        }

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private class notificationStreamCallBack implements Callback<GenericResponse<Stream>> {
        @Override
        public void success(GenericResponse<Stream> streamGenericResponse, Response response) {
            if (streamGenericResponse != null && streamGenericResponse.response != null) {
                if (streamGenericResponse.response.live) {
                    Stream stream = streamGenericResponse.response;
                    sendNotification(stream, notifText);
                    streamID = "";
                    notifText = "";
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            streamID = "";
            notifText = "";
        }
    }

    private class notificationVideoCallback implements Callback<GenericResponse<Video>> {
        @Override
        public void success(GenericResponse<Video> streamGenericResponse, Response response) {
            if (streamGenericResponse != null && streamGenericResponse.response != null) {
                Video video = streamGenericResponse.response;
                sendNotification(video, notifText);
                videoID = "";
                notifText = "";
            }
        }

        @Override
        public void failure(RetrofitError error) {
            videoID = "";
            notifText = "";
        }
    }
}
