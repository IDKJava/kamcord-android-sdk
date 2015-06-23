package com.kamcord.app.server.model.analytics;

import android.os.Build;

import com.kamcord.app.BuildConfig;
import com.kamcord.app.utils.StringUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by pplunkett on 6/15/15.
 */
public class TrackEventEntity {
    private static transient final int MAX_FIELD_CHARACTERS = 50;

    public String app_device_id;
    public Set<Event> event = new HashSet<>();
    public long sent_time;

    public final String client_name = "android_app";
    public final String client_version = BuildConfig.VERSION_NAME;
    public final String os_name = "android";
    public final String os_version = Integer.toString(Build.VERSION.SDK_INT);
    public final String device_name = Build.DEVICE != null ? StringUtils.truncate(Build.DEVICE, MAX_FIELD_CHARACTERS) : "";
    public final String device_model = Build.MODEL != null ? StringUtils.truncate(Build.MODEL, MAX_FIELD_CHARACTERS) : "";
    public final String device_board = Build.BOARD  != null ? StringUtils.truncate(Build.BOARD, MAX_FIELD_CHARACTERS) : "";
    public final String language = Locale.getDefault().getLanguage();
    public final String country = Locale.getDefault().getCountry();

    public static class Builder {
        private TrackEventEntity entity = new TrackEventEntity();

        public Builder setAppDeviceId(String appDeviceId) {
            entity.app_device_id = appDeviceId;
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
