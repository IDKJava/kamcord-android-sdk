package com.kamcord.app.kamcord.activity.application;

import android.app.Application;

/**
 * Created by donliang1 on 4/22/15.
 */
public class KamcordApplication extends Application {

    private boolean recordFlag = false;
    private String packageName = "com.rovio.BadPiggies";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean getRecordFlag() {
        return recordFlag;
    }

    public void setRecordFlag(boolean recordFlag) {
        this.recordFlag = recordFlag;
    }

    public String getPackageName() {
        return packageName;
    }
}
