package com.kamcord.app.service;

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

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.kamcord.app.R;
import com.kamcord.app.server.model.Game;
import com.kamcord.app.utils.AudioRecordThread;
import com.kamcord.app.utils.FileManagement;
import com.kamcord.app.utils.RecordHandlerThread;
import com.kamcord.app.utils.StitchClipsThread;

public class RecordingService extends Service {
    private static final String TAG = RecordingService.class.getSimpleName();
    private static int NOTIFICATION_ID = 3141592;
    private static volatile boolean mIsRunning = false;

    private final IBinder mBinder = new LocalBinder();

    private RecordHandlerThread mRecordHandlerThread;
    private AudioRecordThread mAudioRecordThread;
    private Handler mHandler;
    private Handler mAudioRecordHandler;
    private ExecuteBinaryResponseHandler executeBinaryResponseHandler;
    private StitchSuccessListener stitchSuccessListener;
    private String videoFolderPath;

    public RecordingService() {
        super();
    }

    public static boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIsRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Notification Setting
        Notification.Builder notificationBuilder = new Notification.Builder(this);
        Notification notification = notificationBuilder
                .setContentTitle(getResources().getString(R.string.toolbarTitle))
                .setContentText(getResources().getString(R.string.idle))
                .setSmallIcon(R.drawable.kamcord_app_icon)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // If we're getting destroyed, we should probably just stop the current recording session.
        stopRecording();
        mIsRunning = false;

        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* Interface for starting and stopping a recording session */
    public synchronized void startRecording(MediaProjection mediaProjection, Game gameModel) {
        if (mRecordHandlerThread == null || !mRecordHandlerThread.isAlive()) {
            FileManagement fileManagement = new FileManagement();
            fileManagement.rootFolderInitialize();
            fileManagement.gameFolderInitialize(gameModel.play_store_id);
            fileManagement.sessionFolderInitialize();
            videoFolderPath = fileManagement.getGamePath();

            mRecordHandlerThread = new RecordHandlerThread(mediaProjection, gameModel, getApplicationContext(), fileManagement);
            mRecordHandlerThread.start();

            mHandler = new Handler(mRecordHandlerThread.getLooper(), mRecordHandlerThread);
            mRecordHandlerThread.setHandler(mHandler);
            mHandler.sendEmptyMessage(RecordHandlerThread.Message.POLL);


            mAudioRecordThread = new AudioRecordThread(gameModel, getApplicationContext(), fileManagement);
            mAudioRecordThread.start();
            mAudioRecordHandler = new Handler(mAudioRecordThread.getLooper(), mAudioRecordThread);
            mAudioRecordThread.setHandler(mAudioRecordHandler);

            mAudioRecordHandler.sendEmptyMessage(AudioRecordThread.Message.POLL);

            Notification.Builder notificationBuilder = new Notification.Builder(this);
            Notification notification = notificationBuilder
                    .setContentTitle(getResources().getString(R.string.toolbarTitle))
                    .setContentText(getResources().getString(R.string.recording))
                    .setSmallIcon(R.drawable.kamcord_app_icon)
                    .build();
            ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);

            executeBinaryResponseHandler = new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {
                    Log.d("progress:", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d("FFmpeg execute:", message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d("FFmpeg execute:", message);
                }
                @Override
                public void onFinish() {
                    if(stitchSuccessListener != null) {
                        stitchSuccessListener.getVideoFolderPath(videoFolderPath);
                    }
                }
            };

        } else {
            Log.e(TAG, "Unable to start recording session! There is already a currently running recording session.");
        }
    }

    public interface StitchSuccessListener {
        void getVideoFolderPath(String videoPath);
        void failureStitch();
    }

    public void setStitchSuccessListener(StitchSuccessListener stitchSuccessListener) {
        this.stitchSuccessListener = stitchSuccessListener;
    }

    public synchronized void stopRecording() {
        if (mRecordHandlerThread != null && mRecordHandlerThread.isAlive()) {
            mHandler.sendEmptyMessage(RecordHandlerThread.Message.STOP_RECORDING);
            mRecordHandlerThread.quitSafely();
            mAudioRecordHandler.sendEmptyMessage(AudioRecordThread.Message.STOP_RECORDING);
            mAudioRecordThread.quitSafely();
            StitchClipsThread stitchClipsThread = new StitchClipsThread("/sdcard/Kamcord_Android/" + mRecordHandlerThread.getSessionFolderName(),
                    getApplicationContext(),
                    executeBinaryResponseHandler );
            stitchClipsThread.start();
            ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        } else {
            Log.e(TAG, "Unable to stop recording session! There is no currently running recording session.");
        }
    }

    public class LocalBinder extends Binder {
        public RecordingService getService() {
            return RecordingService.this;
        }
    }
}


