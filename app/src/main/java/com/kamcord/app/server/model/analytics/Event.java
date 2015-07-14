package com.kamcord.app.server.model.analytics;

import com.google.gson.annotations.SerializedName;
import com.kamcord.app.utils.Connectivity;

import java.util.UUID;

/**
 * Created by pplunkett on 6/15/15.
 */
public class Event {
    public enum Name {
        KAMCORD_APP_LAUNCH,
        FIRST_KAMCORD_APP_LAUNCH,
        UPLOAD_VIDEO,
        EXTERNAL_SHARE,
        RECORD_VIDEO,
        REPLAY_VIDEO_VIEW,
        STREAM_DETAIL_VIEW,
        VIDEO_DETAIL_VIEW,
    }

    public Event(Name name, long whenMs, String appSessionId) {
        this.name = name;
        this.event_id = UUID.randomUUID().toString();
        this.app_session_id = appSessionId;

        this.startTimeMs = whenMs;
        if(Connectivity.isConnected()) {
            if( Connectivity.isConnectedWifi()) {
                connection_type = ConnectionType.WIFI;
            } else if( Connectivity.isConnectedMobile() ) {
                connection_type = ConnectionType.MOBILE;
            }
        }
    }

    public void setDurationFromStopTime(long stopTimeMs) {
        this.eventDurationMs = stopTimeMs - startTimeMs;
    }

    public void setRequestTimeFromStopTime(long stopTimeMs) {
        this.requestDurationMs = stopTimeMs - startTimeMs;
    }

    public void convertTimes() {
        this.start_time = startTimeMs / 1000;
        if( this.eventDurationMs != null ) {
            this.event_duration = ((eventDurationMs / 1000f) * 10) / 10f;
        }
        if( this.requestDurationMs != null ) {
            this.request_duration = ((requestDurationMs / 1000f) * 10) / 10f;
        }
    }

    public Name name;
    public long start_time;
    public transient long startTimeMs;

    public String app_session_id;
    public String ui_session_id;
    public String user_registration_id;
    public String event_id;
    public ConnectionType connection_type;

    public enum ConnectionType {
        @SerializedName("wifi")
        WIFI,
        @SerializedName("mobile")
        MOBILE,
    }


    // For navigational events.
    public Float event_duration = null;
    public transient Long eventDurationMs = null;
    public ViewSource view_source = null;
    public enum ViewSource {
        VIDEO_LIST_VIEW,
        PUSH_NOTIFICATION,
    }
    public enum ListType {
        PROFILE,
        HOME,
    }


    // For server events.
    public Float request_duration = null;
    public transient Long requestDurationMs = null;
    public Integer is_success = null;


    // For UPLOAD events.
    public String video_global_id = null;
    public UploadFailureReason failure_reason = null;
    public Integer was_replayed = null;
    public Integer is_retry = null;
    public enum UploadFailureReason {
        RESERVE_VIDEO,
        UPLOAD_TO_S3,
        UPLOAD_COMPLETION,
    }


    // For EXTERNAL_SHARE events.
    public ExternalNetwork external_network = null;
    public enum ExternalNetwork {
        EMAIL,
        FACEBOOK,
        KAKAO,
        LINE,
        NICONICO,
        TWITTER,
        WECHAT,
        YOUTUBE,
    }


    // For RECORD_VIDEO events.
    public String game_id = null;

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


    // For REPLAY_VIDEO_VIEW, VIDEO_DETAIL_VIEW, and STREAM_DETAIL_VIEW
    public Integer num_play_starts;
    public Float buffering_duration;
    public Float video_length_watched;

    // For VIDEO_DETAIL_VIEW and STREAM_DETAIL_VIEW
    public ListType video_list_type;
    public String feed_id;
    public String notification_sent_id;
    public Integer video_list_row;
    public Integer video_list_col;

    // For VIDEO_DETAIL_VIEW
    public String profile_user_id;

    // For STREAM_DETAIL_VIEW
    public String stream_user_id;
    public Integer is_live;
}
