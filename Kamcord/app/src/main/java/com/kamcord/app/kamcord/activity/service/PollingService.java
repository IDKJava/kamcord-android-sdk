package com.kamcord.app.kamcord.activity.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

/**
 * Created by donliang1 on 4/23/15.
 */
public class PollingService extends Service {

    private PollingThread pollingThread;
    public static final String ACTION = "PollingService Action";
    private boolean flag = true;

    @Override
    public void onCreate() {
        Log.d("Polling service", "started!!!");
        this.pollingThread = new PollingThread();
        this.pollingThread.start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class PollingThread extends Thread {

        @Override
        public void run() {
            // Check current running applications list
            ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
            while(flag) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                Log.d("Polling...", "polling...");
                List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
                String packageName;
                for(ActivityManager.AppTask task : appTasks) {
                    packageName = task.getTaskInfo().baseIntent.getComponent().getPackageName();
                    Log.d("Top Activity: ", packageName);
                }
            }
        }
    }
}
