package com.kamcord.app.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.thread.Uploader;

public class UploadService extends IntentService {
    private static final String TAG = RecordingService.class.getSimpleName();
    public static final String ARG_SESSION_TO_SHARE = "session_to_share";
    private static int NOTIFICATION_ID = 271828;

    private static volatile boolean sIsRunning = false;
    public static boolean isRunning()
    {
        return sIsRunning;
    }

    private IBinder mBinder = new LocalBinder();
    private RecordingSession currentlyUploadingSession = null;

    public UploadService() {
        super("Kamcord Upload Service");
        this.setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        currentlyUploadingSession = intent.getParcelableExtra(ARG_SESSION_TO_SHARE);
        sIsRunning = true;

        Notification.Builder notificationBuilder = new Notification.Builder(this);
        Notification notification = notificationBuilder
                .setContentTitle(getResources().getString(R.string.toolbarTitle))
                .setContentText(getResources().getString(R.string.uploading))
                .setSmallIcon(R.drawable.app_icon)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        Uploader uploader = new Uploader(currentlyUploadingSession, getApplicationContext());
        uploader.start();
        try {
            uploader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopForeground(true);
        sIsRunning = false;
        currentlyUploadingSession = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
}

    public RecordingSession getCurrentlyUploadingSession()
    {
        return currentlyUploadingSession;
    }

    public class LocalBinder extends Binder {
        public UploadService getService() {
            return UploadService.this;
        }
    }
}
