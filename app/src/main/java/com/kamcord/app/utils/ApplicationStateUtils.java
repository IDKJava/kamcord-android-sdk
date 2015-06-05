package com.kamcord.app.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

import java.util.HashSet;
import java.util.List;

/**
 * Created by pplunkett on 6/5/15.
 */
public class ApplicationStateUtils {
    private static PowerManager powerManager;
    private static KeyguardManager keyguardManager;
    private static ActivityManager activityManager;

    public static void initializeWith(Context context) {
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    private static HashSet<String> initialForegroundProcesses = new HashSet<>();
    public static synchronized void initializeForeground() {
        initialForegroundProcesses.clear();
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfoList) {
            if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                initialForegroundProcesses.add(runningAppProcessInfo.processName);
            }
        }
    }

    public static synchronized void invalidateForeground() {
        initialForegroundProcesses.clear();
    }

    public static synchronized boolean isGameInForeground(String packageName) {

        if( !powerManager.isInteractive() )
        {
            return false;
        }

        if( keyguardManager.inKeyguardRestrictedInputMode() )
        {
            return false;
        }

        HashSet<String> currentForegroundProcessNames = new HashSet<>();
        boolean isInForeground = false;
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfoList) {
            if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                currentForegroundProcessNames.add(runningAppProcessInfo.processName);
                for (String pkgName : runningAppProcessInfo.pkgList) {
                    if (pkgName.equals(packageName)) {
                        isInForeground = true;
                    }
                }
            }
        }
        return isInForeground && initialForegroundProcesses.equals(currentForegroundProcessNames);
    }

}
