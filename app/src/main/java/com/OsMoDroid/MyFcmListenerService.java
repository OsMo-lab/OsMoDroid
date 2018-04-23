package com.OsMoDroid;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static com.OsMoDroid.LocalService.addlog;

public class MyFcmListenerService extends FirebaseMessagingService
    {
    @Override
    public void onMessageReceived(RemoteMessage message){
        String from = message.getFrom();
        Map data = message.getData();

        if (data.containsKey("GCM"))
            {
                if(OsMoDroid.settings.getBoolean("live", true))
                    {
                        Intent is = new Intent(this, LocalService.class);

                        is.putExtra("GCM",(String)data.get("GCM"));
                        startService(is);
                    }
            }
            else
        {
            NotificationCompat.Builder builder = new  NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.eye)
                    .setContentTitle(message.getNotification().getTitle())
                    .setContentText(message.getNotification().getBody())
                    .setAutoCancel(true);
            if (!OsMoDroid.settings.getBoolean("silentnotify", false))
            {
                builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
            }
            NotificationManager manager = (NotificationManager)     getSystemService(NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }


    }

}
//public class MyGcmListenerService extends GcmListenerService
//    {
//        @Override
//        public void onMessageReceived(String from, Bundle data)
//            {
//                if(OsMoDroid.settings.getBoolean("live", true))
//                    {
//                        Intent is = new Intent(this, LocalService.class);
//                        is.putExtras(data);
//                        startService(is);
//                    }
//            }
//    }
