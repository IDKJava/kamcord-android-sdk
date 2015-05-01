package com.kamcord.app.kamcord.activity.model;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Handler;

public class RecordingMessage extends Object {

    public MediaProjection Projection;
    public Context mContext;
    public boolean mRecordFlag;
    private Handler mHandler;
    private String PackageName;
    private String GameFolderString;

    public RecordingMessage(MediaProjection projection, Context context, boolean recordFlag, Handler handler, String packageName, String gameFolderString) {
        this.Projection = projection;
        this.mContext = context;
        this.mRecordFlag = recordFlag;
        this.mHandler = handler;
        this.PackageName = packageName;
        this.GameFolderString = gameFolderString;
    }

    public MediaProjection getProjection() {
        return Projection;
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
        return PackageName;
    }

    public String getGameFolderString() {
        return GameFolderString;
    }
}
