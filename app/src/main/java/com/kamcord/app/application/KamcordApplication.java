package com.kamcord.app.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.DeviceManager;
import io.fabric.sdk.android.Fabric;

public class KamcordApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AccountManager.initializeWith(this);
        DeviceManager.initialize();
    }
}
