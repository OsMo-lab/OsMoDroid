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
