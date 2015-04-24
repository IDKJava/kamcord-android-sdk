package com.kamcord.app.kamcord.activity.Model;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.os.Handler;

/**
 * Created by donliang1 on 4/24/15.
 */
public class MessageObject extends Object{

    public MediaProjection projection;
    public Context context;
    public boolean recordFlag;
    private Handler handler;

    public MessageObject(MediaProjection projection, Context context, boolean recordFlag, Handler handler) {
        this.projection = projection;
        this.context = context;
        this.recordFlag = recordFlag;
        this.handler = handler;
    }

    public MediaProjection getObjectProjection() {
        return projection;
    }

    public Context getObjectContext() {
        return context;
    }

    public boolean getObjectRecordFlag() {
        return recordFlag;
    }

    public Handler getHandler() {
        return handler;
    }
}
