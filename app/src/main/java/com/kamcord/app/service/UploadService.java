package com.kamcord.app.service;

import android.app.IntentService;
import android.content.Intent;

public class UploadService extends IntentService {
    private static final String TAG = RecordingService.class.getSimpleName();


    public UploadService() {
        super("Kamcord Upload Service");
        this.setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {


    }


}
