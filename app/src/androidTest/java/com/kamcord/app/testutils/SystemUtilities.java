package com.kamcord.app.testutils;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Until;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static com.kamcord.app.testutils.UiUtilities.*;

/**
 * Created by Mehmet on 6/1/15.
 */
public class SystemUtilities {
    public static final String KAMCORD_CACHE_FOLDER = "Kamcord_Android";
    public static final String NOMEDIA_TAG = ".nomedia";
    public static final String SDCARD_ROOT = "/sdcard/";
    public static final String SHARED_PREF_ROOT = "/data/data/";
    private static final String ACTIVE_SESSION_PREFS = "active_sessions_prefs";
    private static final String ACTIVE_SESSIONS_KEY = "active_sessions";

    public static String executeShellCommand(String cmd) {
        try {


            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br;
            if (p.waitFor() == 0) {
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            }
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
                output.append("\r\n");
            }
            return output.toString();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return "Exception!";
        }
    }
    public static void startApplication(String appPackageName) {
        final String launcherPackage = getLauncherPackageName();

        assertThat(launcherPackage, notNullValue());

        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), APP_TIMEOUT_MS);

        Context context = InstrumentationRegistry.getContext();

        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(appPackageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.stopService(intent);
        context.startActivity(intent);


        boolean notTimedOut = mDevice.wait(Until.hasObject(By.pkg(KAMCORD_APP_PACKAGE).depth(0)), APP_TIMEOUT_MS);
        assertTrue("Application load timed out!", notTimedOut);
    }

    public static void stopService(Class<?> serviceClass){

        String intent = serviceClass.getName().replace(".service", "/.service");
        String cmd = String.format("su -c am stopservice %s", intent);
        String result = executeShellCommand(cmd);
    }
    public static void stopActivity(Class<?> activityClass){

        String appPackage = activityClass.getName().replace(".activity", "/.activity");
        String cmd = String.format("su -c pm disable %s", appPackage);
        String result = executeShellCommand(cmd);
    }
    public static void stopApp(String appPackageName){

        String cmd = String.format("su -c am force-stop %s", appPackageName);
        String result = executeShellCommand(cmd);
    }

    public static int getFolderSize(String fullPath){
        String du = executeShellCommand(
                String.format("du -sk %s", fullPath));
        int folderSizeInKB = 0;
        try{
            folderSizeInKB = Integer.parseInt(du.split("\\s+")[0]);
        } catch (Exception e){
            e.printStackTrace();
            folderSizeInKB = -1;
        }
        return folderSizeInKB;
    }
    public static boolean doWeHaveInternet(){
        Context context  = InstrumentationRegistry.getContext();
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null && activeNetwork.isConnected());
        return isConnected;
    }

    public static void toggleNetwork(boolean On){
        String opWord =  On ? "enable" : "disable";
        executeShellCommand(String.format("su -c svc wifi %s", opWord));
        executeShellCommand(String.format("su -c svc data %s", opWord));
        int timeOut = 30000;
        int timeOutCtr = 0;
        while(doWeHaveInternet() != On && timeOutCtr < timeOut){
            sleep(100);
            timeOutCtr++;
        }
        assertTrue("Network toggle timed out!", timeOutCtr < timeOut);
    }

    public static void clearSharedPreferences(){
        String sharedAppPrefs = String.format("%s%s/shared_prefs/%s.xml",
                SHARED_PREF_ROOT,
                KAMCORD_APP_PACKAGE,
                ACTIVE_SESSION_PREFS);
        executeShellCommand(String.format("su -c rm %s", sharedAppPrefs));
    }
    public static ArrayList<Integer> getHeapSize(String appPackageName){
        String meminfo = executeShellCommand(String.format("su -c dumpsys meminfo %s", appPackageName));
        ArrayList<Integer> heapSizes = new ArrayList<>();
        for(String line: meminfo.split("\r\n")){
            if(line.toLowerCase().contains("native heap") ||
                    line.toLowerCase().contains("dalvik heap")){
                String[] items = line.split("\\s+");
                try {
                    heapSizes.add(Integer.parseInt(items[7]));
                } catch (Exception e){
                    heapSizes.add(0);
                }
            }
        }
        //native, dalvik in order
        return heapSizes;
    }
}
