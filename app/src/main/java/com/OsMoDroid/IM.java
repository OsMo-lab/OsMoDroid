package com.OsMoDroid;

import static android.app.PendingIntent.FLAG_MUTABLE;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.OsMoDroid.Channel.Point;
import com.OsMoDroid.Netutil.MyAsyncTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import static com.OsMoDroid.LocalService.activityname;
import static com.OsMoDroid.LocalService.addlog;
import static com.OsMoDroid.LocalService.myManager;
import static com.OsMoDroid.LocalService.privatemode;
import static com.OsMoDroid.LocalService.transportid;
import static com.OsMoDroid.OsMoDroid.context;
import static com.OsMoDroid.OsMoDroid.osmodirFile;
import static com.OsMoDroid.OsMoDroid.timeshift;

//import android.R;

/**
 * @author dfokin
 *         Class for work with osmo server
 */
public class IM implements ResultsListener {

    private static Camera camera;

    final static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int KEEP_ALIVE = 1000 * 270;
    private static final long ERROR_RECONNECT_TIMEOUT = 3 * 1000;
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
    private static int prevRECONNECT_TIMEOUT = 1000 * 30;
    final boolean log = true;
    static long startTraffic = 0;
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
    //PendingIntent onlineTimeoutPIntent;

    Context parent;
    int mestype = 0;
    LocalService localService;
    FileOutputStream fos;
    ObjectOutputStream output = null;
    int socketRetryInt = 0;
    long connectcount = 0;
    long erorconenctcount = 0;

    private UDPReader udpReader;
    private UDPWriter udpWriter;
    volatile private boolean checkadressing = false;
    private String token = "";
    private String poll = "";

    private Thread udpWriterThread;
    private Thread udpReaderThread;
    private int workserverint = -1;
    private String workservername = "";
    private MyAsyncTask sendidtask;
    ArrayList<String> executedCommandArryaList = new ArrayList<String>();

    BroadcastReceiver keepAliveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (connOpened) {
                LocalService.addlog("UPD sendPing");
                if (log) {
                    Log.d(this.getClass().getName(), " send ping");
                }
                sendUDP("P");
            }
        }
    };
    private boolean onlinebybcr = false;
    private long lastsendnet = 0;
    private BroadcastReceiver bcr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (OsMoDroid.permanent && (lastsendnet + RECONNECT_TIMEOUT < SystemClock.uptimeMillis())) {
                lastsendnet = SystemClock.uptimeMillis();
                Intent is = new Intent(context, LocalService.class);
                is.putExtra("GCM", "NEEDSENDNET");
                localService.handleStart(is,0);
                LocalService.addlog("Network broadcast receive - send NEEDSENDNET");


            }
        }
    };
    BroadcastReceiver getTokenTimeoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(this);
            if (log) {
                Log.d(this.getClass().getName(), "checkaddres timeout reciever trigged");
            }
            LocalService.addlog("Get token timeout receiver trigged");
            sendidtask.cancel(true);
            checkadressing = false;
            stop();
            start();
        }
    };
    
    public boolean needtosendpreference = false;
    private boolean warnedsocketconnecterror = false;
    private long timeonline = SystemClock.uptimeMillis();
    private boolean flicking = false;
    private long reconnecttime = 0;
    private DatagramSocket clientSocket =null;

    public IM(String server, int port, LocalService service) {
        RECONNECT_TIMEOUT = Integer.parseInt(OsMoDroid.settings.getString("timeout", "30")) * 1000;
        if (RECONNECT_TIMEOUT < 5000) {
            RECONNECT_TIMEOUT = 5000;
        }
        prevRECONNECT_TIMEOUT = RECONNECT_TIMEOUT;
        localService = service;
        parent = service;
        manager = (AlarmManager) (parent.getSystemService(Context.ALARM_SERVICE));
        reconnectPIntent = PendingIntent.getBroadcast(parent, 0, new Intent(RECONNECT_INTENT), FLAG_MUTABLE);
        keepAlivePIntent = PendingIntent.getBroadcast(parent, 1, new Intent(KEEPALIVE_INTENT), FLAG_MUTABLE);
        getTokenTimeoutPIntent = PendingIntent.getBroadcast(parent, 2, new Intent(GET_TOKEN_TIMEOUT_INTENT), FLAG_MUTABLE);
        //onlineTimeoutPIntent = PendingIntent.getBroadcast(parent, 3, new Intent(ONLINE_TIMEOUT_INTENT), 0);
        SERVER_IP = server;
        SERVERPORT = port;
        LocalService.addlog("IM create");
        LocalService.addlog("GCMID=" + OsMoDroid.settings.getString("GCMRegId", ""));

        try {
            clientSocket = new DatagramSocket();
            udpReader = new UDPReader();
            udpWriter = new UDPWriter();
            udpReaderThread = new Thread(udpReader, "udpreader");
            udpWriterThread = new Thread(udpWriter, "udpwriter");
            udpReaderThread.setPriority(Thread.MIN_PRIORITY);
            udpWriterThread.setPriority(Thread.MIN_PRIORITY);
            udpReaderThread.start();
            udpWriterThread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        startTraffic = TrafficStats.getUidTxBytes(context.getApplicationInfo().uid);

    }

    public void sendUDP (String str)
    {
        addlog(str);
        if (udpWriter.handler != null) {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("write", str);
            msg.setData(b);
            udpWriter.handler.sendMessage(msg);
            localService.refresh();
        }
        else
        {
            LocalService.addlog("udpwriter handler null");
        }
    }

    

    public void setOnlineTimeout() {
        LocalService.addlog("Socket void setOnlineTimeOut");
        timeonline = SystemClock.uptimeMillis();
        //parent.registerReceiver(onlineTimeoutReceiver, new IntentFilter(ONLINE_TIMEOUT_INTENT));
        //localService.alertHandler.postDelayed(new Runnable()
        //  {
        //     public void run()
        {
            //              addlog( "handler online timeout");
            //           parent.sendBroadcast(new Intent(ONLINE_TIMEOUT_INTENT));
        }
        //}, ONLINE_TIMEOUT);

//                manager.cancel(onlineTimeoutPIntent);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
//                    {
//                        manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ONLINE_TIMEOUT, onlineTimeoutPIntent);
//                    }
//                else
//                    {
//                        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ONLINE_TIMEOUT, onlineTimeoutPIntent);
//                    }
    }


    public void setkeepAliveAlarm() {
        if (log) {
            Log.d(this.getClass().getName(), "void setKeepAliveAlarm");
        }
        LocalService.addlog("Socket void setkeepalive");
        //parent.registerReceiver(keepAliveReceiver, new IntentFilter(KEEPALIVE_INTENT));
        manager.cancel(keepAlivePIntent);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + KEEP_ALIVE, KEEP_ALIVE, keepAlivePIntent);
    }

    public void disablekeepAliveAlarm() {
        if (log) {
            Log.d(this.getClass().getName(), "void disableKeepAliveAlarm");
        }
        LocalService.addlog("Socket void disablekeepalive");
//                try
//                    {
//                        parent.unregisterReceiver(keepAliveReceiver);
//                    }
//                catch (Exception e)
//                    {
//                        e.printStackTrace();
//                    }
        manager.cancel(keepAlivePIntent);
    }

    synchronized public void setReconnectAlarm(final boolean fast) {
        if (log) {
            Log.d(this.getClass().getName(), "void setReconnectAlarm fast=" + fast);
        }
        localService.alertHandler.post(new Runnable() {
            @Override
            public void run() {
                LocalService.addlog("Socket setReconnectAlarm fast=" + fast);
            }
        });
        //parent.registerReceiver(reconnectReceiver, new IntentFilter(RECONNECT_INTENT));
        manager.cancel(reconnectPIntent);
        if (reconnecttime == 0) {
            reconnecttime = SystemClock.uptimeMillis();
            addlog("Set reconnecttime=" + reconnecttime);
        } else {
            addlog("Set reconnecttime no executed because recconecttime already set =" + reconnecttime);
        }
        if (fast) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ERROR_RECONNECT_TIMEOUT, reconnectPIntent);
            } else {
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ERROR_RECONNECT_TIMEOUT, reconnectPIntent);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, reconnectPIntent);
            } else {
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, reconnectPIntent);
            }

        }
        checkalarmindozemode();

    }

    /**
     * Выключает IM
     */
    void close() {
        // sendUDP("BYE", false);

        if (log) {
            Log.d(this.getClass().getName(), "void IM.close");
        }
        LocalService.addlog("Socket void close");
        try {
            parent.unregisterReceiver(bcr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            parent.unregisterReceiver(keepAliveReceiver);
        } catch (Exception e) {
        }
        try {
            parent.unregisterReceiver(getTokenTimeoutReceiver);
        } catch (Exception e) {
        }
        
        
        stop();
        start = false;
    }

    ;

    public void checkaddres() {
        LocalService.addlog("Start get token" + ", key=" + OsMoDroid.settings.getString("newkey", ""));
        if (!checkadressing) {
            JSONObject postjson = getNET();
            checkadressing = true;
            APIcomParams params = null;
            params = new APIcomParams("https://api2.osmo.mobi/serv?app="+OsMoDroid.app_code+"&id=" + OsMoDroid.settings.getString("tracker_id", ""), "", "checkaddres");

            sendidtask = new Netutil.MyAsyncTask(this);
            sendidtask.execute(params);
            Log.d(getClass().getSimpleName(), "get token start to execute");
            parent.registerReceiver(getTokenTimeoutReceiver, new IntentFilter(GET_TOKEN_TIMEOUT_INTENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, getTokenTimeoutPIntent);
            } else {
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, getTokenTimeoutPIntent);
            }
        }
    }

    void start() {
        if (SystemClock.uptimeMillis() > timeonline + ONLINE_TIMEOUT) {
            parent.sendBroadcast(new Intent(ONLINE_TIMEOUT_INTENT));
        }
        if (true) {
            start = true;
            if (log) {
                Log.d(this.getClass().getName(), "void IM.start");
            }
            LocalService.addlog("Socket void start");
            running = true;
            connecting = true;
            localService.refresh();


            //
            parent.registerReceiver(bcr, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            parent.registerReceiver(keepAliveReceiver, new IntentFilter(KEEPALIVE_INTENT));
            


        } else {
            LocalService.addlog("Socket void start - but offline mode enabled");
        }
    }

    void stop() {
        if (log) {
            Log.d(this.getClass().getName(), "void IM.stop");
        }
        LocalService.addlog("Socket void stop");
        executedCommandArryaList.clear();
        running = false;
        connOpened = false;
        authed = false;
        connecting = false;
        localService.addlog("set connectcompleted=false");
        LocalService.connectcompleted = false;
        localService.alertHandler.post(new Runnable() {
            @Override
            public void run() {
                ondisconnect();
            }
        });
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                LocalService.addlog("exeption close socket " + e.getMessage());
                e.printStackTrace();
            }
        }
        manager.cancel(getTokenTimeoutPIntent);
        manager.cancel(reconnectPIntent);

        // manager.cancel(onlineTimeoutPIntent);
        localService.refresh();
    }

    void addToChannelChat(int channelU, JSONObject jo, boolean silent) throws JSONException {
        if (log) {
            Log.d(this.getClass().getName(), "type=chch");
        }
        if (log) {
            Log.d(this.getClass().getName(), "Сообщение в чат канала " + jo);
            //LocalService.addlog("Сообщение в чат канала " + jo);
        }
        ChatMessage m = new ChatMessage();
        m.u = jo.optInt("u");
        m.text = Netutil.unescape(jo.optString("text"));
        m.time = OsMoDroid.sdf.format(new Date(timeshift + jo.optLong("time") * 1000));
        m.name = jo.optString("name");
        m.type = jo.optInt("type");
        String fromDevice = "Незнамо кто";
        // LocalService.addlog("Размер спсика групп " + LocalService.channelList.size());


        {
            if (!silent) {
                fromDevice = jo.optString("name");
                Intent intent = new Intent(localService, GPSLocalServiceClient.class).putExtra("channelpos", channelU);
                intent.setAction("channelchat");
                PendingIntent contentIntent = PendingIntent.getActivity(localService, 333, intent, PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_CANCEL_CURRENT);
                Long when = System.currentTimeMillis();
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                        localService.getApplicationContext(), "default")
                        .setWhen(when)
                        .setContentText(fromDevice + ": " + jo.optString("text"))
                        .setContentTitle(jo.optString("group"))
                        .setSmallIcon(R.drawable.white9696)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setContentIntent(contentIntent).setChannelId("silent");
                if (!OsMoDroid.settings.getBoolean("silentnotify", false)) {
                    notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND).setChannelId("noisy");
                }
                Notification notification = notificationBuilder.build();
                if (OsMoDroid.settings.getBoolean("chatnotify", true) || m.type != 0) {
                    LocalService.mNotificationManager.notify(OsMoDroid.mesnotifyid + channelU, notification);
                    if (LocalService.channelsmessagesAdapter != null && LocalService.currentChannel != null && LocalService.currentChannel.u == channelU && LocalService.chatVisible) {
                        LocalService.mNotificationManager.cancel(OsMoDroid.mesnotifyid + channelU);
                    }
                }


            }
            //channel.messagesstringList.add(m);
            //Collections.sort(channel.messagesstringList);
            if (LocalService.channelsmessagesAdapter != null && LocalService.currentChannel != null && LocalService.currentChannel.u == channelU) {
                boolean exist = false;
                for (ChatMessage message : LocalService.currentChannel.messagesstringList) {
                    if (message.u == m.u) {
                        exist = true;
                    }

                }
                if (!exist) {
                    LocalService.currentChannel.messagesstringList.add(m);
                    Collections.sort(LocalService.currentChannel.messagesstringList);
                    LocalService.channelsmessagesAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    void checkalarmindozemode() {
        addlog("reconencttime=" + reconnecttime + " SystemClockUptime=" + SystemClock.uptimeMillis());
        if (reconnecttime != 0 && SystemClock.uptimeMillis() > reconnecttime + RECONNECT_TIMEOUT) {
            reconnecttime=0;
            LocalService.addlog("stuck in doze mode - do recconect ");
            manager.cancel(reconnectPIntent);
            parent.sendBroadcast(new Intent(RECONNECT_INTENT));

        }

    }

    synchronized void parseEx(String toParse, boolean gcm) throws JSONException {
        if (SystemClock.uptimeMillis() > timeonline + ONLINE_TIMEOUT) {
            parent.sendBroadcast(new Intent(ONLINE_TIMEOUT_INTENT));
        }

        //LocalService.addlog("recieve " + toParse);
        if (log) {
            Log.d(this.getClass().getName(), "recive " + toParse);
        }


        if (toParse.equals("P|")) {
            LocalService.addlog("recieve pong");
            return;
        }
        JSONObject jsonObject;
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        String command = "";
        String param = "";
        String addict = "";
        try {
            command = toParse.substring(0, toParse.indexOf('|'));
        } catch (Exception e1) {
            command = toParse;
        }
        if (command.indexOf(':') != -1) {
            param = command.substring(command.indexOf(':') + 1);
            command = command.substring(0, command.indexOf(':'));
        }
        if (toParse.contains("|")) {
            addict = toParse.substring(toParse.indexOf('|') + 1);
        }

        try {
            jo = new JSONObject(addict);
        } catch (JSONException e) {
            try {
                if (log) {
                    Log.d(this.getClass().getName(), "не JSONO ");
                }
                ja = new JSONArray(addict);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                if (log) {
                    Log.d(this.getClass().getName(), "не JSONA ");
                }
            }
        }
        if (!gcm) {
            parseremovefromcommandlist(command, param);
        }

        if (jo.has("error")) {
            if (OsMoDroid.gpslocalserviceclientVisible) {
                final String str = jo.optString("error") + ' ' + jo.optString("error_description");
                Toast.makeText(localService, str, Toast.LENGTH_LONG).show();
            }
        }


        parsedata(jo, ja, command, param, addict, gcm);


    }

    private void parseremovefromcommandlist(String command, String param) {
        if (!running) {
            running = true;
        }
        Iterator<String> comIter = executedCommandArryaList.iterator();
        while (comIter.hasNext()) {
            String str = comIter.next();
            if (log) {
                Log.d(this.getClass().getName(), "ExecutedListItem: " + str);
            }
            if (str.equals(command + ':' + param) || str.equals(command)) {
                comIter.remove();
                if (log) {
                    Log.d(this.getClass().getName(), "ExecutedListItem removed: " + str);
                }
                LocalService.addlog("ExecutedListItem removed: " + str);
            }
        }
        if (log) {
            Log.d(this.getClass().getName(), "ExecuteList=" + executedCommandArryaList.toString());
        }
        LocalService.addlog("ExecuteList=" + executedCommandArryaList.toString());
        if (executedCommandArryaList.size() == 0) {
            LocalService.addlog("Cancel reconnect alarm - no commands in order");
            manager.cancel(reconnectPIntent);
            reconnecttime = 0;
            localService.refresh();
        }
    }

    private void parsedata(JSONObject jo, JSONArray ja, String command, String param, final String addict, boolean gcm) throws JSONException {

        JSONObject jsonObject;
//                if (command.equals("INIT"))
        
        if (command.equals("HISTORY")) {
            for (int i = 0; i < ja.length(); i++) {
                try {
                    jsonObject = ja.getJSONObject(i);
                    if (!jsonObject.getString("u").equals("null")) {
//                                for (TrackFile tf : LocalService.trackFileList)
//                                {
//                                    if (tf.u == Integer.parseInt(jsonObject.optString("u")))
//                                    {
                        TrackFile tr = new TrackFile(jsonObject.optString("name"), jsonObject.optLong("start")*1000, jsonObject.optInt("file_size"));
                        tr.u = jsonObject.getInt("u");
                        tr.name = jsonObject.optString("name");
                        tr.distance=jsonObject.optString("distance");
                        tr.fromServer = true;
                        tr.url = jsonObject.optString("gpx");
                        tr.image = jsonObject.optString("image");
                        boolean exist=false;
                        for  (TrackFile t: LocalService.trackFileList)
                        {
                            if(t.u==tr.u)
                            {
                                exist=true;
                            }
                        }
                        if(!exist) {
                            LocalService.trackFileList.add(0, tr);
                        }
                        Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                        while (it.hasNext()) {
                            ColoredGPX cg = it.next();
                            if (cg.u == tr.u) {
                                tr.showedonmap = true;
                            }
                        }
                        //}
                        //}

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    writeException(e);
                }
            }
            Collections.sort(LocalService.trackFileList);
            LocalService.trackFileAdapter.notifyDataSetChanged();
        }
        if (command.equals("TOKEN"))
        {
            OsMoDroid.editor.putString("udptoken", addict);
            OsMoDroid.editor.commit();
        }

        if (command.equals("MD")) {
            localService.motd = addict;
            OsMoDroid.editor.putString("modt", addict);
            OsMoDroid.editor.commit();
            localService.refresh();
        }
        if (command.equals("GS")) {
            if (param.equals("1")) {
                localService.globalsend = true;
                localService.refresh();
            }
            if (param.equals("-1")) {
                localService.globalsend = false;
                localService.refresh();
            }
        }
        if (command.equals("SOS")) {
            if (addict.equals("1")) {
                localService.sos = true;
                localService.refresh();
            }
            if (addict.equals("-1")) {
                localService.sos = false;
                localService.refresh();
            }
        }
        if (command.equals("LA")) {
            for (PermLink p : LocalService.simlimkslist) {
                if (p.u == Integer.parseInt(param)) {
                    p.active = true;
                    if (LocalService.simlinksadapter != null) {
                        LocalService.simlinksadapter.notifyDataSetChanged();
                    }
                }
            }
        }
        if (command.equals("LD")) {
            for (PermLink p : LocalService.simlimkslist) {
                if (p.u == Integer.parseInt(param)) {
                    p.active = false;
                    if (LocalService.simlinksadapter != null) {
                        LocalService.simlinksadapter.notifyDataSetChanged();
                    }
                }
            }
        }
        if (command.equals("GA")&& !jo.has("error")) {
            for (Channel ch : LocalService.channelList) {
                if (ch.u == Integer.parseInt(param)) {
                    ch.updChannel(jo);
                    ch.send = true;
                    localService.osmAndAddAllChannels();
                }
            }
            if (LocalService.channelsAdapter != null) {
                LocalService.channelsAdapter.notifyDataSetChanged();
            }
            if (log) {
                Log.d(getClass().getSimpleName(), "write group list to file");
            }
            OsMoDroid.saveObject(localService,LocalService.channelList, OsMoDroid.CHANNELLIST);
            //sendUDP("GROUP", false);
        }
        if (command.equals("GD") && !jo.has("error")) {
            for (Channel ch : LocalService.channelList) {
                if (ch.u == Integer.parseInt(param)) {
                    ch.send = false;
                    localService.osmAndDeleteChannel(ch);
                }
            }
            if (LocalService.channelsAdapter != null) {
                LocalService.channelsAdapter.notifyDataSetChanged();
            }
            if (log) {
                Log.d(getClass().getSimpleName(), "write group list to file");
            }
            OsMoDroid.saveObject(localService,LocalService.channelList, OsMoDroid.CHANNELLIST);
        }
        if (command.equals("GC")) {
            for (int k = 0; k < ja.length(); k++) {
                try {
                    addToChannelChat(Integer.parseInt(param), ja.getJSONObject(k), true);
                } catch (Exception e) {
                    writeException(e);
                    e.printStackTrace();
                }
            }
        }
        // IM:7909|[{"u":"17","from":"45694","text":"xcvxcvz","time":"2015-04-11 22:35:18"}]

        //recive IMP|["46191","\u043f\u0432\u0438\u044c\u0431\u043b\u0440","2015-04-16 22:52:13"]

        if (command.equals("GRPA")) {
            //02-08 19:50:40.608 1149-1149/com.OsMoDroid D/com.OsMoDroid.LocalService$6: recive GRPA|{"u":7515,"type":1,"group_id":"QUZM_6745","name":"tytstysy","description":"","policy":"","url":"https:\/\/osmo.mobi\/g\/reyxtidvyzysdhmn","UC":true}
            if (jo.has("UC")) {

                OsMoDroid.saveObject(localService,LocalService.channelList, OsMoDroid.CHANNELLIST);
                localService.refresh();
                stop();
                start();

            } else {
                sendUDP("GROUP");
            }
        }
        if (command.equals("NEEDSENDALARM")) {
            sendUDP("ALARM");
        }
        if (command.equals("WIDGETSOS")) {
            sendUDP("SOS");
        }
        if (command.equals("NEEDSENDNET")) {
            if (!executedCommandArryaList.contains("NET")) {

                JSONObject postjson = getNET();
                sendUDP("NET|" + postjson.toString());
            }
        }
        if (command.equals("NEEDSENDTOKEN")&&addict!=null&&!addict.equals("")) {
            sendUDP("PUSH|" + addict);
        }

        if (command.equals("NEEDSENDCHARGE")) {
            sendUDP("CHARGE|" + addict);
        }
        if (command.equals("SMS")) {
            sendUDP("T|" + addict + "X");
        }
        
        if (command.equals("PP")) {
            sendUDP("PP");
        }
        if (command.equals("GCM")) {
            OsMoDroid.editor.putBoolean("needsendgcmregid", false);
            OsMoDroid.editor.commit();
        }
        if (command.equals("DCU")) {
            needtosendpreference = false;
        }

        if (command.equals("SOFF"))
        {
            stop();
            localService.internetnotify(false);
            localService.refresh();
            start();
        }
        if (command.equals("RC")) {
            if (param.equals("PP")) {
                sendUDP("PP");
            }
            if (param.equals(OsMoDroid.SOS)) {
                Intent dialogIntent = new Intent(localService, SosActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                dialogIntent.putExtra("message", addict);
                localService.startActivity(dialogIntent);
            }
            if (param.equals(OsMoDroid.SOSEXT)) {
                Intent dialogIntent = new Intent(localService, SosActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                dialogIntent.putExtra("jo", addict);
                localService.startActivity(dialogIntent);
            }
            if (param.equals(OsMoDroid.SOS_OFF)) {
                localService.sendBroadcast(new Intent("closesos"));
            }

            if (param.equals(OsMoDroid.TRACKER_GCM_ID)) {

                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            addlog("getInstanceId failed "+ task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult();
                        if(token!=null&&!token.equals(""))
                        {
                            sendUDP("PUSH|" + token);
                        }
                    }
                }) ;
                if(! OsMoDroid.settings.getString("GCMRegId", "").equals("")) {
                    sendUDP("PUSH|" + OsMoDroid.settings.getString("GCMRegId", "no"));
                }
                sendUDP("RCR:" + OsMoDroid.TRACKER_GCM_ID + "|1");

            }
            if (param.equals(OsMoDroid.UPDATE_MOTD)) {
                localService.motd = LocalService.unescape(addict);
                OsMoDroid.editor.putString("startmessage", LocalService.unescape(addict));
                OsMoDroid.editor.commit();
                localService.refresh();
                sendUDP("RCR:" + OsMoDroid.UPDATE_MOTD + "|1");
            }

            if (param.equals(OsMoDroid.FLASH_ON)) {
                localService.alertHandler.removeCallbacks(flickRunable);
                flashOn();
                sendUDP("RCR:" + OsMoDroid.FLASH_ON + "|1");
            }
            if (param.equals(OsMoDroid.FLASH_OFF)) {
                localService.alertHandler.removeCallbacks(flickRunable);
                flashoff();
                sendUDP("RCR:" + OsMoDroid.FLASH_OFF + "|1");
            }
            if (param.equals(OsMoDroid.FLASH_BLINK)) {
                localService.alertHandler.removeCallbacks(flickRunable);
                flick();
                sendUDP("RCR:" + OsMoDroid.FLASH_BLINK + "|1");
            }
            if (param.equals(OsMoDroid.CHANGE_MOTD_TEXT)) {
                localService.motd = addict;
                localService.refresh();
                sendUDP("RCR:" + OsMoDroid.CHANGE_MOTD_TEXT + "|1");
            }


            if (param.equals(OsMoDroid.REFRESH_GROUPS)) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            File dir = new File(OsMoDroid.osmodirFile + "/OsMoDroid/channelsgpx/");
                            dir.mkdirs();
                            //if (dir.isDirectory())
                            {
                                String[] children = dir.list();
                                for (int i = 0; i < children.length; i++) {
                                    new File(dir, children[i]).delete();
                                }
                            }
                            sendUDP("GROUP");
                            sendUDP("RCR:" + OsMoDroid.REFRESH_GROUPS + "|1");
                        } catch (Exception e) {

                            e.printStackTrace();
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsString = sw.toString();
                            LocalService.addlog(exceptionAsString);
                            sendUDP("GROUP");
                            sendUDP("RCR:" + OsMoDroid.REFRESH_GROUPS + "|1");
                        }
                    }
                };
                runnable.run();


            }
            if (param.equals(OsMoDroid.REFRESH_DEVICES)) {
                sendUDP("DEVICE");
                sendUDP("RCR:" + OsMoDroid.REFRESH_DEVICES + "|1");
            }
            if (param.equals(OsMoDroid.TRACKER_FOLLOW_START)) {
                try {
                    localService.startFollow(addict);
                    sendUDP("RCR:" + OsMoDroid.TRACKER_FOLLOW_START + "|1");
                } catch (Exception e) {
                    sendUDP("RCR:" + OsMoDroid.TRACKER_FOLLOW_START + "|0");
                }
            }
            if (param.equals(OsMoDroid.TRACKER_FOLLOW_STOP)) {
                localService.stopFollow();
                sendUDP("RCR:" + OsMoDroid.TRACKER_FOLLOW_STOP + "|1");
            }
            if (param.equals(OsMoDroid.TRACKER_SESSION_START)) {
                if (!localService.state) {
                    localService.startServiceWork(true);
                }
                sendUDP("RCR:" + OsMoDroid.TRACKER_SESSION_START + "|1");
            }
            if (param.equals(OsMoDroid.TRACKER_SESSION_STOP)) {
                if (localService.state) {
                    localService.stopServiceWork(true);
                }
                sendUDP("RCR:" + OsMoDroid.TRACKER_SESSION_STOP + "|1");
            }
            if (param.equals(OsMoDroid.TRACKER_SESSION_CONTINUE)) {
                if (localService.paused) {
                    localService.startServiceWork(false);
                }
                sendUDP("RCR:" + OsMoDroid.TRACKER_SESSION_PAUSE + "|1");
            }
            if (param.equals(OsMoDroid.TRACKER_SESSION_PAUSE)) {
                if (localService.state) {
                    localService.setPause(true);
                    localService.stopServiceWork(false);
                }
                sendUDP("RCR:" + OsMoDroid.TRACKER_SESSION_PAUSE + "|1");
            }
            if (param.equals(OsMoDroid.TTS)) {
                if (OsMoDroid.settings.getBoolean("ttsremote", false) && localService.tts != null) {
                    localService.tts.speak(addict, TextToSpeech.QUEUE_ADD, null);
                }
            }
            if (param.equals(OsMoDroid.SIGNAL_STATUS)) {
                sendUDP("RCR:" + OsMoDroid.SIGNAL_STATUS + '|' + (localService.signalisationOn ? 1 : 0));
            }
            if (param.equals(OsMoDroid.ALARM_ON)) {
                localService.playAlarmOn();
                sendUDP("RCR:" + OsMoDroid.ALARM_ON + "|1");
            }
            if (param.equals(OsMoDroid.ALARM_OFF)) {
                localService.playAlarmOff();
                sendUDP("RCR:" + OsMoDroid.ALARM_OFF + "|1");
            }
            if (param.equals(OsMoDroid.SIGNAL_ON)) {
                localService.enableSignalisation();
                sendUDP("RCR:" + OsMoDroid.SIGNAL_ON + "|1");
            }
            if (param.equals(OsMoDroid.SIGNAL_OFF)) {
                localService.disableSignalisation();
                sendUDP("RCR:" + OsMoDroid.SIGNAL_OFF + "|1");
            }
            if (param.equals(OsMoDroid.TRACKER_BATTERY_INFO)) {
                try {
                    localService.batteryinfo(localService);
                } catch (JSONException e) {
                    e.printStackTrace();
                    writeException(e);
                }
            }
            if (param.equals(OsMoDroid.TRACKER_SATELLITES_INFO)) {
                try {
                    localService.satelliteinfo(localService);
                } catch (JSONException e) {
                    writeException(e);
                    e.printStackTrace();
                }
            }
            if (param.equals(OsMoDroid.TRACKER_SYSTEM_INFO)) {
                try {
                    localService.systeminfo(localService);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (param.equals(OsMoDroid.TRACKER_WIFI_INFO)) {
                try {
                    localService.wifiinfo(localService);
                } catch (JSONException e) {
                    writeException(e);
                    e.printStackTrace();
                }

            }
            if (param.equals(OsMoDroid.WIDGETINFO)) {
                localService.sendwidgetinfo(localService);
            }
            if (param.equals(OsMoDroid.TRACKER_WIFI_OFF)) {
                localService.wifioff(localService);
            }
            if (param.equals(OsMoDroid.TRACKER_WIFI_ON)) {
                localService.wifion(localService);
            }
            if (param.equals(OsMoDroid.TRACKER_VIBRATE)) {
                localService.vibrate(localService, 3000);
            }
            if (param.equals(OsMoDroid.TRACKER_EXIT)) {
                sendUDP("RCR:" + OsMoDroid.TRACKER_EXIT + "|1");
                LocalService.gcmtodolist.clear();
                localService.addlog("set connectcompleted=true");
                LocalService.connectcompleted = true;
                OsMoDroid.saveObject(localService,LocalService.gcmtodolist, OsMoDroid.GCMTODOLIST);
                localService.stopSelf();
                System.exit(0);
            }
            if (param.equals(OsMoDroid.TRACKER_GET_PREFS)) {
                localService.getpreferences(localService);
            }
            if (param.equals(OsMoDroid.TRACKER_SET_PREFS)) {
                localService.setpreferences(jo, localService);
            }
            if (param.equals(OsMoDroid.WHERE_GPS_ONLY)) {
                sendUDP("RCR:" + OsMoDroid.WHERE_GPS_ONLY + "|1");
                localService.where = true;
                List<String> list = myManager.getProviders(true);
                if (list.contains(LocationManager.GPS_PROVIDER)) {
                    if (!localService.state) {
                        localService.alertHandler.post(new Runnable() {
                            public void run() {
                                if (ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                myManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, localService.singleLocationListener, null);

                                if (log) {
                                    addlog("подписались на GPS");
                                }
                            }
                        });

                    }
                }

                localService.alertHandler.postDelayed(new Runnable() {
                    public void run() {
                        addlog("отписались");
                        myManager.removeUpdates(localService.singleLocationListener);
                        localService.where = false;
                    }
                }, 5 * 60 * 1000);
            }
            if (param.equals(OsMoDroid.WHERE_NETWORK_ONLY)) {
                sendUDP("RCR:" + OsMoDroid.WHERE_NETWORK_ONLY + "|1");
                localService.where = true;
                List<String> list = myManager.getProviders(true);
                if (list.contains(LocationManager.NETWORK_PROVIDER)) {
                    if (!localService.state) {
                        localService.alertHandler.post(new Runnable() {
                            public void run() {
                                if (ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                myManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localService.singleLocationListener, null);

                                if (log) {
                                    addlog("подписались на NETWORK");
                                }
                            }
                        });

                    }
                }

                localService.alertHandler.postDelayed(new Runnable() {
                    public void run() {
                        addlog("отписались");
                        myManager.removeUpdates(localService.singleLocationListener);
                        localService.where = false;
                    }
                }, 5 * 60 * 1000);
            }
            if (param.equals(OsMoDroid.WHERE)) {
                sendUDP("RCR:" + OsMoDroid.WHERE + "|1");
                localService.where = true;
                List<String> list = myManager.getProviders(true);
                if (list.contains(LocationManager.GPS_PROVIDER)) {
                    if (!localService.state) {
                        localService.alertHandler.post(new Runnable() {
                            public void run() {
                                if (ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                myManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, localService.singleLocationListener, null);

                                if (log) {
                                    addlog("подписались на GPS");
                                }
                            }
                        });
                        if (list.contains(LocationManager.NETWORK_PROVIDER)) {
                            localService.alertHandler.postDelayed(new Runnable() {
                                public void run() {
                                    if (ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        // here to request the missing permissions, and then overriding
                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                        //                                          int[] grantResults)
                                        // to handle the case where the user grants the permission. See the documentation
                                        // for ActivityCompat#requestPermissions for more details.
                                        return;
                                    }
                                    myManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localService.singleLocationListener, null);
                                    if (log) {
                                        addlog("подписались на NETWORK");
                                    }
                                }
                            }, 30000);
                        }
                    }
                } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
                    localService.alertHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocalService.serContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            myManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, localService.singleLocationListener, null);
                                                        if (log)
                                                            {
                                                                addlog( "подписались на NETWORK");
                                                            }
                                                    }
                                            }, 0);
                                    }
                                localService.alertHandler.postDelayed(new Runnable()
                                {
                                    public void run()
                                        {
                                            addlog( "отписались");
                                            myManager.removeUpdates(localService.singleLocationListener);
                                            localService.where=false;
                                        }
                                }, 60000);
                            }
                        if(param.equals(OsMoDroid.SOS_DEPRESS))
                            {
                                localService.sos=false;
                                localService.refresh();
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
                        sendUDP("GROUP");
                    }
                if (command.equals("DS"))
                    {
                        sendUDP("DEVICE");
                    }
                if (command.equals("DSS"))
                    {
                        sendUDP("DEVICE");
                    }
                if (command.equals("DSA"))
                    {
                        sendUDP("DEVICE");
                    }
                if (command.equals("DSD"))
                    {
                        sendUDP("DEVICE");
                    }


                if (command.equals("GROUP"))
                    {
                        ArrayList<Channel> recievedChannelList = new ArrayList<Channel>();
                        for (int i = 0; i < ja.length(); i++)
                            {
                                try
                                    {
                                        jsonObject = ja.getJSONObject(i);
                                        if (!jsonObject.getString("u").equals("null"))
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
                        for(Channel ch:LocalService.channelList)
                            {
                                if(!recievedChannelList.contains(ch))
                                    {
                                        localService.osmAndDeleteChannel(ch);
                                    }
                            }
                        LocalService.channelList.retainAll(recievedChannelList);
                        recievedChannelList.removeAll(LocalService.channelList);
                        LocalService.channelList.addAll(recievedChannelList);
                        for(Channel ch:recievedChannelList)
                            {
                                localService.osmandaddchannel(ch);
                            }
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
                        OsMoDroid.saveObject(localService,LocalService.channelList, OsMoDroid.CHANNELLIST);
//                        for (Channel ch : LocalService.channelList)
//                            {
//                                if (ch.send)
//                                    {
//                                        sendUDP("GC:" + ch.u, false);
//                                    }
//                            }
                        //sendUDP("PG");
                        for (String s: LocalService.gcmtodolist)
                            {
                                try
                                    {
                                        parseEx(s,true);
                                    }
                                catch (Exception e)
                                    {
                                        e.printStackTrace();
                                        StringWriter sw = new StringWriter();
                                        e.printStackTrace(new PrintWriter(sw));
                                        String exceptionAsString = sw.toString();
                                        LocalService.addlog(exceptionAsString);
                                    }
                            }
                        LocalService.gcmtodolist.clear();
                        localService.addlog("set connectcompleted=true");
                        LocalService.connectcompleted =true;
                        OsMoDroid.saveObject(localService,LocalService.gcmtodolist, OsMoDroid.GCMTODOLIST);
                        localService.osmAndAddAllChannels();
                        localService.bitmapmapview();
                    }
                if (command.equals("GL"))
                    {
                        if(addict.equals("1"))
                            {
                                Channel chToDel = null;
                                for (Channel ch : LocalService.channelList)
                                    {
                                        if (ch.u == Integer.parseInt(param))
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
                                OsMoDroid.saveObject(localService,LocalService.channelList, OsMoDroid.CHANNELLIST);
                            }
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
                                        pl.description=jsonObject.optString("description");

                                        if (jsonObject.optInt("active") == 1)
                                        {
                                            pl.active = true;
                                        }
                                        else
                                        {
                                            pl.active = false;
                                        }
                                        pl.count=jsonObject.optInt("count");
                                        pl.start=jsonObject.optLong("start");
                                        pl.finish=jsonObject.optLong("finish");
                                        pl.time=jsonObject.optLong("time");
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
                                pl.description = jo.optString("description","");
                                LocalService.simlimkslist.add(pl);

                                if (jo.optInt("active") == 1)
                                {
                                    pl.active = true;
                                }
                                else
                                {
                                    pl.active = false;
                                }
                                pl.count=jo.optInt("count");
                                pl.start=jo.optLong("start");
                                pl.finish=jo.optLong("finish");
                                pl.time=jo.optLong("time");
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
                                                                                    if (dev.state != 1 && jsonObject.getInt("state") == 1 &&dev.state!=-1)
                                                                                        {
                                                                                            notifydevicemonitoring(ch,dev, true);
                                                                                        }
                                                                                    if ((dev.state == 1||dev.state==-1) && jsonObject.getInt("state") == 0)
                                                                                        {
                                                                                            notifydevicemonitoring(ch,dev, false);
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
                        localService.bitmapmapview();
                    }


                if (command.equals("GP"))
                    {
                        long eid=0;
                        if (jo.has("refresh"))
                            {
                                if (jo.getBoolean("refresh"))
                                    {
                                        sendUDP("GROUP");
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
                                        JSONArray near = jo.optJSONArray("near");
                                        JSONArray alarm = jo.optJSONArray("alarm");
                                        JSONArray join = jo.optJSONArray("join");
                                        JSONArray leave = jo.optJSONArray("leave");
                                        //GP:4971|{"join":[{"u":12345,"nick":"Vasya","time":1482106191,"e":"54321"}]
                                        //GP:6342|{"leave":[{"u":12345,"nick":"Vasya","time":1482106191,"e":"54321"}]
                                        //GP:1234|{"alarm":{"gu"=>123,"name"=>"Vasya"}
                                        if(join!=null)
                                            {
                                                for (int i=0; i<join.length();i++)
                                                    {
                                                        jsonObject=join.getJSONObject(i);
                                                        long e1=jsonObject.optLong("e");
                                                        if (e1>eid)
                                                            {
                                                                eid=e1;
                                                            }

                                                        String nick = jsonObject.optString("nick");

                                                        String time="";
                                                        String messageText="";
                                                        time= OsMoDroid.sdf.format(new Date((timeshift+jsonObject.optLong("time"))*1000));
                                                        messageText = ch.name + ": "+nick + ' ' +parent.getString(R.string.join_to_group);
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
                                        if(leave!=null)
                                            {
                                                for (int i=0; i<leave.length();i++)
                                                    {
                                                        jsonObject=leave.getJSONObject(i);
                                                        long e1=jsonObject.optLong("e");
                                                        if (e1>eid)
                                                            {
                                                                eid=e1;
                                                            }

                                                        String nick = jsonObject.optString("nick");

                                                        String time="";
                                                        String messageText="";
                                                        time= OsMoDroid.sdf.format(new Date((timeshift+jsonObject.optLong("time"))*1000));
                                                        messageText = ch.name + ": "+nick + ' ' +parent.getString(R.string.leave_group);
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

                                        if(alarm!=null)
                                            {
                                                for (int i=0; i<alarm.length();i++)
                                                    {
                                                        jsonObject=alarm.getJSONObject(i);
                                                        long e1=jsonObject.optLong("e");
                                                        if (e1>eid)
                                                            {
                                                                eid=e1;
                                                            }
                                                        String name = jsonObject.optString("nick");
                                                        String time="";

                                                        time= OsMoDroid.sdf.format(new Date((timeshift+jsonObject.optLong("time"))*1000));


                                                        String messageText =ch.name+": "+parent.getString(R.string.alarmengaged)+' '+name;


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

                                        if(near!=null)
                                            {
                                                for (int i=0; i<near.length();i++)
                                                    {
                                                        jsonObject=near.getJSONObject(i);
                                                        long e1=jsonObject.optLong("e");
                                                        if (e1>eid)
                                                            {
                                                                eid=e1;
                                                            }
                                                        String type = jsonObject.optString("type");
                                                        String name = jsonObject.optString("name");
                                                        String nick = jsonObject.optString("nick");
                                                        int mode = jsonObject.optInt("mode");
                                                        String time="";
                                                        String messageText="";
                                                        time= OsMoDroid.sdf.format(new Date((timeshift+jsonObject.optLong("time"))*1000));
                                                        String who ="";
                                                        if(ch.gu==jsonObject.optInt("u"))
                                                            {
                                                                who=parent.getString(R.string.you);
                                                            }
                                                        else
                                                            {
                                                        for (Device dev : ch.deviceList)
                                                            {
                                                                if (dev.u == jsonObject.optInt("u"))
                                                                    {
                                                                        who=dev.name;
                                                                    }
                                                            }
                                                    }

                                                        if(type.equals("user"))
                                                            {
                                                                if(mode==1)
                                                                    {
                                                                         messageText = ch.name + ": " + who + ' ' + parent.getString(R.string.approached) + ' '+nick+' ' + jsonObject.optString("dst");
                                                                    }
                                                                if(mode==0)
                                                                    {
                                                                         messageText = ch.name + ": " + who + ' ' + parent.getString(R.string.getaway) + ' ' +nick+' '+ jsonObject.optString("dst");
                                                                    }
                                                            }
                                                        if(type.equals("waypoint"))
                                                            {
                                                                if(mode==1)
                                                                    {
                                                                         messageText = ch.name + ": "+who + ' ' + parent.getString(R.string.approached) + ' ' +name +' '+ jsonObject.optString("dst");
                                                                    }
                                                                if(mode==0)
                                                                    {
                                                                         messageText = ch.name + ": "+who + ' ' +parent.getString(R.string.getaway) + ' ' +name +' '+ jsonObject.optString("dst");
                                                                    }
                                                            }





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


                                        if(geoevents!=null)
                                            {
                                                for (int i = 0; i<geoevents.length();i++)
                                                    {
                                                        jsonObject=geoevents.getJSONObject(i);
                                                        long e1=jsonObject.optLong("e");
                                                        if (e1>eid)
                                                            {
                                                                eid=e1;
                                                            }
                                                        String name = jsonObject.optString("nick");
                                                        String time="";

                                                        time= OsMoDroid.sdf.format(new Date((timeshift+jsonObject.optLong("time"))*1000));


                                                        String messageText =ch.name+": "+name+' '+ (jsonObject.optInt("type")==1?parent.getString(R.string.enterin):parent.getString(R.string.exitfrom)) +" "+jsonObject.optString("name");


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
                                                        long e1=jsonObject.optLong("e");
                                                        if (e1>eid)
                                                            {
                                                                eid=e1;
                                                            }
                                                        String messageText =ch.name+": "+jsonObject.optString("text");
                                                        String time="";

                                                        time= OsMoDroid.sdf.format(new Date((timeshift+jsonObject.optLong("time"))*1000));
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
                                                        long e1=jsonObject.optLong("e");
                                                        if (e1>eid)
                                                            {
                                                                eid=e1;
                                                            }
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
                                                                if(ch.gpxList.contains(cgpx))
                                                                    {
                                                                       ch.gpxList.get(ch.gpxList.indexOf(cgpx)).color=cgpx.color;
                                                                       if(jsonObject.optBoolean("reload"))
                                                                       {
                                                                           cgpx.status = ColoredGPX.Statuses.DOWNLOADING;
                                                                           Netutil.downloadfile(ch, cgpx.url, cgpx);
                                                                       }
                                                                    }
                                                                else
                                                                    {
                                                                        ch.gpxList.add(cgpx);
                                                                        cgpx.status = ColoredGPX.Statuses.DOWNLOADING;
                                                                        Netutil.downloadfile(ch, cgpx.url, cgpx);
                                                                    }
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
                                                                long e1=jsonObject.optLong("e");
                                                                if (e1>eid)
                                                                    {
                                                                        eid=e1;
                                                                    }
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
                                                                                localService.osmanddelpoint(ch,pointToDel);
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
                                                                                        p.time= OsMoDroid.sdf.format(new Date((timeshift+jsonObject.optLong("time"))*1000));
                                                                                    }
                                                                            }
                                                                        if (!exist)
                                                                            {
                                                                                ch.pointList.add(new Point(jsonObject));
                                                                                localService.osmandaddpoint(ch,new Point(jsonObject));
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
                                                                long e1=jsonObject.optLong("e");
                                                                if (e1>eid)
                                                                    {
                                                                        eid=e1;
                                                                    }
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
                                                                                        localService.osmanddeldev(ch,deviceToDel);
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

                                                                                                        if(jsonObject.has("time"))
                                                                                                            {
                                                                                                                try
                                                                                                                    {
                                                                                                                        dev.updatated=  (timeshift+jsonObject.optLong("time"))*1000;
                                                                                                                    }
                                                                                                                catch (IllegalArgumentException e)
                                                                                                                    {
                                                                                                                        e.printStackTrace();
                                                                                                                    }
                                                                                                            }
                                                                                                        float newlat = Float.parseFloat(jsonObject.getString("lat"));
                                                                                                        float newlon = Float.parseFloat(jsonObject.getString("lon"));
                                                                                                        if (newlat != dev.lat & newlon != dev.lon && (System.currentTimeMillis() - dev.updatated) > 5 * 60 * 1000) {
                                                                                                            dev.lat = newlat;
                                                                                                            dev.lon = newlon;
                                                                                                            localService.osmandupdDevice(dev);
                                                                                                            //notifydevicemonitoring(dev,true);
                                                                                                        }
                                                                                                    }
                                                                                                    catch (NumberFormatException e)
                                                                                                    {
                                                                                                        writeException(e);
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                                getDevtrace(jsonObject, dev);

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

                                                                                                                if (dev.state != 1 && jsonObject.getInt("state") == 1)
                                                                                                                    {
                                                                                                                        notifydevicemonitoring(ch,dev, true);
                                                                                                                    }
                                                                                                                if (dev.state == 1 && jsonObject.getInt("state") == 0)
                                                                                                                    {
                                                                                                                        notifydevicemonitoring(ch,dev, false);
                                                                                                                    }
                                                                                                                dev.state = jsonObject.getInt("state");

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

                                                                                                if(jsonObject.has("time"))
                                                                                                    {
                                                                                                        try
                                                                                                            {
                                                                                                                dev.updatated=  (timeshift+jsonObject.optLong("time"))*1000;
                                                                                                            }
                                                                                                        catch (IllegalArgumentException e)
                                                                                                            {
                                                                                                                e.printStackTrace();
                                                                                                            }
                                                                                                    }
                                                                                                getDevtrace(jsonObject, dev);
                                                                                                ch.deviceList.add(dev);
                                                                                                localService.osmandadddevice(ch,dev);
                                                                                                Collections.sort(ch.deviceList);
                                                                                                if (dev.state == 1 )
                                                                                                    {
                                                                                                        notifydevicemonitoring(ch,dev, true);
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
                        OsMoDroid.saveObject(localService,LocalService.channelList, OsMoDroid.CHANNELLIST);
                        if(gcm)
                            {
                                sendUDP("GPI:" + param);
                            }
                        else
                            {
                                sendUDP("GPR:" + param+'|'+eid);
                            }
                    }

                if (command.equals("GPC"))
                    {
                        addToChannelChat(Integer.parseInt(param), jo, false);
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
                                                                localService.osmandadddevice(ch,updatedDev);
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
                        localService.bitmapmapview();
                    }

            }
        @NonNull
        private JSONObject getNET()
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
                                WifiManager wifi = (WifiManager) localService.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                WifiInfo wifiInfo = wifi.getConnectionInfo();
                                String wifiname = wifiInfo.getSSID();
                                String mac = wifiInfo.getMacAddress();
                                //String strength = Integer.toString(wifiInfo.getRssi());
                                postjson.put("network_ssid", wifiname.replaceAll("\"", ""));
                                //postjson.put("network_mac", mac);

                            }
                    }
                catch (Exception e)
                    {

                    }
                return postjson;
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
                                                dev.devicePath.add(new SerPoint(new PointL(gp.getLatitudeE6(),gp.getLongitudeE6())));

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
                                                LocalService.addlog(d.substring(idxS + 1, i));
                                                if(!OsMoDroid.settings.getBoolean("imperial",false))
                                                    {
                                                        dev.speed = OsMoDroid.df0.format((((Float.parseFloat(d.substring(idxS + 1, i)) * 3.6))));
                                                    }
                                                else
                                                    {
                                                        dev.speed = OsMoDroid.df0.format((((Float.parseFloat(d.substring(idxS + 1, i)) * 3.6*0.621371))));
                                                    }
                                                break;
                                            }
                                    }
                            }
                    }
                int idxT = d.indexOf("T");
                if(idxT!=-1)
                    {
                        for (int i = idxT + 1; i <= d.length()-1 ; i++)

                            {

                                if (!Character.isDigit(d.charAt(i)) || i == (d.length()-1 ))

                                    {

                                                LocalService.addlog(d.substring(idxT + 1, i+1));
                                                dev.updatated = 1000 * Long.parseLong(d.substring(idxT + 1, i+1));
                                                break;

                                    }
                    }
                }
                localService.alertHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                        {
                            GeoPoint gp = new GeoPoint(dev.lat, dev.lon);
                            if(dev.state==1)
                                {
                                    dev.devicePath.add(new SerPoint(new PointL(gp.getLatitudeE6(),gp.getLongitudeE6())));
                                }
                            if (LocalService.devlistener != null)
                                {
                                    LocalService.devlistener.onDeviceChange(dev);
                                }
                            localService.osmandupdDevice(dev);
                        }
                });
                //}
            }
        void ondisconnect()
            {
            }
        void notifydevicemonitoring(Channel ch,Device dev,boolean start)
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

                messageText = messageText +ch.name+": " +localService.getString(R.string.monitoringondevice) + dev.name + " " + status;
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

                localService.addlog("set connectcompleted=false");
                LocalService.connectcompleted=false;
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
                localService.alertHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ondisconnect();
                    }
                });
                localService.alertHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                            {
                                LocalService.addlog("setReconnectAlarm on error");
                                setReconnectAlarm(true);
                                LocalService.addlog("setReconnectAlarm on error setted " + SystemClock.elapsedRealtime());


                            }
                    });
                if (localService.isOnline())
                    {
                        addlog("is online in recconect on error set");
                    }
                else
                    {
                        addlog("not online in recconect on error set");
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
                                        long servertime=result.Jo.optLong("time");
                                        if(servertime>0)
                                        {
                                            OsMoDroid.timeshift=System.currentTimeMillis()/1000-servertime;
                                            addlog("timesift="+ OsMoDroid.timeshift+" sec");
                                        }

                                        try
                                            {

                                            }
                                        catch (IllegalThreadStateException e)
                                            {
                                                Log.d(getClass().getSimpleName(), "hernya");
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
                                        localService.notifywarnactivity(LocalService.unescape(result.Jo.optString("error_description")), true, OsMoDroid.NOTIFY_EXPIRY_USER);
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
//                        if (socketRetryInt > 3 && !OsMoDroid.settings.getBoolean("understand", false))
//                            {
//                                localService.notifywarnactivity(localService.getString(R.string.checkfirewall), false, OsMoDroid.NOTIFY_NO_CONNECT);
//                            }
                        Log.d(getClass().getSimpleName(), "herrrr");
                        setReconnectOnError();
                    }
            }
        
         private  class UDPWriter implements  Runnable {


             public  Handler handler;
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


                                 try {
                                     InetAddress IPAddress = InetAddress.getByName("osmo.mobi");
                                     byte[] sendData = new byte[1024];
                                     sendData = b.getString("write").getBytes();
                                     DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,// workserverint
                                             24827);
                                     sendPacket.setLength(sendData.length);
                                     clientSocket.send(sendPacket);

                                     Log.d(this.getClass().getName(), "Write UDP OK " );
                                 } catch (Exception e) {
                                     e.printStackTrace();
                                     Log.d(this.getClass().getName(), "Write UDP Exception " +e.toString());
                                 }


                         super.handleMessage(msg);
                     }
                 };
                 Looper.loop();
             }
         }

         private class UDPReader implements Runnable {
             private String str;

             @Override
             public void run() {

                 while (!Thread.currentThread().isInterrupted()) {
                     LocalService.addlog("create datagram reader");
                     byte[] msg = new byte[1000];
                     DatagramPacket dp = new DatagramPacket(msg, msg.length);
                    // DatagramSocket ds = null;

                     try {
                         //ds = new DatagramSocket(0);
                         clientSocket.receive(dp);
                         str = new String(msg, 0, dp.getLength());
                         LocalService.addlog("receive  "+ str);
                         Message message = new Message();
                         Bundle b = new Bundle();
                         b.putString("udpread", str);
                         message.setData(b);
                         if (localService.alertHandler != null) {
                             localService.alertHandler.sendMessage(message);
                         }
                     } catch (IOException e) {
                         e.printStackTrace();

                     }

                 }
             }
         }

        
        
        void flashOn()
            {
                flicking=true;
                try
                    {
                        camera = Camera.open();
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                if (camera == null)
                    {
                        addlog("no camera");
                  //      sendUDP("RCR:" + OsMoDroid.FLASH_ON + "|0", false);
                    }
                else
                    {

                        try
                            {
                                Camera.Parameters p = camera.getParameters();
                                List supportedFlashModes = p.getSupportedFlashModes();
                                if(supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH ))
                                    {
                                        p.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
                                    }
                                else if(supportedFlashModes.contains(Camera.Parameters. FLASH_MODE_ON ))
                                    {
                                        p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                    }

                                camera.setParameters(p);
                                camera.startPreview();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                    {
                                        camera.setPreviewTexture(new SurfaceTexture(0));
                                    }
                                camera.autoFocus(new Camera.AutoFocusCallback()
                                    {
                                        @Override
                                        public void onAutoFocus(boolean success, Camera camera)
                                            {
                                            }
                                    });
                            //    sendUDP("RCR:" + OsMoDroid.FLASH_ON + "|1", false);
                            }
                        catch (Exception e)
                            {
                                addlog("cannot start preview");
                               // sendUDP("RCR:" + OsMoDroid.FLASH_ON + "|0", false);
                            }
                    }
            }
        void flashoff()
            {
                flicking=false;
                if (camera != null)
                    {
                        camera.stopPreview();
                        camera.release();
                        camera = null;

                    }
                else
                    {

                    }

            }
        void flick()
            {
                flicking=true;
                localService.alertHandler.post(flickRunable);
            }
        Runnable flickRunable =new Runnable()
        {
            @Override
            public void run()
            {
                if(flicking)
                    {
                        flashoff();
                    }
                else
                    {
                        flashOn();
                    }
                localService.alertHandler.postDelayed(this,150);
            }
        };
    }
