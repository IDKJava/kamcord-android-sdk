package com.kamcord.app.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

import java.util.ArrayList;
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

    public static synchronized boolean isGameInForeground(String packageName) {

        if (!powerManager.isInteractive()) {
            return false;
        }

        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            return false;
        }

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        if (runningAppProcessInfoList == null) {
            runningAppProcessInfoList = new ArrayList<>();
        }
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfoList) {
            if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String pkgName : runningAppProcessInfo.pkgList) {
                    if (pkgName.equals(packageName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
