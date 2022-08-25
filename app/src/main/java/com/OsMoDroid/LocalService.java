package com.OsMoDroid;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.OsMoDroid.Netutil.MyAsyncTask;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.messaging.FirebaseMessaging;


import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;
import net.osmand.aidl.map.ALatLon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.views.MapView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.OsMoDroid.IM.writeException;
import static com.OsMoDroid.OsMoDroid.context;
import static com.OsMoDroid.OsMoDroid.loadObject;
import static com.OsMoDroid.OsMoDroid.osmodirFile;
import static java.lang.Math.abs;
import static org.osmdroid.util.GeometryMath.DEG2RAD;
import static org.osmdroid.util.constants.GeoConstants.RADIUS_EARTH_METERS;

public class LocalService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener, LocationListener, GpsStatus.Listener, TextToSpeech.OnInitListener, ResultsListener, SensorEventListener, OsmAndHelper.OnOsmandMissingListener

    {
        public static JSONArray transport = new JSONArray();
        public static Device mydev = new Device();
        //public static List<Point> traceList = new ArrayList<Point>();
        public static ArrayList<ColoredGPX> showedgpxList = new ArrayList<ColoredGPX>();
        public static int lastGroupSpinnerSelectedPosition=0;
        static boolean connectcompleted =false;
        public static boolean osmandbind=false;
        static TrackFileAdapter trackFileAdapter;
        static ArrayList<TrackFile> trackFileList = new ArrayList<TrackFile>();
        static int transportid;
        static int privatemode=1;
        public long sessionopentime;
        boolean binded = false;
        private SensorManager mSensorManager;
        private Sensor mAccelerometer;
        final double calibration = SensorManager.STANDARD_GRAVITY;
        float currentAcceleration;
        static final int OSMODROID_ID = 1;
        Boolean sessionstarted = false;
        Boolean globalsend = false;
        static Boolean sos = false;
        Boolean signalisationOn = false;
        int notifyid = 2;
        boolean followmonstarted=false;
       // int gpson;
       // int gpsoff;
       // int ineton;
       // int inetoff;
        int sendpalyer;
       // int startsound;
       // int stopsound;
        static int alarmsound;
        int signalonoff;
        static SoundPool soundPool;
        private Netutil.MyAsyncTask starttask;
        private Intent in;
        public boolean mayak = false;
        private boolean glonas = false;
        private boolean playsound = false;
        boolean sendsound = false;
        private boolean vibrate;
        private boolean usecourse;
        private int vibratetime;
        private double brng;
        private double brng_gpx;
        private double prevbrng;
        private double prevbrng_gpx;
        private float speedbearing;
        private float bearing;
        private int speed;
        private boolean beepedon = false;
        private boolean beepedoff = false;
        private boolean gpsbeepedon = false;
        private boolean gpsbeepedoff = false;
        static float  maxspeed = 0;
        int totalclimb;
        int altitude=Integer.MIN_VALUE;
        int prevaltitude=Integer.MIN_VALUE;
        int[] altitudesamples = {Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE};
        float avgspeed;
        float currentspeed;
        long timeperiod = 0;
        float workdistance;
        long workmilli = 0;
        private boolean firstsend = true;
        private boolean sended = true;
        private boolean gpx = false;
        private boolean live = true;
        private int hdop;
        private boolean fileheaderok = false;
        private File gpxfile = null;
        private int period;
        private int distance;
        private String hash;
        private int n;
        private String position;
        String sendresult = "";
        public static LocationManager myManager;
        private Location prevlocation;
        public static Location currentLocation;
        private Location prevlocation_gpx;
        private Location prevlocation_spd;
        private String URLadr;
        private Vibrator vibrator;
        private PowerManager pm;
        private WakeLock wakeLock;
        private WakeLock LocwakeLock;
        private WakeLock SendwakeLock;
        private int speedbearing_gpx;
        private int bearing_gpx;
        private long lastgpslocationtime = 0;
        private int hdop_gpx =30;
        private int period_gpx;
        private int distance_gpx;
        private int speed_gpx;
        int sendcounter;
        int writecounter = 0;
        int buffercounter = 0;
        BroadcastReceiver receiver;
        BroadcastReceiver checkreceiver;
        BroadcastReceiver onlinePauseforStartReciever;
        BroadcastReceiver batteryReciever;
        private final IBinder mBinder = new LocalBinder();
        private String gpxbuffer = new String();
        private String satellite = "";
        private String accuracy = "";
        private boolean usebuffer = true;

        static NotificationManager mNotificationManager;
        private String lastsendforbuffer = "First";
        private long lastgpsontime = 0;
        private long lastgpsofftime = 0;
        private long notifyperiod = 30000;
        private AlarmManager am;
        StringBuilder stringBuilder = new StringBuilder();
        PendingIntent pi;
        private Object[] mStartForegroundArgs = new Object[2];
        private Object[] mStopForegroundArgs = new Object[1];
        private String pass;
        private String lastsay = "a";
        Boolean state = false;
        int gpsperiod;
        int gpsdistance;
        long prevnetworklocationtime = 0;
        StringBuilder buffersb = new StringBuilder(327681);
        StringBuilder lastbuffersb = new StringBuilder(327681);
        StringBuilder sendedsb = new StringBuilder(327681);
        private int lcounter = 0;
        private int scounter = 0;
        protected boolean firstgpsbeepedon = false;
        static IM myIM;
        //static IM trackerIM;
        TextToSpeech tts;
        private int langTTSavailable = -1;
        String text;
        //static SharedPreferences settings;
        int batteryprocent = -1;
        int plugged = -1;
        int temperature = -1;
        int voltage = -1;
        public static List<Channel> channelList = new ArrayList<Channel>();
        public static List<JSONObject> waypointsList = new ArrayList<JSONObject>();

        public static Channel currentChannel;
        public static List<Device> currentchanneldeviceList = new ArrayList<Device>();
        public static ArrayList<String> messagelist = new ArrayList<String>();
        public static ArrayList<String> debuglist = new ArrayList<String>();
        public static ArrayList<PermLink> simlimkslist = new ArrayList<PermLink>();
        public static PermLinksAdapter simlinksadapter;
        public static List<ChatMessage> chatmessagelist = new ArrayList<ChatMessage>();
        public static ArrayList<String> gcmtodolist = new ArrayList<String>();
        public static Device currentDevice;

        public static ChannelsAdapter channelsAdapter;
        public static ChannelsDevicesAdapter channelsDevicesAdapter;
        public static ArrayAdapter<ChatMessage> channelsmessagesAdapter;
        public static ArrayAdapter<String> debugAdapter;
        public static DeviceChatAdapter chatmessagesAdapter;
        static Context serContext;
        protected static boolean uploadto = false;
        public static DeviceChange devlistener;
        public static boolean channelsupdated = false;
        public static boolean chatVisible = false;
        public static String currentItemName = "";
        public static ArrayAdapter<String> notificationStringsAdapter;
        public static ArrayList<Entry> speeddistanceEntryList = new ArrayList<Entry>();
        public static ArrayList<Entry> avgspeeddistanceEntryList = new ArrayList<Entry>();
        public static ArrayList<Entry> altitudedistanceEntryList = new ArrayList<Entry>();
        public static ArrayList<String> distanceStringList = new ArrayList<String>();
        private UploadServiceBroadcastReceiver uploadServiceBroadcastReceiver = new UploadServiceBroadcastReceiver() {
            @Override
            public void onProgress(Context context, UploadInfo uploadInfo) {
                // your implementation
            }

            @Override
            public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                addlog("Error in upload with ID: " + uploadInfo.getUploadId() + ". "
                        + exception!=null? String.valueOf(exception) :"");
                if(exception!=null)writeException(exception);
            }

            @Override
            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                // your implementation
            }

            @Override
            public void onCancelled(Context context, UploadInfo uploadInfo) {
                // your implementation
            }
        };


        LocationListener followLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myIM.sendToServer(locationtoSending(location)+'F',false);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        LocationListener singleLocationListener = new LocationListener()
            {
                @Override
                public void onLocationChanged(Location location)
                    {
                        if (where)
                            {

                                //  тогда RCR:12 вроде (пример опять же с батарейки) и json lat lon hdop altitude (остальное не знаю надо ли, можно и speed)
                                //  RCR:12|{"lat"60.534543,"lon":30.1244,"speed":4.2,"hdop":500,"altitude":1200}
                                LocalService.addlog("send on where");
                                JSONObject postjson = new JSONObject();
                                try
                                    {
                                        postjson.put("lat",  OsMoDroid.df6.format(location.getLatitude()));
                                        postjson.put("lon",  OsMoDroid.df6.format(location.getLongitude()));
                                        postjson.put("speed",  OsMoDroid.df0.format(location.getSpeed()));
                                        postjson.put("hdop",  OsMoDroid.df0.format(location.getAccuracy()));
                                        postjson.put("altitude",  OsMoDroid.df0.format(location.getAltitude()));
                                        if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
                                            {
                                                postjson.put("mobile",true);
                                            }
                                    }
                                catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                                myIM.sendToServer("RCR:" + OsMoDroid.WHERE + "|" + postjson.toString(), false);
                                where = false;
                                if (!state)
                                    {
                                        //LocalService.addlog("remove updates because state");
                                        myManager.removeUpdates(singleLocationListener);
                                    }
                                return;
                            }
                    }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras)
                    {
                    }
                @Override
                public void onProviderEnabled(String provider)
                    {
                    }
                @Override
                public void onProviderDisabled(String provider)
                    {
                    }
            };
        static int numberofnotif=0;
        final static Handler alertHandler = new Handler()
        {
            @Override
            public void handleMessage(Message message)
                {

                    if (log)
                        {
                            Log.d(this.getClass().getName(), "Handle message " + message.toString());
                        }
                    Bundle b = message.getData();
                    if (log)
                        {
                            Log.d(this.getClass().getName(), "deviceU " + b.getInt("deviceU"));
                        }
                    if (b.containsKey("read"))
                        {
                            String str = "";
                            str = b.getString("read");
                            LocalService.addlog(str);
                            if (str.substring(str.length() - 1, str.length()).equals("\n"))
                                {
                                    str = str.substring(0, str.length() - 1);
                                    try
                                        {
                                            myIM.parseEx(new String(str),false);
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
                            else
                                {
                                    try
                                        {
                                            myIM.parseEx(new String(str),false);
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
                            return;
                        }

                    if (b.containsKey("deviceU") && LocalService.currentDevice != null && LocalService.currentDevice.u == (b.getInt("deviceU")))
                        {
                            LocalService.mNotificationManager.cancel(OsMoDroid.mesnotifyid);
                        }
                    String text = "";
                    if (b.containsKey("MessageText"))
                        {
                            text = b.getString("MessageText");
                        }
                    if (b.containsKey("om_online") && b.getBoolean("om_online", false))
                        {
                        }
                    if (text != null && !text.equals(""))
                        {
                           // Toast.makeText(serContext, text, Toast.LENGTH_SHORT).show();
                            LocalService.messagelist.add(0, text);


                            if(notificationStringsAdapter !=null)
                                {
                                    notificationStringsAdapter.clear();
                                    for (String s:LocalService.messagelist)
                                        {
                                            notificationStringsAdapter.add(s);
                                        }
                                    notificationStringsAdapter.notifyDataSetChanged();
                                }
//			if(log)Log.d(this.getClass().getName(), "try to save messaglsit");
//			saveObject(messagelist, OsMoDroid.NOTIFIESFILENAME);
//		    if(log)Log.d(this.getClass().getName(), "Success saved messaglsit");
//			if(log)Log.d(this.getClass().getName(), "List:"+LocalService.messagelist);
                            Bundle a = new Bundle();
                            a.putStringArrayList("meslist", LocalService.messagelist);
                            Intent activ = new Intent(serContext, GPSLocalServiceClient.class);
                            activ.setAction("notif");
                            activ.putExtras(a);
                            PendingIntent contentIntent = PendingIntent.getActivity(serContext, OsMoDroid.notifyidApp(), activ, 0);
                            Long when = System.currentTimeMillis();
                            numberofnotif++;
                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                                    serContext.getApplicationContext(),"default")
                                    .setWhen(when)
                                    .setContentText(text)
                                    .setContentTitle("OsMoDroid")
                                    .setSmallIcon(android.R.drawable.ic_menu_send)
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_LIGHTS)
                                    .setContentIntent(contentIntent).setNumber(numberofnotif).setChannelId("silent");
                            if (!OsMoDroid.settings.getBoolean("silentnotify", false))
                                {
                                    notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND).setChannelId("noisy");
                                }
                            Notification notification = notificationBuilder.build();

                            LocalService.mNotificationManager.notify(OsMoDroid.mesnotifyid, notification);
                            if (OsMoDroid.mesactivityVisible)
                                {
                                    try
                                        {
                                            contentIntent.send(serContext, 0, activ);
                                            LocalService.mNotificationManager.cancel(OsMoDroid.mesnotifyid);
                                        }
                                    catch (CanceledException e)
                                        {
                                            if (log)
                                                {
                                                    Log.d(this.getClass().getName(), "pending intent exception" + e);
                                                }
                                            e.printStackTrace();
                                        }
                                }
                        }
                }
        };
        private Float sensivity;
        private int alarmStreamId = 0;
        private int count = 0;
        private int countFix = 0;
        //Notification notification;
        PendingIntent osmodroidLaunchIntent;
        private String strVersionName;
        private String androidver;

        //boolean connect=false;
        private boolean bindedremote;
        private boolean bindedlocaly;
        private int pollperiod = 0;
        static boolean paused = false;
        private static boolean log = true;
        String sending = "";
        ArrayList<String> buffer = new ArrayList<String>();
        ArrayList<String> sendingbuffer = new ArrayList<String>();
        public String motd = "";
        private long pausemill;
        int intKM;
        boolean where = false;
        //static int selectedTileSourceInt = 1;

        //boolean connecting=false;
        NotificationCompat.Builder foregroundnotificationBuilder;
        boolean pro;
        private View linearview;
        private IMapController mapController;
        private long lastsmstime=0;
        private OsmAndAidlHelper osmand;
        private MapView mMapView;
        private NotificationChannel silentchannel;
        private NotificationChannel noisechannel;
        private NotificationChannel uploadNotificationChannel;
        private int health;

        static String formatInterval(final long l)
            {
                return String.format("%02d:%02d:%02d", l / (1000 * 60 * 60), (l % (1000 * 60 * 60)) / (1000 * 60), ((l % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
            }
        @Override
        public boolean onUnbind(Intent intent)
            {
                if (intent.getAction().equals("OsMoDroid.remote"))
                    {
                        bindedremote = false;
                    }
                else
                    {
                        bindedlocaly = false;
                    }
                if (!bindedremote && !bindedlocaly)
                    {
                        binded = false;
                    }
                if (log)
                    {
                        Log.d(this.getClass().getName(), "on unbind " + binded + "intent=" + intent.getAction() + " bindedremote=" + bindedremote + " bindedlocaly=" + bindedlocaly);
                    }
                return true;
            }
        @Override
        public void onRebind(Intent intent)
            {
                if (intent.getAction().equals("OsMoDroid.remote"))
                    {
                        bindedremote = true;
                        if (!OsMoDroid.settings.getString("key", "").equals(""))
                            {
                                Netutil.newapicommand((ResultsListener) LocalService.this, "om_device_channel_adaptive:" + OsMoDroid.settings.getString("device", ""));
                            }
                    }
                else
                    {
                        bindedlocaly = true;
                    }
                binded = true;
                Log.d(this.getClass().getName(), "on rebind " + binded + "intent=" + intent.getAction() + " bindedremote=" + bindedremote + " bindedlocaly=" + bindedlocaly);
                super.onRebind(intent);
            }
        @Override
        public IBinder onBind(Intent intent)
            {
                bindedlocaly = true;
                binded = true;
                Log.d(this.getClass().getName(), "on rebind " + binded + "intent=" + intent.getAction() + " bindedremote=" + bindedremote + " bindedlocaly=" + bindedlocaly);

                return mBinder;
            }
        public synchronized void refresh()
            {
                if (state&&myIM.connOpened && !myIM.connecting)
                    {
                                int icon = R.drawable.eyeo;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            icon=R.drawable.eyeo26;
                        }
                                updateNotification(icon);
                    }
                else if (state&&myIM.connecting)
                    {
                                int icon = R.drawable.eyeu;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            icon=R.drawable.eyeu26;
                        }
                                updateNotification(icon);

                    }
                else if (state)
                    {
                                int icon = R.drawable.eyen;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            icon=R.drawable.eyen26;
                        }
                                updateNotification(icon);
                    }
                in.removeExtra("startmessage");
                in.putExtra("position", position + "\n" + satellite + " " + getString(R.string.accuracy) + accuracy);
                in.putExtra("sattelite", satellite + " " + getString(R.string.accuracy) + accuracy);
                in.putExtra("sendresult", sendresult);
                in.putExtra("buffercounter", buffercounter);

                in.putExtra("started", state);
                in.putExtra("globalsend", globalsend);
                in.putExtra("sos", sos);
                in.putExtra("signalisationon", signalisationOn);
                in.putExtra("sendcounter", sendcounter);
                in.putExtra("writecounter", writecounter);
                in.putExtra("currentspeed", OsMoDroid.df0.format(currentspeed * 3.6));
                in.putExtra("avgspeed", OsMoDroid.df1.format(avgspeed * 3600));
                in.putExtra("maxspeed", OsMoDroid.df1.format(maxspeed * 3.6));
                in.putExtra("workdistance", OsMoDroid.df2.format(workdistance / 1000));
                in.putExtra("timeperiod", formatInterval(timeperiod));
                if(altitude!=Integer.MIN_VALUE)
                    {
                        in.putExtra("altitude", OsMoDroid.df0.format(altitude));
                    }
                else
                    {
                        in.putExtra("altitude", "");
                    }

                in.putExtra("totalclimb",OsMoDroid.df0.format(totalclimb));
                if (myIM != null)
                    {
                        in.putExtra("connect", myIM.connOpened);
                        in.putExtra("connecting", myIM.connecting);
                        in.putExtra("executedlistsize", myIM.executedCommandArryaList.size());
                    }
                in.putExtra("motd", motd);
                in.putExtra("traffic", Long.toString((TrafficStats.getUidTxBytes(context.getApplicationInfo().uid)-myIM.startTraffic) / 1024) + OsMoDroid.dot.getDecimalSeparator() + Long.toString((TrafficStats.getUidTxBytes(context.getApplicationInfo().uid)-myIM.startTraffic) % 1000) + "KB " + myIM.connectcount + "|" + myIM.erorconenctcount);
                in.putExtra("pro", pro);

                sendBroadcast(in);
                updatewidgets();
            }
        public void startcomand()
            {
                String version = getversion();
                APIcomParams params = new APIcomParams("http://a.t.esya.ru/?act=start&hash=" + OsMoDroid.settings.getString("hash", "") + "&n=" + OsMoDroid.settings.getString("n", "") + "&c=OsMoDroid&v=" + version.replace(".", "") + "&key=" + OsMoDroid.settings.getString("key", ""), null, "start");
                starttask = new Netutil.MyAsyncTask(this);
                starttask.execute(params);
                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "startcommand");
                    }
            }
        String getversion()
            {
                androidver = android.os.Build.VERSION.RELEASE;
                strVersionName = getString(R.string.Unknow);
                String version = getString(R.string.Unknow);
                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "startcommand");
                    }
                try
                    {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        strVersionName = packageInfo.packageName + ' ' + packageInfo.versionName + ' ' + packageInfo.versionCode;
                        version = packageInfo.versionName + ' ' + packageInfo.versionCode;
                    }
                catch (NameNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                return version;
            }
        public void stopcomand()
            {
                if (starttask != null)
                    {
                        starttask.cancel(true);
                        starttask.close();
                    }
            }
        void setPause(boolean pause)
            {
                if (pause)
                    {
                        paused = true;
                        pausemill = System.currentTimeMillis();
//					if(myManager!=null)
//						{
//							myManager.removeUpdates(this);
//						}
                    }
                else
                    {
                        paused = false;
                        workmilli = workmilli + (System.currentTimeMillis() - pausemill);
                        //requestLocationUpdates();
                    }
            }
        public String getPosition()
            {
                String result = getString(R.string.NotDefined) + "\n" + getString(R.string.speed);
                if (position == null)
                    {
                        return result;
                    }
                else
                    {
                        return position;
                    }
            }
        public String getSendResult()
            {
                return sendresult;
            }
//        public void sendPosition()
//            {
//                Location forcelocation=null;
//                Location forcenetworklocation=null;
//                List<String> list = myManager.getAllProviders();
//                if (list.contains(LocationManager.GPS_PROVIDER))
//                    {
//                        forcelocation = myManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                        if (list.contains(LocationManager.NETWORK_PROVIDER))
//                            {
//                                forcenetworklocation = myManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                            }
//                    }
//
//                if (forcelocation == null)
//                    {
//                        if (forcenetworklocation == null)
//                            {
//                            }
//                        else
//                            {
//                                sendlocation(forcenetworklocation, false);
//                            }
//                    }
//                else
//                    {
//                        sendlocation(forcelocation,false);
//                    }
//            }
        public int getSendCounter()
            {
                return sendcounter;
            }

        @Override
        public void onCreate()
            {

                super.onCreate();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&OsMoDroid.settings.getBoolean("autostart", false)) {
                    openForeground();
                }
                OsMoDroid.settings.registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) LocalService.this);
                Log.d(this.getClass().getName(), "localserviceoncreate");
                osmand = new OsmAndAidlHelper(this, this);
                ttsManage();
                getversion();
                serContext = LocalService.this;
                AssetManager assetManagerm = context.getAssets();
                registerReceiver(uploadServiceBroadcastReceiver, new IntentFilter("com.OsMoDroid.uploadservice.broadcast.status"));

                try {
                    InputStream stream = this.getAssets().open("maps.json");
                    OutputStream output = new BufferedOutputStream(new FileOutputStream(OsMoDroid.osmodirFile + "/maps.json"));
                    byte data[] = new byte[1024];
                    int count;
                    while((count = stream.read(data)) != -1)
                    {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    stream.close();
                    addlog("maps.json cpoy success");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (OsMoDroid.settings.contains("signalisation"))
                    {
                        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        signalisationOn = true;
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "Enable signalisation after start ");
                            }
                    }
                myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                satellite = getString(R.string.Sputniki);
                position = getString(R.string.NotDefined) + "\n" + getString(R.string.speed);
                OsMoDroid.sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
                currentLocation = new Location("");
                prevlocation = new Location("");
                prevlocation_gpx = new Location("");
                currentLocation.setLatitude((double) OsMoDroid.settings.getFloat("lat", 0f));
                currentLocation.setLongitude((double) OsMoDroid.settings.getFloat("lon", 0f));

//                OsMoDroid.dot.setDecimalSeparator('.');
//                OsMoDroid.df1.setDecimalSeparatorAlwaysShown(false);
//                OsMoDroid.df6.setDecimalSeparatorAlwaysShown(false);
//                OsMoDroid.df1.setDecimalFormatSymbols(OsMoDroid.dot);
//                OsMoDroid.df6.setDecimalFormatSymbols(OsMoDroid.dot);
                ReadPref();


                String alarm = Context.ALARM_SERVICE;
                am = (AlarmManager) getSystemService(alarm);
                Intent intent = new Intent("CHECK_GPS");
                pi = PendingIntent.getBroadcast(this, 0, intent, 0);
                batteryReciever = new BroadcastReceiver()
                {
                    public void onReceive(Context context, Intent intent)
                        {
                            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                            plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                            temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                            int level = -1;
                            if (rawlevel >= 0 && scale > 0)
                                {
                                    level = (rawlevel * 100) / scale;
                                }
                            batteryprocent = level;
                        }
                };
                registerReceiver(batteryReciever, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                checkreceiver = new BroadcastReceiver()
                {
                    @Override
                    public void onReceive(Context context, Intent intent)
                        {
                            if (System.currentTimeMillis() > lastgpsontime + notifyperiod && gpsbeepedon)
                                {
                                    if (vibrate)
                                        {
                                            vibrator.vibrate(vibratetime);
                                        }
                                    if (playsound)
                                        {
                                            //soundPool.play(gpson, 1f, 1f, 1, 0, 1f);
                                            if (tts != null && OsMoDroid.settings.getBoolean("usetts", false))
                                                {
                                                    tts.speak(getString(R.string.gpson), TextToSpeech.QUEUE_ADD, null);
                                                }
                                        }
                                }
                            else
                                {
                                    gpsbeepedon = false;
                                }
                            if (System.currentTimeMillis() > lastgpsofftime + notifyperiod && gpsbeepedoff)
                                {
                                    if (vibrate)
                                        {
                                            vibrator.vibrate(vibratetime);
                                        }
                                    if (playsound)
                                        {
                                            //soundPool.play(gpsoff, 1f, 1f, 1, 0, 1f);
                                            if (tts != null && OsMoDroid.settings.getBoolean("usetts", false))
                                                {
                                                    tts.speak(getString(R.string.gpsoff), TextToSpeech.QUEUE_ADD, null);
                                                }
                                        }
                                }
                            else
                                {
                                    gpsbeepedoff = false;
                                }
                        }
                };
                receiver = new BroadcastReceiver()
                {
                    @Override
                    public void onReceive(Context context, Intent intent)
                        {
                            boolean gpxfix = false;
                            gpxfix = intent.getBooleanExtra("enabled", false);
                            if (gpxfix)
                                {
                                    lastgpsontime = System.currentTimeMillis();
                                    gpsbeepedon = true;
                                    if (playsound && !firstgpsbeepedon)
                                        {
                                            firstgpsbeepedon = true;
                                            //soundPool.play(gpson, 1f, 1f, 1, 0, 1f);
                                            if (tts != null && OsMoDroid.settings.getBoolean("usetts", false))
                                                {
                                                    tts.speak(getString(R.string.gpson), TextToSpeech.QUEUE_ADD, null);
                                                }
                                        }
                                }
                            else
                                {
                                    lastgpsofftime = System.currentTimeMillis();
                                    gpsbeepedoff = true;
                                }
                        }
                };
                soundPool = new SoundPool(10, AudioManager.STREAM_NOTIFICATION, 0);
               // gpson = soundPool.load(this, R.raw.gpson, 1);
               // gpsoff = soundPool.load(this, R.raw.gpsoff, 1);
               // ineton = soundPool.load(this, R.raw.ineton, 1);
               // inetoff = soundPool.load(this, R.raw.inetoff, 1);
                sendpalyer = soundPool.load(this, R.raw.sendsound, 1);
                //startsound = soundPool.load(this, R.raw.start, 1);
                //stopsound = soundPool.load(this, R.raw.stop, 1);
                alarmsound = soundPool.load(this, R.raw.signal, 1);
                signalonoff = soundPool.load(this, R.raw.signalonoff, 1);
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"OsMoDroid:wakelock");
                in = new Intent("OsMoDroid");
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    silentchannel = new NotificationChannel("silent","OsMoDroid Silent",NotificationManager.IMPORTANCE_LOW);
                    noisechannel = new NotificationChannel("noisy","OsMoDroid Noisy",NotificationManager.IMPORTANCE_DEFAULT);
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build();
                    noisechannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, attributes);
                    mNotificationManager.createNotificationChannel(silentchannel);
                    mNotificationManager.createNotificationChannel(noisechannel);
                }
                    if (!OsMoDroid.settings.getBoolean("ondestroy", false))
                    {
                        List<Channel> loaded = (List<Channel>) OsMoDroid.loadObject(this,OsMoDroid.CHANNELLIST, channelList.getClass());
                        if (loaded != null)
                            {
                                Log.d(this.getClass().getName(), "channelList is not empty");
                                channelList.addAll(loaded);
                                for (Channel ch : channelList)
                                    {
                                        for (ColoredGPX cgpx : ch.gpxList)
                                            {
                                                cgpx.initPathOverlay();
                                            }
                                    }
                                //connectcompleted =true;
                                osmAndAddAllChannels();
                            }
                    }
                Collection<? extends JSONObject> list = (Collection<? extends JSONObject>) loadObject(this, "waypointsList", waypointsList.getClass());
                    if(list!=null)
                    waypointsList.addAll(list);

                ArrayList<String> loadedgcm = (ArrayList<String>) OsMoDroid.loadObject(this,OsMoDroid.GCMTODOLIST, gcmtodolist.getClass());
                if(loadedgcm!=null)
                    {
                        gcmtodolist.addAll(loadedgcm);
                    }

                APIcomParams params = new APIcomParams("https://osmo.mobi/ts.json", osmodirFile+"/maps.json");
               new MyAsyncTask(this::onResultsSucceeded).execute(params);
                //myIM = new IM("osmo.mobi", 4254, this)
                myIM = new IM("osmo.mobi", 4260, this)
                {
                    @Override
                    void ondisconnect()
                        {
                            addlog("ondisconnect, sending="+sending);
                            addlog("ondisconnect, sendingbufeer size="+sendingbuffer.size()+ " .buffer.size=" + buffer.size());
                            buffer.addAll(sendingbuffer);
                            sendingbuffer.clear();
                            if (!OsMoDroid.settings.getBoolean("udpmode",false)&&!sending.equals(""))
                                {
                                    buffer.add(sending);
                                    sending = "";
                                    buffercounter++;
                                    String time = OsMoDroid.sdf3.format(new Date(System.currentTimeMillis()));
                                    sendresult = time + " " + getString(R.string.error);
                                    updateNotification(-1);
                                    refresh();
                                    addlog("ondisconnect, bufeer size="+buffer.size()+ " .buffer.size=" + buffer.size());
                                }

                        }
                };
                if (OsMoDroid.settings.getString("newkey", "").equals(""))
                    {
                        sendid();
                    }
                else
                    {
                        if (true)
                            {
                                myIM.start();
                            }
                    }
                if (OsMoDroid.settings.getBoolean("started", false)||OsMoDroid.settings.getBoolean("autostartsession", false))
                    {
                        startServiceWork(true);
                    }
                OsMoDroid.settings.edit().putBoolean("ondestroy", false).commit();

                uploadTrackstoServer();

                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                linearview = inflater.inflate(R.layout.map, null, false);
                RelativeLayout rl = (RelativeLayout) linearview.findViewById(R.id.relative);
                MapFragment.CustomTileProvider customTileProvider;

                ComponentName myWidget = new ComponentName(this, MapWidget.class);
                int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(myWidget);
                if(ids.length > 0)
                    {
                        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        Display display = wm.getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int w = size.x/3;
                        int h = size.x/3;
                        //AppWidgetManager.getInstance(this).getAppWidgetInfo(ids[0]).
                        ImageButton c =(ImageButton) linearview.findViewById(R.id.imageButtonCenter);
                                c.setVisibility(View.GONE);
                         mMapView = new MapView(getApplicationContext());
                        ChannelsOverlay choverlay = new ChannelsOverlay(mMapView, true);
                        mapController = mMapView.getController();
                        mMapView.getOverlays().add(choverlay);
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        mMapView.setLayoutParams(lp);
                        mMapView.setTilesScaledToDpi(OsMoDroid.settings.getBoolean("adjust_to_dpi", true));
                        mMapView.setTileSource(TileSourceFactory.MAPNIK);
                        //mMapView.setTilesScaledToDpi(true);
                        rl.addView(mMapView, 0);
//                        int w = 300;
//                        int h = 300;
                        linearview.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
                        linearview.layout(0, 0, linearview.getMeasuredWidth(), linearview.getMeasuredHeight());
                        linearview.setDrawingCacheEnabled(true);
                        linearview.buildDrawingCache();
                    }


            }

        private void uploadTrackstoServer() {
            //upload files
            if(OsMoDroid.settings.getBoolean("udpmode",false)) {
                File path = new File(osmodirFile, "OsMoDroid/");
                File[] fileArray = path.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.toLowerCase().endsWith(".gpx");
                    }
                });
                if (fileArray != null) {
                    for (File file : fileArray)
                    {
                        uploadMultipart(file.getAbsolutePath());
                    }
                }
            }
        }

        void Pong(Context context) throws JSONException
            {
                myIM.sendToServer("RCR|1", false);
            }
        void batteryinfo(Context context) throws JSONException
            {
                JSONObject postjson = new JSONObject();
                postjson.put("percent", batteryprocent);
                postjson.put("temperature", temperature);
                postjson.put("voltage", voltage);
                postjson.put("plugged", plugged);
                postjson.put("health", health);
                myIM.sendToServer("RCR:" + OsMoDroid.TRACKER_BATTERY_INFO + "|" + postjson.toString(), false);
            }
        private String capitalize(String s)
            {
                if (s == null || s.length() == 0)
                    {
                        return "";
                    }
                char first = s.charAt(0);
                if (Character.isUpperCase(first))
                    {
                        return s;
                    }
                else
                    {
                        return Character.toUpperCase(first) + s.substring(1);
                    }
            }
        public String getDeviceName()
            {
                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;
                if (model.startsWith(manufacturer))
                    {
                        return capitalize(model);
                    }
                else
                    {
                        return capitalize(manufacturer) + " " + model;
                    }
            }
        void systeminfo(Context context) throws JSONException
            {
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                int width = display.getWidth();  // deprecated
                int height = display.getHeight();  // deprecated
                JSONObject postjson = new JSONObject();
                postjson.put("version", strVersionName);
                postjson.put("androidversion", androidver);
                postjson.put("devicename", getDeviceName());
                postjson.put("display", Integer.toString(width) + "x" + Integer.toString(height));
                postjson.put("tz", TimeZone.getDefault().getID());
                postjson.put("locale", Locale.getDefault().getISO3Language());

                myIM.sendToServer("RCR:" + OsMoDroid.TRACKER_SYSTEM_INFO + "|" + postjson.toString(), false);
            }
        void vibrate(Context context, long milliseconds)
            {
                vibrator.vibrate(milliseconds);
                myIM.sendToServer("RCR:" + OsMoDroid.TRACKER_VIBRATE + "|1", false);
            }
        void satelliteinfo(Context context) throws JSONException
            {
                JSONObject postjson = new JSONObject();
                postjson.put("view", count);
                postjson.put("active", countFix);
                postjson.put("accuracy", accuracy);
                myIM.sendToServer("RCR:" + OsMoDroid.TRACKER_SATELLITES_INFO + "|" + postjson.toString(), false);
            }
        void getpreferences(Context context) throws JSONException
            {

                JSONObject postjson = new JSONObject();
                for (Map.Entry<String, ?> entry : OsMoDroid.settings.getAll().entrySet())
                    {
                        Object v = entry.getValue();
                        String key = entry.getKey();
                        postjson.put(key,  v);
                    };
                postjson.remove("tracker_id");
                postjson.remove("device");
                postjson.remove("GCMregId");
                postjson.remove("newkey");
                postjson.remove("modt");
                postjson.remove("motdtime");


                myIM.sendToServer("RCR:"+OsMoDroid.TRACKER_GET_PREFS+"|"+postjson.toString(),false);
            }
        void setpreferences(JSONObject jo, Context context) throws JSONException
            {
                Iterator<?> i = jo.keys();
                while (i.hasNext()){
                    try
                        {

                            String key = i.next().toString();
                            Object v = jo.get(key);
                            if (v instanceof Boolean)
                                {
                                    OsMoDroid.editor.putBoolean(key, ((Boolean) v).booleanValue());
                                }
                            else if (v instanceof Float)
                                {
                                    OsMoDroid.editor.putFloat(key, ((Float) v).floatValue());
                                }
                            else if (v instanceof Integer)
                                {
                                    OsMoDroid.editor.putInt(key, ((Integer) v).intValue());
                                }
                            else if (v instanceof Long)
                                {
                                    OsMoDroid.editor.putLong(key, ((Long) v).longValue());
                                }
                            else if (v instanceof String)
                                {
                                    OsMoDroid.editor.putString(key, ((String) v));
                                }
                        }
                    catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    OsMoDroid.editor.commit();
                }
                myIM.sendToServer("RCR:"+OsMoDroid.TRACKER_SET_PREFS+"|"+1,false);
            }
        void wifion(Context context)
            {
                WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifi.setWifiEnabled(true);
                myIM.sendToServer("RCR:" + OsMoDroid.TRACKER_WIFI_ON + "|1", false);
            }
        void wifioff(Context context)
            {
                WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifi.setWifiEnabled(false);
                myIM.sendToServer("RCR:" + OsMoDroid.TRACKER_WIFI_OFF + "|1", false);
            }
        void wifiinfo(Context context) throws JSONException
            {
                JSONObject postjson = new JSONObject();
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected())
                    {
                        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifi.getConnectionInfo();
                        String wifiname = wifiInfo.getSSID();
                        String mac = wifiInfo.getMacAddress();
                        String strength = Integer.toString(wifiInfo.getRssi());
                        postjson.put("ssid", wifiname.replaceAll("\"", ""));
                        //postjson.put("mac", mac);
                        //postjson.put("strength", strength);
                    }
                else
                    {
                        postjson.put("state", "noconnect");
                    }
                myIM.sendToServer("RCR:" + OsMoDroid.TRACKER_WIFI_INFO + "|" + postjson.toString(), false);
            }
        @Override
        public void onDestroy()
            {
                stopFollow();
                try {
                    osmand.cleanupResources();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (tts != null)
                    {
                        tts.stop();
                        tts.shutdown();
                        tts = null;
                    }
                mSensorManager.unregisterListener(this);
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Disable signalisation after destroy");
                    }

                    if (state)
                    {
                        stopServiceWork(false);
                    }
                if (myIM != null)
                    {
                        myIM.close();
                    }
                deleteFile(OsMoDroid.NOTIFIESFILENAME);
                //deleteFile(OsMoDroid.DEVLIST);
                stopcomand();
                try
                {
                    if (receiver != null)
                    {
                        unregisterReceiver(uploadServiceBroadcastReceiver);
                    }
                }
                catch (Exception e)
                {
                    if (log)
                    {
                        Log.d(getClass().getSimpleName(), "А он и не зареген");
                    }
                }
                try
                    {
                        if (receiver != null)
                            {
                                unregisterReceiver(receiver);
                            }
                    }
                catch (Exception e)
                    {
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "А он и не зареген");
                            }
                    }
                try
                    {
                        if (checkreceiver != null)
                            {
                                unregisterReceiver(checkreceiver);
                            }
                    }
                catch (Exception e)
                    {
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "А он и не зареген");
                            }
                    }
                try
                    {
                        if (onlinePauseforStartReciever != null)
                            {
                                unregisterReceiver(onlinePauseforStartReciever);
                            }
                    }
                catch (Exception e)
                    {
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "А он и не зареген");
                            }
                    }
                try
                    {
                        if (batteryReciever != null)
                            {
                                unregisterReceiver(batteryReciever);
                            }
                    }
                catch (Exception e)
                    {
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "А он и не зареген");
                            }
                    }
                if (soundPool != null)
                    {
                        soundPool.release();
                    }
                if (!(wakeLock == null) && wakeLock.isHeld())
                    {
                        wakeLock.release();
                    }
                if (!(LocwakeLock == null) && LocwakeLock.isHeld())
                    {
                        LocwakeLock.release();
                    }
                if (!(SendwakeLock == null) && SendwakeLock.isHeld())
                    {
                        SendwakeLock.release();
                    }
                mNotificationManager.cancelAll();
                OsMoDroid.settings.edit().remove("globalsend").commit();
                OsMoDroid.settings.edit().putBoolean("ondestroy", true).commit();
                deleteFile(OsMoDroid.CHANNELLIST);
                deleteFile(OsMoDroid.DEVLIST);
                deleteFile(OsMoDroid.NOTIFIESFILENAME);
                super.onDestroy();
                System.exit(0);
            }
        private void ReadPref()
            {
                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "readpref() localserv");
                    }
                try
                    {
                        //pollperiod = 1000*(OsMoDroid.settings.getInt("refreshrateinteger", 5));
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
//                speed = Integer.parseInt(OsMoDroid.settings.getString("speed", "3").equals("") ? "3" : OsMoDroid.settings.getString("speed", "3"));
//                period = Integer.parseInt(OsMoDroid.settings.getInt("period", "10000").equals("") ? "10000" : OsMoDroid.settings.getInt("period", "10000"));
//                distance = Integer.parseInt(OsMoDroid.settings.getString("distance", "50").equals("") ? "50" : OsMoDroid.settings.getString("distance", "50"));
 //               hash = OsMoDroid.settings.getString("hash", "");
   //             n = Integer.parseInt(OsMoDroid.settings.getString("n", "0").equals("") ? "0" : OsMoDroid.settings.getString("n", "0"));
    //            speedbearing = Integer.parseInt(OsMoDroid.settings.getString("speedbearing", "2").equals("") ? "2" : OsMoDroid.settings.getString("speedbearing", "2"));
  //              bearing = Integer.parseInt(OsMoDroid.settings.getString("bearing", "10").equals("") ? "10" : OsMoDroid.settings.getString("bearing", "2"));
                //hdop = Integer.parseInt(OsMoDroid.settings.getString("hdop", "30").equals("") ? "30" : OsMoDroid.settings.getString("hdop", "30"));
                gpx = false;//OsMoDroid.settings.getBoolean("gpx", false);
                live = true;
                vibrate = OsMoDroid.settings.getBoolean("vibrate", false);
    //            usecourse = OsMoDroid.settings.getBoolean("usecourse", false);
                vibratetime = Integer.parseInt(OsMoDroid.settings.getString("vibratetime", "200").equals("") ? "200" : OsMoDroid.settings.getString("vibratetime", "0"));
                playsound = OsMoDroid.settings.getBoolean("playsound", false);
                try {
                    period_gpx = Integer.parseInt(OsMoDroid.settings.getString("period_gpx", "0").equals("") ? "0" : OsMoDroid.settings.getString("period_gpx", "0"));
                    distance_gpx = Integer.parseInt(OsMoDroid.settings.getString("distance_gpx", "0").equals("") ? "0" : OsMoDroid.settings.getString("distance_gpx", "0"));
                    speedbearing_gpx = Integer.parseInt(OsMoDroid.settings.getString("speedbearing_gpx", "0").equals("") ? "0" : OsMoDroid.settings.getString("speedbearing_gpx", "0"));
                    bearing_gpx = Integer.parseInt(OsMoDroid.settings.getString("bearing_gpx", "0").equals("") ? "0" : OsMoDroid.settings.getString("bearing", "0"));
                    hdop_gpx = Integer.parseInt(OsMoDroid.settings.getString("hdop_gpx", "30").equals("") ? "30" : OsMoDroid.settings.getString("hdop_gpx", "30"));
                    speed_gpx = Integer.parseInt(OsMoDroid.settings.getString("speed_gpx", "3").equals("") ? "3" : OsMoDroid.settings.getString("speed_gpx", "3"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //usebuffer = OsMoDroid.settings.getBoolean("usebuffer", false);

                try {
                    notifyperiod = Integer.parseInt(OsMoDroid.settings.getString("notifyperiod", "30000").equals("") ? "30000" : OsMoDroid.settings.getString("notifyperiod", "30000"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendsound = OsMoDroid.settings.getBoolean("sendsound", false);
            }
        @Override
        public void onStart(Intent intent, int startId)
            {
                //super.onStart(intent, startId);
                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "on start ");
                    }
                updatewidgets();
                handleStart(intent, startId);

            }
        @Override
        public int onStartCommand(Intent intent, int flags, int startId)
            {
                //super.onStartCommand(intent, flags, startId);
                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "on startcommand");
                    }
                updatewidgets();
                handleStart(intent, startId);
                return START_STICKY;
            }
        void handleStart(Intent intent, int startId)
            {
                if(myIM!=null)
                    {
                        myIM.checkalarmindozemode();
                    }


                Log.d(getClass().getSimpleName(), "on handleStart"+intent);

                if (intent != null)
                    {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null)
                            {
                                for (String key : bundle.keySet())
                                    {
                                        Object value = bundle.get(key);
                                        Log.d(getClass().getSimpleName(), String.format("%s %s (%s)", key,
                                                value.toString(), value.getClass().getName()));
                                        addlog(String.format("%s %s (%s)", key,
                                                value.toString(), value.getClass().getName()));
                                    }
                            }
                        if ( intent.hasExtra("SMS"))
                            {
                                if (intent.getStringExtra("SMS").equals("START") && !state)
                                    {
                                        startServiceWork(true);
                                    }
                                if (intent.getStringExtra("SMS").equals("STOP") && state)
                                    {
                                        stopServiceWork(true);
                                    }
                            }
                        if (intent.hasExtra("ACTION"))
                            {
                                Log.d(getClass().getSimpleName(), "on handleStart intent has ACTION=" + intent.getStringExtra("ACTION"));
                                stopFollow();
                                if (intent.getStringExtra("ACTION").equals("STOP")&& state)
                                    {
                                        stopServiceWork(true);
                                    }
                                if (intent.getStringExtra("ACTION").equals("START")&& !state)
                                    {
                                        startServiceWork(true);
                                    }


                            }
                        if(intent.hasExtra("GCM"))
                            {
                                String toParse = intent.getStringExtra("GCM");
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
                                if (toParse.contains("|"))
                                    {
                                        addict = toParse.substring(toParse.indexOf('|') + 1);
                                    }
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
                                if (command.equals("GPC"))
                                    {
                                        try
                                            {
                                                myIM.addToChannelChat(Integer.parseInt(param), jo, false);
                                            }
                                        catch (JSONException e)
                                            {
                                                e.printStackTrace();
                                            }
                                    }
                                else
                                    {
                                        if(myIM!=null&&!myIM.start)
                                            {
                                                myIM.start();
                                                addlog("starr connect because gcm");
                                            }
                                        try
                                            {
                                                if (connectcompleted)
                                                    {
                                                        addlog("parse because connectcompleted");
                                                        myIM.parseEx(intent.getStringExtra("GCM"), true);
                                                    }
                                                else
                                                    {
                                                        addlog("addtogcmtodo because not connectcompleted");
                                                        gcmtodolist.add(intent.getStringExtra("GCM"));
                                                        OsMoDroid.saveObject(this,gcmtodolist, OsMoDroid.GCMTODOLIST);
                                                    }
                                            }
                                        catch (JSONException e)
                                            {
                                                StringWriter sw = new StringWriter();
                                                e.printStackTrace(new PrintWriter(sw));
                                                String exceptionAsString = sw.toString();
                                                addlog(exceptionAsString);
                                            }
                                    }
                            }
                        else
                            {
                                if (myIM != null && !myIM.start )
                                    {
                                        myIM.start();
                                        addlog("starr connect because intent");
                                    }
                            }

                    }
            }

        public void osmandmanage()
            {

                if(OsMoDroid.settings.getBoolean("osmand",false))
                    {
                        osmand.bindService();
                        osmAndAddAllChannels();
                    }
                else
                    {
                        for(Channel ch: channelList)
                            {
                                osmAndDeleteChannel(ch);
                            }
                        try {
                            osmand.cleanupResources();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
        public void applyPreference()
            {

                ReadPref();
                ttsManage();
                osmandmanage();
                manageGPSFixAlarm();
                if (state)
                    {
                        myManager.removeUpdates(this);
                        if (gpx && !fileheaderok)
                            {
                                openGPX();
                            }
                        if (!gpx && fileheaderok)
                            {
                                closeGPX();
                            }
                        requestLocationUpdates(LocalService.this);
                    }
                if (myIM.start == false && live)
                    {
                        myIM.start();
                    }
                if (myIM.start == true && !live)
                    {
                        myIM.close();
                    }
                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "applyPreferecne end");

                    }
                addlog("apply pref");
            }

        public void startFollow(String text)
        {
            try {
                JSONObject jo = new JSONObject(text);
                //RC:7|{"interval":5, "source": "gps", "message": "Вася наблюдает за вами"}
                int interval = jo.optInt("interval", 5);
                String source = jo.optString("source", "all");
                String message = jo.optString("message", "Server watch you");


            if(!state)
            {
                myManager.removeUpdates(followLocationListener);
                requestLocationUpdates(followLocationListener,  source , interval);
                int icon = R.drawable.eye;
                long when = System.currentTimeMillis();
                Intent notificationIntent = new Intent(this, GPSLocalServiceClient.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                osmodroidLaunchIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                foregroundnotificationBuilder = new NotificationCompat.Builder(this,"default");
                foregroundnotificationBuilder.setWhen(System.currentTimeMillis());
                foregroundnotificationBuilder.setContentText(message);
                foregroundnotificationBuilder.setContentTitle("OsMoDroid");
                foregroundnotificationBuilder.setSmallIcon(icon);
                foregroundnotificationBuilder.setContentIntent(osmodroidLaunchIntent);
                foregroundnotificationBuilder.setChannelId("noisy");

                Intent is = new Intent(this, ConfirmDialog.class);
                //is.putExtra("ACTION", "STOP");

                PendingIntent stop = PendingIntent.getActivity(this, 0, is, PendingIntent.FLAG_UPDATE_CURRENT);
                foregroundnotificationBuilder.addAction(android.R.drawable.ic_delete, getString(R.string.stop_monitoring), stop);
                Notification notification = foregroundnotificationBuilder.build();
                startForeground(OSMODROID_ID, notification);
                followmonstarted=true;
                addlog("follow started");

            }
            } catch (JSONException e) {
                addlog("incorrect data in command -json not parsed");
                return;
            }

        }
        public void stopFollow()
        {
            if(followmonstarted)
            {
                myManager.removeUpdates(followLocationListener);
                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&OsMoDroid.settings.getBoolean("autostart", false)))
                stopForeground(true);
                else
                    openForeground();

                followmonstarted=false;
                addlog("follow stoped");
            }

        }
        public void startServiceWork(boolean opensession)
        {
            startServiceWork(opensession, 0);
        }
        public void startServiceWork(boolean opensession,int transportid)
            {
                addlog("startservicework opensession="+opensession);
              /*  if (wakeLock != null && !wakeLock.isHeld()) {
                    wakeLock.acquire();
                    addlog("wakelock acquired");
                }*/
                OsMoDroid.mFirebaseAnalytics.logEvent("TRIP_START",null);
                if (!paused)
                    {

                        altitudedistanceEntryList.clear();
                        avgspeeddistanceEntryList.clear();;
                        speeddistanceEntryList.clear();
                        distanceStringList.clear();
                        writecounter=0;
                        sendingbuffer.clear();
                        totalclimb=0;
                        altitude=Integer.MIN_VALUE;



                        firstsend = true;
                        avgspeed = 0;
                        maxspeed = 0;
                        intKM = 0;
                        workdistance = 0;
                        timeperiod = 0;
                        workmilli = 0;
                        buffercounter = 0;
                        buffersb.setLength(0);
                        lastbuffersb.setLength(0);
                        sendedsb.setLength(0);
                        lcounter = 0;
                        scounter = 0;
                        sendcounter = 0;
                        sended = true;
                        mydev.devicePath.clear();
                        mydev.iprecomputed=0;
                        sending = "";
                        ReadPref();
                        if (OsMoDroid.settings.getBoolean("playsound", false))
                            {
                                //soundPool.play(startsound, 1f, 1f, 1, 0, 1f);
                                if (tts != null && OsMoDroid.settings.getBoolean("usetts", false))
                                    {
                                        tts.speak(getString(R.string.monitoring_started), TextToSpeech.QUEUE_ADD, null);
                                    }
                            }
                        manageGPSFixAlarm();
                        boolean crtfile = false;
                        if (gpx||OsMoDroid.settings.getBoolean("udpmode",false))
                            {
                                openGPX();
                            }
                    }
                setPause(false);
                requestLocationUpdates(LocalService.this);
                int icon = R.drawable.eye;
                CharSequence tickerText = getString(R.string.monitoringstarted); //getString(R.string.Working);
                long when = System.currentTimeMillis();
                Intent notificationIntent = new Intent(this, GPSLocalServiceClient.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                osmodroidLaunchIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                foregroundnotificationBuilder = new NotificationCompat.Builder(this,"default");
                foregroundnotificationBuilder.setWhen(System.currentTimeMillis());
                foregroundnotificationBuilder.setContentText(tickerText);
                foregroundnotificationBuilder.setContentTitle("OsMoDroid");
                foregroundnotificationBuilder.setSmallIcon(icon);
                foregroundnotificationBuilder.setContentIntent(osmodroidLaunchIntent);
                foregroundnotificationBuilder.setChannelId("silent");

                Intent is = new Intent(this, ConfirmDialog.class);
                //is.putExtra("ACTION", "STOP");

                PendingIntent stop = PendingIntent.getActivity(this, 0, is, PendingIntent.FLAG_UPDATE_CURRENT);
                foregroundnotificationBuilder.addAction(android.R.drawable.ic_delete, getString(R.string.stop_monitoring), stop);
                Notification notification = foregroundnotificationBuilder.build();
                //notification = new Notification(icon, tickerText, when);
                //notification.setLatestEventInfo(getApplicationContext(), "OsMoDroid", getString(R.string.monitoringactive), osmodroidLaunchIntent);
                startForeground(OSMODROID_ID, notification);
                setstarted(true);
                if (live)
                    {
                        if (myIM != null && myIM.authed)
                            {
                                if(opensession) {
                                    sessionopentime = System.currentTimeMillis() / 1000;
                                    JSONObject jo = new JSONObject();
                                    try {
                                        jo.put("time", sessionopentime);
                                        jo.put("transportid",transportid);
                                        jo.put("private",privatemode);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if( !OsMoDroid.settings.getBoolean("udpmode",false)) myIM.sendToServer("TO|"+jo.toString(), false);
                                    myIM.needopensession = true;
                                    myIM.needclosesession = false;
                                }
                            }
                        else
                            {
                                if(opensession) {
                                    myIM.needopensession = true;
                                    myIM.needclosesession = false;
                                }
                            }
                    }
                if (log)
                    {
                        Log.d(getClass().getSimpleName(), "notify:" + notification.toString());
                    }
                if (tts != null && OsMoDroid.settings.getBoolean("usetts", false))
                    {
                        tts.speak(getString(R.string.letsgo), TextToSpeech.QUEUE_ADD, null);
                    }
                updatewidgets();
            }
        private void manageGPSFixAlarm()
            {
                int type = AlarmManager.ELAPSED_REALTIME_WAKEUP;
                long triggerTime = SystemClock.elapsedRealtime() + notifyperiod;
                if (playsound || vibrate)
                    {
                        am.setRepeating(type, triggerTime, notifyperiod, pi);
                    }
                else
                    {
                        am.cancel(pi);
                    }
                registerReceiver(receiver, new IntentFilter("android.location.GPS_FIX_CHANGE"));
                registerReceiver(checkreceiver, new IntentFilter("CHECK_GPS"));
            }
        private static String convertToHex(byte[] data)
            {
                StringBuilder buf = new StringBuilder();
                for (byte b : data)
                    {
                        int halfbyte = (b >>> 4) & 0x0F;
                        int two_halfs = 0;
                        do
                            {
                                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                                halfbyte = b & 0x0F;
                            } while (two_halfs++ < 1);
                    }
                return buf.toString();
            }
        public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException
            {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(text.getBytes("iso-8859-1"), 0, text.length());
                byte[] sha1hash = md.digest();
                return convertToHex(sha1hash);
            }

        public void sendid() {
            OsMoDroid.editor.putString("p", "");
            OsMoDroid.editor.putString("u", "");
            OsMoDroid.editor.commit();
            String version = android.os.Build.VERSION.RELEASE;
            String androidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
            if (version == null) {
                version = "unknown";
            }
            if (androidID == null) {
                androidID = "unknown";
            }
            APIcomParams params = new APIcomParams("https://api2.osmo.mobi/new", "platform=" + getDeviceName() + version + android.os.Build.PRODUCT + "&app=" + OsMoDroid.app_code + "&id=" + androidID + "&tz=" + TimeZone.getDefault().getDisplayName(Locale.ENGLISH) + "&locale=" + Locale.getDefault().getISO3Language(), "sendid");
            MyAsyncTask sendidtask = new Netutil.MyAsyncTask(this);
            sendidtask.execute(params);
            Log.d(getClass().getSimpleName(), "sendidtask start to execute");

        }
        private void ttsManage()
            {
                if (OsMoDroid.settings.getBoolean("usetts", false) && tts == null)
                    {
                        tts = new TextToSpeech(this,
                                (OnInitListener) this  // TextToSpeech.OnInitListener
                        );
                    }
                if (!OsMoDroid.settings.getBoolean("usetts", false) && tts != null)
                    {
                        tts.stop();
                        tts.shutdown();
                        tts = null;
                    }
            }
        public void requestLocationUpdates(LocationListener locationListener, String source, int interval) throws SecurityException
        {
            addlog("poll period "+interval*1000);
            addlog("Source "+source);
            List<String> list = myManager.getAllProviders();
            switch (source)
            {
                case ("gps"):
                    if (list.contains(LocationManager.GPS_PROVIDER)) {
                        myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval * 1000, 0, locationListener);
                    }
                    break;
                case ("net"):
                    if (list.contains(LocationManager.NETWORK_PROVIDER)) {
                        myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval * 1000, 0, locationListener);
                    }
                    break;
                case ("all"):
                    if (list.contains(LocationManager.GPS_PROVIDER)) {
                        myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval * 1000, 0, locationListener);
                    }
                    if (list.contains(LocationManager.NETWORK_PROVIDER)) {
                        myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval * 1000, 0, locationListener);
                    }

                    break;
            }
        }
        public void requestLocationUpdates(LocationListener locationListener)
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Запускаем провайдера по настройкам");
                    }
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Период опроса:" + pollperiod);
                    }
                addlog("Период опроса:" + pollperiod);
                List<String> list = myManager.getAllProviders();

                    {
                        if (list.contains(LocationManager.GPS_PROVIDER))
                            {
                                try {
                                    myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, pollperiod, 0, locationListener);
                                    myManager.addGpsStatusListener(LocalService.this);
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                        else
                            {
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "GPS провайдер не обнаружен");
                                    }
                            }
                    }
                if (OsMoDroid.settings.getBoolean("usenetwork", true))
                    {
                        if (list.contains(LocationManager.NETWORK_PROVIDER))
                            {
                                try {
                                    myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, pollperiod, 0, locationListener);
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                        else
                            {
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "NETWORK провайдер не обнаружен");
                                    }
                            }
                    }
            }
        /**
         *
         */
        private void openGPX()
            {
                boolean crtfile;


                        File sdDir = OsMoDroid.osmodirFile;
                       /* if (!OsMoDroid.settings.getString("sdpath", "").equals(""))
                            {
                                sdDir = new File(OsMoDroid.settings.getString("sdpath", ""));
                            }
                        else
                            {
                                Editor editor = OsMoDroid.settings.edit();
                                editor.putString("sdpath", sdDir.getPath());
                                editor.commit();
                            }*/
                        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String time = OsMoDroid.sdf2.format(new Date());
                        gpxfile = new File(sdDir, "OsMoDroid/");
                        gpxfile.mkdirs();
                        if (OsMoDroid.settings.getString("gpxname", "").equals(""))
                            {
                                gpxfile = new File(sdDir, "OsMoDroid/" + time + ".gpx");
                            }
                        else
                            {
                                gpxfile = new File(sdDir, "OsMoDroid/" + OsMoDroid.settings.getString("gpxname", ""));
                                fileheaderok = true;
                            }

                if (!gpxfile.exists())
                    {
                        try
                            {
                                crtfile = gpxfile.createNewFile();
                                OsMoDroid.editor.putString("gpxname", gpxfile.getName());
                                OsMoDroid.editor.commit();
                            }
                        catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        //if(log)Log.d(getClass().getSimpleName(), Boolean.toString(crtfile));
                        try
                            {
                                // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ");
                                time = OsMoDroid.sdf1.format(new Date(System.currentTimeMillis())) + "Z";
                                FileWriter trackwr = new FileWriter(gpxfile);
                                trackwr.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                                trackwr.write("<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"OsMoDroid\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
                                trackwr.write("<time>" + time + "</time>");
                                trackwr.write("<trk>");
                                trackwr.write("<name>" + time + "</name>");
                                trackwr.write("<trkseg>");
                                trackwr.flush();
                                trackwr.close();
                                fileheaderok = true;
                            }
                        catch (Exception e)
                            {
                                //e.printStackTrace();
                                Toast.makeText(LocalService.this, getString(R.string.CanNotWriteHeader) + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    }


            }
        public void stopServiceWork(Boolean stopsession)
            {
                addlog("Stopservicework,stop session "+stopsession);
                if (!(wakeLock == null) && wakeLock.isHeld())
                {
                    wakeLock.release();
                    addlog("wakeloc released");
                }
                OsMoDroid.mFirebaseAnalytics.logEvent("STOP_TRIP",null);
                OsMoDroid.editor.putFloat("lat", (float) currentLocation.getLatitude());
                OsMoDroid.editor.putFloat("lon", (float) currentLocation.getLongitude());
                OsMoDroid.editor.commit();
                firstgpsbeepedon = false;

                if (OsMoDroid.settings.getBoolean("playsound", false))
                    {
                        //soundPool.play(stopsound, 1f, 1f, 1, 0, 1f);
                        if (tts != null && OsMoDroid.settings.getBoolean("usetts", false))
                            {
                                tts.speak(getString(R.string.monitoring_stopped), TextToSpeech.QUEUE_ADD, null);
                            }
                    }
                am.cancel(pi);
                if (live && stopsession)
                    {
                        //String[] params = {"http://a.t.esya.ru/?act=session_stop&hash="+OsMoDroid.settings.getString("hash", "")+"&n="+OsMoDroid.settings.getString("n", ""),"false","","session_stop"};
                        //APIcomParams params = new APIcomParams("http://a.t.esya.ru/?act=session_stop&hash="+OsMoDroid.settings.getString("hash", "")+"&n="+OsMoDroid.settings.getString("n", "")+"&ttl="+OsMoDroid.settings.getString("session_ttl", "30"),null,"session_stop");
                        //new Netutil.MyAsyncTask(this).execute(params);
                        if (myIM.authed)
                            {
                                if (sendingbuffer.size() == 0 && buffer.size() != 0)
                                    {
                                        sendingbuffer.addAll(buffer.subList(0,buffer.size()>100?100:buffer.size()));
                                        buffer.removeAll(sendingbuffer);
                                        myIM.sendToServer("B|" + new JSONArray(sendingbuffer), false);
                                    }
                                if(!OsMoDroid.settings.getBoolean("udpmode",false))myIM.sendToServer("TC", false);
                                myIM.needclosesession = true;
                                myIM.needopensession = false;
                            }
                        else
                            {
                                myIM.needclosesession = true;
                                myIM.needopensession = false;
                            }
                        //buffer.clear();
                    }
                if ((OsMoDroid.settings.getBoolean("udpmode",false)||gpx) && fileheaderok && stopsession)
                    {
                        closeGPX();
                        if(OsMoDroid.settings.getBoolean("udpmode",false)&&gpxfile.exists())
                        {
                            uploadMultipart(gpxfile.getAbsolutePath());
                        }
                    }
                if (myManager != null)
                    {
                        myManager.removeUpdates(this);
                        addlog("removeUpdates");
                    }
                setstarted(false);
                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&OsMoDroid.settings.getBoolean("autostart", false)))
                stopForeground(true);
                else openForeground();
                updatewidgets();
            }
        /**
         *
         */
        private void closeGPX()
            {
                try
                    {
                        FileWriter trackwr = new FileWriter(gpxfile, true);
                        String towright = gpxbuffer;
                        trackwr.write(towright.replace(",", "."));
                        gpxbuffer = "";
                        trackwr.write("</trkseg></trk>");
                        for( JSONObject jo : waypointsList)
                        {
                            trackwr.write("<wpt lat=\""+jo.optString("lat")+ "\" lon=\""+jo.optString("lon")+"\">"
                            +"<name>"+jo.optString("name")+"</name></wpt>");
                        }
                        trackwr.write("</gpx>");
                        trackwr.flush();
                        trackwr.close();
                        fileheaderok = false;
                        waypointsList.clear();
                        OsMoDroid.saveObject(myIM.localService,  waypointsList, "waypointsList");
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(LocalService.this, getString(R.string.CanNotWriteEnd), Toast.LENGTH_SHORT).show();
                    }
                OsMoDroid.editor.remove("gpxname");
                OsMoDroid.editor.commit();
                if (gpxfile.length() > 1024 && uploadto)
                    {
                        upload(gpxfile);
                    }
                if (gpxfile.length() < 1024)
                    {
                        gpxfile.delete();
                        Toast.makeText(LocalService.this, R.string.tracktoshort, Toast.LENGTH_LONG).show();
                    }
            }
        public void upload(File file)
            {
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                        serContext.getApplicationContext(),"default")
                        .setWhen(System.currentTimeMillis())
                        .setContentText(file.getName())
                        .setContentTitle(getString(R.string.osmodroiduploadfile))
                        .setSmallIcon(android.R.drawable.arrow_up_float)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
                        .setProgress(100, 0, false).setChannelId("silent");
                Notification notification = notificationBuilder.build();
                int uploadid = OsMoDroid.uploadnotifyid();
                LocalService.mNotificationManager.notify(uploadid, notification);
                Netutil.newapicommand((ResultsListener) LocalService.this, "tr_track_upload:1", file, notificationBuilder, uploadid);
            }
        private void setstarted(boolean started)
            {
                //if(log)Log.d(getClass().getSimpleName(), "setstarted() localservice");
                OsMoDroid.editor.putBoolean("started", started);
                OsMoDroid.editor.commit();
                state = started;
                refresh();
            }
        public void onLocationChanged(Location location)
            {

                if (!state)
                    {
                        //LocalService.addlog("remove updates because state");
                        myManager.removeUpdates(this);
                    }
                currentLocation.set(location);
                if (LocalService.channelsDevicesAdapter != null && LocalService.currentChannel != null)
                    {
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "Adapter:" + LocalService.channelsDevicesAdapter.toString());
                            }
                        LocalService.channelsDevicesAdapter.notifyDataSetChanged();
                    }
                accuracy = Integer.toString((int) location.getAccuracy());
                if (System.currentTimeMillis() < lastgpslocationtime + pollperiod + 30000 && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
                    {
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "У нас есть GPS еще");
                            }
                        //LocalService.addlog("We still have GPS");
                        return;
                    }
                else
                    {
                        //LocalService.addlog("We still have GPS -ELSE");
                    }
                if (System.currentTimeMillis() > lastgpslocationtime + pollperiod + 30000 && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
                    {
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "У нас уже нет GPS");
                            }
                        //LocalService.addlog("Lost GPS till");
                      //  if ((location.distanceTo(prevlocation) > distance && System.currentTimeMillis() > (prevnetworklocationtime + period)))
                            {
                                LocalService.addlog("send on because networklocation");
                                prevnetworklocationtime = System.currentTimeMillis();
                                sendlocation(location,false);
                                return;
                            }
//                        else
//                            {
//                                //LocalService.addlog("send on because networklocation - ELSE");
//                            }
                    }
                else
                    {
                        //LocalService.addlog("Lost GPS till - ELSE");
                    }
                if (firstsend)
                    {
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "Первая отправка");
                            }
                        //LocalService.addlog("First send");
                        sendlocation(location,true);
                        prevlocation.set(location);
                        prevlocation_gpx.set(location);
                        prevlocation_spd = new Location("");
                        prevlocation_spd.set(location);
                        prevbrng = brng;
                        workmilli = System.currentTimeMillis();
                        firstsend = false;
                    }
                else
                    {
                        //LocalService.addlog("First send - ELSE");
                    }
                if (location.getSpeed() >= speed_gpx / 3.6 && (int) location.getAccuracy() < hdop_gpx && prevlocation_spd != null)
                    {
                        GeoPoint curGeoPoint = new GeoPoint(location);
                        GeoPoint prevGeoPoint = new GeoPoint(prevlocation_spd);
                        if (OsMoDroid.settings.getBoolean("imperial", false))
                            {
                                workdistance = workdistance +distanceBetween(curGeoPoint,prevGeoPoint)/ 1.609344f;//location.distanceTo(prevlocation_spd);
                            }
                        else
                            {
                                workdistance = workdistance + distanceBetween(curGeoPoint,prevGeoPoint);//location.distanceTo(prevlocation_spd);
                            }
                        if (OsMoDroid.settings.getBoolean("imperial", false))
                            {
                            if (OsMoDroid.settings.getBoolean("ttsavgspeed", false) && OsMoDroid.settings.getBoolean("usetts", false) && tts != null && !tts.isSpeaking() && ((int) workdistance) / 1000/1.609344 > intKM)
                                {
                                    intKM = (int)( workdistance / 1000/1.609344);
                                    tts.speak(getString(R.string.going) + ' ' + Integer.toString(intKM) + ' ' + "Miles" + ',' + getString(R.string.avg) + ' ' + OsMoDroid.df1.format(avgspeed * 3600) + ',' + getString(R.string.inway) + ' ' + formatInterval(timeperiod), TextToSpeech.QUEUE_ADD, null);

                                }
                            }
                        else
                            {
                                if (OsMoDroid.settings.getBoolean("ttsavgspeed", false) && OsMoDroid.settings.getBoolean("usetts", false) && tts != null && !tts.isSpeaking() && ((int) workdistance) / 1000 > intKM)
                                    {
                                        intKM = (int) workdistance / 1000;
                                        tts.speak(getString(R.string.going) + ' ' + Integer.toString(intKM) + ' ' + "KM" + ',' + getString(R.string.avg) + ' ' + OsMoDroid.df1.format(avgspeed * 3600) + ',' + getString(R.string.inway) + ' ' + formatInterval(timeperiod), TextToSpeech.QUEUE_ADD, null);
                                    }
                            }
                        //if(log)Log.d(this.getClass().getName(),"Log of Workdistance, Workdistance="+ Float.toString(workdistance)+" location="+location.toString()+" prevlocation_spd="+prevlocation_spd.toString()+" distanceto="+Float.toString(location.distanceTo(prevlocation_spd)));
                        prevlocation_spd.set(location);
                        GeoPoint geopoint = new GeoPoint(location);
                        //if(devlistener!=null){devlistener.onNewPoint(geopoint);}
                        mydev.devicePath.add(new SerPoint(new PointL(geopoint.getLatitudeE6(), geopoint.getLongitudeE6())));
                    }
                if ((int) location.getAccuracy() < hdop_gpx)
                    {
                        if(OsMoDroid.settings.getBoolean("imperial",false))
                            {
                                currentspeed = location.getSpeed()*0.621371f;
                                altitude= (int) (location.getAltitude()*3.28084);
                            }
                        else
                            {
                                currentspeed = location.getSpeed();
                                altitude= (int) location.getAltitude();
                            }


                        boolean filled=true;
                        int summ=0;
                        int meanaltitude=Integer.MIN_VALUE;


                        altitudesamples[altitudesamples.length-1] = altitude;

                                for( int index =0; index < altitudesamples.length-1 ; index++ )
                                    {

                                        altitudesamples[index]=altitudesamples[index+1];
                                        if(altitudesamples[index]==Integer.MIN_VALUE)
                                            {
                                                filled=false;
                                            }
                                        summ=summ+altitudesamples[index];
                                    }


                        if(filled)
                            {
                                meanaltitude = summ / altitudesamples.length;
                                if (prevaltitude == Integer.MIN_VALUE)
                                    {
                                        prevaltitude = meanaltitude;
                                    }
                                else
                                    {
                                        if (abs(meanaltitude - prevaltitude) > 5)
                                            {
                                                if (meanaltitude > prevaltitude)
                                                    {
                                                        totalclimb = totalclimb + meanaltitude - prevaltitude;
                                                    }
                                                prevaltitude = meanaltitude;
                                            }
                                    }
                            }

                        if(OsMoDroid.settings.getBoolean("imperial",false))
                            {
                                if (location.getSpeed()*0.621371f > maxspeed)
                                    {
                                        maxspeed = location.getSpeed()*0.621371f;
                                    }
                            }
                        else
                            {
                                if (location.getSpeed() > maxspeed)
                                    {
                                        maxspeed = location.getSpeed();
                                    }

                            }
                    }
                //if(log)Log.d(this.getClass().getName(),"workmilli="+ Float.toString(workmilli)+" gettime="+location.getTime());
                //if(log)Log.d(this.getClass().getName(),"diff="+ Float.toString(location.getTime()-workmilli));
                if ((System.currentTimeMillis() - workmilli) > 0)
                    {
                        avgspeed = workdistance / (System.currentTimeMillis() - workmilli);
                        //if(log)Log.d(this.getClass().getName(),"avgspeed="+ Float.toString(avgspeed));
                    }
                //if(log)Log.d(this.getClass().getName(), df0.format(location.getSpeed()*3.6).toString());
                //if(log)Log.d(this.getClass().getName(), df0.format(prevlocation.getSpeed()*3.6).toString());
                if (OsMoDroid.settings.getBoolean("ttsspeed", false) && OsMoDroid.settings.getBoolean("usetts", false) && tts != null && !tts.isSpeaking() && !(OsMoDroid.df0.format(location.getSpeed() * 3.6).toString()).equals(lastsay))
                    {
                        //if(log)Log.d(this.getClass().getName(), df0.format(location.getSpeed()*3.6).toString());
                        //if(log)Log.d(this.getClass().getName(), df0.format(prevlocation.getSpeed()*3.6).toString());
                        tts.speak(OsMoDroid.df0.format(location.getSpeed() * 3.6), TextToSpeech.QUEUE_ADD, null);
                        lastsay = OsMoDroid.df0.format(location.getSpeed() * 3.6).toString();
                    }
                position = (OsMoDroid.df6.format(location.getLatitude()) + ", " + OsMoDroid.df6.format(location.getLongitude()) + "\nСкорость:" + OsMoDroid.df1.format(location.getSpeed() * 3.6)) + " Км/ч";
                //position = ( String.format("%.6f", location.getLatitude())+", "+String.format("%.6f", location.getLongitude())+" = "+String.format("%.1f", location.getSpeed()));
                //if (location.getTime()>lastfix+3000)notifygps(false);
                //if (location.getTime()<lastfix+3000)notifygps(true);
                timeperiod = System.currentTimeMillis() - workmilli;


                for (int index = distanceStringList.size(); index <= (int) workdistance; index++)
                    {
                        distanceStringList.add(Integer.toString(index/1000)+','+Integer.toString(index%1000));
                    }
                Entry e = new Entry((int) workdistance,currentspeed* 3.6f);
                speeddistanceEntryList.add(e);
                Entry avge = new Entry((int) workdistance,avgspeed * 3600f);
                avgspeeddistanceEntryList.add(avge);
                Entry alte = new Entry((int) workdistance,(float) location.getAltitude());
                altitudedistanceEntryList.add(alte);


                //speeddistanceEntryList.add(e);
                refresh();
                if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
                    {
                        //LocalService.addlog("Provider=GPS");
                        lastgpslocationtime = System.currentTimeMillis();
                        if(OsMoDroid.settings.getBoolean("udpmode",false)&&fileheaderok)
                        {
                            writegpx(location);
                        }
                        else {
                            if (gpx && fileheaderok) {

                                if (bearing_gpx > 0) {
                                    //if(log)Log.d(this.getClass().getName(), "Пишем трек с курсом");
                                    double lon1 = location.getLongitude();
                                    double lon2 = prevlocation_gpx.getLongitude();
                                    double lat1 = location.getLatitude();
                                    double lat2 = prevlocation_gpx.getLatitude();
                                    double dLon = lon2 - lon1;
                                    double y = Math.sin(dLon) * Math.cos(lat2);
                                    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
                                    brng_gpx = Math.toDegrees(Math.atan2(y, x)); //.toDeg();
                                    position = position + "\n" + getString(R.string.TrackCourseChange) + OsMoDroid.df1.format(abs(brng_gpx - prevbrng_gpx));
                                    refresh();
                                    if (OsMoDroid.settings.getBoolean("modeAND_gpx", false) && (int) location.getAccuracy() < hdop_gpx && location.getSpeed() >= speed_gpx / 3.6 && (location.distanceTo(prevlocation_gpx) > distance_gpx && location.getTime() > (prevlocation_gpx.getTime() + period_gpx) && (location.getSpeed() >= speedbearing_gpx / 3.6 && abs(brng_gpx - prevbrng_gpx) >= bearing_gpx))) {
                                        prevlocation_gpx.set(location);
                                        prevbrng_gpx = brng_gpx;
                                        writegpx(location);
                                    }
                                    if (!OsMoDroid.settings.getBoolean("modeAND_gpx", false) && (int) location.getAccuracy() < hdop_gpx && location.getSpeed() >= speed_gpx / 3.6 && (location.distanceTo(prevlocation_gpx) > distance_gpx || location.getTime() > (prevlocation_gpx.getTime() + period_gpx) || (location.getSpeed() >= speedbearing_gpx / 3.6 && abs(brng_gpx - prevbrng_gpx) >= bearing_gpx))) {
                                        prevlocation_gpx.set(location);
                                        prevbrng_gpx = brng_gpx;
                                        writegpx(location);
                                    }
                                } else {
                                    //if(log)Log.d(this.getClass().getName(), "Пишем трек без курса");
                                    if (OsMoDroid.settings.getBoolean("modeAND_gpx", false) && location.getSpeed() >= speed_gpx / 3.6 && (int) location.getAccuracy() < hdop_gpx && (location.distanceTo(prevlocation_gpx) > distance_gpx && location.getTime() > (prevlocation_gpx.getTime() + period_gpx))) {
                                        writegpx(location);
                                        prevlocation_gpx.set(location);
                                    }
                                    if (!OsMoDroid.settings.getBoolean("modeAND_gpx", false) && location.getSpeed() >= speed_gpx / 3.6 && (int) location.getAccuracy() < hdop_gpx && (location.distanceTo(prevlocation_gpx) > distance_gpx || location.getTime() > (prevlocation_gpx.getTime() + period_gpx))) {
                                        writegpx(location);
                                        prevlocation_gpx.set(location);
                                    }

                                }
                            }
                        }
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "sessionstarted=" + sessionstarted);
                            }
                        //LocalService.addlog("Session started="+sessionstarted);
                        if (live)
                            {
                                //if((int) location.getAccuracy() < hdop)
                                {
                                    if(OsMoDroid.settings.getBoolean("udpmode",false))
                                    {
                                      if(location.getTime()>prevlocation.getTime()+1000*Integer.valueOf(OsMoDroid.settings.getInt("period",10)))
                                      {
                                          prevlocation.set(location);
                                          sendlocation(location, true);
                                      }

                                    }
                                    else {
                                        prevlocation.set(location);
                                        sendlocation(location, true);
                                    }
                                }

                                //LocalService.addlog("live and session satrted");
                                /*if (bearing > 0)
                                    {
                                        //LocalService.addlog("bearing>0");
                                        //if(log)Log.d(this.getClass().getName(), "Попали в проверку курса для отправки");
                                        //if(log)Log.d(this.getClass().getName(), "Accuracey"+location.getAccuracy()+"hdop"+hdop);
                                        double lon1 = location.getLongitude();
                                        double lon2 = prevlocation.getLongitude();
                                        double lat1 = location.getLatitude();
                                        double lat2 = prevlocation.getLatitude();
                                        double dLon = lon2 - lon1;
                                        double y = Math.sin(dLon) * Math.cos(lat2);
                                        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
                                        brng = Math.toDegrees(Math.atan2(y, x)); //.toDeg();
                                        position = position + "\n" + getString(R.string.SendCourseChange) + OsMoDroid.df1.format(abs(brng - prevbrng));
                                        refresh();
                                        if (OsMoDroid.settings.getBoolean("modeAND", false) && (int) location.getAccuracy() < hdop && location.getSpeed() >= speed / 3.6 && (location.distanceTo(prevlocation) > distance && location.getTime() > (prevlocation.getTime() + period) && (location.getSpeed() >= (speedbearing / 3.6) && abs(brng - prevbrng) >= bearing)))
                                            {
                                                //LocalService.addlog("modeAND and accuracy and speed");
                                                prevlocation.set(location);
                                                prevbrng = brng;
                                                //if(log)Log.d(this.getClass().getName(), "send(location)="+location);
                                                sendlocation(location,true);
                                            }
                                        else
                                            {
                                                //LocalService.addlog("modeAND and accuracy and speed -ELSE");
                                            }
                                        if (!OsMoDroid.settings.getBoolean("modeAND", false) && (int) location.getAccuracy() < hdop && location.getSpeed() >= speed / 3.6 && (location.distanceTo(prevlocation) > distance || location.getTime() > (prevlocation.getTime() + period) || (location.getSpeed() >= (speedbearing / 3.6) && abs(brng - prevbrng) >= bearing)))
                                            {
                                                //LocalService.addlog("not modeAND and accuracy and speed");
                                                prevlocation.set(location);
                                                prevbrng = brng;
                                                //if(log)Log.d(this.getClass().getName(), "send(location)="+location);
                                                sendlocation(location,true);
                                            }
                                        else
                                            {
                                                //LocalService.addlog("not modeAND and accuracy and speed - ELSE");
                                            }
                                    }
                                else
                                    {
                                        //LocalService.addlog("bearing>0 - ELSE");
                                        //if(log)Log.d(this.getClass().getName(), "Отправляем без курса");
                                        if (OsMoDroid.settings.getBoolean("modeAND", false) && (int) location.getAccuracy() < hdop && location.getSpeed() >= speed / 3.6 && (location.distanceTo(prevlocation) > distance && location.getTime() > (prevlocation.getTime() + period)))
                                            {
                                                //LocalService.addlog("modeAND and accuracy and speed");
                                                //if(log)Log.d(this.getClass().getName(), "Accuracey"+location.getAccuracy()+"hdop"+hdop);
                                                prevlocation.set(location);
                                                //if(log)Log.d(this.getClass().getName(), "send(location)="+location);
                                                sendlocation(location,true);
                                            }
                                        else
                                            {
                                                //LocalService.addlog("modeAND and accuracy and speed - ELSE");
                                            }
                                        if (!OsMoDroid.settings.getBoolean("modeAND", false) && (int) location.getAccuracy() < hdop && location.getSpeed() >= speed / 3.6 && (location.distanceTo(prevlocation) > distance || location.getTime() > (prevlocation.getTime() + period)))
                                            {
                                                //LocalService.addlog("not modeAND and accuracy and speed");
                                                //if(log)Log.d(this.getClass().getName(), "Accuracey"+location.getAccuracy()+"hdop"+hdop);
                                                prevlocation.set(location);
                                                //if(log)Log.d(this.getClass().getName(), "send(location)="+location);
                                                sendlocation(location,true);
                                            }
                                        else
                                            {
                                                //LocalService.addlog("modeAND and accuracy and speed - ELSE");
                                            }
                                    }*/
                            }
                        else
                            {
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), " not !hash.equals() && live&&sessionstarted");
                                    }
                                //LocalService.addlog("live and session satrted - ELSE");
                            }
                    }
                else
                    {
                        //LocalService.addlog("Provider=GPS - ELSE");
                    }
            }
        public void onProviderDisabled(String provider)
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Выключен провайдер:" + provider);
                    }
                addlog("Выключен провайдер:" + provider);
            }
        public void onProviderEnabled(String provider)
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Включен провайдер:" + provider);
                    }
                addlog("Включен провайдер:" + provider);
            }
        public void onStatusChanged(String provider, int status, Bundle extras)
            {
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Изменился статус провайдера:" + provider + " статус:" + status + " Бандл:" + extras.getInt("satellites"));
                    }
                //addlog("Изменился статус провайдера:"+provider+" статус:"+status+" Бандл:"+extras.getInt("satellites"));
            }

        void internetnotify(boolean internet)
            {
                if (!internet)
                    {
                        if (!beepedoff)
                            {
                                //long[] pattern = {0,50, 0, 30, 0, 50};
                                //vibrator.vibrate(pattern, 2);
                                if (vibrate)
                                    {
                                        vibrator.vibrate(vibratetime);
                                    }
                                //if (playsound &&inetoff!=null&& !inetoff.isPlaying()){inetoff.start();}
                                if (playsound)
                                    {
                                        //soundPool.play(inetoff, 1f, 1f, 1, 0, 1f);
                                        if (tts != null && OsMoDroid.settings.getBoolean("usetts", false)&&state)
                                            {
                                                tts.speak(getString(R.string.inetoff), TextToSpeech.QUEUE_ADD, null);
                                            }
                                    }
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "Интернет пропал");
                                    }
                                beepedoff = true;
                                beepedon = false;
                            }
                    }
                else
                    {
                        if (!beepedon)
                            {
                                //long[] pattern = {0,50, 0, 30, 0, 50};
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "Интернет появился");
                                    }
                                //vibrator.vibrate(pattern, 2);
                                if (vibrate)
                                    {
                                        vibrator.vibrate(vibratetime);
                                    }
//				if (playsound &&ineton!=null&&!ineton.isPlaying()){
//
//
//
//				ineton.start();
//
//				}
                                if (playsound)
                                    {
                                        //soundPool.play(ineton, 1f, 1f, 1, 0, 1f);
                                        if (tts != null && OsMoDroid.settings.getBoolean("usetts", false)&&state)
                                            {
                                                tts.speak(getString(R.string.ineton), TextToSpeech.QUEUE_ADD, null);
                                            }
                                    }
                                beepedon = true;
                                beepedoff = false;
                            }
                    }
            }
        public static String unescape(String s)
            {
                while (true)
                    {
                        int n = s.indexOf("&#");
                        if (n < 0)
                            {
                                break;
                            }
                        int m = s.indexOf(";", n + 2);
                        if (m < 0)
                            {
                                break;
                            }
                        try
                            {
                                s = s.substring(0, n) + (char) (Integer.parseInt(s.substring(n + 2, m))) +
                                        s.substring(m + 1);
                            }
                        catch (Exception e)
                            {
                                return s;
                            }
                    }
                s = s.replace("&quot;", "\"");
                s = s.replace("&lt;", "<");
                s = s.replace("&gt;", ">");
                s = s.replace("&amp;", "&");
                return s;
            }
        private void writegpx(Location location)
            {
                FileWriter trackwr;
                long gpstime = location.getTime();
                Date date = new Date(gpstime);
                // SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ");
                String strgpstime = OsMoDroid.sdf1.format(date) + "Z";
                writecounter++;
                if ((gpxbuffer).length() < 5000)
                    {
                        gpxbuffer = gpxbuffer
                                + "<trkpt lat=\"" + OsMoDroid.df6.format(location.getLatitude()) + "\""
                                + " lon=\"" + OsMoDroid.df6.format(location.getLongitude()) + "\">"
                                +"<ele>" + OsMoDroid.df0.format(location.getAltitude())+ "</ele>"
                                +"<time>" + strgpstime + "</time>"
                                +"<speed>" + OsMoDroid.df1.format(location.getSpeed())+ "</speed>"
                                + "<sat>"+ countFix + "</sat>"
                                + "<hdop>" + OsMoDroid.df0.format(location.getAccuracy() / 4) + "</hdop>"
                                + "</trkpt>";
                    }
                else
                    {
                        try
                            {
                                trackwr = new FileWriter(gpxfile, true);
                                String towright = gpxbuffer;
                                trackwr.write(towright);//.replace(",", "."));
                                trackwr.flush();
                                trackwr.close();
                                gpxbuffer = "";
                            }
                        catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                    }
            }
        private void sendlocation(Location location, boolean gps)
            {
                LocalService.addlog("void sendlocation");
                if (log)
                    {
                        Log.d(this.getClass().getName(), "void sendlocation");
                    }
//http://t.esya.ru/?60.452323:30.153262:5:53:25:hadfDgF:352
//	- 0 = latitudedecimal(9,6) (широта)
//	- 1 = longitudedecimal(9,6) (долгота)
//	- 2 = HDOPfloat (горизонтальная ошибка: метры)
//	- 3 = altitudefloat (высота на уровнем моря: метры)
//	- 4 = speedfloat(1) (скорость: метры в секунду)
//	- 5 = hashstring (уникальный хеш пользователя)
//	- 6 = checknumint(3) (контрольное число к хешу)
                //T|L53.1:30.3S2A4H2B23


                if(OsMoDroid.settings.getBoolean("udpmode", false))
                {
                    String sendingUDP = locationtoSending(location);
                    LocalService.addlog("udpmode is on");
                    if(!OsMoDroid.settings.getString("udptoken","UNKNOWN").equals("UNKNOWN")
                    ||!OsMoDroid.settings.getString("udptoken","UNKNOWN").equals("null")
                    )
                    myIM.sendUDP(OsMoDroid.settings.getString("udptoken","UNKNOWN")+"__"+sendingUDP);
                    else
                        LocalService.addlog("udpmode no TOKEN!!!! ");

                }
                else {
                    if (myIM != null && myIM.authed && sending.equals("") && sessionstarted)
                    {
                        sending = locationtoSending(location);
                        if (!gps)
                        {
                            sending = sending + "M";
                        }
                        LocalService.addlog("Send:AUTHED=" + myIM.authed + " Sending:" + sending);
                        myIM.sendToServer(sending, false);
                        LocalService.addlog("Sendaf:AUTHED=" + myIM.authed + " Sending:" + sending);
                    }
                    else
                        {
                         LocalService.addlog("Send not executed:AUTHED=" + myIM.authed + " Sending:" + sending);
                         if (usebuffer)
                         {
                            buffer.add(locationtoSending(location));
                            buffercounter++;
                         }
                        }
                     }
                if (myIM != null && !myIM.authed && OsMoDroid.settings.getBoolean("sendsms",false))
                    {
                        if(SystemClock.uptimeMillis()>lastsmstime+1000*Integer.parseInt(OsMoDroid.settings.getString("smsperiod","300")))
                            {
                                lastsmstime=SystemClock.uptimeMillis();
                                try
                                    {
                                        addlog("try to send sms");

                                        String messageText = "L" + OsMoDroid.df6.format(location.getLatitude()) + ":" + OsMoDroid.df6.format(location.getLongitude())
                                            + "S" + OsMoDroid.df1.format(location.getSpeed())
                                            + "A" + OsMoDroid.df0.format(location.getAltitude())
                                            + "H" + OsMoDroid.df0.format(location.getAccuracy())
                                            + "C" + OsMoDroid.df0.format(location.getBearing());
                                        short port = 901;


                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendDataMessage(OsMoDroid.settings.getString("sendsmsnumber",""), null, port, messageText.getBytes(), null, null);

                                    }
                                    catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            writeException(e);

                                        }
                            }
                        else
                            {
                                addlog("Еще рано!");
                            }
                    }
                else
                    {
                        addlog(Boolean.toString(myIM==null)+Boolean.toString(!myIM.authed)+Boolean.toString(OsMoDroid.settings.getBoolean("sendsms",false)));
                    }
            }

        private String locationtoSending(Location location) {
            String sending="";
            if (log)
                {
                    Log.d(this.getClass().getName(), "Отправка:" + myIM.authed + " s " + sending);
                }
            if ((location.getSpeed() * 3.6) >= 6)
                {
                    sending =
                            ((OsMoDroid.settings.getBoolean("udpmode", false))?"Z":"T")
                                    + "|L" + OsMoDroid.df6.format(location.getLatitude()) + ":" + OsMoDroid.df6.format(location.getLongitude())
                                    + ((OsMoDroid.settings.getBoolean("udpmode", false))?"B"+batteryprocent:"")
                                    + "S" + OsMoDroid.df1.format(location.getSpeed())
                                    + "A" + OsMoDroid.df0.format(location.getAltitude())
                                    + "H" + OsMoDroid.df0.format(location.getAccuracy())
                                    + "C" + OsMoDroid.df0.format(location.getBearing());
                    if (usebuffer)
                        {
                            sending = sending + "T" + location.getTime() / 1000;
                        }
                }
            if ((location.getSpeed() * 3.6) < 6)
                {
                    sending =
                            ((OsMoDroid.settings.getBoolean("udpmode", false))?"Z":"T")
                                    + "|L" + OsMoDroid.df6.format(location.getLatitude()) + ":" + OsMoDroid.df6.format(location.getLongitude())
                                    + ((OsMoDroid.settings.getBoolean("udpmode", false))?"B"+batteryprocent:"")
                                    + "S" + OsMoDroid.df1.format(location.getSpeed())
                                    + "A" + OsMoDroid.df0.format(location.getAltitude())
                                    + "H" + OsMoDroid.df0.format(location.getAccuracy());
                    if (usebuffer)
                        {
                            sending = sending + "T" + location.getTime() / 1000;
                        }
                }
            if ((location.getSpeed() * 3.6) <= 1)
                {
                    sending =
                            ((OsMoDroid.settings.getBoolean("udpmode", false))?"Z":"T")
                                    + "|L" +OsMoDroid.df6.format(location.getLatitude()) + ":" + OsMoDroid.df6.format(location.getLongitude())
                                    + ((OsMoDroid.settings.getBoolean("udpmode", false))?"B"+batteryprocent:"")
                                    + "A" + OsMoDroid.df0.format(location.getAltitude())
                                    + "H" + OsMoDroid.df0.format(location.getAccuracy());
                    if (usebuffer)
                        {
                            sending = sending + "T" + location.getTime() / 1000;
                        }
                }
                return sending;
        }

        public void onGpsStatusChanged(int event)
            {
                int MaxPrn = 0;
                int count1 = 0;
                int countFix1 = 0;
                boolean hasA = false;
                boolean hasE = false;
                try {
                    GpsStatus xGpsStatus = myManager.getGpsStatus(null);
                    Iterable<GpsSatellite> iSatellites = xGpsStatus.getSatellites();
                    Iterator<GpsSatellite> it = iSatellites.iterator();
                    while (it.hasNext())
                        {
                            GpsSatellite oSat = (GpsSatellite) it.next();
                            count1 = count1 + 1;
                            hasA = oSat.hasAlmanac();
                            hasE = oSat.hasEphemeris();
                            if (oSat.usedInFix())
                                {
                                    countFix1 = countFix1 + 1;
                                    if (oSat.getPrn() > MaxPrn)
                                        {
                                            MaxPrn = oSat.getPrn();
                                        }
                                    //Log.e("A fost folosit ", "int fix!");
                                }
                        }
                    satellite = getString(R.string.Sputniki) + count + ":" + countFix; //+" ("+hasA+"-"+hasE+")";
                    count = count1;
                    countFix = countFix1;
                    refresh();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                //addlog("onGpsStatusChanged "+count1+" "+countFix1);
            }
        public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                    {
                        langTTSavailable = tts.setLanguage(Locale.getDefault());
                        if (langTTSavailable == TextToSpeech.LANG_MISSING_DATA || langTTSavailable == TextToSpeech.LANG_NOT_SUPPORTED)
                            {
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "No TTS for system language");
                                    }
                            }
                        else if (langTTSavailable >= 0 && OsMoDroid.settings.getBoolean("usetts", false))
                            {
                                if (log)
                                    {
                                        Log.d(this.getClass().getName(), "TTS succefully start");
                                    }
                            }
                    }
                else
                    {
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "TTS succefully inited");
                            }
                    }
            }
        void bitmapmapview()
            {
                boolean odin =false;
                if(mapController!=null)
                    {



                        double nord = 0, sud = 0, ovest = 0, est = 0;
                        for(Channel ch:LocalService.channelList)
                            {
                                if(ch.type==2)
                                    {
                                        if(ch.deviceList.size()>1)
                                            {
                                                odin=false;
                                            }
                                        else
                                        {
                                            odin=true;
                                        }
                                        for (Device dev : ch.deviceList)
                                            {
                                                int count=0;
                                                if(dev.lat!=0&&dev.lon!=0) {
                                                    count++;
                                                    double lat = dev.lat;
                                                    double lon = dev.lon;
                                                    if (nord == 0) {
                                                        nord = lat;
                                                    }
                                                    if (sud == 0) {
                                                        sud = lat;
                                                    }
                                                    if (ovest == 0) {
                                                        ovest = lon;
                                                    }
                                                    if (est == 0) {
                                                        est = lon;
                                                    }

                                                    if ((lat > nord)) nord = lat;
                                                    if ((lat < sud)) sud = lat;
                                                    if ((lon < ovest)) ovest = lon;
                                                    if ((lon > est)) est = lon;
                                                }
                                                if(count<2)
                                                {
                                                    odin=true;
                                                }


                                            }
//                                        if(ch.deviceList.size()>0){
//                                            lat=lat/ch.deviceList.size();
//                                            lon=lon/ch.deviceList.size();
//                                        }

                                    }
                            }

                       final BoundingBox b = new BoundingBox(nord, est, sud, ovest);
                        //GeoPoint startPoint = new GeoPoint(lat, lon);
                        //mapController.setCenter(startPoint);
                        final boolean finalOdin = odin;
                        mMapView.zoomToBoundingBox(b, false);


                        alertHandler.postDelayed(new Runnable()
                                                     {
                                                         @Override
                                                         public void run()
                                                             {if(finalOdin){
                                                               mapController.setZoom(10);


                                                                // Toast.makeText(LocalService.this, "ОДИН", Toast.LENGTH_LONG).show();
                                                             }
//                                                                 else
//                                                                 {
//                                                                     mMapView.zoomToBoundingBox(b, false);
//                                                                     mapController.zoomOut();
//                                                                  //   Toast.makeText(LocalService.this, "НЕ ОДИН", Toast.LENGTH_LONG).show();
//                                                                 }

                                                                 //Toast.makeText(LocalService.this, b.toString(), Toast.LENGTH_LONG).show();
                                                             }
                                                     },3000);


                        alertHandler.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                    {
                                        Bitmap bm = Bitmap.createBitmap(linearview.getMeasuredWidth(), linearview.getMeasuredHeight(), Bitmap.Config.ARGB_4444);
                                        Canvas c = new Canvas(bm);
                                        linearview.draw(c);
                                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                                        ComponentName thisWidget;
                                        thisWidget = new ComponentName(LocalService.this, MapWidget.class);
                                        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                                        Intent is = new Intent(LocalService.this, LocalService.class);
                                        for (int widgetId : allWidgetIds)
                                            {
                                                RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.map_widget);
                                                Intent notificationIntent = new Intent(LocalService.this, GPSLocalServiceClient.class);
                                                notificationIntent.setAction("mapfromwidget");
                                                //notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                                                osmodroidLaunchIntent = PendingIntent.getActivity(LocalService.this, 0, notificationIntent, 0);
                                                remoteViews.setOnClickPendingIntent(R.id.mapimageView, osmodroidLaunchIntent);
                                                remoteViews.setImageViewBitmap(R.id.mapimageView, bm);

                                                appWidgetManager.updateAppWidget(widgetId, remoteViews);
                                            }
                                    }
                            }, 10000);
                    }
            }
        public synchronized boolean isOnline()
            {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo nInfo = cm.getActiveNetworkInfo();
                if (nInfo != null && nInfo.isConnected())
                    {
                        Log.v("status", "ONLINE");
                        addlog("isOnline=true");
                        return true;
                    }
                else
                    {
                        Log.v("status", "OFFLINE");
                        addlog("isOnline=false");
                        return false;
                    }
            }
        void notifywarnactivity(String info, boolean supportButton, int mode)
            {
                if (!OsMoDroid.gpslocalserviceclientVisible)
                    {
                        Long when = System.currentTimeMillis();
                        Intent notificationIntent = new Intent(this, WarnActivity.class);
                        notificationIntent.removeExtra("info");
                        notificationIntent.putExtra("info", info);
                        notificationIntent.removeExtra("supportButton");
                        notificationIntent.putExtra("supportButton", supportButton);
                        notificationIntent.removeExtra("mode");
                        notificationIntent.putExtra("mode", mode);
                        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP	| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent contentIntent = PendingIntent.getActivity(this, OsMoDroid.notifyidApp(), notificationIntent, 0);
                        NotificationCompat.Builder notificationBuilder = null;
                        if (OsMoDroid.settings.getBoolean("silentnotify", false))
                            {
                                notificationBuilder = new NotificationCompat.Builder(
                                        getApplicationContext(),"default")
                                        .setWhen(when)
                                        .setContentText(info)
                                        .setContentTitle("OsMoDroid")
                                        .setSmallIcon(R.drawable.warn)
                                        .setAutoCancel(true)
                                        .setDefaults(Notification.DEFAULT_LIGHTS)
                                        .setContentIntent(contentIntent).setChannelId("silent");
                            }
                        else
                            {
                                notificationBuilder = new NotificationCompat.Builder(
                                        getApplicationContext(),"default")
                                        .setWhen(when)
                                        .setContentText(info)
                                        .setContentTitle("OsMoDroid")
                                        .setSmallIcon(R.drawable.warn)
                                        .setAutoCancel(true)
                                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND).setChannelId("noisy")
                                        .setContentIntent(contentIntent);
                            }
                        Notification notification = notificationBuilder.build();
                        mNotificationManager.notify(OsMoDroid.warnnotifyid, notification);
                    }
                else
                    {
                        Intent notificationIntent = new Intent(this, WarnActivity.class);
                        notificationIntent.removeExtra("info");
                        notificationIntent.putExtra("info", info);
                        notificationIntent.removeExtra("supportButton");
                        notificationIntent.putExtra("supportButton", supportButton);
                        notificationIntent.removeExtra("mode");
                        notificationIntent.putExtra("mode", mode);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        getApplication().startActivity(notificationIntent);
                    }
            }
        public void onResultsSucceeded(APIComResult result)
            {
                JSONArray a = null;
                if (result.Jo == null && result.ja == null&&result.Command!=null)
                    {
                        if (log)
                            {
                                Log.d(getClass().getSimpleName(), "notifwar1 Команда:" + result.Command + " Ответ сервера:" + result.rawresponse + getString(R.string.query) + result.url);
                            }
                        if (OsMoDroid.gpslocalserviceclientVisible)
                            {
                                Toast.makeText(LocalService.this, R.string.esya_ru_notrespond, Toast.LENGTH_LONG).show();
                            }
                    }
                if (result.Command!=null&&result.Command.equals("sendid"))
                    {
                        if (!(result.Jo == null))
                            {
                                if (log)
                                    {
                                        Log.d(getClass().getSimpleName(), "sendid response:" + result.Jo.toString());
                                    }
                                if (result.Jo.has("device"))
                                    {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    FirebaseMessaging.getInstance().deleteToken();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                        try
                                            {
                                                OsMoDroid.editor.putString("newkey", result.Jo.getString("device"));
                                                OsMoDroid.editor.commit();
                                                if (true)
                                                    {
                                                        myIM.start();
                                                    }
                                            }
                                        catch (JSONException e)
                                            {
                                                e.printStackTrace();
                                            }
                                    }
                            }
                        else
                            {
                                notifywarnactivity(getString(R.string.warnhash), true, OsMoDroid.NOTIFY_ERROR_SENDID);
                            }
                    }
            }
        public void playAlarmOn()
            {
                if (alarmStreamId == 0)
                    {
                        alarmStreamId = soundPool.play(alarmsound, 1f, 1f, 1, -1, 1f);
                    }
                if (log)
                    {
                        Log.d(this.getClass().getName(), "play alarm on ");
                    }
            }
        public void playAlarmOff()
            {
                soundPool.stop(alarmStreamId);
                alarmStreamId = 0;
                if (log)
                    {
                        Log.d(this.getClass().getName(), "play alarm off ");
                    }
            }
        public void enableSignalisation()
            {
                OsMoDroid.editor.putLong("signalisation", System.currentTimeMillis());
                OsMoDroid.editor.commit();
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Enable signalisation ");
                    }
                soundPool.play(signalonoff, 1f, 1f, 1, 0, 1f);
                signalisationOn = true;
                refresh();
            }
        public void disableSignalisation()
            {
                if(myIM!=null)
                    {
                        myIM.sendToServer("DISALARM",true);
                    }
                OsMoDroid.editor.remove("signalisation");
                OsMoDroid.editor.commit();
                mSensorManager.unregisterListener(this);
                if (log)
                    {
                        Log.d(this.getClass().getName(), "Disable signalisation ");
                    }
                playAlarmOff();
                soundPool.play(signalonoff, 1f, 1f, 1, 0, 1f);
                signalisationOn = false;
                refresh();
            }
        public void updatewidgets()
            {
                Log.d(getClass().getSimpleName(), "on updatewidgets state="+state);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
                ComponentName thisWidget = new ComponentName(this,OsMoWidget.class);
                int[] allWidgetIds =  appWidgetManager.getAppWidgetIds(thisWidget);
                Intent is1 = new Intent(this, LocalService.class);
                for (int widgetId : allWidgetIds) {
                    RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.os_mo_widget);
                    if(state)
                        {
                            remoteViews.setImageViewResource(R.id.imageButtonWidget, R.drawable.on);
                            is1.putExtra("ACTION", "STOP");
                            Log.d(getClass().getSimpleName(), "on updatewidgets set action=STOP state=" + state);
                        }
                    else
                        {
                            remoteViews.setImageViewResource(R.id.imageButtonWidget, R.drawable.off);
                            is1.putExtra("ACTION", "START");
                            Log.d(getClass().getSimpleName(), "on updatewidgets set action=START state=" + state);
                        }
                    PendingIntent stop1 = PendingIntent.getService(this, 0, is1, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.imageButtonWidget, stop1);
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);

                }
                thisWidget = new ComponentName(this,DoubleOsmoWidget.class);
                allWidgetIds =  appWidgetManager.getAppWidgetIds(thisWidget);
                Intent is2 = new Intent(this, LocalService.class);
                for (int widgetId : allWidgetIds) {
                    RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.double_osmo_widget);
                    if(state)
                        {
                            remoteViews.setImageViewResource(R.id.imageButtonWidget, R.drawable.on);
                            is2.putExtra("ACTION", "STOP");
                            Log.d(getClass().getSimpleName(), "on updatewidgets set action=STOP state=" + state);
                        }
                    else
                        {
                            remoteViews.setImageViewResource(R.id.imageButtonWidget, R.drawable.off);
                            is2.putExtra("ACTION", "START");
                            Log.d(getClass().getSimpleName(), "on updatewidgets set action=START state=" + state);
                        }
                    PendingIntent stop2 = PendingIntent.getService(this, 0, is2, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.imageButtonWidget, stop2);
                    remoteViews.setTextViewText(R.id.textViewWidget, OsMoDroid.df2.format(workdistance / 1000)+'\n'+OsMoDroid.df0.format(avgspeed*3600)+'\n'+formatInterval(timeperiod));

                    Intent notificationIntent = new Intent(this, GPSLocalServiceClient.class);
                    notificationIntent.setAction(Intent.ACTION_MAIN);
                    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    osmodroidLaunchIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                    remoteViews.setOnClickPendingIntent(R.id.textViewWidget, osmodroidLaunchIntent);

                    appWidgetManager.updateAppWidget(widgetId, remoteViews);

                }
                thisWidget = new ComponentName(this,SOSWidget.class);
                allWidgetIds =  appWidgetManager.getAppWidgetIds(thisWidget);
                Intent is3 = new Intent(this, LocalService.class);
                for (int widgetId : allWidgetIds) {
                    RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.soswidget);


                        is3.putExtra("GCM", "WIDGETSOS");


                    PendingIntent stop3 = PendingIntent.getService(this, 101, is3, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.soswidgetbutton, stop3);
                    Log.d(getClass().getSimpleName(), "on updatewidgets SOS state=" + sos);
                    if(LocalService.sos)
                    {
                        remoteViews.setInt(R.id.soswidgetbutton, "setBackgroundColor", Color.RED);
                    }
                    else
                    {
                        remoteViews.setInt(R.id.soswidgetbutton, "setBackgroundColor", Color.parseColor("#ffff8800"));
                    }

                    appWidgetManager.updateAppWidget(widgetId, remoteViews);

                }

            }
        public void onAccuracyChanged(Sensor sensor, int accuracy)
            {
                // TODO Auto-generated method stub
            }
        public void onSensorChanged(SensorEvent event)
            {
                double x = event.values[0];
                double y = event.values[1];
                double z = event.values[2];
                double a = Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
                        + Math.pow(z, 2)));
                currentAcceleration = abs((float) (a - calibration));
                addlog("Current accseleration="+Float.toString(currentAcceleration));
                try
                    {
                        sensivity = ((float) Integer.parseInt(OsMoDroid.settings.getString("sensivity", "5"))) / 10f;
                    }
                catch (NumberFormatException e)
                    {
                        sensivity = 0.5f;
                    }
                if (OsMoDroid.settings.contains("signalisation") && OsMoDroid.settings.getLong("signalisation", 0) + 60000 < System.currentTimeMillis() && currentAcceleration > sensivity)
                    {
                        OsMoDroid.editor.putLong("signalisation", System.currentTimeMillis());
                        OsMoDroid.editor.commit();
                        //myIM.sendToServer("REMOTE_CONTROL:"+OsMoDroid.settings.getString("tracker_id", "") +"|"+"ALARM");
                        //myIM.sendToServer("ALARM", false);
                        Intent is = new Intent(this, LocalService.class);
                        is.putExtra("GCM","NEEDSENDALARM");
                        handleStart(is,0);
                        if (log)
                            {
                                Log.d(this.getClass().getName(), "Alarm Alarm Alarm " + Float.toString(currentAcceleration));
                            }
                    }
            }


        void updateNotification(int icon)
            {
                if (foregroundnotificationBuilder != null)
                    {
                        foregroundnotificationBuilder.setContentText(getString(R.string.Sendcount) + sendcounter + ' ' + getString(R.string.writen) + writecounter);
                        if (icon != -1)
                            {
                                foregroundnotificationBuilder.setSmallIcon(icon);
                                foregroundnotificationBuilder.setColor(Color.parseColor("#FFA500"));
                            }
                        mNotificationManager.notify(OSMODROID_ID, foregroundnotificationBuilder.build());
                    }
            }
        @Override
        public void osmandMissing()
            {

            }

        public void sendwidgetinfo(LocalService localService) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
            ComponentName thisWidget1 = new ComponentName(this,OsMoWidget.class);
            ComponentName thisWidget2 = new ComponentName(this,DoubleOsmoWidget.class);
            ComponentName thisWidget3 = new ComponentName(this,MapWidget.class);
            ComponentName thisWidget4 = new ComponentName(this,SOSWidget.class);
            int[] allWidgetIds1 =  appWidgetManager.getAppWidgetIds(thisWidget1);
            int[] allWidgetIds2 =  appWidgetManager.getAppWidgetIds(thisWidget2);
            int[] allWidgetIds3 =  appWidgetManager.getAppWidgetIds(thisWidget3);
            int[] allWidgetIds4 =  appWidgetManager.getAppWidgetIds(thisWidget4);
            JSONArray ja = new JSONArray();
            ja.put(allWidgetIds1.length);
            ja.put(allWidgetIds2.length);
            ja.put(allWidgetIds3.length);
            ja.put(allWidgetIds4.length);

            myIM.sendToServer("RCR:" + OsMoDroid.WIDGETINFO + "|" + ja.toString(), false);

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            addlog("preference changed "+s);
            if(s.equals("autostart"))
            {
                if (!state&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&OsMoDroid.settings.getBoolean("autostart", false)) {
                    openForeground();
                }
                if (!state&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&!OsMoDroid.settings.getBoolean("autostart", false)) {
                    stopForeground(true);
                }
            }
        }


        public class LocalBinder extends Binder
            {
                LocalService getService()
                    {
                        return LocalService.this;
                    }
            }
        static void addlog(final String str)
            {
                if(log)Log.d("OsMoDroid", str);
                alertHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                        {
                            //if(OsMoDroid.debug)ExceptionHandler.reportOnlyHandler(parent.getApplicationContext()).uncaughtException(Thread.currentThread(), new Throwable(str));
                            if (OsMoDroid.debug)
                                {
                                    debuglist.add(IM.sdf1.format(new Date(System.currentTimeMillis())) + " " + str + " S=" + IM.sendBytes + " R=" + IM.recievedBytes+ " overall by netstat="+(TrafficStats.getUidTxBytes(context.getApplicationInfo().uid)-IM.startTraffic));
                                    if (debuglist.size() > 50000)
                                        {
                                            debuglist.remove(0);
                                        }
                                }
                            if (debugAdapter != null)
                                {
                                    debugAdapter.notifyDataSetChanged();
                                }
                        }
                });
            }
        ArrayList<String> emptyList = new ArrayList<>();
        void osmAndAddAllChannels()
            {
                for(Channel ch: LocalService.channelList)
                    {
                        if(ch.send)
                            {
                                osmand.addMapLayer(Integer.toString(ch.u), ch.name, 5.5f, null);
                                for (Device dev : ch.deviceList)
                                    {
                                        osmand.addMapPoint(Integer.toString(ch.u), Integer.toString(dev.u), dev.name.length() > 0 ? dev.name.substring(0, 1) : "", dev.name, "User", dev.color, new ALatLon(dev.lat, dev.lon), emptyList);
                                    }
                                for (Channel.Point p : ch.pointList)
                                    {
                                        osmand.addMapPoint(Integer.toString(ch.u), Integer.toString(p.u), p.name.length() > 0 ? p.name.substring(0, 1) : p.name, p.name, "Point", Color.parseColor(p.color), new ALatLon(p.lat, p.lon), emptyList);
                                    }
                                for (ColoredGPX cg : ch.gpxList)
                                    {
                                        osmand.importGpxFromFile(cg.gpxfile, cg.gpxfile.getName(), "translucent_orange", true);
                                        osmand.showGpx(cg.gpxfile.getName());
                                    }
                            }
                    }
                //osmand.refreshMap();
               // addlog("osmAndAddAllChannels");
            }
        void osmandaddchannel(Channel ch)
            {
                osmand.addMapLayer(Integer.toString(ch.u),ch.name,5.5f,null);
               // osmand.refreshMap();
              //  addlog("osmandaddchannel");
            }
        void osmAndDeleteChannel(Channel ch)
            {
                osmand.removeMapLayer(Integer.toString(ch.u));
              //  osmand.refreshMap();
              //  addlog("osmAndDeleteChannel");
            }
        void osmanddeldev(Channel ch, Device dev)
            {
                osmand.removeMapPoint(Integer.toString(ch.u), Integer.toString(dev.u));
               // osmand.refreshMap();
             //   addlog("osmanddeldev");
            }
        void osmandadddevice(Channel ch, Device dev)
            {
                osmand.addMapPoint(Integer.toString(ch.u),Integer.toString(dev.u),dev.name.length()>0 ? dev.name.substring(0,1) : "",dev.name,"User",dev.color,new ALatLon(dev.lat,dev.lon),emptyList);
               // osmand.refreshMap();
              //  addlog("osmandadddevice");
            }
        void osmandaddpoint(Channel ch, Channel.Point p)
            {
                ArrayList<String> descriptionList = new ArrayList<>();
                descriptionList.add(p.description);
                osmand.addMapPoint(Integer.toString(ch.u),Integer.toString(p.u),p.name.length()>0 ? p.name.substring(0,1):p.name,p.name,"Point", Color.parseColor(p.color),new ALatLon(p.lat,p.lon),descriptionList);
              //  osmand.refreshMap();
              //  addlog("osmandaddpoint");
            }
        void osmanddelpoint(Channel ch, Channel.Point p)
            {
                osmand.removeMapPoint(Integer.toString(ch.u), Integer.toString(p.u));
               // osmand.refreshMap();
               // addlog("osmanddelpoint");
            }

        void osmandupdDevice(Device dev)
            {
                for(Channel ch:LocalService.channelList)
                    {
                        for(Device d:ch.deviceList)
                            {
                                if(dev.equals(d))
                                    {
                                        osmand.updateMapPoint(Integer.toString(ch.u),Integer.toString(dev.u),dev.name.length()>0 ? dev.name.substring(0,1) : "",dev.name,"User",dev.color,new ALatLon(dev.lat,dev.lon),emptyList);
                                    }
                            }
                    }
              //  osmand.refreshMap();
              //  addlog("osmandupdDevice");

            }
        public void uploadMultipart(final String filepath) {
            try {
                String uploadId =
                        new MultipartUploadRequest(this, "https://api2.osmo.mobi/deviceTripUpload")
                                // starting from 3.1+, you can also use content:// URI string instead of absolute file
                                .addFileToUpload(filepath, "file")
                                .setNotificationConfig(new UploadNotificationConfig())
                                .setMaxRetries(2)
                                .addParameter("k",OsMoDroid.settings.getString("newkey", ""))
                                .setAutoDeleteFilesAfterSuccessfulUpload(true)
                                .startUpload();
            } catch (Exception exc) {
                writeException(exc);
            }
        }

        public static float distanceBetween(final IGeoPoint that,final IGeoPoint other) {

            final double a1 = DEG2RAD * that.getLatitude();
            final double a2 = DEG2RAD * that.getLongitude();
            final double b1 = DEG2RAD * other.getLatitude();
            final double b2 = DEG2RAD * other.getLongitude();

            final double cosa1 = Math.cos(a1);
            final double cosb1 = Math.cos(b1);

            final double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);

            final double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);

            final double t3 = Math.sin(a1) * Math.sin(b1);

            final double tt = Math.acos(t1 + t2 + t3);
            if(Float.isNaN((float)tt))
            {
                return 0f;
            }
            return  ((float)RADIUS_EARTH_METERS) * (float)tt;
        }

        void openForeground()
        {
            addlog("open foreground");
            int icon = R.drawable.eye;
            CharSequence tickerText = "Family mode is active"; //getString(R.string.Working);
            long when = System.currentTimeMillis();
            Intent notificationIntent = new Intent(this, GPSLocalServiceClient.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            osmodroidLaunchIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            foregroundnotificationBuilder = new NotificationCompat.Builder(this,"default");
            foregroundnotificationBuilder.setWhen(System.currentTimeMillis());
            foregroundnotificationBuilder.setContentText(tickerText);
            foregroundnotificationBuilder.setContentTitle("OsMoDroid");
            foregroundnotificationBuilder.setSmallIcon(icon);
            foregroundnotificationBuilder.setContentIntent(osmodroidLaunchIntent);
            foregroundnotificationBuilder.setChannelId("silent");
            Intent is = new Intent(this, ConfirmDialog.class);
            Notification notification = foregroundnotificationBuilder.build();
            startForeground(OSMODROID_ID, notification);
        }
    }







