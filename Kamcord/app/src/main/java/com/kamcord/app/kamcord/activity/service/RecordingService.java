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

    private static RecordHandlerThread mRecordHandlerThread;
    private static Handler RecordHandler;
    private Context ServiceContext;
    private static String GameFolderString;
    private static String LaunchPackageName;

    public RecordingService() {
        super();
    }

    @Override
    public void onCreate() {
        ServiceContext = getApplicationContext();
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

        GameFolderString = intent.getStringExtra("GameFolder");
        LaunchPackageName = intent.getStringExtra("PackageName");
        Intent recordIntent = new Intent(this, ServiceActivity.class);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(recordIntent);
        Toast.makeText(ServiceContext, "Start Recording", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecordHandlerThread != null) {
            mRecordHandlerThread.interrupt();
            stopSelf();
            Toast.makeText(ServiceContext, "Stop Recording", Toast.LENGTH_SHORT).show();
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

                    mRecordHandlerThread = new RecordHandlerThread("HandlerThread");
                    mRecordHandlerThread.start();
                    RecordHandler = new Handler(mRecordHandlerThread.getLooper(), mRecordHandlerThread);
                    RecordingMessage messageObject = new RecordingMessage(
                            projection,
                            getApplicationContext(),
                            true,
                            RecordHandler,
                            RecordingService.LaunchPackageName,
                            RecordingService.GameFolderString);
                    Message msg = Message.obtain(RecordHandler, whatMemberValue, messageObject);
                    RecordHandler.sendMessage(msg);

                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(RecordingService.LaunchPackageName);
                    startActivity(launchIntent);
                }
            } else {
                // do something else
            }
            finish();
        }
    }
}


