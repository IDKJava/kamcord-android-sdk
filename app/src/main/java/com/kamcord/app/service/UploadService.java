package com.kamcord.app.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;

import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.thread.Uploader;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UploadService extends IntentService {
    private static final String TAG = RecordingService.class.getSimpleName();
    public static final String ARG_SESSION_TO_SHARE = "session_to_share";
    private static int NOTIFICATION_ID = 271828;

    private RecordingSession currentlyUploadingSession = null;
    private Queue<RecordingSession> queuedSessions = new ConcurrentLinkedQueue<>();
    private static UploadService sInstance = null;

    public UploadService() {
        super("Kamcord Upload Service");
        this.setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int out = super.onStartCommand(intent, flags, startId);
        RecordingSession sessionToQueue = intent.getParcelableExtra(ARG_SESSION_TO_SHARE);
        queuedSessions.add(sessionToQueue);
        return out;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        currentlyUploadingSession = intent.getParcelableExtra(ARG_SESSION_TO_SHARE);

        RecordingSession nextSession = queuedSessions.poll();
        if( !nextSession.getUUID().equals(currentlyUploadingSession.getUUID()) ) {
            Log.w(TAG, "Inconsistency in the upload queue...");
        }

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
        currentlyUploadingSession = null;
    }

    public static UploadService getInstance() {
        return sInstance;
    }

    public RecordingSession getCurrentlyUploadingSession()
    {
        return currentlyUploadingSession;
    }

    public Queue<RecordingSession> getQueuedSessions() {
        return queuedSessions;
    }
}
