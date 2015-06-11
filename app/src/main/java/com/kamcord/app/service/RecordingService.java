package com.kamcord.app.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.thread.AudioRecordThread;
import com.kamcord.app.thread.RecordHandlerThread;
import com.kamcord.app.utils.NotificationUtils;

import java.util.concurrent.CyclicBarrier;

public class RecordingService extends Service {
    private static final String TAG = RecordingService.class.getSimpleName();
    private static int NOTIFICATION_ID = 3141592;

    private final IBinder mBinder = new InstanceBinder();
    private MediaProjection mMediaProjection = null;
    private RecordingSession mRecordingSession = null;

    public static final long DROP_FIRST_MS = 3000;

    private RecordHandlerThread mRecordHandlerThread;
    private AudioRecordThread mAudioRecordThread;
    private Handler mHandler;
    private Handler mAudioRecordHandler;

    public RecordingService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( mMediaProjection != null && mRecordingSession != null ) {
            // Notification Setting
            NotificationUtils.initializeWith(this.getApplicationContext());
            startForeground(NOTIFICATION_ID, NotificationUtils.getNotification());
            startRecordingThreads();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // If we're getting destroyed, we should probably just stop the current recording session.
        stopRecording();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* Interface for starting and stopping a recording session */
    public synchronized void startRecording(MediaProjection mediaProjection, RecordingSession recordingSession) {
        if (mRecordHandlerThread == null || !mRecordHandlerThread.isAlive()) {

            mMediaProjection = mediaProjection;
            mRecordingSession = recordingSession;
            this.startService(new Intent(this, RecordingService.class));

        }
    }

    private void startRecordingThreads() {
        if( mRecordHandlerThread == null || !mRecordHandlerThread.isAlive()) {

            CyclicBarrier clipStartBarrier = new CyclicBarrier(2);

            mRecordHandlerThread = new RecordHandlerThread(mMediaProjection, getApplicationContext(), mRecordingSession, clipStartBarrier);
            mRecordHandlerThread.start();
            mHandler = new Handler(mRecordHandlerThread.getLooper(), mRecordHandlerThread);
            mRecordHandlerThread.setHandler(mHandler);
            mHandler.sendEmptyMessage(RecordHandlerThread.Message.POLL);
            mMediaProjection = null;

            mAudioRecordThread = new AudioRecordThread(getApplicationContext(), mRecordingSession, clipStartBarrier);
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
        stopSelf();
    }

    public synchronized boolean isRecording() {
        return (mRecordHandlerThread != null && mRecordHandlerThread.isAlive())
                || (mAudioRecordThread != null && mAudioRecordThread.isAlive());
    }

    public RecordingSession getRecordingSession()
    {
        return mRecordingSession;
    }

    public class InstanceBinder extends Binder {
        public RecordingService getInstance() {
            return RecordingService.this;
        }
    }
}


