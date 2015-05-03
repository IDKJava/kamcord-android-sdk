package com.kamcord.app.kamcord.activity.model;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Handler;

public class RecordingMessage extends Object {

    public MediaProjection projection;
    public Context mContext;
    public boolean mRecordFlag;
    private Handler mHandler;
    private String packageName;
    private String gameFolderString;

    public RecordingMessage(MediaProjection projection, Context context, boolean recordFlag, Handler handler, String packageName, String gameFolderString) {
        this.projection = projection;
        this.mContext = context;
        this.mRecordFlag = recordFlag;
        this.mHandler = handler;
        this.packageName = packageName;
        this.gameFolderString = gameFolderString;
    }

    public MediaProjection getProjection() {
        return projection;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean getRecordFlag() {
        return mRecordFlag;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getGameFolderString() {
        return gameFolderString;
    }
}
