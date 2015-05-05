package com.kamcord.app.kamcord.activity.utils;

import java.io.File;
import java.io.FilenameFilter;

public class AudioFileFilter implements FilenameFilter {

    @Override
    public boolean accept(File directory, String fileName) {
        if (fileName.endsWith(".aac")) {
            return true;
        }
        return false;
    }
}
