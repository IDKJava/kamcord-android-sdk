package com.kamcord.app.kamcord.activity.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.kamcord.app.kamcord.activity.Model.MessageObject;
import com.kamcord.app.kamcord.activity.activity.RecordActivity;
import com.kamcord.app.kamcord.activity.utils.RecordHandlerThread;
import com.kamcord.app.kamcord.activity.utils.ScreenRecorder;

@TargetApi(21)
public class RecordingService extends Service {

    private static final String LOG_TAG = "ForegroundService";
    private static int PERMISSION_CODE = 1;
    private static ScreenRecorder recordThread;

    private static RecordHandlerThread recordHandlerThread;
    private static Handler recordHandler;

    private Context serviceContext;

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


        int recordStopFlag = intent.getFlags();

        if (recordStopFlag != 2) {
            // Notification Setting

            Intent noficationIntent = new Intent(serviceContext, RecordActivity.class);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            Notification.Builder notificationBuilder = new Notification.Builder(this);
            Notification notification = notificationBuilder
                    .setContentTitle("Kamcord")
                    .setContentText("Recording")
                    .setSmallIcon(R.drawable.paranoid_img)
                    .build();


            startForeground(3141592, notification);

            Intent recordIntent = new Intent(this, ServiceActivity.class);
            recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(recordIntent);
            Toast.makeText(serviceContext, "Start Recording", Toast.LENGTH_SHORT).show();

        } else {
            if (recordHandlerThread != null) {
                recordHandlerThread.interrupt();
                stopSelf();
                Toast.makeText(serviceContext, "Stop Recording", Toast.LENGTH_SHORT).show();
                Log.d("Stop recording", "" + recordStopFlag);
            }
        }
        return START_REDELIVER_INTENT;
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

                    // Calling Record Thread
                    recordHandlerThread = new RecordHandlerThread("HandlerThread");
                    recordHandlerThread.start();

                    recordHandler = new Handler(recordHandlerThread.getLooper(), recordHandlerThread);
                    MessageObject messageObject = new MessageObject(projection, getApplicationContext(), true, recordHandler);
                    Message msg = Message.obtain(recordHandler, 1, messageObject);
                    recordHandler.sendMessage(msg);

                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.rovio.BadPiggies");
                    startActivity(launchIntent);
                }
            } else {
                // do something else
            }
            finish();
        }
    }
}


