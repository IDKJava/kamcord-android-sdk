package com.kamcord.app.server.model.analytics;

import android.os.Build;

import com.kamcord.app.BuildConfig;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by pplunkett on 6/15/15.
 */
public class TrackEventEntity {
    public String app_device_id;
    public String user_registration_id;
    public Set<Event> event = new HashSet<>();
    public long sent_time;

    public final String client_name = "android_app";
    public final String client_version = BuildConfig.VERSION_NAME;
    public final String os_name = "android";
    public final String os_version = Integer.toString(Build.VERSION.SDK_INT);
    public final String device_name = Build.DEVICE;
    public final String device_model = Build.MODEL;
    public final String language = Locale.getDefault().getLanguage();
    public final String country = Locale.getDefault().getCountry();

    public static class Builder {
        private TrackEventEntity entity = new TrackEventEntity();

        public Builder setAppDeviceId(String appDeviceId) {
            entity.app_device_id = appDeviceId;
            return this;
        }

        public Builder setUserRegistrationId(String userRegistrationId) {
            entity.user_registration_id = userRegistrationId;
            return this;
        }

        public Builder addEvent(Event e) {
            entity.event.add(e);
            return this;
        }

        public TrackEventEntity build() {
            entity.sent_time = System.currentTimeMillis() / 1000;
            return entity;
        }
    }
}
