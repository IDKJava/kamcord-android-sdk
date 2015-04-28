package com.kamcord.app.kamcord.activity.model;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Handler;

/**
 * Created by donliang1 on 4/28/15.
 */
public class RecordingMessage extends Object {

    public MediaProjection projection;
    public Context context;
    public boolean recordFlag;
    private Handler handler;
    private String packageName;
    private String gameFolderString;

    public RecordingMessage(MediaProjection projection, Context context, boolean recordFlag, Handler handler, String packageName, String gameFolderString) {
        this.projection = projection;
        this.context = context;
        this.recordFlag = recordFlag;
        this.handler = handler;
        this.packageName = packageName;
        this.gameFolderString = gameFolderString;
    }

    public MediaProjection getProjection() {
        return projection;
    }

    public Context getContext() {
        return context;
    }

    public boolean getRecordFlag() {
        return recordFlag;
    }

    public Handler getHandler() {
        return handler;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getGameFolderString() {
        return gameFolderString;
    }
}
