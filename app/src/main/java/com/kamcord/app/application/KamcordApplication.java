package com.kamcord.app.application;

import android.app.Application;

import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.DeviceManager;

public class KamcordApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AccountManager.initializeWith(this);
        DeviceManager.initialize();
    }
}
