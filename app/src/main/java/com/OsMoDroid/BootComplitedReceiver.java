package com.OsMoDroid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
public class BootComplitedReceiver extends BroadcastReceiver
    {
        private static final String ACTION_SMS = "android.provider.Telephony.SMS_RECEIVED";
        private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
        @Override
        public void onReceive(Context context, Intent recievedIntent)
            {
                PreferenceManager.setDefaultValues(context, R.xml.pref, true);
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                if (recievedIntent != null && recievedIntent.getAction() != null && recievedIntent.getAction().equalsIgnoreCase(ACTION_BOOT))
                    {
                        if (settings.getBoolean("autostart", false))
                            {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                    Intent is = new Intent(context, LocalService.class);
                                    context.startService(is);
                                }
                            }
                    }
                if (recievedIntent != null && recievedIntent.getAction() != null && recievedIntent.getAction().equalsIgnoreCase(ACTION_SMS))
                    {
                        if (settings.getBoolean("usesms", true))
                            {
                                try
                                    {
                                        Object[] pduArray = (Object[]) recievedIntent.getExtras().get("pdus");
                                        SmsMessage[] messages = new SmsMessage[pduArray.length];
                                        for (int i = 0; i < pduArray.length; i++)
                                            {
                                                messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                                            }
                                        StringBuilder bodyText = new StringBuilder();
                                        for (int i = 0; i < messages.length; i++)
                                            {
                                                bodyText.append(messages[i].getMessageBody());
                                            }
                                        String body = bodyText.toString();
                                        if (body.length() > 5 && body.startsWith("OSMO "))
                                            {
                                                Intent is = new Intent(context, LocalService.class);
                                                is.putExtra("SMS", body.substring(5));
                                                context.startService(is);
                                                abortBroadcast();
                                            }
                                    }
                                catch (Exception e)
                                    {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                            }
                    }
            }
    }
