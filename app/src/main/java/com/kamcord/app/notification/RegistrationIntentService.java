package com.kamcord.app.notification;

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
            Log.d("Register ID", token);

            // Send Token to Server
            if (token != null) {
                sendRegistrationToServer(token);
            }
        } catch (Exception e) {
            Log.d("Registration Service", "onHandleIntent");
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
            Log.d("Push Notif Success", "  " + "Oh hi");
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Push Notif Failure", "  " + error.toString());
        }
    }
}
