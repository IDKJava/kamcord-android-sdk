package com.kamcord.app.service;

import android.app.IntentService;
import android.content.Intent;

import com.kamcord.app.model.RecordingSession;
import com.kamcord.app.server.model.ReserveVideoResponse;

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
    }

    public interface UploadCallback
    {
        void uploadStarted(RecordingSession theVideo);
        void videoReserved(RecordingSession theVideo, ReserveVideoResponse reserveVideoResponse);
    }
}
