package com.OsMoDroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Created by 1 on 07.12.2016.
 */
public class PowerConnectionReceiver extends BroadcastReceiver
    {
        int chargePlug=-1;
        @Override
        public void onReceive(Context context, Intent intent)
            {

                if(true&&OsMoDroid.permanent)
                    {
                        String action = intent.getAction();

                        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
                            chargePlug=1;
                        }
                        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                            chargePlug=0;
                        }
                        Intent is = new Intent(context, LocalService.class);
                        is.putExtra("GCM","NEEDSENDCHARGE|"+chargePlug);

                        context.startService(is);
                    }
            }
    }
