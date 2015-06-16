package com.kamcord.app.server.model.analytics;

import android.os.Build;

import com.kamcord.app.BuildConfig;

import java.util.List;
import java.util.Locale;

/**
 * Created by pplunkett on 6/15/15.
 */
public class TrackEventRequestEntity {
    public String app_device_id;
    public String user_registration_id;
    public List<Event> event;
    public long sent_time;

    public final String client_name = "android_app";
    public final String client_version = BuildConfig.VERSION_NAME;
    public final String os_name = "android";
    public final String os_version = Integer.toString(BuildConfig.VERSION_CODE);
    public final String device_name = Build.DEVICE;
    public final String device_model = Build.MODEL;
    public final String language = Locale.getDefault().getLanguage();
    public final String country = Locale.getDefault().getCountry();
}
