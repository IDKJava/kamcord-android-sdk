package com.kamcord.app.server.model.analytics;

import com.google.gson.annotations.SerializedName;
import com.kamcord.app.utils.Connectivity;

import java.util.UUID;

/**
 * Created by pplunkett on 6/15/15.
 */
public class Event {

    public Event(Name name, long whenMs, String appSessionId) {
        this.name = name;
        this.event_id = UUID.randomUUID().toString();
        this.app_session_id = appSessionId;

        this.start_time = whenMs / 1000;
        if(Connectivity.isConnected()) {
            if( Connectivity.isConnectedWifi()) {
                connection_type = ConnectionType.WIFI;
            } else if( Connectivity.isConnectedMobile() ) {
                connection_type = ConnectionType.MOBILE;
            }
        }
    }

    public void setDurationFromStopTime(long stopTimeMs) {
        float stopTimeS = ((float) stopTimeMs) / 1000f;
        this.event_duration = Math.max(((float) start_time) - stopTimeS, 0f);
    }

    public Name name;
    public long start_time;

    public String app_session_id;
    public String ui_session_id;
    public String event_id;
    public ConnectionType connection_type;

    public enum Name {
        KAMCORD_APP_LAUNCH,
        FIRST_APP_LAUNCH,
        UPLOAD,
        EXTERNAL_SHARE,
    }

    public enum ConnectionType {
        @SerializedName("wifi")
        WIFI,
        @SerializedName("mobile")
        MOBILE,
    }

    // For navigational events.
    public Float event_duration = null;
    public SourceView source_view = null;

    public enum SourceView {

    }

    // For server events.
    public Long request_duration = null;
    public Boolean is_success = null;

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
