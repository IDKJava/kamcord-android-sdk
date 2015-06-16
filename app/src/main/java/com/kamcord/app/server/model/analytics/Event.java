package com.kamcord.app.server.model.analytics;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pplunkett on 6/15/15.
 */
public class Event {

    public Name name;
    public long start_time;

    public String app_session_id;
    public String ui_session_id;
    public String event_id;
    public ConnectionType connection_type;

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

    @Override
    public int hashCode() {
        return event_id != null ? event_id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object other) {
        if( other != null && other instanceof Event &&
                ((Event) other).event_id != null &&
                ((Event) other).event_id.equals(this.event_id) ) {
            return true;
        }
        return false;
    }
}
