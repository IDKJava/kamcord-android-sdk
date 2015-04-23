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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.utils.ScreenRecorder;

@TargetApi(21)
public class RecordingService extends Service {

    private static final String LOG_TAG = "ForegroundService";
    private static int PERMISSION_CODE = 1;
    private static ScreenRecorder recordThread;

    public RecordingService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        int recordStopFlag = intent.getFlags();
        if (recordStopFlag != 2) {
            // Notification Setting
            // RemoteViews bigContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_big_contentview);
            // notification.bigContentView = bigContentView;
            Notification.Builder notificationBuilder = new Notification.Builder(this);
            Notification notification = notificationBuilder.setContentTitle("Kamcord")
                    .setContentText("Recording")
                    .setSmallIcon(R.drawable.paranoid_img)
                    .build();

            startForeground(3141592, notification);
            Log.d("start ServiceActivity", "yeah!");
            Intent recordIntent = new Intent(this, ServiceActivity.class);
            recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(recordIntent);
            Toast.makeText(getApplicationContext(), "Start Recording", Toast.LENGTH_SHORT).show();

        } else {
            recordThread.setFlag(false);
            recordThread.interrupt();
            stopSelf();
            Toast.makeText(getApplicationContext(), "Stop Recording", Toast.LENGTH_SHORT).show();
            Log.d("Stop recording", "" + recordStopFlag);
        }
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

    public static class ServiceActivity extends Activity {

        private MediaProjectionManager mediaProjectionManager;

        public ServiceActivity() {
            super();
        }

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            Log.d("onActivityResult", "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
            if (resultCode == RESULT_OK && requestCode == PERMISSION_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    MediaProjection projection = mediaProjectionManager.getMediaProjection(resultCode, data);
                    String pkgNameForService = getApplication().getPackageName();
                    recordThread = new ScreenRecorder(projection, getApplicationContext(), true);
                    recordThread.start();

                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.sgn.pandapop.gp");
                    startActivity(launchIntent);
                }
            } else {
                // do something else
            }
            finish();
        }
    }
}


