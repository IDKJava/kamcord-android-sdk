package com.kamcord.app.application;

import android.app.Application;

import com.kamcord.app.utils.AccountManager;

public class KamcordApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AccountManager.initializeWith(this);
    }
}
