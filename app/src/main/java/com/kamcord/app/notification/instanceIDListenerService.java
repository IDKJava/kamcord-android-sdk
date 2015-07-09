package com.kamcord.app.notification;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class instanceIDListenerService extends InstanceIDListenerService {

    public instanceIDListenerService() {
    }

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
