package com.OsMoDroid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;

public class OsMoDroid extends Application {
	
	private static final int MIN_UPLOAD_ID = 4;
	private static final int MAX_UPLOAD_ID = 1000;
	public static boolean mesactivityVisible = false;
	public static boolean gpslocalserviceclientVisible = false;
	private static int notifyid = 1;
	final public static int warnnotifyid = 3;
	final public static int mesnotifyid = 2;
	public static final String NOTIFIESFILENAME = "messagelist";
	public static final String DEVLIST = "devlist";
	public static final String CHANNELLIST = "chlist";
	static int uploadnotifyid = MIN_UPLOAD_ID;
	public static SharedPreferences settings;
	static GPSLocalServiceClient activity;
	public static int notifyidApp() {
	return notifyid++;
	}
	static InputMethodManager inputMethodManager;
	public static Context context;
	public static Editor editor;
	public static boolean debug=true;
	public static final String app_code = "VA3h_va2j44fva";
	public static final String TRACKER_BATTERY_INFO = "11";
	public static final String TRACKER_SATELLITES_INFO = "13";
	public static final String TRACKER_SYSTEM_INFO = "14";
	public static final String TRACKER_WIFI_INFO = "20";
	public static final String TRACKER_WIFI_ON = "21";
	public static final String TRACKER_WIFI_OFF = "22";
	public static final String TRACKER_VIBRATE = "41";
	public static final String TRACKER_EXIT = "42";
	public static final String TRACKER_SESSION_START = "1";
	public static final String TRACKER_SESSION_STOP = "2";
	public static final String TTS = "TTS";
	public static final String WHERE = "12";
	public static final String SIGNAL_OFF = "32";
	public static final String SIGNAL_ON = "31";
	public static final String ALARM_OFF = "34";
	public static final String ALARM_ON = "33";
	public static final int NOTIFY_ERROR_SENDID = 0;
	public static final int NOTIFY_NO_DEVICE = 1;
	public static final int NOTIFY_EXPIRY_USER = 2;
	public static final int NOTIFY_NO_CONNECT = 3;
	

	@Override
	public void onCreate() {
		settings =  PreferenceManager.getDefaultSharedPreferences(this);
		editor=settings.edit();
		context = getApplicationContext();
		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.inContext(context));
		inputMethodManager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		super.onCreate();
	}

	public static int uploadnotifyid() {

		if (uploadnotifyid < MAX_UPLOAD_ID) {
			return uploadnotifyid++;
		} else {
			return MIN_UPLOAD_ID;
		}

	}

}
