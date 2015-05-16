package com.kamcord.app.service;

import android.app.IntentService;
import android.content.Intent;

import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.ReserveVideoEntity;
import com.kamcord.app.server.model.ReserveVideoResponse;
import com.kamcord.app.server.model.builder.ReserveVideoEntityBuilder;

import retrofit.RetrofitError;

public class UploadService extends IntentService {
    private static final String TAG = RecordingService.class.getSimpleName();

    public static final String ARG_SESSION_TO_SHARE = "session_to_share";

    public UploadService() {
        super("Kamcord Upload Service");
        this.setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        RecordingSession recordingSession = intent.getParcelableExtra(ARG_SESSION_TO_SHARE);

        ReserveVideoEntity reserveVideoEntity = new ReserveVideoEntityBuilder()
                .setUserTitle(recordingSession.getVideoTitle())
                .setDescription(recordingSession.getVideoDescription())
                .setDefaultTitle("default title") // TODO: fill this in with something that makes sense.
                .setGameId(recordingSession.getGameServerID())
                .build();

        ReserveVideoResponse reserveVideoResponse;
        try
        {
            reserveVideoResponse = AppServerClient.getInstance().reserveVideo(reserveVideoEntity);
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
