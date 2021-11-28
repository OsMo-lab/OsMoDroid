package com.OsMoDroid;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFcmListenerService extends FirebaseMessagingService
    {
    @Override
    public void onMessageReceived(RemoteMessage message){
        String from = message.getFrom();
        Map data = message.getData();

        if (data.containsKey("GCM"))
            {
                if(true)
                    {
                        Intent is = new Intent(this, LocalService.class);

                        is.putExtra("GCM",(String)data.get("GCM"));
                        startService(is);
                    }
            }
            else
        {
            NotificationCompat.Builder builder = new  NotificationCompat.Builder(this,"default")
                    .setSmallIcon(R.drawable.eye)
                    .setContentTitle(message.getNotification().getTitle())
                    .setContentText(message.getNotification().getBody())
                    .setAutoCancel(true).setChannelId("silent");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                builder.setSmallIcon(R.drawable.eyeo26);
            }
            if (!OsMoDroid.settings.getBoolean("silentnotify", false))
            {
                builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND).setChannelId("noisy");
            }
            NotificationManager manager = (NotificationManager)     getSystemService(NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }


    }

        @Override
        public void onNewToken(@NonNull String s) {
            OsMoDroid.editor.putString ("GCMregId", s);
            OsMoDroid.editor.putBoolean("needsendgcmregid", true);
            OsMoDroid.editor.commit();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Intent is = new Intent(this, LocalService.class);
                is.putExtra("GCM", "NEEDSENDTOKEN|" + s);
                startService(is);
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
