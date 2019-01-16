package com.OsMoDroid;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
public class MyInstanceIDListenerService extends FirebaseInstanceIdService
    {
        /**
         * Called if InstanceID token is updated. This may occur if the security of
         * the previous token had been compromised. Note that this is also called
         * when the InstanceID token is initially generated, so this is where
         * you retrieve the token.
         */
        // [START refresh_token]
        @Override
        public void onTokenRefresh()
            {
                // Get updated InstanceID token.
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
               // LocalService.addlog(refreshedToken);
                OsMoDroid.editor.putString("GCMRegId", refreshedToken);
                OsMoDroid.editor.putBoolean("needsendgcmregid", true);
                OsMoDroid.editor.commit();
                // TODO: Implement this method to send any registration to your app's servers.
                sendRegistrationToServer(refreshedToken);

            }
        private void sendRegistrationToServer(String token)
            {
                Log.d(this.getClass().getName(), "sendRegistrationToServer: " + token);
             //   LocalService.addlog("RegId=" + token);
                if (LocalService.myIM != null && LocalService.myIM.authed)
                    {
                        LocalService.myIM.sendToServer("GCM|" + token, false);
                    }
            }
    }
