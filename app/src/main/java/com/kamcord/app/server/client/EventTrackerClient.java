package com.kamcord.app.server.client;

import com.kamcord.app.BuildConfig;
import com.kamcord.app.server.model.analytics.TrackEventEntity;
import com.kamcord.app.server.model.analytics.WrappedResponse;

import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by pplunkett on 6/15/15.
 */
public class EventTrackerClient {
    private static final String BASE_URL = BuildConfig.DEBUG ? "foo" : "https://curry.kamcord.com";

    public interface EventTracker {
        @POST("v1/trackevent")
        WrappedResponse<?> trackEvent(@Body TrackEventEntity body);
    }
}
