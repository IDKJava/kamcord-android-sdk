package com.kamcord.app.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.kamcord.app.R;
import com.kamcord.app.activity.VideoViewActivity;


public class NotifGcmListenerService extends GcmListenerService {

    private static int NOTIFICATION_ID = 3141592;
    private static Notification notification;
    private static Notification.Builder notificationBuilder;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = "";
        if (data != null) {
            Log.d("Data Title: ", data.getString("title"));
            message = data.getString("streamUserId");
            Log.d("Message sender:", "From: " + from);
            Log.d("Message content:", "Message: " + message);
        } else {
            Log.d("data", "is null");
        }

        sendNotification("LiveStreaming", message);
    }

    public void sendNotification(String liveStreamer, String message) {
        notificationBuilder = new Notification.Builder(this)
                .setContentTitle(liveStreamer)
                .setContentText(message)
                .setSmallIcon(R.drawable.notif_logo_small)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true);

        Intent resultIntent = new Intent(this, VideoViewActivity.class);
        resultIntent.setData(Uri.parse("http://content.kamcord.com/live/377125/playlist.m3u8"));
        resultIntent.putExtra(VideoViewActivity.ARG_STREAM,
                "hi");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(VideoViewActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
