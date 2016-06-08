package com.OsMoDroid;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import com.OsMoDroid.Channel.Point;
import com.OsMoDroid.Netutil.MyAsyncTask;
//import android.R;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author dfokin
 *         Class for work with osmo server
 */
public class IM implements ResultsListener
    {
        final static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private static final int KEEP_ALIVE = 1000 * 270;
        private static final long ERROR_RECONNECT_TIMEOUT = 10 * 1000;
        private static final long ONLINE_TIMEOUT = 60 * 1000;
        private static final String RECONNECT_INTENT = "com.osmodroid.reconnect";
        private static final String GET_TOKEN_TIMEOUT_INTENT = "com.osmodroid.gettokentimeout";
        private static final String KEEPALIVE_INTENT = "com.osmodroid.keepalive";
        private static final String ONLINE_TIMEOUT_INTENT = "com.osmodroid.onlinetimeout";
        static String SERVER_IP = "osmo.mobi";
        static int SERVERPORT = 4260;
        static long sendBytes = 0;
        static long recievedBytes = 0;
        private static int RECONNECT_TIMEOUT = 1000 * 30;
        final boolean log = true;
        static  long startTraffic = 0;
        public Socket socket;
        public SSLSocket sslsocket;
        volatile public boolean authed = false;
        public BufferedReader rd;
        public PrintWriter wr;
        volatile public boolean needopensession = false;
        volatile public boolean needclosesession = false;
        volatile protected boolean running = false;
        volatile protected boolean start = false;
        volatile protected boolean connOpened = false;
        volatile protected boolean connecting = false;
        AlarmManager manager;
        PendingIntent reconnectPIntent;
        PendingIntent keepAlivePIntent;
        PendingIntent getTokenTimeoutPIntent;
        PendingIntent onlineTimeoutPIntent;
        Thread connectThread;
        Context parent;
        int mestype = 0;
        LocalService localService;
        FileOutputStream fos;
        ObjectOutputStream output = null;
        int socketRetryInt = 0;
        long connectcount = 0;
        long erorconenctcount = 0;
        private IMWriter iMWriter;
        private IMReader iMReader;
        volatile private boolean checkadressing = false;
        private String token = "";
        private String poll = "";
        private Thread readerThread;
        private Thread writerThread;
        private int workserverint = -1;
        private String workservername = "";
        private MyAsyncTask sendidtask;
        ArrayList<String> executedCommandArryaList = new ArrayList<String>();
        BroadcastReceiver onlineTimeoutReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                    {
                        LocalService.addlog("Online timeout onReceive, OsmodroidVisible="+OsMoDroid.gpslocalserviceclientVisible);
                        if(OsMoDroid.gpslocalserviceclientVisible||localService.state||localService.gcmtodolist.size()>0)
                            {
                                setOnlineTimeout();
                            }
                        else
                            {
                                LocalService.addlog("Online timeout onReceive close because not visible");
                                close();
                            }
                    }
            };
        BroadcastReceiver keepAliveReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent _)
                {
                    if (connOpened)
                        {
                            LocalService.addlog("Socket sendPing");
                            if (log)
                                {
                                    Log.d(this.getClass().getName(), " send ping");
                                }
                            sendToServer("P", false);
                        }
                }
        };
        private BroadcastReceiver bcr = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
                {
                    LocalService.addlog("Network broadcast receive:");

                    if (intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
                        {
                            Bundle extras = intent.getExtras();
                            for (String key : extras.keySet())
                                {
                                    Object value = extras.get(key);
                                    if(value!=null)
                                        {
                                            //LocalService.addlog(String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
                                        }
                                }

                            if (localService.isOnline())
                                {
                                    if (log)
                                        {
                                            Log.d(this.getClass().getName(), "BCR Network is connected");
                                        }
                                    if (log)
                                        {
                                            Log.d(this.getClass().getName(), "Running:" + running);
                                        }
                                    // Network is connected
                                    LocalService.addlog(" Network is connected, running=" + running);
                                    if (!running)
                                        {
                                            //SetAlarm();
                                            start();
                                            LocalService.addlog("Socket start by broadcast because no running");
                                        }
                                }
                            else
                                {
                                    if (log)
                                        {
                                            Log.d(this.getClass().getName(), "BCR Network is not connected");
                                        }
                                    if (log)
                                        {
                                            Log.d(this.getClass().getName(), "Running:" + running);
                                        }
                                    LocalService.addlog("Socket Network is not connected, running=" + running);
                                    if (running)
                                        {
                                            LocalService.addlog("Socket stop by broadcast because running");
                                            localService.internetnotify(false);
                                            stop();
                                        }
                                }
                        }
                }
        };
        BroadcastReceiver getTokenTimeoutReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent _)
                {
                    context.unregisterReceiver(this);
                    if (log)
                        {
                            Log.d(this.getClass().getName(), "checkaddres timeout reciever trigged");
                        }
                    LocalService.addlog("Get token timeout receiver trigged");
                    sendidtask.cancel(true);
                    checkadressing = false;
                    stop();
                    start();
                }
        };
        BroadcastReceiver reconnectReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent _)
                {
                    LocalService.addlog("Socket reconnect receiver trigged");
                    disablekeepAliveAlarm();
                    stop();
                    localService.internetnotify(false);
                    localService.refresh();
                    start();
                    context.unregisterReceiver(this);
                }
        };
        public boolean needtosendpreference=false;
        public IM(String server, int port, LocalService service)
            {
                RECONNECT_TIMEOUT = Integer.parseInt(OsMoDroid.settings.getString("timeout", "30")) * 1000;
                if(RECONNECT_TIMEOUT<5000)
                    {
                        RECONNECT_TIMEOUT=5000;
                    }
                localService = service;
                parent = service;
                manager = (AlarmManager) (parent.getSystemService(Context.ALARM_SERVICE));
                reconnectPIntent = PendingIntent.getBroadcast(parent, 0, new Intent(RECONNECT_INTENT), 0);
                keepAlivePIntent = PendingIntent.getBroadcast(parent, 0, new Intent(KEEPALIVE_INTENT), 0);
                getTokenTimeoutPIntent = PendingIntent.getBroadcast(parent, 0, new Intent(GET_TOKEN_TIMEOUT_INTENT), 0);
                onlineTimeoutPIntent = PendingIntent.getBroadcast(parent, 0, new Intent(ONLINE_TIMEOUT_INTENT), 0);
                SERVER_IP = server;
                SERVERPORT = port;
                LocalService.addlog("IM create");
                iMWriter = new IMWriter();
                writerThread = new Thread(iMWriter, "writer");
                writerThread.start();
                startTraffic=TrafficStats.getUidTxBytes(OsMoDroid.context.getApplicationInfo().uid);
            }
        public void sendToServer(String str, boolean gui)
            {
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("write", str);
                b.putBoolean("pp", str.equals("PP"));
                msg.setData(b);
                if (running)
                    {
                        if (iMWriter.handler != null)
                            {
                                String[] data = str.split("\\===");
                                ArrayList<String> cl = new ArrayList<String>();
                                for (int index = 0; index < data.length; index++)
                                    {
                                        if (data[index].contains("|"))
                                            {
                                                data[index] = data[index].substring(0, data[index].indexOf('|'));
                                            }
                                        if (!data[index].equals("PP"))
                                            {
                                                cl.add(data[index]);
                                            }
                                    }
                                executedCommandArryaList.addAll(cl);
                                LocalService.addlog("Add to command order " + cl);
                                iMWriter.handler.sendMessage(msg);
                                localService.refresh();
                            }
                        else
                            {
                                LocalService.addlog("panic! handler is null");
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), " handler is null!!!");
                                    }
                            }
                    }
                else
                    {
                        if (gui)
                            {
                                Toast.makeText(localService, localService.getString(R.string.offline_on), Toast.LENGTH_LONG).show();
                            }
                    }
            }
        public void setOnlineTimeout()
            {
                LocalService.addlog("Socket void setOnlineTimeOut");
                parent.registerReceiver(onlineTimeoutReceiver, new IntentFilter(ONLINE_TIMEOUT_INTENT));
                manager.cancel(onlineTimeoutPIntent);
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ONLINE_TIMEOUT,  onlineTimeoutPIntent);
            }
        public void disableOnlineTimeout()
            {
                LocalService.addlog("Socket void setOnlineTimeOut");
                try
                    {
                        parent.unregisterReceiver(onlineTimeoutReceiver);
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                manager.cancel(onlineTimeoutPIntent);
            }

        public void setkeepAliveAlarm()
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "void setKeepAliveAlarm");
                    }
                LocalService.addlog("Socket void setkeepalive");
                parent.registerReceiver(keepAliveReceiver, new IntentFilter(KEEPALIVE_INTENT));
                manager.cancel(keepAlivePIntent);
                manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + KEEP_ALIVE, KEEP_ALIVE, keepAlivePIntent);
            }
        public void disablekeepAliveAlarm()
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "void disableKeepAliveAlarm");
                    }
                LocalService.addlog("Socket void disablekeepalive");
                try
                    {
                        parent.unregisterReceiver(keepAliveReceiver);
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                manager.cancel(keepAlivePIntent);
            }
        synchronized public void setReconnectAlarm()
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "void setReconnectAlarm");
                    }
                localService.alertHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                        {
                            LocalService.addlog("Socket setReconnectAlarm");
                        }
                });
                parent.registerReceiver(reconnectReceiver, new IntentFilter(RECONNECT_INTENT));
                manager.cancel(reconnectPIntent);
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, reconnectPIntent);
            }
        /**
         * Выключает IM
         */
        void close()
            {
               // sendToServer("BYE", false);

                if (log)
                    {
                        Log.d(this.getClass().getName(), "void IM.close");
                    }
                LocalService.addlog("Socket void close");
                try
                    {
                        parent.unregisterReceiver(bcr);
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                try
                    {
                        parent.unregisterReceiver(reconnectReceiver);
                    }
                catch (Exception e)
                    {
                    }
                try
                    {
                        parent.unregisterReceiver(keepAliveReceiver);
                    }
                catch (Exception e)
                    {
                    }
                try
                    {
                        parent.unregisterReceiver(getTokenTimeoutReceiver);
                    }
                catch (Exception e)
                    {
                    }
                try
                    {
                     parent.unregisterReceiver(onlineTimeoutReceiver);
                    }
                catch (Exception e)
                    {
                    }
                stop();
                start = false;
            }
        ;
        public void checkaddres()
            {
                LocalService.addlog("Start get token" + ", key=" + OsMoDroid.settings.getString("newkey", ""));
                if (!checkadressing)
                    {
                        JSONObject postjson = new JSONObject();
                        ConnectivityManager connManager = (ConnectivityManager)localService.getSystemService(localService.CONNECTIVITY_SERVICE);
                        NetworkInfo netinfo =connManager.getActiveNetworkInfo();

                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        try
                            {
                                postjson.put("type", netinfo.getSubtypeName());
                        if (mWifi.isConnected())
                            {
                                WifiManager wifi = (WifiManager) localService.getSystemService(Context.WIFI_SERVICE);
                                WifiInfo wifiInfo = wifi.getConnectionInfo();
                                String wifiname = wifiInfo.getSSID();
                                String mac = wifiInfo.getMacAddress();
                                String strength = Integer.toString(wifiInfo.getRssi());
                                postjson.put("network_ssid", wifiname.replaceAll("\"", ""));
                                postjson.put("network_mac", mac);

                            }
                            }
                            catch (Exception e)
                                {

                                }
                        checkadressing = true;
                        APIcomParams params = null;
                        params = new APIcomParams("https://api.osmo.mobi/serv", "", "checkaddres");
//                        if (OsMoDroid.settings.getString("p", "").equals(""))
//                            {
//
//                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
//                                    {
//                                        params = new APIcomParams("https://api.osmo.mobi/init?device=" + OsMoDroid.settings.getString("newkey", "")
//                                                //+"&protocol=2"
//                                                + "&app=" +  OsMoDroid.app_code
//                                                //+"&version="+localService.getversion()
//                                                + "&network="+postjson.toString()
//                                                , "", "checkaddres");
//
//                                    }
//                                else
//                                    {
//                                        params = new APIcomParams("http://api.osmo.mobi/init?device=" + OsMoDroid.settings.getString("newkey", "")
//                                                //+"&protocol=2"
//                                                + "&app=" +  OsMoDroid.app_code + "&dinosaur=yes"
//                                                //+"&version="+localService.getversion()
//                                                + "&network="+postjson.toString()
//                                                , "", "checkaddres");
//                                    }
//                            }
//                        else
//                            {
//                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
//                                    {
//                                        params = new APIcomParams("https://api.osmo.mobi/init?device=" + OsMoDroid.settings.getString("newkey", "")
//                                                //+"&protocol=1"
//                                                + "&user=" + OsMoDroid.settings.getString("p", "")
//                                                + "&app=" +  OsMoDroid.app_code
//                                                //+"&version="+localService.getversion()
//                                                + "&network="+postjson.toString()
//                                                , "", "checkaddres");
//                                    }
//                                else
//                                    {
//                                        params = new APIcomParams("http://api.osmo.mobi/init?device=" + OsMoDroid.settings.getString("newkey", "")
//                                                //+"&protocol=1"
//                                                + "&user=" + OsMoDroid.settings.getString("p", "")
//                                                + "&app=" +  OsMoDroid.app_code  + "&dinosaur=yes"
//                                                //+"&version="+localService.getversion()
//                                                + "&network="+postjson.toString()
//                                                , "", "checkaddres");
//                                    }
//                            }
                        sendidtask = new Netutil.MyAsyncTask(this);
                        sendidtask.execute(params);
                        Log.d(getClass().getSimpleName(), "get token start to execute");
                        parent.registerReceiver(getTokenTimeoutReceiver, new IntentFilter(GET_TOKEN_TIMEOUT_INTENT));
                        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, getTokenTimeoutPIntent);
                    }
            }
        void start()
            {
                start = true;
                if (log)
                    {
                        Log.d(this.getClass().getName(), "void IM.start");
                    }
                LocalService.addlog("Socket void start");
                running = true;
                connecting = true;
                localService.refresh();
                iMReader = new IMReader();
                connectThread = new Thread(new IMConnect(), "connecter");
                readerThread = new Thread(iMReader, "reader");
                connectThread.setPriority(Thread.MIN_PRIORITY);
                readerThread.setPriority(Thread.MIN_PRIORITY);
                writerThread.setPriority(Thread.MIN_PRIORITY);
                if(workserverint==-1)
                    {
                        checkaddres();
                    }
                else
                    {
                        connectThread.start();
                    }
                //
                parent.registerReceiver(bcr, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
                setOnlineTimeout();

            }
        void stop()
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "void IM.stop");
                    }
                LocalService.addlog("Socket void stop");
                executedCommandArryaList.clear();
                running = false;
                connOpened = false;
                authed = false;
                connecting = false;
                localService.alertHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                        {
                            ondisconnect();
                        }
                });
                if (socket != null)
                    {
                        try
                            {
                                socket.close();
                            }
                        catch (IOException e)
                            {
                                LocalService.addlog("exeption close socket " + e.getMessage());
                                e.printStackTrace();
                            }
                    }
                manager.cancel(getTokenTimeoutPIntent);
                manager.cancel(reconnectPIntent);
                localService.refresh();
            }
        public void addtoDeviceChat(int u, JSONObject jo)
            {
                // IM:7909|[{"u":"17","from":"45694","text":"xcvxcvz","time":"2015-04-11 22:35:18"}]
                ChatMessage m = new ChatMessage();
                m.u = jo.optInt("u");
                m.text = Netutil.unescape(jo.optString("text"));
                m.time = OsMoDroid.sdf.format(new Date(jo.optLong("time")*1000));
                m.from = jo.optString("from");
                for (Device dev : LocalService.deviceList)
                    {
                        if ((dev.u) == u)
                            {
                                if (!dev.messagesstringList.contains(m))
                                    {
                                        dev.messagesstringList.add(m);
                                    }
                            }
                    }
                if (LocalService.currentDevice != null && u == LocalService.currentDevice.u)
                    {
                        localService.alertHandler.post(new Runnable()
                        {
                            public void run()
                                {
                                    if (LocalService.chatmessagesAdapter != null)
                                        {
                                            LocalService.chatmessagesAdapter.notifyDataSetChanged();
                                        }
                                }
                        });
                    }
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putInt("deviceU", u);
                msg.setData(b);
                localService.alertHandler.sendMessage(msg);
            }
        private void addToChannelChat(int channelU, JSONObject jo, boolean silent) throws JSONException
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "type=chch");
                    }
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Сообщение в чат канала " + jo);
                        //LocalService.addlog("Сообщение в чат канала " + jo);
                    }
                ChatMessage m = new ChatMessage();
                m.u = jo.optInt("u");
                m.text = Netutil.unescape(jo.optString("text"));
                m.time = OsMoDroid.sdf.format(new Date(jo.optLong("time")*1000));
                m.name = jo.optString("name");
                m.type = jo.optInt("type");
                String fromDevice = "Незнамо кто";
               // LocalService.addlog("Размер спсика групп " + LocalService.channelList.size());
                for (final Channel channel : LocalService.channelList)
                    {
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "chanal nest" + channel.name);
                            }
                        if (channelU == channel.u)
                            {
                                if (!channel.messagesstringList.contains(m))
                                    {
                                        if (!silent&&channel.gu!=jo.optInt("gu"))
                                            {
                                                fromDevice = jo.optString("name");
                                                Intent intent = new Intent(localService, GPSLocalServiceClient.class).putExtra("channelpos", channel.u);
                                                intent.setAction("channelchat");
                                                PendingIntent contentIntent = PendingIntent.getActivity(localService, 333, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                                Long when = System.currentTimeMillis();
                                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                                                        localService.getApplicationContext())
                                                        .setWhen(when)
                                                        .setContentText(fromDevice + ": " + jo.optString("text"))
                                                        .setContentTitle(channel.name)
                                                        .setSmallIcon(R.drawable.white9696)
                                                        .setAutoCancel(true)
                                                        .setDefaults(Notification.DEFAULT_LIGHTS)
                                                        .setContentIntent(contentIntent);
                                                if (!OsMoDroid.settings.getBoolean("silentnotify", false))
                                                    {
                                                        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
                                                    }
                                                Notification notification = notificationBuilder.build();
                                                if(OsMoDroid.settings.getBoolean("chatnotify", true)|| m.type!=0)
                                                    {
                                                        LocalService.mNotificationManager.notify(OsMoDroid.mesnotifyid + channel.u, notification);
                                                        if (LocalService.channelsmessagesAdapter != null && LocalService.currentChannel != null && LocalService.currentChannel.u == channel.u && LocalService.chatVisible)
                                                            {
                                                                LocalService.mNotificationManager.cancel(OsMoDroid.mesnotifyid+channel.u);
                                                            }
                                                    }


                                            }
                                        channel.messagesstringList.add(m);
                                        Collections.sort(channel.messagesstringList);
                                        if (LocalService.channelsmessagesAdapter != null && LocalService.currentChannel != null && LocalService.currentChannel.u == channel.u)
                                            {
                                                LocalService.channelsmessagesAdapter.notifyDataSetChanged();
                                            }
                                    }
                            }
                    }
            }
        synchronized void parseEx(String toParse,boolean gcm) throws JSONException
            {
                //LocalService.addlog("recieve " + toParse);
                if (log)
                    {
                        Log.d(this.getClass().getName(), "recive " + toParse);
                    }

                if (toParse.equals("P|"))
                    {
                        LocalService.addlog("recieve pong");
                        return;
                    }
                JSONObject jsonObject;
                JSONObject jo = new JSONObject();
                JSONArray ja = new JSONArray();
                String command = "";
                String param = "";
                String addict = "";
                try
                    {
                        command = toParse.substring(0, toParse.indexOf('|'));
                    }
                catch (Exception e1)
                    {
                        command = toParse;
                    }
                if (command.indexOf(':') != -1)
                    {
                        param = command.substring(command.indexOf(':') + 1);
                        command = command.substring(0, command.indexOf(':'));
                    }
                addict = toParse.substring(toParse.indexOf('|') + 1);

                try
                    {
                        jo = new JSONObject(addict);
                    }
                catch (JSONException e)
                    {
                        try
                            {
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "не JSONO ");
                                    }
                                ja = new JSONArray(addict);
                            }
                        catch (JSONException e1)
                            {
                                // TODO Auto-generated catch block
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "не JSONA ");
                                    }
                            }
                    }
                if(!gcm)
                    {
                    parseremovefromcommandlist(command, param);
                }

                if (jo.has("error"))
                    {
                        if(OsMoDroid.gpslocalserviceclientVisible)
                            {
                                final String str = jo.optString("error_description");
                                Toast.makeText(localService, str, Toast.LENGTH_LONG).show();
                            }
                    }
                else
                    {
                        parsedata(jo, ja, command, param, addict);
                    }
            }
        private void parseremovefromcommandlist(String command, String param)
            {
                if (!running)
                    {
                        running = true;
                    }
                Iterator<String> comIter = executedCommandArryaList.iterator();
                while (comIter.hasNext())
                    {
                        String str = comIter.next();
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "ExecutedListItem: " + str);
                            }
                        if (str.equals(command + ':' + param) || str.equals(command))
                            {
                                comIter.remove();
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "ExecutedListItem removed: " + str);
                                    }
                                LocalService.addlog("ExecutedListItem removed: " + str);
                            }
                    }
                if (log)
                    {
                        Log.d(this.getClass().getName(), "ExecuteList=" + executedCommandArryaList.toString());
                    }
                LocalService.addlog("ExecuteList=" + executedCommandArryaList.toString());
                if (executedCommandArryaList.size() == 0)
                    {
                        LocalService.addlog("Cancel reconnect alarm - no commands in order");
                        manager.cancel(reconnectPIntent);
                        localService.refresh();
                    }
            }
        private void parsedata(JSONObject jo, JSONArray ja, String command, String param, String addict) throws JSONException
            {

                JSONObject jsonObject;
//                if (command.equals("INIT"))
                if (command.equals("AUTH"))
                    {
                        if (!jo.has("error"))
                            {
                                JSONObject postjson = new JSONObject();
                                ConnectivityManager connManager = (ConnectivityManager)localService.getSystemService(localService.CONNECTIVITY_SERVICE);
                                NetworkInfo netinfo =connManager.getActiveNetworkInfo();

                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                try
                                    {
                                        postjson.put("type", netinfo.getSubtypeName());
                                        if (mWifi.isConnected())
                                            {
                                                WifiManager wifi = (WifiManager) localService.getSystemService(Context.WIFI_SERVICE);
                                                WifiInfo wifiInfo = wifi.getConnectionInfo();
                                                String wifiname = wifiInfo.getSSID();
                                                String mac = wifiInfo.getMacAddress();
                                                String strength = Integer.toString(wifiInfo.getRssi());
                                                postjson.put("network_ssid", wifiname.replaceAll("\"", ""));
                                                postjson.put("network_mac", mac);

                                            }
                                    }
                                catch (Exception e)
                                    {

                                    }
                                sendToServer("NET|"+postjson.toString(),false);
                                if (jo.optInt("motd") > OsMoDroid.settings.getInt("modtime", 0))
                                    {
                                        sendToServer("MD", false);
                                    }
                                else
                                    {
                                        localService.motd = OsMoDroid.settings.getString("motd", "");
                                    }
                                if (jo.optInt("pro") == 1)
                                    {
                                        localService.pro = true;
                                    }
                                else
                                    {
                                        localService.pro = false;
                                    }
                                OsMoDroid.editor.putString("device", jo.optString("id"));
                                OsMoDroid.editor.putString("tracker_id", jo.optString("id"));
                                OsMoDroid.editor.putString("motdtime", jo.optString("motd"));
                                OsMoDroid.editor.putBoolean("pro", localService.pro);
                                OsMoDroid.editor.commit();
                                authed = true;
                                if(jo.has("uid"))
                                    {
                                        if(jo.optInt("uid")>0)
                                            {
                                                OsMoDroid.editor.putString("u", jo.optString("name", ""));
                                                OsMoDroid.editor.putString("p", jo.optString("uid", ""));
                                                OsMoDroid.editor.commit();

                                            }
                                        else
                                            {
                                                OsMoDroid.editor.remove("p");
                                                OsMoDroid.editor.remove("u");
                                                OsMoDroid.editor.commit();

                                            }
                                        localService.refresh();
                                    }


                                if (needopensession)
                                    {
                                        sendToServer("TO", false);
                                    }
                                if (needclosesession)
                                    {
                                        sendToServer("TC", false);
                                    }
                                if(OsMoDroid.settings.getBoolean("needsendgcmregid",true))
                                    {
                                        LocalService.myIM.sendToServer("GCM|" +OsMoDroid.tmpGCMRegId, false);
                                    }
                                if(needclosesession)
                                    {
                                        sendToServer("DCU",false);
                                    }
                                if (LocalService.channelList.isEmpty())
                                    {
                                        sendToServer("GROUP", false);
                                    }
                                else
                                    {
                                        for (String s: LocalService.gcmtodolist)
                                            {
                                                parseEx(s,true);
                                            }
                                        LocalService.gcmtodolist.clear();
                                        LocalService.connectcompleted =true;
                                        localService.saveObject(LocalService.gcmtodolist, OsMoDroid.GCMTODOLIST);
                                    }



//                                if (LocalService.deviceList.isEmpty())
//                                    {
//                                        sendToServer("DEVICE", false);
//                                    }
                                setkeepAliveAlarm();
                                localService.internetnotify(true);
                                if (!OsMoDroid.settings.getBoolean("subscribebackground", false))
                                    {
                                        if(!OsMoDroid.gpslocalserviceclientVisible)
                                        {
                                            sendToServer("PG:-1", false);
                                        }
                                    }

                                if (jo.has("group"))
                                    {
                                        if (jo.optInt("group") == 1)
                                            {
                                                localService.globalsend = true;
                                            }
                                        if (jo.optInt("group") == 0)
                                            {
                                                localService.globalsend = false;
                                            }
                                        localService.refresh();
                                    }
                                if (jo.has("sos"))
                                    {
                                        if (jo.optInt("sos") == 1)
                                            {
                                                localService.sos = true;
                                            }
                                       else
                                            {
                                                localService.sos = false;
                                            }
                                        localService.refresh();
                                    }
                            }
                        else
                            {

                            if (jo.optInt("error") == 10 || jo.optInt("error") == 100 || jo.optString("token").equals("false"))
                                {
                                    localService.internetnotify(false);
                                    close();
                                    localService.notifywarnactivity(LocalService.unescape(jo.optString("error_description")), false, OsMoDroid.NOTIFY_NO_DEVICE);
                                    //localService.motd=LocalService.unescape(result.Jo.optString("error_description"));
                                    localService.sendid();
                                    localService.refresh();
                                }
                            else if (jo.optInt("error") == 67 || jo.optInt("error") == 68 || jo.optInt("error") == 69)
                                {
                                    close();
                                    localService.notifywarnactivity(LocalService.unescape(jo.optString("error_description")), false, OsMoDroid.NOTIFY_EXPIRY_USER);
                                    localService.motd = LocalService.unescape(jo.optString("error_description"));
                                    localService.refresh();
                                }
                            else if (jo.optInt("error") == 21 )
                                {
                                    close();
                                    localService.notifywarnactivity(LocalService.unescape(jo.optString("error_description")), true,0);
                                    localService.motd = LocalService.unescape(jo.optString("error_description"));
                                    localService.refresh();
                                }


                            }
                        localService.refresh();
                    }
                if (command.equals("MD"))
                    {
                        localService.motd = addict;
                        OsMoDroid.editor.putString("modt", addict);
                        OsMoDroid.editor.commit();
                        localService.refresh();
                    }
                if (command.equals("GS"))
                    {
                        if (param.equals("1"))
                            {
                                localService.globalsend = true;
                                localService.refresh();
                            }
                        if (param.equals("-1"))
                            {
                                localService.globalsend = false;
                                localService.refresh();
                            }
                    }
                if (command.equals("SOS"))
                    {
                        if (addict.equals("1"))
                            {
                                localService.sos = true;
                                localService.refresh();
                            }
                        if (addict.equals("-1"))
                            {
                                localService.sos = false;
                                localService.refresh();
                            }
                    }
                if (command.equals("GA"))
                    {
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.u == Integer.parseInt(param))
                                    {
                                        ch.updChannel(jo);
                                        ch.send = true;
                                    }
                            }
                        if (LocalService.channelsAdapter != null)
                            {
                                LocalService.channelsAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write group list to file");
                            }
                        localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
                        //sendToServer("GROUP", false);
                    }
                if (command.equals("GD"))
                    {
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.u == Integer.parseInt(param))
                                    {
                                        ch.send = false;
                                    }
                            }
                        if (LocalService.channelsAdapter != null)
                            {
                                LocalService.channelsAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write group list to file");
                            }
                        localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
                    }
                if (command.equals("GC"))
                    {
                        for (int k = 0; k < ja.length(); k++)
                            {
                                try
                                    {
                                        addToChannelChat(Integer.parseInt(param), ja.getJSONObject(k), true);
                                    }
                                catch (Exception e)
                                    {
                                        writeException(e);
                                        e.printStackTrace();
                                    }
                            }
                    }
                // IM:7909|[{"u":"17","from":"45694","text":"xcvxcvz","time":"2015-04-11 22:35:18"}]
                if (command.equals("IM"))
                    {
                        for (int k = 0; k < ja.length(); k++)
                            {
                                try
                                    {
                                        addtoDeviceChat(Integer.parseInt(param), ja.getJSONObject(k));
                                    }
                                catch (Exception e)
                                    {
                                        writeException(e);
                                        e.printStackTrace();
                                    }
                            }
                    }
                //recive IMP|["46191","\u043f\u0432\u0438\u044c\u0431\u043b\u0440","2015-04-16 22:52:13"]
                if (command.equals("IMP"))
                    {
                        addict = addict.replace("\"", "");
                        addict = addict.replace("[", "");
                        addict = addict.replace("]", "");
                        String[] data = addict.split(",");
                        JSONObject j = new JSONObject();
                        j.put("from", data[0]);
                        j.put("text", data[1]);
                        j.put("time", data[2]);
                        addtoDeviceChat(Integer.parseInt(data[0]), new JSONObject(addict));
                    }
                if (command.equals("GRPA"))
                    {
                        sendToServer("GROUP", false);
                    }
                if (command.equals("TO"))
                    {
                        localService.sessionstarted = true;
                        sendBytes = 0;
                        recievedBytes = 0;
                        connectcount = 0;
                        erorconenctcount = 0;
                        needopensession = false;
                        OsMoDroid.editor.putString("viewurl", "https://osmo.mobi/s/" + jo.optString("url"));
                        OsMoDroid.editor.commit();
                        localService.refresh();
                    }
                if (command.equals("TC"))
                    {
                        localService.sessionstarted = false;
                        needclosesession = false;
                        OsMoDroid.editor.putString("viewurl", "");
                        OsMoDroid.editor.commit();
                        localService.refresh();
                    }
                if (command.equals("T"))
                    {
                        localService.sendcounter++;
                        localService.sending = "";
                        if (localService.sendingbuffer.size() == 0 && localService.buffer.size() != 0)
                            {
                                localService.sendingbuffer.addAll(localService.buffer);
                                localService.buffer.clear();
                                sendToServer("B|" + new JSONArray(localService.sendingbuffer), false);
                            }
                        if (localService.sendsound && !localService.mayak)
                            {
                                localService.soundPool.play(localService.sendpalyer, 1f, 1f, 1, 0, 1f);
                                localService.mayak = false;
                            }
                        String time = OsMoDroid.sdf3.format(new Date(System.currentTimeMillis()));
                        localService.sendresult = time + " " + localService.getString(R.string.succes);
                        localService.refresh();
                        return;
                    }
                if (command.equals("B"))
                    {
                        localService.buffercounter = localService.buffercounter - localService.sendingbuffer.size();
                        localService.sendcounter = localService.sendcounter + localService.sendingbuffer.size();
                        localService.sendingbuffer.clear();
                        localService.refresh();
                    }
                if (command.equals("PP"))
                    {
                        sendToServer("PP", false);
                    }
                if (command.equals("GCM"))
                    {
                        OsMoDroid.editor.putString("GCMRegId", OsMoDroid.tmpGCMRegId);
                        OsMoDroid.editor.putBoolean("needsendgcmregid", false);
                        OsMoDroid.editor.commit();
                    }
                if(command.equals("DCU"))
                    {
                        needtosendpreference=false;
                    }
                if (command.equals("RC"))
                    {
                        if (param.equals("PP"))
                            {
                                sendToServer("PP", false);
                            }
                        if (param.equals(OsMoDroid.SOS))
                            {
                                Intent dialogIntent = new Intent(localService, SosActivity.class);
                                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                dialogIntent.putExtra("message",addict);
                                localService.startActivity(dialogIntent);
                            }
                        if (param.equals(OsMoDroid.SOS_OFF))
                            {
                                localService.sendBroadcast(new Intent("closesos"));
                            }

                        if (param.equals(OsMoDroid.TRACKER_GCM_ID))
                            {
                                if(OsMoDroid.tmpGCMRegId.equals(""))
                                    {
                                        sendToServer("GCM|" + OsMoDroid.settings.getString("GCMRegId", "no"), false);
                                        sendToServer("RCR:" + OsMoDroid.TRACKER_GCM_ID + "|1", false);
                                    }
                                else
                                    {
                                        sendToServer("GCM|" +OsMoDroid.tmpGCMRegId, false);
                                        sendToServer("RCR:" + OsMoDroid.TRACKER_GCM_ID + "|1", false);
                                    }
                            }


                        if (param.equals(OsMoDroid.REFRESH_GROUPS))
                            {
                                sendToServer("GROUP",false);
                                sendToServer("RCR:" + OsMoDroid.REFRESH_GROUPS + "|1", false);
                            }
                        if (param.equals(OsMoDroid.REFRESH_DEVICES))
                            {
                                sendToServer("DEVICE",false);
                                sendToServer("RCR:" + OsMoDroid.REFRESH_DEVICES + "|1", false);
                            }
                        if (param.equals(OsMoDroid.TRACKER_SESSION_START))
                            {
                                if(!localService.state)
                                    {
                                        localService.startServiceWork(true);
                                    }
                                sendToServer("RCR:" + OsMoDroid.TRACKER_SESSION_START + "|1", false);
                            }
                        if (param.equals(OsMoDroid.TRACKER_SESSION_STOP))
                            {
                                localService.stopServiceWork(true);
                                sendToServer("RCR:" + OsMoDroid.TRACKER_SESSION_STOP + "|1", false);
                            }
                        if (param.equals(OsMoDroid.TRACKER_SESSION_CONTINUE))
                        {
                            localService.startServiceWork(false);
                            sendToServer("RCR:" + OsMoDroid.TRACKER_SESSION_PAUSE + "|1", false);
                        }
                        if (param.equals(OsMoDroid.TRACKER_SESSION_PAUSE))
                            {
                                localService.stopServiceWork(false);
                                sendToServer("RCR:" + OsMoDroid.TRACKER_SESSION_PAUSE + "|1", false);
                            }
                        if (param.equals(OsMoDroid.TTS))
                            {
                                if (OsMoDroid.settings.getBoolean("ttsremote", false) && localService.tts != null)
                                    {
                                        localService.tts.speak(addict, TextToSpeech.QUEUE_ADD, null);
                                    }
                            }
                        if (param.equals(OsMoDroid.ALARM_ON))
                            {
                                localService.playAlarmOn();
                                sendToServer("RCR:" + OsMoDroid.ALARM_ON + "|1", false);
                            }
                        if (param.equals(OsMoDroid.ALARM_OFF))
                            {
                                localService.playAlarmOff();
                                sendToServer("RCR:" + OsMoDroid.ALARM_OFF + "|1", false);
                            }
                        if (param.equals(OsMoDroid.SIGNAL_ON))
                            {
                                localService.enableSignalisation();
                                sendToServer("RCR:" + OsMoDroid.SIGNAL_ON + "|1", false);
                            }
                        if (param.equals(OsMoDroid.SIGNAL_OFF))
                            {
                                localService.disableSignalisation();
                                sendToServer("RCR:" + OsMoDroid.SIGNAL_OFF + "|1", false);
                            }
                        if (param.equals(OsMoDroid.TRACKER_BATTERY_INFO))
                            {
                                try
                                    {
                                        localService.batteryinfo(localService);
                                    }
                                catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                        writeException(e);
                                    }
                            }
                        if (param.equals(OsMoDroid.TRACKER_SATELLITES_INFO))
                            {
                                try
                                    {
                                        localService.satelliteinfo(localService);
                                    }
                                catch (JSONException e)
                                    {
                                        writeException(e);
                                        e.printStackTrace();
                                    }
                            }
                        if (param.equals(OsMoDroid.TRACKER_SYSTEM_INFO))
                            {
                                try
                                    {
                                        localService.systeminfo(localService);
                                    }
                                catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                            }
                        if (param.equals(OsMoDroid.TRACKER_WIFI_INFO))
                            {
                                try
                                    {
                                        localService.wifiinfo(localService);
                                    }
                                catch (JSONException e)
                                    {
                                        writeException(e);
                                        e.printStackTrace();
                                    }
                            }
                        if (param.equals(OsMoDroid.TRACKER_WIFI_OFF))
                            {
                                localService.wifioff(localService);
                            }
                        if (param.equals(OsMoDroid.TRACKER_WIFI_ON))
                            {
                                localService.wifion(localService);
                            }
                        if (param.equals(OsMoDroid.TRACKER_VIBRATE))
                            {
                                localService.vibrate(localService, 3000);
                            }
                        if (param.equals(OsMoDroid.TRACKER_EXIT))
                            {
                                sendToServer("RCR:" + OsMoDroid.TRACKER_EXIT + "|1", false);
                                LocalService.gcmtodolist.clear();
                                LocalService.connectcompleted =true;
                                localService.saveObject(LocalService.gcmtodolist, OsMoDroid.GCMTODOLIST);
                                localService.stopSelf();
                                System.exit(0);
                            }
                        if (param.equals(OsMoDroid.TRACKER_GET_PREFS))
                            {
                                localService.getpreferences(localService);
                            }
                        if (param.equals(OsMoDroid.TRACKER_SET_PREFS))
                            {
                                localService.setpreferences(jo,localService);
                            }
                        if (param.equals(OsMoDroid.WHERE))
                            {
                                sendToServer("RCR:" + OsMoDroid.WHERE + "|1", false);
                                localService.where = true;
                                if (!localService.state)
                                    {
                                        localService.alertHandler.post(new Runnable()
                                        {
                                            public void run()
                                                {
                                                    localService.myManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, localService, null);

                                                    if (log)
                                                        {
                                                            Log.d(this.getClass().getName(), "подписались на GPS");
                                                        }
                                                }
                                        });
                                    }
                                localService.alertHandler.postDelayed(new Runnable()
                                {
                                    public void run()
                                        {
                                            localService.myManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localService, null);
                                            if (log)
                                                {
                                                    Log.d(this.getClass().getName(), "подписались на NETWORK");
                                                }
                                        }
                                }, 30000);
                                localService.alertHandler.postDelayed(new Runnable()
                                {
                                    public void run()
                                        {
                                            if (localService.state)
                                                {
                                                    localService.myManager.removeUpdates(localService);
                                                    localService.requestLocationUpdates();
                                                    if (log)
                                                        {
                                                            Log.d(this.getClass().getName(), "Переподписались");
                                                        }
                                                }
                                            else
                                                {
                                                    localService.myManager.removeUpdates(localService);
                                                    if (log)
                                                        {
                                                            Log.d(this.getClass().getName(), "Отписались");
                                                        }
                                                }
                                        }
                                }, 90000);
                            }
                    }
                if (command.equals("MOTD"))
                    {
                        localService.motd = LocalService.unescape(addict);
                        OsMoDroid.editor.putString("startmessage", LocalService.unescape(addict));
                        OsMoDroid.editor.commit();
                        localService.refresh();
                    }
                if (command.equals("GE"))
                    {
                        sendToServer("GROUP", false);
                    }
                if (command.equals("DS"))
                    {
                        sendToServer("DEVICE", false);
                    }
                if (command.equals("DSS"))
                    {
                        sendToServer("DEVICE", false);
                    }
                if (command.equals("DSA"))
                    {
                        sendToServer("DEVICE", false);
                    }
                if (command.equals("DSD"))
                    {
                        sendToServer("DEVICE", false);
                    }
                if (command.equals("DEVICE"))
                    {
                        Iterator<Device> i = LocalService.deviceList.iterator();
                        while (i.hasNext())
                            {
                                Device dev = i.next(); // must be called before you can call i.remove()
                                boolean exist = false;
                                for (int k = 0; k < ja.length(); k++)
                                    {
                                        try
                                            {
                                                jsonObject = ja.getJSONObject(k);
                                                if (dev.u == jsonObject.optInt("u"))
                                                    {
                                                        exist = true;
                                                        dev.name = jsonObject.optString("name");
                                                        dev.tracker_id = jsonObject.optString("id");
                                                        dev.subscribed = jsonObject.has("sub");
                                                        dev.u = jsonObject.optInt("u");
                                                        dev.online = jsonObject.optInt("online");
                                                        dev.state = jsonObject.optInt("state");
                                                        if (jsonObject.has("data"))
                                                            {
                                                                if (jsonObject.optJSONObject("data") != null && !jsonObject.optJSONObject("data").optString("color").equals(""))
                                                                    {
                                                                        String color = jsonObject.optJSONObject("data").optString("color");
                                                                        try
                                                                            {
                                                                                dev.color = Color.parseColor(color);
                                                                            }
                                                                        catch (Exception e)
                                                                            {
                                                                                writeException(e);
                                                                                e.printStackTrace();
                                                                            }
                                                                        if (log)
                                                                            {
                                                                                Log.d(this.getClass().getName(), "detected color " + color);
                                                                            }
                                                                    }
                                                            }
                                                    }
                                            }
                                        catch (Exception e)
                                            {
                                                e.printStackTrace();
                                                writeException(e);
                                            }
                                    }
                                if (!exist)
                                    {
                                        i.remove();
                                    }
                            }
                        for (int n = 0; n < ja.length(); n++)
                            {
                                try
                                    {
                                        jsonObject = ja.getJSONObject(n);
                                        boolean exist = false;
                                        Device newdev = new Device();
                                        newdev.name = jsonObject.optString("name");
                                        newdev.tracker_id = jsonObject.optString("id");
                                        newdev.subscribed = jsonObject.has("sub");
                                        newdev.u = jsonObject.optInt("u");
                                        newdev.online = jsonObject.optInt("online");
                                        newdev.state = jsonObject.optInt("state");
                                        if (jsonObject.has("data"))
                                            {
                                                if (jsonObject.optJSONObject("data") != null && !jsonObject.optJSONObject("data").optString("color").equals(""))
                                                    {
                                                        String color = jsonObject.optJSONObject("data").optString("color");
                                                        try
                                                            {
                                                                newdev.color = Color.parseColor(color);
                                                            }
                                                        catch (Exception e)
                                                            {
                                                                writeException(e);
                                                                e.printStackTrace();
                                                            }
                                                        if (log)
                                                            {
                                                                Log.d(this.getClass().getName(), "detected color " + color);
                                                            }
                                                    }
                                            }
                                        for (Device dev : LocalService.deviceList)
                                            {
                                                if (newdev.u == dev.u)
                                                    {
                                                        exist = true;
                                                    }
                                            }
                                        if (!exist)
                                            {
                                                LocalService.deviceList.add(newdev);
                                                if (!newdev.tracker_id.equals(OsMoDroid.settings.getString("tracker_id", "")))
                                                    {
                                                    }
                                            }
                                    }
                                catch (Exception e)
                                    {
                                        e.printStackTrace();
                                        writeException(e);
                                    }
                            }
                        Collections.sort(LocalService.deviceList);
                        int mypos = -1;
                        for (Device dev : LocalService.deviceList)
                            {
                                if (dev.tracker_id.equals(OsMoDroid.settings.getString("tracker_id", "")))
                                    {
                                        mypos = LocalService.deviceList.indexOf(dev);
                                    }
                            }
                        if (mypos != -1)
                            {
                                Device mydev = LocalService.deviceList.get(mypos);
                                LocalService.deviceList.remove(mypos);
                                LocalService.deviceList.add(0, mydev);
                            }
                        if (LocalService.deviceAdapter != null)
                            {
                                LocalService.deviceAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write device list to file");
                            }
                        localService.saveObject(LocalService.deviceList, OsMoDroid.DEVLIST);
                        //sendToServer("PD");
                    }

                if (command.equals("GROUP"))
                    {
                        ArrayList<Channel> recievedChannelList = new ArrayList<Channel>();
                        for (int i = 0; i < ja.length(); i++)
                            {
                                try
                                    {
                                        jsonObject = ja.getJSONObject(i);
                                        if (!jsonObject.getString("id").equals("null") && !jsonObject.getString("u").equals("null"))
                                            {
                                                for (Channel ch : LocalService.channelList)
                                                    {
                                                        if (ch.u == Integer.parseInt(jsonObject.optString("u")))
                                                            {
                                                                ch.updChannel(jsonObject);
                                                            }
                                                    }
                                                Channel ch = new Channel();
                                                ch.updChannel(jsonObject);
                                                recievedChannelList.add(ch);
                                            }
                                    }
                                catch (Exception e)
                                    {
                                        e.printStackTrace();
                                        writeException(e);
                                    }
                            }
                        LocalService.channelList.retainAll(recievedChannelList);
                        recievedChannelList.removeAll(LocalService.channelList);
                        LocalService.channelList.addAll(recievedChannelList);
                        if (LocalService.channelsAdapter != null)
                            {
                                LocalService.channelsAdapter.notifyDataSetChanged();
                            }
                        if (LocalService.channelsDevicesAdapter != null)
                            {
                                LocalService.channelsDevicesAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write group list to file");
                            }
                        localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.send)
                                    {
                                        sendToServer("GC:" + ch.u, false);
                                    }
                            }
                        //sendToServer("PG");
                        for (String s: LocalService.gcmtodolist)
                            {
                                parseEx(s,true);
                            }
                        LocalService.gcmtodolist.clear();
                        LocalService.connectcompleted =true;
                        localService.saveObject(LocalService.gcmtodolist, OsMoDroid.GCMTODOLIST);
                    }
                if (command.equals("GL"))
                    {
                        Channel chToDel = null;
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.u==Integer.parseInt(param))
                                    {
                                        chToDel = ch;
                                    }
                            }
                        if (chToDel != null)
                            {
                                LocalService.channelList.remove(chToDel);
                            }
                        if (LocalService.channelsAdapter != null)
                            {
                                LocalService.channelsAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write group list to file");
                            }
                        localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
                    }
                if (command.equals("LINK"))
                    {
                        LocalService.simlimkslist.clear();
                        for (int i = 0; i < ja.length(); i++)
                            {
                                try
                                    {
                                        jsonObject = ja.getJSONObject(i);
                                        PermLink pl = new PermLink();
                                        pl.u = jsonObject.getInt("u");
                                        pl.url = "https://osmo.mobi/u/" + jsonObject.optString("url");
                                        LocalService.simlimkslist.add(pl);
                                    }
                                catch (JSONException e)
                                    {
                                        writeException(e);
                                        e.printStackTrace();
                                    }
                            }
                        if (LocalService.simlinksadapter != null)
                            {
                                LocalService.simlinksadapter.notifyDataSetChanged();
                            }
                    }

                if (command.equals("LNKD"))
                    {
                        int positiontodel = -1;
                        for (PermLink pl : LocalService.simlimkslist)
                            {
                                if (Integer.parseInt(param) == pl.u)
                                    {
                                        positiontodel = LocalService.simlimkslist.indexOf(pl);
                                    }
                            }
                        if (positiontodel != -1)
                            {
                                LocalService.simlimkslist.remove(positiontodel);
                                if (LocalService.simlinksadapter != null)
                                    {
                                        LocalService.simlinksadapter.notifyDataSetChanged();
                                    }
                            }
                    }
                if (command.equals("LNKA"))
                    {
                        PermLink pl = new PermLink();
                        try
                            {
                                pl.u = jo.getInt("u");
                                pl.url = "https://osmo.mobi/u/" + jo.getString("url");
                                LocalService.simlimkslist.add(pl);
                                if (LocalService.simlinksadapter != null)
                                    {
                                        LocalService.simlinksadapter.notifyDataSetChanged();
                                    }
                            }
                        catch (JSONException e)
                            {
                                e.printStackTrace();
                                writeException(e);
                            }
                    }
                if(command.equals("GPU"))
                    {
                        if(jo.has("users"))
                            {
                                JSONArray users = jo.optJSONArray("users");
                                for (int i = 0; i < users.length(); i++)
                                    {
                                        try
                                            {
                                                jsonObject = users.getJSONObject(i);
                                                for (Channel ch: LocalService.channelList)
                                                    {
                                                        if (ch.type == 2)
                                                            for (Device dev : ch.deviceList)
                                                                {
                                                                    if (dev.u == jsonObject.optInt("u"))
                                                                        {
                                                                            if (jsonObject.has("state"))
                                                                                {
                                                                                    if (dev.state != 1 && jsonObject.getInt("state") == 1)
                                                                                        {
                                                                                            notifydevicemonitoring(dev, true);
                                                                                        }
                                                                                    if (dev.state == 1 && jsonObject.getInt("state") == 0)
                                                                                        {
                                                                                            notifydevicemonitoring(dev, false);
                                                                                        }
                                                                                    dev.state = jsonObject.getInt("state");
                                                                                }
                                                                        }
                                                                }
                                                    }
                                            }
                                            catch (Exception e)
                                                {

                                                }
                                    }


                            }
                    }


                if (command.equals("GP"))
                    {
                        if (jo.has("refresh"))
                            {
                                if (jo.getBoolean("refresh"))
                                    {
                                        sendToServer("GROUP", false);
                                    }
                            }
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.u == Integer.parseInt(param))
                                    {
                                        JSONArray users = jo.optJSONArray("users");
                                        JSONArray points = jo.optJSONArray("point");
                                        JSONArray tracks = jo.optJSONArray("track");
                                        JSONArray announcements = jo.optJSONArray("announcement");
                                        JSONArray geoevents = jo.optJSONArray("geoevent");
                                        if(geoevents!=null)
                                            {
                                                for (int i = 0; i<geoevents.length();i++)
                                                    {
                                                        jsonObject=geoevents.getJSONObject(i);
                                                        String name = jsonObject.optString("nick");
                                                        String time="";

                                                        time= OsMoDroid.sdf.format(new Date(jsonObject.optLong("time")*1000));


                                                        String messageText =ch.name+": "+name+ (jsonObject.optInt("type")==1?parent.getString(R.string.enterin):parent.getString(R.string.exitfrom)) +" "+jsonObject.optString("name");


                                                        Message msg = new Message();
                                                                Bundle b = new Bundle();
                                                                b.putBoolean("om_online", true);
                                                                b.putString("MessageText",time + " " + messageText);
                                                                msg.setData(b);
                                                                if (log)
                                                                    {
                                                                        Log.d(this.getClass().getName(), "statenotify entered");
                                                                    }
                                                                localService.alertHandler.sendMessage(msg);

                                                    }
                                            }
                                        if(announcements!=null)
                                            {
                                                for (int i = 0; i<announcements.length();i++)
                                                    {
                                                        jsonObject=announcements.getJSONObject(i);
                                                        String messageText =ch.name+": "+jsonObject.optString("text");
                                                        String time="";

                                                        time= OsMoDroid.sdf.format(new Date(jsonObject.optLong("time")*1000));
                                                                Message msg = new Message();
                                                                Bundle b = new Bundle();
                                                                b.putBoolean("om_online", true);
                                                                b.putString("MessageText", time + " " + messageText);
                                                                msg.setData(b);
                                                                if (log)
                                                                    {
                                                                        Log.d(this.getClass().getName(), "statenotify entered");
                                                                    }
                                                                localService.alertHandler.sendMessage(msg);

                                                    }
                                            }
                                        if (tracks != null)
                                            {
                                                for (int i = 0; i < tracks.length(); i++)
                                                    {
                                                        jsonObject = tracks.getJSONObject(i);
                                                        boolean exist = false;
                                                        if (jsonObject.has("deleted"))
                                                            {
                                                                Iterator<ColoredGPX> iterator = ch.gpxList.iterator();
                                                                while (iterator.hasNext())
                                                                    {
                                                                        ColoredGPX cg = iterator.next();
                                                                        if (cg.u == jsonObject.optInt("u"))
                                                                            {
                                                                                iterator.remove();
                                                                            }
                                                                    }
                                                            }
                                                        else
                                                            {
                                                                ch.fileName.mkdirs();
                                                                Log.d(getClass().getSimpleName(), "filename=" + ch.fileName);
                                                                ColoredGPX cgpx = new ColoredGPX(jsonObject.getInt("u"), new File(ch.sdDir, "OsMoDroid/channelsgpx/" + jsonObject.getString("u") + ".gpx"), jsonObject.getString("color"), jsonObject.getString("url"));
                                                                ch.gpxList.add(cgpx);
                                                                cgpx.status = ColoredGPX.Statuses.DOWNLOADING;
                                                                Netutil.downloadfile(ch, cgpx.url, cgpx);
                                                            }
                                                    }
                                                if (LocalService.devlistener != null)
                                                    {
                                                        LocalService.devlistener.onChannelListChange();
                                                    }
                                            }
                                        if (points != null)
                                            {
                                                for (int i = 0; i < points.length(); i++)
                                                    {
                                                        try
                                                            {
                                                                jsonObject = points.getJSONObject(i);
                                                                boolean exist = false;
                                                                if (jsonObject.has("deleted"))
                                                                    {
                                                                        Point pointToDel = null;
                                                                        for (Point p : ch.pointList)
                                                                            {
                                                                                if (p.u == jsonObject.optInt("u"))
                                                                                    {
                                                                                        pointToDel = p;
                                                                                    }
                                                                            }
                                                                        if (pointToDel != null)
                                                                            {
                                                                                ch.pointList.remove(pointToDel);
                                                                            }
                                                                    }
                                                                else
                                                                    {
                                                                        for (Point p : ch.pointList)
                                                                            {
                                                                                if (p.u == jsonObject.optInt("u"))
                                                                                    {
                                                                                        exist = true;
                                                                                        p.lat = Float.parseFloat(jsonObject.getString("lat"));
                                                                                        p.lon = Float.parseFloat(jsonObject.getString("lon"));
                                                                                        p.description = jsonObject.optString("description");
                                                                                        p.color = jsonObject.optString("color");
                                                                                        p.name = jsonObject.getString("name");
                                                                                        p.url=jsonObject.optString("url");
                                                                                        p.time= OsMoDroid.sdf.format(new Date(jsonObject.optLong("time")*1000));
                                                                                    }
                                                                            }
                                                                        if (!exist)
                                                                            {
                                                                                ch.pointList.add(new Point(jsonObject));
                                                                            }
                                                                    }
                                                            }
                                                        catch (JSONException e)
                                                            {
                                                                writeException(e);
                                                                e.printStackTrace();
                                                            }
                                                    }
                                                if (LocalService.devlistener != null)
                                                    {
                                                        LocalService.devlistener.onChannelListChange();
                                                    }
                                            }
                                        if (users != null)
                                            {
                                                for (int i = 0; i < users.length(); i++)
                                                    {
                                                        try
                                                            {
                                                                jsonObject = users.getJSONObject(i);
                                                                try
                                                                    {
                                                                        if (jsonObject.has("deleted"))
                                                                            {
                                                                                Device deviceToDel = null;
                                                                                for (Device dev : ch.deviceList)
                                                                                    {
                                                                                        if (dev.u == jsonObject.optInt("u"))
                                                                                            {
                                                                                                deviceToDel = dev;
                                                                                            }
                                                                                    }
                                                                                if (deviceToDel != null)
                                                                                    {
                                                                                        ch.deviceList.remove(deviceToDel);
                                                                                        Collections.sort(ch.deviceList);
                                                                                    }
                                                                            }
                                                                        else
                                                                            {
                                                                                boolean exist = false;
                                                                                for (Device dev : ch.deviceList)
                                                                                    {
                                                                                        if (dev.u == jsonObject.optInt("u"))
                                                                                            {
                                                                                                exist = true;
                                                                                                if (jsonObject.has("lat") && jsonObject.has("lon")) {
                                                                                                    try {


                                                                                                        float newlat = Float.parseFloat(jsonObject.getString("lat"));
                                                                                                        float newlon = Float.parseFloat(jsonObject.getString("lon"));
                                                                                                        if (newlat != dev.lat & newlon != dev.lon && (System.currentTimeMillis() - dev.updatated) > 5 * 60 * 1000) {
                                                                                                            dev.lat = newlat;
                                                                                                            dev.lon = newlon;
                                                                                                            notifydevicemonitoring(dev,true);
                                                                                                        }
                                                                                                    }
                                                                                                    catch (NumberFormatException e)
                                                                                                    {
                                                                                                        writeException(e);
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                                getDevtrace(jsonObject, dev);
                                                                                                if(jsonObject.has("time"))
                                                                                                    {
                                                                                                        try
                                                                                                            {
                                                                                                                dev.updatated=  jsonObject.optLong("time")*1000;
                                                                                                            }
                                                                                                        catch (IllegalArgumentException e)
                                                                                                            {
                                                                                                                e.printStackTrace();
                                                                                                            }
                                                                                                    }
                                                                                                if(jsonObject.has("color"))
                                                                                                    {
                                                                                                        dev.color = Color.parseColor(jsonObject.getString("color"));
                                                                                                    }
                                                                                                if(jsonObject.has("name"))
                                                                                                    {
                                                                                                        dev.name = jsonObject.getString("name");
                                                                                                    }
                                                                                                if(jsonObject.has("state"))
                                                                                                    {
                                                                                                        if(ch.type!=2)
                                                                                                            {
                                                                                                                if (dev.state != 1 && jsonObject.getInt("state") == 1)
                                                                                                                    {
                                                                                                                        notifydevicemonitoring(dev, true);
                                                                                                                    }
                                                                                                                if (dev.state == 1 && jsonObject.getInt("state") == 0)
                                                                                                                    {
                                                                                                                        notifydevicemonitoring(dev, false);
                                                                                                                    }
                                                                                                                dev.state = jsonObject.getInt("state");
                                                                                                            }
                                                                                                    }
                                                                                            }
                                                                                    }
                                                                                if (!exist)
                                                                                    {
                                                                                        try
                                                                                            {
                                                                                                Device dev = new Device(jsonObject.getInt("u"), jsonObject.optString("name"), jsonObject.optString("color"),jsonObject.optInt("state"));
                                                                                                if (jsonObject.has("lat") && jsonObject.has("lon"))
                                                                                                    {
                                                                                                       try {


                                                                                                           dev.lat = Float.parseFloat(jsonObject.getString("lat"));
                                                                                                           dev.lon = Float.parseFloat(jsonObject.getString("lon"));

                                                                                                       }
                                                                                                       catch (NumberFormatException e)
                                                                                                       {
                                                                                                           writeException(e);
                                                                                                           e.printStackTrace();
                                                                                                       }
                                                                                                    }
                                                                                                getDevtrace(jsonObject, dev);
                                                                                                ch.deviceList.add(dev);
                                                                                                Collections.sort(ch.deviceList);
                                                                                            }
                                                                                        catch (JSONException e)
                                                                                            {
                                                                                                writeException(e);
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                    }
                                                                            }
                                                                    }
                                                                catch (NumberFormatException e)
                                                                    {
                                                                        Log.d(getClass().getSimpleName(), "Wrong device info");
                                                                        e.printStackTrace();
                                                                        writeException(e);
                                                                    }
                                                            }
                                                        catch (JSONException e)
                                                            {
                                                                writeException(e);
                                                                e.printStackTrace();
                                                            }
                                                    }
                                            }
                                    }
                            }
                        if (LocalService.channelsDevicesAdapter != null)
                            {
                                LocalService.channelsDevicesAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write group list to file");
                            }
                        //localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
                        sendToServer("GPR:" + param, false);
                    }

                if (command.equals("GPC"))
                    {
                        addToChannelChat(Integer.parseInt(param), jo, false);
                    }

                if (command.equals("DP"))
                    {
                        for (Device dev : LocalService.deviceList)
                            {
                                if (dev.u == Integer.parseInt(param))
                                    {
                                        if (Integer.parseInt(addict.substring(2, 3)) == 2)
                                            {
                                                dev.state = Integer.parseInt(addict.substring(6, 7));
                                                String status;
                                                String messageText = "";
                                                if (dev.state == 1)
                                                    {
                                                        status = localService.getString(R.string.started);
                                                    }
                                                else
                                                    {
                                                        status = localService.getString(R.string.stoped);
                                                    }
                                                messageText = messageText + localService.getString(R.string.monitoringondevice) + dev.name + "\" " + status;
                                                if (OsMoDroid.settings.getBoolean("statenotify", true))
                                                    {
                                                        Message msg = new Message();
                                                        Bundle b = new Bundle();
                                                        b.putBoolean("om_online", true);
                                                        b.putString("MessageText", sdf1.format(new Date()) + " " + messageText);
                                                        msg.setData(b);
                                                        if (log)
                                                            {
                                                                Log.d(this.getClass().getName(), "statenotify entered");
                                                            }
                                                        localService.alertHandler.sendMessage(msg);
                                                    }
                                            }
                                        {
                                            if (Integer.parseInt(addict.substring(2, 3)) == 3)
                                                {
                                                    dev.online = Integer.parseInt(addict.substring(6, 7));
                                                    String status = "";
                                                    String messageText = "";
                                                    if (dev.online == 1)
                                                        {
                                                            status = localService.getString(R.string.enternet);
                                                        }
                                                    if (dev.online == 0)
                                                        {
                                                            status = localService.getString(R.string.exitnet);
                                                        }
                                                    messageText = messageText + localService.getString(R.string.device) + dev.name + "\" " + status;
                                                    if (OsMoDroid.settings.getBoolean("onlinenotify", false))
                                                        {
                                                            if (log)
                                                                {
                                                                    Log.d(this.getClass().getName(), "statenotify onlaine entered");
                                                                }
                                                            Message msg = new Message();
                                                            Bundle b = new Bundle();
                                                            b.putBoolean("om_online", true);
                                                            b.putString("MessageText", sdf1.format(new Date()) + " " + messageText);
                                                            msg.setData(b);
                                                            localService.alertHandler.sendMessage(msg);
                                                        }
                                                }
                                        }
                                        if (LocalService.deviceAdapter != null)
                                            {
                                                LocalService.deviceAdapter.notifyDataSetChanged();
                                            }
                                    }
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write device list to file");
                            }
                        localService.saveObject(LocalService.deviceList, OsMoDroid.DEVLIST);
                    }
                if (command.equals("G"))
                    {
                        for (Channel ch : LocalService.channelList)
                            {
                                if (Integer.parseInt(param) == ch.u)
                                    {
                                        for (int n = 0; n < ja.length(); n++)
                                            {
                                                try
                                                    {
                                                        Device updatedDev = new Device(Integer.parseInt(ja.getString(n).substring(0, ja.getString(n).indexOf("|"))), ja.getString(n).substring(0, ja.getString(n).indexOf("|")), "black",1);
                                                        if (!ch.deviceList.contains(updatedDev))
                                                            {
                                                                ch.deviceList.add(updatedDev);
                                                            }
                                                        for (final Device dev : ch.deviceList)
                                                            {
                                                                if (Integer.parseInt(ja.getString(n).substring(0, ja.getString(n).indexOf("|"))) == dev.u)
                                                                    {
                                                                        updateCoordinates(ja.getString(n).substring(ja.getString(n).indexOf("|") + 1), dev);
                                                                    }
                                                            }
                                                    }
                                                catch (JSONException e)
                                                    {
                                                        e.printStackTrace();
                                                        writeException(e);
                                                    }
                                            }
                                    }
                            }

                        if (LocalService.channelsDevicesAdapter != null)
                            {
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "Adapter:" + LocalService.channelsDevicesAdapter.toString());
                                    }
                                LocalService.channelsDevicesAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write group list to file");
                            }
                        //localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
                    }
                if (command.equals("D"))
                    {
                        for (Device dev : LocalService.deviceList)
                            {
                                if (Integer.parseInt(param) == dev.u)
                                    {
                                        try
                                            {
                                                updateCoordinates(addict, dev);
                                            }
                                        catch (Exception e)
                                            {
                                                writeException(e);
                                                e.printStackTrace();
                                            }
                                    }
                            }
                        if (LocalService.deviceAdapter != null)
                            {
                                LocalService.deviceAdapter.notifyDataSetChanged();
                            }
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "write group list to file");
                            }
                        localService.saveObject(LocalService.deviceList, OsMoDroid.DEVLIST);
                    }
            }
        static void  getDevtrace(JSONObject jsonObject, Device dev)
            {
                if(jsonObject.has("track"))
                    {
                        if(dev.devicePath.isEmpty())
                            {
                                JSONArray devtrackpoints = jsonObject.optJSONArray("track");
                                Log.d("osmo", "track json size="+devtrackpoints.length());
                                for (int k = 0; k <devtrackpoints.length() ; k++)
                                    {
                                        JSONArray p = (JSONArray) devtrackpoints.opt(k);
                                        float lat=0;
                                        float lon=0;
                                        try
                                            {
                                                lat = Float.parseFloat(p.optString(0));
                                                lon = Float.parseFloat(p.optString(1));
                                            }
                                        catch (NumberFormatException e)
                                            {
                                                e.printStackTrace();
                                            }

                                        if(lat!=0&&lon!=0)
                                            {
                                                GeoPoint gp = new GeoPoint(lat, lon);
                                                dev.devicePath.add(new SerPoint(new android.graphics.Point(gp.getLatitudeE6(),gp.getLongitudeE6())));

                                            }

                                    }
                                Log.d("osmo", "track size="+dev.devicePath.size());
                                Log.d("osmo", "track size="+dev.devicePath.toString());
                            }
                    }
            }
        public static void writeException(Exception e)
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsString = sw.toString();
                LocalService.addlog(exceptionAsString);
            }
        private void updateCoordinates(String d, final Device dev)
            {
                //	if (Integer.parseInt(c.substring(c.indexOf(":")+1), c.length()) == dev.u){
                dev.speed = "";
                float newlat = 0;
                float newlon = 0;
                newlat = Float.parseFloat(d.substring(d.indexOf("L") + 1, d.indexOf(":")));
                for (int i = d.indexOf(":") + 1; i <= d.length(); i++)
                    {
                        if (!(d.charAt(i) == '-') && !Character.isDigit(d.charAt(i)))
                            {
                                if (!Character.toString(d.charAt(i)).equals("."))
                                    {
                                        newlon = Float.parseFloat(d.substring(d.indexOf(":") + 1, i));
                                        break;
                                    }
                            }
                    }
                if (newlat != dev.lat & newlon != dev.lon && (System.currentTimeMillis() - dev.updatated) > 5 * 60 * 1000)
                    {
                        //notifydevicemonitoring(dev,true);
                    }
                dev.lat = newlat;
                dev.lon = newlon;
                dev.updatated = System.currentTimeMillis();
                int idxS = d.indexOf("S");
                if(idxS!=-1)
                    {
                        for (int i = idxS + 1; i <= d.length() - 1; i++)
                            {
                                if (!Character.isDigit(d.charAt(i)) || i == (d.length() - 1))
                                    {
                                        if (!Character.toString(d.charAt(i)).equals(".") || i == (d.length() - 1))
                                            {
                                                LocalService.addlog(d.substring(d.indexOf("S") + 1, i));
                                                dev.speed = OsMoDroid.df0.format((((Float.parseFloat(d.substring(d.indexOf("S") + 1, i)) * 3.6))));
                                                break;
                                            }
                                    }
                            }
                    }
                int idxT = d.indexOf("T");
                if(idxT!=-1)
                    {
                        for (int i = idxT + 1; i <= d.length() - 1; i++)

                            {

                                if (!Character.isDigit(d.charAt(i)) || i == (d.length() - 1))

                                    {
                                        if (!Character.toString(d.charAt(i)).equals(".") || i == (d.length() - 1))
                                            {
                                                LocalService.addlog(d.substring(d.indexOf("T") + 1, i));
                                                dev.updatated = 1000 * Long.parseLong(d.substring(d.indexOf("S") + 1, i));
                                                break;
                                            }
                            }
                    }
                }
                localService.alertHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                        {
                            GeoPoint gp = new GeoPoint(dev.lat, dev.lon);
                            dev.devicePath.add(new SerPoint(new android.graphics.Point(gp.getLatitudeE6(),gp.getLongitudeE6())));
                            if (LocalService.devlistener != null)
                                {
                                    LocalService.devlistener.onDeviceChange(dev);
                                }
                        }
                });
                //}
            }
        void ondisconnect()
            {
            }
        void notifydevicemonitoring(Device dev,boolean start)
            {
                String status;
                String messageText = "";
                if(start)
                    {
                        status = localService.getString(R.string.started);
                    }
                else
                    {
                        status = localService.getString(R.string.stoped);
                    }

                messageText = messageText + localService.getString(R.string.monitoringondevice) + dev.name + "\" " + status;
                if (OsMoDroid.settings.getBoolean("statenotify", true))
                    {
                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putBoolean("om_online", true);
                        b.putString("MessageText", sdf1.format(new Date()) + " " + messageText);
                        msg.setData(b);
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "statenotify entered");
                            }
                        localService.alertHandler.sendMessage(msg);
                    }
            }
        private void setReconnectOnError()
            {
                try
                    {
                        if (socket != null)
                            {
                                socket.close();
                            }
                    }
                catch (IOException e)
                    {
                        writeException(e);
                        e.printStackTrace();
                    }
                disablekeepAliveAlarm();
                authed = false;
                connecting = false;
                connOpened = false;
                localService.alertHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                        {
                            localService.internetnotify(false);
                        }
                });
                running = false;
                localService.refresh();
                if (localService.isOnline())
                    {
                        localService.alertHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                                {
                                    LocalService.addlog("setReconnectAlarm on error");
                                    parent.registerReceiver(reconnectReceiver, new IntentFilter(RECONNECT_INTENT));
                                    manager.cancel(reconnectPIntent);
                                    manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ERROR_RECONNECT_TIMEOUT, reconnectPIntent);
                                    LocalService.addlog("setReconnectAlarm on error setted " + SystemClock.elapsedRealtime());
                                }
                        });
                    }
            }
        @Override
        public void onResultsSucceeded(APIComResult result)
            {
                checkadressing = false;

                manager.cancel(getTokenTimeoutPIntent);


                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "OnResultSucceded " + result.rawresponse);
                    }
                if (result.Command.equals("checkaddres") && !(result.Jo == null))
                    {
                        socketRetryInt = 0;
                        LocalService.addlog("Receive token " + result.Jo.toString());
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "checkaddres response:" + result.Jo.toString());
                            }
                        //Toast.makeText(localService,result.Jo.optString("state")+" "+ result.Jo.optString("error_description"),5).show();
                        if (result.Jo.has("address"))
                            {
                                try
                                    {
                                        //token = result.Jo.getString("token");
                                        workservername = result.Jo.optString("address").substring(0, result.Jo.optString("address").indexOf(':'));
                                        workserverint = Integer.parseInt(result.Jo.optString("address").substring(result.Jo.optString("address").indexOf(':') + 1));
                                        try
                                            {
                                                connectThread.start();
                                            }
                                        catch (IllegalThreadStateException e)
                                            {
                                                setReconnectOnError();
                                                writeException(e);
                                                e.printStackTrace();
                                            }
                                    }
                                catch (Exception e)
                                    {
                                        writeException(e);
                                        e.printStackTrace();
                                    }
                                if(result.Jo.has("uid"))
                                    {
                                        if(result.Jo.optInt("uid")>0)
                                            {
                                                OsMoDroid.editor.putString("u", result.Jo.optString("name", ""));
                                                OsMoDroid.editor.putString("p", result.Jo.optString("uid", ""));
                                                OsMoDroid.editor.commit();

                                            }
                                        else
                                            {
                                                OsMoDroid.editor.remove("p");
                                                OsMoDroid.editor.remove("u");
                                                OsMoDroid.editor.commit();

                                            }
                                        localService.refresh();
                                    }
                            }
                        else
                            {
                                if (result.Jo.optInt("error") == 10 || result.Jo.optInt("error") == 100 || result.Jo.optString("token").equals("false"))
                                    {
                                        localService.internetnotify(false);
                                        close();
                                        localService.notifywarnactivity(LocalService.unescape(result.Jo.optString("error_description")), false, OsMoDroid.NOTIFY_NO_DEVICE);
                                        //localService.motd=LocalService.unescape(result.Jo.optString("error_description"));
                                        localService.sendid();
                                        localService.refresh();
                                    }
                                else if (result.Jo.optInt("error") == 67 || result.Jo.optInt("error") == 68 || result.Jo.optInt("error") == 69)
                                    {
                                        close();
                                        localService.notifywarnactivity(LocalService.unescape(result.Jo.optString("error_description")), false, OsMoDroid.NOTIFY_EXPIRY_USER);
                                        localService.motd = LocalService.unescape(result.Jo.optString("error_description"));
                                        localService.refresh();
                                    }
                                else if (result.Jo.optInt("error") == 21 )
                                    {
                                        close();
                                        localService.notifywarnactivity(LocalService.unescape(result.Jo.optString("error_description")), true,0);
                                        localService.motd = LocalService.unescape(result.Jo.optString("error_description"));
                                        localService.refresh();
                                    }

                            }
                    }
                else
                    {
                        LocalService.addlog("Receive token error - shall reconnecting " + result.rawresponse);
                        socketRetryInt++;
                        if (socketRetryInt > 3 && !OsMoDroid.settings.getBoolean("understand", false))
                            {
                                localService.notifywarnactivity(localService.getString(R.string.checkfirewall), false, OsMoDroid.NOTIFY_NO_CONNECT);
                            }
                        setReconnectOnError();
                    }
            }
        public class IMWriter implements Runnable
            {
                public Handler handler;
                boolean error = false;
                @Override
                public void run()
                    {
                        Looper.prepare();
                        handler = new Handler()
                        {
                            @Override
                            public void handleMessage(Message msg)
                                {
                                    Bundle b = msg.getData();
                                    if (running)
                                        {
                                            if (socket != null && socket.isConnected() && wr != null)
                                                {
                                                    if (!b.getBoolean("pp"))
                                                        {
                                                            setReconnectAlarm();
                                                        }
                                                    try
                                                        {
                                                            Thread.sleep(0);
                                                        }
                                                    catch (InterruptedException e)
                                                        {
                                                            writeException(e);
                                                            e.printStackTrace();
                                                        }
                                                    wr.println(b.getString("write"));
                                                    error = wr.checkError();
                                                    if (log)
                                                        {
                                                            Log.d(this.getClass().getName(), "Write " + b.getString("write") + " error=" + error);
                                                        }
                                                    LocalService.addlog("SocketWrite " + b.getString("write") + " error=" + error);
                                                    if (error)
                                                        {
                                                            if (running)
                                                                {
                                                                    setReconnectOnError();
                                                                }
                                                            //Looper.myLooper().quit();
                                                        }
                                                    else
                                                        {
                                                            sendBytes = sendBytes + b.getString("write").getBytes().length;
                                                        }
                                                }
                                        }
                                    else
                                        {
                                            LocalService.addlog("not connected now");
                                            if (OsMoDroid.gpslocalserviceclientVisible)
                                                {
                                                    Toast.makeText(localService, localService.getString(R.string.CheckInternet), Toast.LENGTH_SHORT).show();
                                                }
                                        }
                                    super.handleMessage(msg);
                                }
                        };
                        Looper.loop();
                    }
            }
        private class IMReader implements Runnable
            {
                private StringBuilder stringBuilder = new StringBuilder(1024);
                private String str;
                @Override
                public void run()
                    {
                        while (connOpened && !Thread.currentThread().isInterrupted())
                            {
                                try
                                    {
                                        stringBuilder.setLength(0);
                                        int c = 0;
                                        int i = 0;
                                        while (!(c == 10) && !Thread.currentThread().isInterrupted())
                                            {
                                                c = rd.read();
                                                if (!(c == -1))
                                                    {
                                                        stringBuilder.append((char) c);
                                                    }
                                                else
                                                    {
                                                        if (log)
                                                            {
                                                                Log.d(this.getClass().getName(), "inputstream c=-1 ");
                                                            }
                                                        LocalService.addlog("inputstream c=-1 ");
                                                        setReconnectOnError();
                                                        break;
                                                    }
                                                i = i + 1;
                                            }
                                        if (stringBuilder.length() != 0 && connOpened)
                                            {
                                                str = stringBuilder.toString();
                                                recievedBytes = recievedBytes + str.getBytes().length;
                                                Message msg = new Message();
                                                Bundle b = new Bundle();
                                                b.putString("read", str);
                                                msg.setData(b);
                                                if (localService.alertHandler != null)
                                                    {
                                                        localService.alertHandler.sendMessage(msg);
                                                    }
                                                else
                                                    {
                                                        LocalService.addlog("panic!alert handler is null ");
                                                        if (log)
                                                            {
                                                                Log.d(this.getClass().getName(), " alert handler is null!!!");
                                                            }
                                                    }
                                            }
                                    }
                                catch (IOException e)
                                    {
                                        if (running)
                                            {
                                                setReconnectOnError();
                                            }
                                        writeException(e);
                                        e.printStackTrace();
                                    }
                            }
                    }
            }
        private class IMConnect implements Runnable
            {
                @Override
                public void run()
                    {
                        SocketAddress sockAddr;
                        connectcount++;
                        try
                            {

                                        InetAddress serverAddr = InetAddress.getByName(workservername);
                                        sockAddr = new InetSocketAddress(serverAddr, workserverint);
//                                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
//                                        sockAddr = new InetSocketAddress(serverAddr, SERVERPORT);


                                socket = new Socket();


                                if (log){Log.d(this.getClass().getName(), "sockAddr=" + sockAddr);}
                                        socket.connect(sockAddr, 5000);
                                        //socket.connect(new InetSocketAddress("osmo.mobi", 5050), 5000);
                                        //SSLContext sslContext = SSLContext.getInstance("TLS");
                                SSLContext sslContext = SSLContext.getDefault();
                               // TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                              //  trustManagerFactory.init((KeyStore)null);
                              //  TrustManager[] trustAndroidCerts = trustManagerFactory.getTrustManagers();
                                        //sslContext.init(null, null, null);
                                        TrustManager[] trustAllCerts = new TrustManager[]{
                                                new X509TrustManager()
                                                    {
                                                        public X509Certificate[] getAcceptedIssuers()
                                                            {
                                                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                                                return myTrustedAnchors;
                                                            }
                                                        @Override
                                                        public void checkClientTrusted(X509Certificate[] certs, String authType)
                                                            {
                                                            }
                                                        @Override
                                                        public void checkServerTrusted(X509Certificate[] certs, String authType)
                                                            {
                                                            }
                                                    }
                                        };
                                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
                                            {
                                                sslContext.init(null, trustAllCerts, new SecureRandom());
                                            }
                                        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                                      //  sslsocket = (SSLSocket) socketFactory.createSocket(socket, workservername,workserverint, false);
                                sslsocket = (SSLSocket) socketFactory.createSocket(socket, workservername   ,workserverint, false);
                                sslsocket.setUseClientMode(true);
                                        SSLSession sslSession = sslsocket.getSession();
                                        if (log){Log.d(this.getClass().getName(), "Secured=" + sslSession.isValid());}
                                        if (!sslSession.isValid())
                                            {
                                                throw new Exception();
                                            }
                                //workserverint = -1;
                                //workservername = "";
                                        LocalService.addlog("SSL TCP Connected");
                                        rd = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
                                        wr = new PrintWriter(new OutputStreamWriter(sslsocket.getOutputStream(), "UTF8"), true);


                                socketRetryInt = 0;
                                connOpened = true;
                                connecting = false;

                                localService.alertHandler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                            {

                                                localService.refresh();
                                            }
                                    });
                                setReconnectAlarm();
                                readerThread.start();
                                //sendToServer("INIT|" + token, false);
                                sendToServer("AUTH|" + OsMoDroid.settings.getString("newkey", ""), false);
                            }
                        catch (final Exception e1)
                            {
                                erorconenctcount++;
                                socketRetryInt++;
                                e1.printStackTrace();
                                connecting = false;
                                setReconnectOnError();

                                            LocalService.addlog("could no conenct to socket " + socketRetryInt + e1.getMessage());

                                if (socketRetryInt > 3 && !OsMoDroid.settings.getBoolean("understand", false))
                                    {
                                        localService.notifywarnactivity(localService.getString(R.string.checkfirewall), false, OsMoDroid.NOTIFY_NO_CONNECT);
                                    }
                            }
                    }
            }
    }
