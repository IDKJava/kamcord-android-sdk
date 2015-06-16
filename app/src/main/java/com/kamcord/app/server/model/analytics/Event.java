package com.kamcord.app.server.model.analytics;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pplunkett on 6/15/15.
 */
public class Event {

    public Name name;
    public long start_time;

    // public long event_duration; // put into navigation subclass
    // public SourceView source_view; // put into navigation subclass

    // public long request_duration; // put into server subclass
    // public boolean is_success; // put into server subclass

    public String app_session_id;
    public String ui_session_id;
    public String event_id;

    public enum Name {
        LAUNCH,
        FIRST_LAUNCH,
        UPLOAD,
        EXTERNAL_SHARE,
    }

    public enum ConnectionType {
        @SerializedName("wifi")
        WIFI,
        @SerializedName("mobile")
        MOBILE,
    }
}
