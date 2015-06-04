package com.kamcord.app.testutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    public static void forceStopApp(String appPackageName){
        executeShellCommand(String.format("am force-stop %s", appPackageName));
    }
}
