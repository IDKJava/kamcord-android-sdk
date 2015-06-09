package com.kamcord.app.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.kamcord.app.R;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.thread.AudioRecordThread;
import com.kamcord.app.thread.RecordHandlerThread;
import com.kamcord.app.utils.NotificationUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.CyclicBarrier;

public class RecordingService extends Service {
    private static final String TAG = RecordingService.class.getSimpleName();
    private static int NOTIFICATION_ID = 3141592;

    private static WeakReference<RecordingService> sInstanceRef = null;
    private static MediaProjection sMediaProjection = null;
    private static RecordingSession sRecordingSession = null;

    public static final long DROP_FIRST_MS = 3000;

    private RecordHandlerThread mRecordHandlerThread;
    private AudioRecordThread mAudioRecordThread;
    private Handler mHandler;
    private Handler mAudioRecordHandler;
    private RecordingSession recordingSession;

    public RecordingService() {
        super();
    }

    public static boolean isRunning() {
        return sInstanceRef != null && sInstanceRef.get() != null;
    }

    public static RecordingService getInstance() {
        return sInstanceRef != null ? sInstanceRef.get() : null;
    }

    public static void initializeForRecording(MediaProjection mediaProjection, RecordingSession recordingSession) {
        sMediaProjection = mediaProjection;
        sRecordingSession = recordingSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstanceRef = new WeakReference<>(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( sMediaProjection != null && sRecordingSession != null ) {
            // Notification Setting
            NotificationUtils.initializeWith(this.getApplicationContext());
            startForeground(NOTIFICATION_ID, NotificationUtils.getNotification());
            startRecording(sMediaProjection, sRecordingSession);
            sMediaProjection = null;
            sRecordingSession = null;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstanceRef.clear();
        // If we're getting destroyed, we should probably just stop the current recording session.
        stopRecording();
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* Interface for starting and stopping a recording session */
    public synchronized void startRecording(MediaProjection mediaProjection, RecordingSession recordingSession) {
        if (mRecordHandlerThread == null || !mRecordHandlerThread.isAlive()) {

            NotificationUtils.updateNotification(getResources().getString(R.string.recording));

            this.recordingSession = recordingSession;

            CyclicBarrier clipStartBarrier = new CyclicBarrier(2);

            mRecordHandlerThread = new RecordHandlerThread(mediaProjection, getApplicationContext(), recordingSession, clipStartBarrier);
            mRecordHandlerThread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            mRecordHandlerThread.start();
            mHandler = new Handler(mRecordHandlerThread.getLooper(), mRecordHandlerThread);
            mRecordHandlerThread.setHandler(mHandler);
            mHandler.sendEmptyMessage(RecordHandlerThread.Message.POLL);

            mAudioRecordThread = new AudioRecordThread(getApplicationContext(), recordingSession, clipStartBarrier);
            mAudioRecordThread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            mAudioRecordThread.start();
            mAudioRecordHandler = new Handler(mAudioRecordThread.getLooper(), mAudioRecordThread);
            mAudioRecordThread.setHandler(mAudioRecordHandler);
            mAudioRecordHandler.sendEmptyMessage(AudioRecordThread.Message.POLL);

        } else {
            Log.e(TAG, "Unable to start recording session! There is already a currently running recording session.");
        }
    }

    public synchronized void stopRecording() {
        if (mRecordHandlerThread != null && mRecordHandlerThread.isAlive()) {
            mHandler.sendEmptyMessage(RecordHandlerThread.Message.STOP_RECORDING);
            mRecordHandlerThread.quitSafely();
            mAudioRecordHandler.sendEmptyMessage(AudioRecordThread.Message.STOP_RECORDING);
            mAudioRecordThread.quitSafely();
        } else {
            Log.e(TAG, "Unable to stop recording session! There is no currently running recording session.");
        }
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        stopForeground(true);
    }

    public RecordingSession getRecordingSession()
    {
        return recordingSession;
    }

    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            try {
                stopForeground(true);
            } catch( Exception e ) {
            }
        }
    };
}


