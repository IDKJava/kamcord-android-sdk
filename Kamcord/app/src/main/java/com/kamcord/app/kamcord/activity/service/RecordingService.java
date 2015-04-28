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
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.kamcord.app.kamcord.R;
import com.kamcord.app.kamcord.activity.model.RecordingMessage;
import com.kamcord.app.kamcord.activity.utils.RecordHandlerThread;

@TargetApi(21)
public class RecordingService extends Service {

    private static int PERMISSION_CODE = 1;

    private static RecordHandlerThread recordHandlerThread;
    private static Handler recordHandler;
    private Context serviceContext;
    public static String gameFolderString;
    private static String launchPackageName;

    public RecordingService() {
        super();
    }

    @Override
    public void onCreate() {
        serviceContext = getApplicationContext();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Notification Setting
        Notification.Builder notificationBuilder = new Notification.Builder(this);
        Notification notification = notificationBuilder
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setContentText(getResources().getString(R.string.notification_content))
                .setSmallIcon(R.drawable.kamcord_appicon)
                .build();
        startForeground(3141592, notification);

        gameFolderString = intent.getStringExtra("GameFolder");
        Log.d("GameFolder", gameFolderString);
        launchPackageName = intent.getStringExtra("PackageName");
        Log.d("PackageName", launchPackageName);
        Intent recordIntent = new Intent(this, ServiceActivity.class);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(recordIntent);
        Toast.makeText(serviceContext, "Start Recording", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recordHandlerThread != null) {
            recordHandlerThread.interrupt();
            stopSelf();
            Toast.makeText(serviceContext, "Stop Recording", Toast.LENGTH_SHORT).show();
        }
        stopSelf();
        Log.d(RecordingService.class.getSimpleName(), "kill service.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class ServiceActivity extends Activity {

        private MediaProjectionManager mediaProjectionManager;
        private static final int whatMemberValue = 1;

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

            if (resultCode == RESULT_OK && requestCode == PERMISSION_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    MediaProjection projection = mediaProjectionManager.getMediaProjection(resultCode, data);

                    recordHandlerThread = new RecordHandlerThread("HandlerThread");
                    recordHandlerThread.start();
                    recordHandler = new Handler(recordHandlerThread.getLooper(), recordHandlerThread);
                    RecordingMessage messageObject = new RecordingMessage(
                            projection,
                            getApplicationContext(),
                            true,
                            recordHandler,
                            RecordingService.launchPackageName,
                            RecordingService.gameFolderString);
                    Message msg = Message.obtain(recordHandler, whatMemberValue, messageObject);
                    recordHandler.sendMessage(msg);

                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(RecordingService.launchPackageName);
                    startActivity(launchIntent);
                }
            } else {
                // do something else
            }
            finish();
        }
    }
}


