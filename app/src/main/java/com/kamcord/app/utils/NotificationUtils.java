package com.kamcord.app.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import com.kamcord.app.R;
import com.kamcord.app.activity.RecordActivity;

/**
 * Created by donliang1 on 5/27/15.
 */
public class NotificationUtils {

    private static int NOTIFICATION_ID = 3141592;
    private static Notification notification;
    private static Notification.Builder notificationBuilder;
    private static Context mContext;

    public synchronized static void initializeWith(Context context) {
        mContext = context;
        notificationBuilder = new Notification.Builder(context);
        notification = notificationBuilder
                .setContentTitle(context.getResources().getString(R.string.toolbarTitle))
                .setContentText(context.getResources().getString(R.string.idle))
                .setSmallIcon(R.drawable.app_icon)
                .build();

        Intent backToAppIntent = new Intent(context, RecordActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(RecordActivity.class);
        stackBuilder.addNextIntent(backToAppIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    public static Notification getNotification() {
        return notification;
    }

    public static void updateNotification(String string) {
        notificationBuilder.setContentText(string);
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

}
