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

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class NotifGcmListenerService extends GcmListenerService {

    private static int NOTIFICATION_ID = 3141592;
    private static Notification.Builder notificationBuilder;
    String streamID = "";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (data != null) {
            streamID = data.getString("streamId");
        }
        AppServerClient.getInstance().getStream(streamID, new notificationVideoCallBack());
    }


    public void sendNotification(Stream stream) {
        notificationBuilder = new Notification.Builder(this)
                .setContentTitle("Kamcord")
                .setContentText("Tap to view livestream!")
                .setSmallIcon(R.drawable.notif_logo_small)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        Intent resultIntent = new Intent(this, VideoViewActivity.class);
        resultIntent.putExtra(VideoViewActivity.ARG_STREAM, new Gson().toJson(stream));
        resultIntent.putExtra(VideoViewActivity.STREAM_ID, streamID);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private class notificationVideoCallBack implements Callback<GenericResponse<Stream>> {
        @Override
        public void success(GenericResponse<Stream> streamGenericResponse, Response response) {
            if (streamGenericResponse != null && streamGenericResponse.response != null) {
                if (streamGenericResponse.response.live) {
                    Stream stream = streamGenericResponse.response;
                    sendNotification(stream);
                    streamID = "";
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
        }
    }
}
