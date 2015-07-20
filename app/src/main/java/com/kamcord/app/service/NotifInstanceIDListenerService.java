package com.kamcord.app.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by donliang1 on 15/7/9.
 */
public class NotifInstanceIDListenerService extends InstanceIDListenerService{


    public NotifInstanceIDListenerService() {
    }

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
