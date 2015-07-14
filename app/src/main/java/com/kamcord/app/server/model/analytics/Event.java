package com.kamcord.app.server.model.analytics;

import android.os.Bundle;

import com.google.gson.annotations.SerializedName;
import com.kamcord.app.analytics.KamcordAnalytics;
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
        FOLLOW_USER,
        PROFILE_CREATION,
        PROFILE_CREATION_VIEW,
        PROFILE_LOGIN,
        PROFILE_LOGIN_VIEW,
        PROFILE_INTERSTITIAL,
    }

    public enum ViewSource {
        VIDEO_LIST_VIEW,
        PUSH_NOTIFICATION,
        VIDEO_DETAIL_VIEW,
        STREAM_DETAIL_VIEW,
        REPLAY_VIDEO_VIEW,
        PROFILE_DETAIL_VIEW,
        PROFILE_CREATION_VIEW,
        PROFILE_LOGIN_VIEW,
        SHARE_VIEW,
    }

    public enum ListType {
        PROFILE,
        HOME,
    }

    public enum UploadFailureReason {
        RESERVE_VIDEO,
        UPLOAD_TO_S3,
        UPLOAD_COMPLETION,
    }

    public enum ConnectionType {
        @SerializedName("wifi")
        WIFI,
        @SerializedName("mobile")
        MOBILE,
    }

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

    public enum InducingAction {
        FOLLOW_USER,
        COMMENT_VIDEO,
        FOLLOWERS_LIST,
        FOLLOWING_LIST,
        PROFILE_LOGOUT,
        SHARE_VIDEO,
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



    // For navigational events.
    public Float event_duration = null;
    public transient Long eventDurationMs = null;
    public ViewSource view_source = null;


    // For server events.
    public Float request_duration = null;
    public transient Long requestDurationMs = null;
    public Integer is_success = null;


    // For UPLOAD events.
    public String video_global_id = null;
    public String failure_reason = null;
    public Integer was_replayed = null;
    public Integer is_retry = null;


    // For EXTERNAL_SHARE events.
    public ExternalNetwork external_network = null;


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
    public Integer num_replays;
    public Float buffering_duration;
    public Float video_length_watched;
    public Float video_length;

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


    // For PROFILE_LOGIN
    public Integer is_login;


    // For PROFILE_INTERSTITIAL
    public InducingAction inducing_action;

    public void completeFromData(Bundle bundle) {

        if( bundle == null ) {
            return;
        }

        if( bundle.containsKey(KamcordAnalytics.VIEW_SOURCE_KEY) ) {
            this.view_source = (ViewSource) bundle.getSerializable(KamcordAnalytics.VIEW_SOURCE_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.IS_SUCCESS_KEY) ) {
            this.is_success = bundle.getInt(KamcordAnalytics.IS_SUCCESS_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.VIDEO_ID_KEY) ) {
            this.video_global_id = bundle.getString(KamcordAnalytics.VIDEO_ID_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.FAILURE_REASON_KEY) ) {
            this.failure_reason = bundle.getString(KamcordAnalytics.FAILURE_REASON_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.WAS_REPLAYED_KEY) ) {
            this.was_replayed = bundle.getInt(KamcordAnalytics.WAS_REPLAYED_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.IS_UPLOAD_RETRY_KEY) ) {
            this.is_retry = bundle.getInt(KamcordAnalytics.IS_UPLOAD_RETRY_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.EXTERNAL_NETWORK_KEY) ) {
            this.external_network = (ExternalNetwork) bundle.getSerializable(KamcordAnalytics.EXTERNAL_NETWORK_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.GAME_ID_KEY) ) {
            this.game_id = bundle.getString(KamcordAnalytics.GAME_ID_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.NUM_PLAY_STARTS_KEY) ) {
            this.num_play_starts = bundle.getInt(KamcordAnalytics.NUM_PLAY_STARTS_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.NUM_PLAY_STARTS_KEY) ) {
            this.num_replays = bundle.getInt(KamcordAnalytics.NUM_PLAY_STARTS_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.BUFFERING_DURATION_KEY) ) {
            this.buffering_duration = bundle.getFloat(KamcordAnalytics.BUFFERING_DURATION_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.VIDEO_LENGTH_WATCHED_KEY) ) {
            this.video_length_watched = bundle.getFloat(KamcordAnalytics.VIDEO_LENGTH_WATCHED_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.VIDEO_LENGTH_KEY) ) {
            this.video_length = bundle.getFloat(KamcordAnalytics.VIDEO_LENGTH_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.VIDEO_LIST_TYPE_KEY) ) {
            this.video_list_type = (ListType) bundle.getSerializable(KamcordAnalytics.VIDEO_LIST_TYPE_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.FEED_ID_KEY) ) {
            this.feed_id = bundle.getString(KamcordAnalytics.FEED_ID_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY) ) {
            this.notification_sent_id = bundle.getString(KamcordAnalytics.NOTIFICATION_SENT_ID_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.VIDEO_LIST_ROW_KEY) ) {
            this.video_list_row = bundle.getInt(KamcordAnalytics.VIDEO_LIST_ROW_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.VIDEO_LIST_COL_KEY) ) {
            this.video_list_col = bundle.getInt(KamcordAnalytics.VIDEO_LIST_COL_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.PROFILE_USER_ID_KEY) ) {
            this.profile_user_id = bundle.getString(KamcordAnalytics.PROFILE_USER_ID_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.STREAM_USER_ID_KEY) ) {
            this.stream_user_id = bundle.getString(KamcordAnalytics.STREAM_USER_ID_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.IS_LIVE_KEY) ) {
            this.is_live = bundle.getInt(KamcordAnalytics.IS_LIVE_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.IS_LOGIN_KEY) ) {
            this.is_login = bundle.getInt(KamcordAnalytics.IS_LOGIN_KEY);
        }
        if( bundle.containsKey(KamcordAnalytics.INDUCING_ACTION_KEY) ) {
            this.inducing_action = (InducingAction) bundle.getSerializable(KamcordAnalytics.INDUCING_ACTION_KEY);
        }

    }
}
