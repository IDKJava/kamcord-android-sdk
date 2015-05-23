package com.kamcord.app.application;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.kamcord.app.R;
import com.kamcord.app.utils.AccountManager;
import com.kamcord.app.utils.DeviceManager;
import com.kamcord.app.utils.GameListUtils;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class KamcordApplication extends Application {

    private final static String FLURRY_API_KEY = "WMPGQQFYFHW74XG4MGVY";
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AccountManager.initializeWith(this);
        DeviceManager.initialize();
        GameListUtils.initializeWith(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/proximanova_regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.VERBOSE);
        FlurryAgent.init(this, FLURRY_API_KEY);
    }
}
