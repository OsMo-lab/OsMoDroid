package com.OsMoDroid;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
public class MyGcmListenerService extends GcmListenerService
    {
        @Override
        public void onMessageReceived(String from, Bundle data)
            {
                if(OsMoDroid.settings.getBoolean("live", true))
                    {
                        Intent is = new Intent(this, LocalService.class);
                        is.putExtras(data);
                        startService(is);
                    }
            }
    }
