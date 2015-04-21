package com.kamcord.app.kamcord.activity.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.ScreenRecorder;

/**
 * Created by donliang1 on 4/20/15.
 */
@TargetApi(21)
public class RecordingService extends Service {

    private static final String LOG_TAG = "ForegroundService";
    private static int PERMISSION_CODE = 1;

    public RecordingService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "create service.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "start service.");
        Notification.Builder builder = new Notification.Builder(this);
        Notification notification = builder.setContentTitle("kamcord")
                .setContentText("kamcord recording")
                .setSmallIcon(R.drawable.paranoid_img)
                .build();
        startForeground(3141592, notification);
        Log.d("start dummyactivity", "yeah!");
        Intent recordIntent = new Intent(this, DummyActivity.class);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(recordIntent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        Log.d(LOG_TAG, "kill service.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class DummyActivity extends Activity {

        private MediaProjectionManager mediaProjectionManager;
        public DummyActivity() {
            super();
        }

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Log.d("startactivityforresult"," hahah");
            mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            Log.d("adls;fj", "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
            if(resultCode == RESULT_OK && requestCode == PERMISSION_CODE) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    MediaProjection projection = mediaProjectionManager.getMediaProjection(resultCode, data);
                    Log.d("start screenrecorder", "start recording");
                    new ScreenRecorder(projection, getApplicationContext()).start();
                }
            } else {
                // do something else
            }
            finish();
        }
    }
}


