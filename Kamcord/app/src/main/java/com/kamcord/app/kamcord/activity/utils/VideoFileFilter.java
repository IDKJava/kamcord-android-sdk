package com.kamcord.app.kamcord.activity.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by donliang1 on 5/4/15.
 */
public class VideoFileFilter implements FilenameFilter {

    @Override
    public boolean accept(File directory, String fileName) {
        if(fileName.endsWith(".mp4")) {
            return true;
        }
        return false;
    }
}
