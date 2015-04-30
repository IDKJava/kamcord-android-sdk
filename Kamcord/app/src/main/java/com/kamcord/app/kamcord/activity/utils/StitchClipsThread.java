package com.kamcord.app.kamcord.activity.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by donliang1 on 4/29/15.
 */
public class StitchClipsThread extends Thread {

    private String ResultPath;
    private ArrayList<String> inputVideoClips;

    public StitchClipsThread(ArrayList<String> inputVideos) {
        this.inputVideoClips = inputVideos;
    }

    @Override
    public void run() {
        ResultPath = Environment.getExternalStorageDirectory().getParent() + "/" + Environment.getExternalStorageDirectory().getName() + "/Kamcord_Video";
        File ResultFolder = new File(ResultPath);
        if (!ResultFolder.exists() || !ResultFolder.isDirectory()) {
            ResultFolder.mkdir();
        }
        startStitching(inputVideoClips);
    }

    public void startStitching(ArrayList<String> inputVideos) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
