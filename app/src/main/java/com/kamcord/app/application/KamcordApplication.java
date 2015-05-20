package com.kamcord.app.application;

import android.app.Application;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.crashlytics.android.Crashlytics;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.DeviceManager;
import io.fabric.sdk.android.Fabric;

public class KamcordApplication extends Application {

    private final static String FLURRY_API_KEY = "WMPGQQFYFHW74XG4MGVY";
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AccountManager.initializeWith(this);
        DeviceManager.initialize();

        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.VERBOSE);
        FlurryAgent.init(this, FLURRY_API_KEY);
    }
}
