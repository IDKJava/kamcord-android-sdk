package com.kamcord.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.ReserveVideoEntity;
import com.kamcord.app.server.model.ReserveVideoResponse;
import com.kamcord.app.server.model.builder.ReserveVideoEntityBuilder;

import retrofit.RetrofitError;

public class UploadService extends IntentService {
    private static final String TAG = RecordingService.class.getSimpleName();

    public static final String ARG_VIDEO_TO_SHARE = "video_to_share";

    public UploadService() {
        super("Kamcord Upload Service");
        this.setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        RecordingSession videoToShare = intent.getParcelableExtra(ARG_VIDEO_TO_SHARE);

        ReserveVideoEntity reserveVideoEntity = new ReserveVideoEntityBuilder()
                .setUserTitle(videoToShare.getVideoTitle())
                .setDescription(videoToShare.getVideoDescription())
                .setDefaultTitle("default title")
                .setGameId("17636")
                .build();

        ReserveVideoResponse reserveVideoResponse;
        try
        {
            Log.v("FindMe", "Attempting to reserve video...");
            reserveVideoResponse = AppServerClient.getInstance().reserveVideo(reserveVideoEntity);
            Log.v("FindMe", "Received response: " + new Gson().toJson(reserveVideoResponse));
        }
        catch( RetrofitError e )
        {
            e.printStackTrace();
        }
    }

    public interface UploadCallback
    {
        void uploadStarted(RecordingSession theVideo);
        void videoReserved(RecordingSession theVideo, ReserveVideoResponse reserveVideoResponse);
    }
}
