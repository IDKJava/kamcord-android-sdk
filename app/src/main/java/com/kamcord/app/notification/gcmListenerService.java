package com.kamcord.app.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.kamcord.app.R;


public class gcmListenerService extends GcmListenerService {

    private static int NOTIFICATION_ID = 3141592;
    private static Notification notification;
    private static Notification.Builder notificationBuilder;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = "";
        if(data != null) {
            message = data.getString("kamcord");
            Log.d("Message sender:", "From: " + from);
            Log.d("Message content:", "Message: " + message);
        } else {
            Log.d("data", "is null");
        }

        // Display Message as a notification
         sendNotification("LiveStreaming", message);
    }

    public void sendNotification(String liveStreamer, String message) {
        notificationBuilder = new Notification.Builder(this);
        notification = notificationBuilder
                .setContentTitle(liveStreamer)
                .setContentText(message)
                .setSmallIcon(R.drawable.app_icon)
                .build();
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
