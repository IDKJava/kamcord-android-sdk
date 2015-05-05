package com.kamcord.app.kamcord.activity.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.model.GameModel;
import com.kamcord.app.kamcord.activity.utils.RecordHandlerThread;

public class RecordingService extends Service
{
    private static final String TAG = RecordingService.class.getSimpleName();
    private static int NOTIFICATION_ID = 3141592;

    private final IBinder mBinder = new LocalBinder();

    private RecordHandlerThread mRecordHandlerThread;
    private Handler mHandler;

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // Notification Setting
        Notification.Builder notificationBuilder = new Notification.Builder(this);
        Notification notification = notificationBuilder
                .setContentTitle(getResources().getString(R.string.kamcord))
                .setContentText(getResources().getString(R.string.idle))
                .setSmallIcon(R.drawable.kamcord_appicon)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // If we're getting destroyed, we should probably just stop the current recording session.
        stopRecording();

        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
            stopSelf();
        }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    /* Interface for starting and stopping a recording session */
    public synchronized void startRecording(MediaProjection mediaProjection, GameModel gameModel)
    {
        if( mRecordHandlerThread == null || !mRecordHandlerThread.isAlive() )
        {
            mRecordHandlerThread = new RecordHandlerThread(mediaProjection, gameModel, getApplicationContext());
            mRecordHandlerThread.start();

            mHandler = new Handler(mRecordHandlerThread.getLooper(), mRecordHandlerThread);
            mRecordHandlerThread.setHandler(mHandler);

            mHandler.sendEmptyMessage(RecordHandlerThread.Message.RECORD_CLIP);
        }
        else
        {
            Log.e(TAG, "Unable to start recording session! There is already a currently running recording session.");
        }
    }

    public synchronized void stopRecording()
    {
        if( mRecordHandlerThread != null && mRecordHandlerThread.isAlive() )
        {
            mHandler.sendEmptyMessage(RecordHandlerThread.Message.STOP_RECORDING);
            mRecordHandlerThread.quitSafely();
                }
        else
        {
            Log.e(TAG, "Unable to stop recording session! There is no currently running recording session.");
            }
        }

    public class LocalBinder extends Binder
    {
        public RecordingService getService()
        {
            return RecordingService.this;
    }
}
}


