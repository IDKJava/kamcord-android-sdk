package com.kamcord.app.testutils;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Until;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
    public static final String SDCARD_ROOT = "/storage/sdcard0/";

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
            return e.getStackTrace().toString();
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


}
