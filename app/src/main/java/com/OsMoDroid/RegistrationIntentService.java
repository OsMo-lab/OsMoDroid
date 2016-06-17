package com.OsMoDroid;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {


    public RegistrationIntentService() {
        super("regservice");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToServer(token);
            OsMoDroid.editor.putBoolean(OsMoDroid.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {

            OsMoDroid.editor.putBoolean(OsMoDroid.SENT_TOKEN_TO_SERVER, false).apply();
        }
        Intent registrationComplete = new Intent(OsMoDroid.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }


    private void sendRegistrationToServer(String token) {
        Log.d(this.getClass().getName(), "sendRegistrationToServer: "+token);
        LocalService.addlog("RegId=" + token);
        if(!OsMoDroid.settings.getString("GCMRegId","").equals(token))
            {
              OsMoDroid.tmpGCMRegId=token;
                if (LocalService.myIM != null && LocalService.myIM.authed)
                    {
                        LocalService.myIM.sendToServer("GCM|" +  OsMoDroid.tmpGCMRegId, false);
                    }

                        OsMoDroid.editor.putBoolean("needsendgcmregid", true).apply();

            }
    }
}
