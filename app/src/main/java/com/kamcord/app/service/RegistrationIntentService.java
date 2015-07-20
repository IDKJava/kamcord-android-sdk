package com.kamcord.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kamcord.app.server.client.AppServerClient;
import com.kamcord.app.server.model.GenericResponse;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegistrationIntentService extends IntentService {

    private static final String registrationServiceTag = "RegistrationService";
    private static final String senderID = "1003397135098";

    public RegistrationIntentService() {
        super(registrationServiceTag);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            try{
                instanceID.deleteInstanceID();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String token = instanceID.getToken(
                    senderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null
            );

            if (token != null) {
                sendRegistrationToServer(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void sendRegistrationToServer(String token) {
        AppServerClient.getInstance().registerPushNotif(token, new regPushNotifCallback());
    }

    private class regPushNotifCallback implements Callback<GenericResponse<?>> {
        @Override
        public void success(GenericResponse<?> responseWrapper, Response response) {
            Log.e("Notif Registration", "  " + "success");
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Notif Registration", "  " + error.toString());
        }
    }
}
