package com.OsMoDroid;

import static com.OsMoDroid.LocalService.addlog;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.analytics.FirebaseAnalytics;

//import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceConfig;
import net.gotev.uploadservice.data.UploadNotificationConfig;
import net.gotev.uploadservice.logger.UploadServiceLogger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
public class OsMoDroid extends Application
    {
        final public static int warnnotifyid = 3;
        final public static int mesnotifyid = 2;
        public static final String NOTIFIESFILENAME = "messagelist";
        public static final String DEVLIST = "devlist";
        public static final String CHANNELLIST = "chlist";
        public static final String app_code = "vb3JvA3m";
        public static final String TRACKER_GCM_ID = "80";
        public static final String TRACKER_BATTERY_INFO = "11";
        public static final String TRACKER_SATELLITES_INFO = "13";
        public static final String TRACKER_SYSTEM_INFO = "14";
        public static final String TRACKER_WIFI_INFO = "20";
        public static final String TRACKER_WIFI_ON = "21";
        public static final String TRACKER_WIFI_OFF = "22";
        public static final String TRACKER_VIBRATE = "41";
        public static final String TRACKER_EXIT = "42";
        public static final String TRACKER_GET_PREFS = "43";
        public static final String TRACKER_SET_PREFS = "44";
        public static final String TRACKER_SESSION_CONTINUE = "5";
        public static final String TRACKER_SESSION_PAUSE = "6";
        public static final String TRACKER_SESSION_START = "1";
        public static final String TRACKER_SESSION_STOP = "2";
        public static final String TRACKER_FOLLOW_START = "7";
        public static final String TRACKER_FOLLOW_STOP = "8";
        public static final String TTS = "46";
        public static final String WIDGETINFO = "36";
        public static final String WHERE = "12";
        public static final String WHERE_GPS_ONLY = "15";
        public static final String WHERE_NETWORK_ONLY = "16";
        public static final String SIGNAL_STATUS = "30";
        public static final String SIGNAL_OFF = "32";
        public static final String SIGNAL_ON = "31";
        public static final String ALARM_OFF = "34";
        public static final String ALARM_ON = "33";
        public static final String REFRESH_DEVICES = "91";
        public static final String REFRESH_GROUPS = "92";
        public static final String SOS_DEPRESS = "95";
        public static final String FLASH_ON = "47";
        public static final String FLASH_BLINK = "48";
        public static final String FLASH_OFF = "49";
        public static final String CHANGE_MOTD_TEXT = "85";
        public static final int NOTIFY_ERROR_SENDID = 0;
        public static final int NOTIFY_NO_DEVICE = 1;
        public static final int NOTIFY_EXPIRY_USER = 2;
        public static final int NOTIFY_NO_CONNECT = 3;
        public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
        public static final String REGISTRATION_COMPLETE = "registrationComplete";

        final static DecimalFormat df1 = new DecimalFormat("#######0.0" , DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        final static DecimalFormat df2 = new DecimalFormat("#######0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        final static DecimalFormat df0 = new DecimalFormat("########", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        final static SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm:ss");
        private static final int MIN_UPLOAD_ID = 4;
        private static final int MAX_UPLOAD_ID = 1000;
        final static DecimalFormat df6 = new DecimalFormat("########.######", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        final static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
        final static DecimalFormatSymbols dot = new DecimalFormatSymbols();
        public static final String GCMTODOLIST = "gcmtodolist";
        public static final String SOS ="99" ;
        public static final String SOSEXT ="98" ;
        public static final String SOS_OFF ="100" ;
        public static final String UPDATE_MOTD="95";
        public static boolean mesactivityVisible = false;
        public static boolean gpslocalserviceclientVisible = false;
        public static SharedPreferences settings;
        public static Context context;
        public static Editor editor;
        public static boolean debug = true;
        static int uploadnotifyid = MIN_UPLOAD_ID;
        static GPSLocalServiceClient activity;
        static InputMethodManager inputMethodManager;
        private static int notifyid = 1;
        public static FirebaseAnalytics mFirebaseAnalytics;
        static long timeshift=0;
        public static boolean permanent=false;
        public static File osmodirFile;
        public static int notifyidApp()
            {
                return notifyid++;
            }
        public static int uploadnotifyid()
            {
                if (uploadnotifyid < MAX_UPLOAD_ID)
                    {
                        return uploadnotifyid++;
                    }
                else
                    {
                        return MIN_UPLOAD_ID;
                    }
            }
        @Override
        public void onCreate()
            {
                super.onCreate();
                UploadServiceConfig.initialize(this,"silent",true);
                UploadServiceLogger.setLogLevel(UploadServiceLogger.LogLevel.Debug);
                UploadServiceLogger.setDelegate(new UploadServiceLogger.Delegate() {
                    @Override
                    public void error(@NotNull String s, @NotNull String s1, @NotNull String s2, @Nullable Throwable throwable) {
                        addlog(s+s1+s2);
                    }

                    @Override
                    public void debug(@NotNull String s, @NotNull String s1, @NotNull String s2) {
                        addlog(s+s1+s2);
                    }

                    @Override
                    public void info(@NotNull String s, @NotNull String s1, @NotNull String s2) {
                        addlog(s+s1+s2);
                    }
                });
                settings = PreferenceManager.getDefaultSharedPreferences(this);
                editor = settings.edit();
                context = getApplicationContext();
                osmodirFile = context.getFilesDir();
                Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.inContext(context));
                inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    Configuration.getInstance()
                            .setOsmdroidTileCache(
                                    context.getCacheDir()
                            );
                    Configuration.getInstance()
                            .setOsmdroidBasePath(
                                    context.getApplicationContext().getFilesDir()
                            );
                    Configuration.getInstance().load(this,settings);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);



            }
        static void saveObject(Context ctx, Object obj, String filename)
        {
            try
            {
                FileOutputStream fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream output = new ObjectOutputStream(fos);
                output.writeObject(obj);
                output.flush();
                output.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        static Object loadObject(Context ctx, String filename, Class type)
        {
            try
            {
                ObjectInputStream input = new ObjectInputStream(ctx.openFileInput(filename));
                return type.cast(input.readObject());
            }
            catch (StreamCorruptedException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                addlog("object not loaded from file - excepion");

            }
            return null;
        }
    }
