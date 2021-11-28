package com.OsMoDroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import static com.OsMoDroid.IM.writeException;
import static com.OsMoDroid.LocalService.addlog;
/**
 * Created by 1 on 18.04.2017.
 */
public class SMSReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
            {
                try
                    {
                        Bundle bundle = intent.getExtras();
                        String recMsgString = "";
                        String fromAddress = "";
                        SmsMessage recMsg = null;
                        byte[] data = null;
                        if (bundle != null)
                            {
                                //---retrieve the SMS message received---
                                Object[] pdus = (Object[]) bundle.get("pdus");
                                for (int i = 0; i < pdus.length; i++)
                                    {

                                        recMsg = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                        try
                                            {
                                                data = recMsg.getUserData();
                                            }
                                        catch (Exception e)
                                            {
                                            }
                                        if (data != null)
                                            {
                                                for (int index = 0; index < data.length; ++index)
                                                    {
                                                        recMsgString += Character.toString((char) data[index]);
                                                    }
                                            }
                                        fromAddress = recMsg.getOriginatingAddress();
                                    }
                                Log.d("SMS",recMsgString);
                                addlog("SMS From "+fromAddress+ " Data"+recMsgString);
                                if(OsMoDroid.settings.getBoolean("getsms",false)&&fromAddress!=null && fromAddress.equals(OsMoDroid.settings.getString("getsmsnumber","1")))
                                    {
                                        if(true)
                                            {
                                                Intent is = new Intent(context, LocalService.class);
                                                is.putExtra("GCM","SMS|"+recMsgString);
                                                context.startService(is);
                                                abortBroadcast();
                                            }
                                    }

                            }
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                        writeException(e);
                    }
                //throw new UnsupportedOperationException("Not yet implemented");
            }
    }

