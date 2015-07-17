package com.kamcord.app.server.callbacks;

import android.os.Bundle;
import android.util.Log;

import com.kamcord.app.analytics.KamcordAnalytics;
import com.kamcord.app.server.model.GenericResponse;
import com.kamcord.app.server.model.StatusCode;
import com.kamcord.app.server.model.analytics.Event;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pplunkett on 7/17/15.
 */
public class FollowCallback implements Callback<GenericResponse<?>> {
    private String receivingUserId = null;
    private String videoId = null;
    private Event.ViewSource viewSource;

    public FollowCallback(String receivingUserId, String videoId, Event.ViewSource viewSource) {
        this.receivingUserId = receivingUserId;
        this.videoId = videoId;
        this.viewSource = viewSource;
    }

    @Override
    public void success(GenericResponse<?> responseWrapper, Response response) {
        boolean isSuccess =
                responseWrapper != null && responseWrapper.status != null && responseWrapper.status.equals(StatusCode.OK);
        String failureReason =
                responseWrapper != null && responseWrapper.status != null && !responseWrapper.status.equals(StatusCode.OK) ?
                        responseWrapper.status.status_reason : null;

        Bundle extras = analyticsFollowExtras(isSuccess ? 1 : 0, failureReason);
        KamcordAnalytics.endSession(this, Event.Name.FOLLOW_USER, extras);
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e("Retrofit Failure", "  " + error.toString());
        Bundle extras = analyticsFollowExtras(0, null);
        KamcordAnalytics.endSession(this, Event.Name.FOLLOW_USER, extras);
    }

    private Bundle analyticsFollowExtras(int isSuccess, String failureReason) {
        Bundle analyticsExtras = new Bundle();

        analyticsExtras.putString(KamcordAnalytics.FOLLOWED_USER_ID_KEY, receivingUserId);
        analyticsExtras.putInt(KamcordAnalytics.IS_FOLLOW_KEY, 1);
        analyticsExtras.putString(KamcordAnalytics.VIDEO_ID_KEY, videoId);
        analyticsExtras.putInt(KamcordAnalytics.IS_SUCCESS_KEY, isSuccess);
        analyticsExtras.putString(KamcordAnalytics.FAILURE_REASON_KEY, failureReason);
        analyticsExtras.putSerializable(KamcordAnalytics.VIEW_SOURCE_KEY, viewSource);

        return analyticsExtras;
    }
}
