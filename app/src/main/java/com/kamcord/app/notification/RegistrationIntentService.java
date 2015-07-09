package com.kamcord.app.notification;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

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
            String token = instanceID.getToken(
                    senderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                    null
            );
            Log.d("RegisterToken", token);
            // Send Token to Server
            // sendRegistrationToServer()

        } catch (Exception e){
            Log.d("Registration Service", "onHandleIntent");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
