package com.OsMoDroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.OsMoDroid.LocalService.LocalBinder;
import com.OsMoDroid.Netutil.MyAsyncTask;
import com.OsMoDroid.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;


import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.text.util.Linkify;
import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.SubMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
  
public class GPSLocalServiceClient extends ActionBarActivity {
	
	 


//void showFragment(SherlockFragment fragment)
//	{
//		FragmentTransaction ft = fMan.beginTransaction();
//		ft.replace(R.id.fragment_container, fragment);
//		ft.commit();
//	}
void showFragment(Fragment fragment, boolean backstack) {
	FragmentTransaction ft = fMan.beginTransaction();
	
	if(backstack)
		{	
			ft.replace(R.id.fragment_container, fragment);
			ft.addToBackStack("backstack");
		}
	else 
	{	
		ft.replace(R.id.fragment_container, fragment);
		fMan.popBackStack();
	}
	ft.commit();

}
 


	boolean messageShowed=false;
	
	
	private int speed;
	int speedbearing_gpx;
	int bearing_gpx;
	private long notifyperiod = 30000;
	int hdop_gpx;
	int period_gpx;
	int distance_gpx;
	private boolean sendsound = false;
	private boolean playsound = false;
	boolean started = false;
	private boolean vibrate;
	private boolean usecourse;
	private int vibratetime;
	LocalService mService;
	boolean mBound = false;
	int speedbearing;
	int bearing;
	private boolean gpx = false;
	boolean live = true;
	int hdop;
	int period;
	int distance;
	
	
	private int n;
	
	
	
	String position;
	String sendresult;
	BroadcastReceiver receiver;
	int sendcounter;
	int buffercounter=0;
	private boolean usebuffer = false;
	private boolean usewake = false;
	File fileName = null;
	PowerManager pm;
	WakeLock	wakeLock;// = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyWakeLock");
	String version="Unknown";
	//SharedPreferences OsMoDroid.settings;
	
	public android.support.v7.app.ActionBar actionBar;
	private ArrayList<String> mDrawerItems=new ArrayList<String>();
	 DrawerLayout mDrawerLayout;
	 ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
	private CharSequence mDrawerTitle;
	FragmentManager fMan;
	DrawerItemClickListener drawClickListener;
	upd mainUpdListener;
	private BroadcastReceiver mIMstatusReciever;
	//private boolean afterrotate=false;
	private Intent needIntent;
	public interface upd {
		void update();
	}
	
	
	ServiceConnection conn = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			 Log.d(this.getClass().getSimpleName(), "onserviceconnected gpsclient");
			
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			 
			invokeService();
			started = true;
			updateMainUI();
			
		
			if(needIntent!=null){
				intentAction(needIntent);
				needIntent=null;
			}
			
				
				 if (mService.myIM!=null){
					 if(mService.myIM.connOpened&&!mService.myIM.connecting){
				
						 actionBar.setLogo(R.drawable.eyeo);
						 if(mService.state)
							 {
								 int icon = R.drawable.eyeo;
								 //CharSequence tickerText = getString(R.string.monitoringstarted); //getString(R.string.Working);
								 long when = System.currentTimeMillis();
								 mService.notification = new Notification(icon, "", when);
								 mService.notification.setLatestEventInfo(getApplicationContext(), "OsMoDroid", getString(R.string.Sendcount)+mService.sendcounter+getString(R.string.writen)+mService.writecounter, mService.osmodroidLaunchIntent);
								 mService.mNotificationManager.notify(mService.OSMODROID_ID, mService.notification);
							 }
					 } else if (mService.myIM.connecting) 
					 {
						 actionBar.setLogo(R.drawable.eyeu);
						 if(mService.state)
							 {
								 int icon = R.drawable.eyeu;
								 //CharSequence tickerText = getString(R.string.monitoringstarted); //getString(R.string.Working);
								 long when = System.currentTimeMillis();
								 mService.notification = new Notification(icon, "", when);
								 mService.notification.setLatestEventInfo(getApplicationContext(), "OsMoDroid", getString(R.string.Sendcount)+mService.sendcounter+getString(R.string.writen)+mService.writecounter, mService.osmodroidLaunchIntent);
								 mService.mNotificationManager.notify(mService.OSMODROID_ID, mService.notification);
							 }
					 }
					 else
					 {
						 actionBar.setLogo(R.drawable.eyen);
						 if(mService.state)
							 {
								 int icon = R.drawable.eyen;
								 //CharSequence tickerText = getString(R.string.monitoringstarted); //getString(R.string.Working);
								 long when = System.currentTimeMillis();
								 mService.notification = new Notification(icon, "", when);
								 mService.notification.setLatestEventInfo(getApplicationContext(), "OsMoDroid", getString(R.string.Sendcount)+mService.sendcounter+getString(R.string.writen)+mService.writecounter, mService.osmodroidLaunchIntent);
								 mService.mNotificationManager.notify(mService.OSMODROID_ID, mService.notification);
							 }
					 }
						 
				 }
				
			
		}

		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
			
			Log.d(this.getClass().getSimpleName(), "onservicedisconnected gpsclient");
		}
	};
	private boolean proceednewintent=false;
	private ArrayAdapter<String> menuAdapter;

	 protected void updateMainUI() {
		 Log.d(this.getClass().getSimpleName(), "updateMainUI gpsclient");
		 if(mainUpdListener!=null){
			 Log.d(this.getClass().getSimpleName(), "updateMainUI not null gpsclient");
			mainUpdListener.update();
		}
		
	}

	@Override
	 protected void onPause(){
		 Log.d(this.getClass().getSimpleName(), "onPause() gpsclient");
	 
		 
	 if (!(wakeLock==null) &&wakeLock.isHeld())wakeLock.release();

		super.onPause();

	 }

	@Override
	protected void onStop() {
		Log.d(this.getClass().getSimpleName(), "onStop() gpsclient");
		OsMoDroid.gpslocalserviceclientVisible=false;
		super.onStop();

	}



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//outState.putString("currentItem", drawClickListener.currentItemName);
		Log.d(this.getClass().getSimpleName(), "onsave gpsclient");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(this.getClass().getSimpleName(), "onCreate() gpsclient"); 
		
		super.onCreate(savedInstanceState);
		OsMoDroid.activity=this;
		PreferenceManager.setDefaultValues(this, R.xml.pref, true);
		ReadPref();
		String sdState = android.os.Environment.getExternalStorageState();

		
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {

		 File sdDir = android.os.Environment.getExternalStorageDirectory();

		 fileName = new File (sdDir, "OsMoDroid/");

		 fileName.mkdirs();

		 fileName = new File(sdDir, "OsMoDroid/settings.dat");

		 }
		

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		String strVersionName = getString(R.string.Unknow);

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			strVersionName = packageInfo.packageName + " "
					+ packageInfo.versionName;
			version=packageInfo.versionName;
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
		}
		
			setContentView(R.layout.activity_main);
	        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	        mDrawerList = (ListView) findViewById(R.id.left_drawer);
		        // Set the adapter for the list view
		    	setupDrawerList();

		     
		        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		        mDrawerLayout.setBackgroundColor(Color.WHITE);
		        mDrawerList.setCacheColorHint(0);
		       
			
	         actionBar = getSupportActionBar();
	         actionBar.setDisplayHomeAsUpEnabled(true);
	         actionBar.setHomeButtonEnabled(true);

	        mTitle = mDrawerTitle = getTitle();
	        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  /* host Activity */
	                mDrawerLayout,         /* DrawerLayout object */
	                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
	                R.string.drawer_open,  /* "open drawer" description for accessibility */
	                R.string.drawer_close  /* "close drawer" description for accessibility */
	                ) {
	            public void onDrawerClosed(View view) {
	            	super.onDrawerClosed(view);
	            	
	            }

	            public void onDrawerOpened(View drawerView) {
	            	  super.onDrawerOpened(drawerView);
	            	
	            }
	        };
	        mDrawerLayout.setDrawerListener(mDrawerToggle);
	        fMan=getSupportFragmentManager();
			drawClickListener = new DrawerItemClickListener(fMan, mDrawerList, mDrawerLayout, this);
			   mDrawerList.setOnItemClickListener(drawClickListener);
			
			bindService();
			if(savedInstanceState!=null){
			//	afterrotate=true;
				//drawClickListener.selectItem(savedInstanceState.getString("currentItem"));
			}else
			{
				
				//drawClickListener.selectItem(getString(R.string.tracker));
			}
			mIMstatusReciever = new BroadcastReceiver(){

				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.hasExtra("connecting")&&intent.hasExtra("connect"))
					{
						
						if(intent.getBooleanExtra("connect", false)&&!intent.getBooleanExtra("connecting", false))
							{
								actionBar.setLogo(R.drawable.eyeo);
								 if(mService!=null&&mService.state)
									 {
										 int icon = R.drawable.eyeo;
										 //CharSequence tickerText = getString(R.string.monitoringstarted); //getString(R.string.Working);
										 long when = System.currentTimeMillis();
										 mService.notification = new Notification(icon, "", when);
										 mService.notification.setLatestEventInfo(getApplicationContext(), "OsMoDroid", getString(R.string.Sendcount)+mService.sendcounter+getString(R.string.writen)+mService.writecounter, mService.osmodroidLaunchIntent);
										 mService.mNotificationManager.notify(mService.OSMODROID_ID, mService.notification);
										 
									 }
							}
						else if (intent.getBooleanExtra("connecting", false))
							{
								actionBar.setLogo(R.drawable.eyeu);
								 if(mService!=null&&mService.state)
									 {
										 int icon = R.drawable.eyeu;
										 //CharSequence tickerText = getString(R.string.monitoringstarted); //getString(R.string.Working);
										 long when = System.currentTimeMillis();
										 mService.notification = new Notification(icon, "", when);
										 mService.notification.setLatestEventInfo(getApplicationContext(), "OsMoDroid", getString(R.string.Sendcount)+mService.sendcounter+getString(R.string.writen)+mService.writecounter, mService.osmodroidLaunchIntent);
										 mService.mNotificationManager.notify(mService.OSMODROID_ID, mService.notification);
									 }
							}
						else	
							{
								actionBar.setLogo(R.drawable.eyen);
								 if(mService!=null&&mService.state)
									 {
										 int icon = R.drawable.eyen;
										 //CharSequence tickerText = getString(R.string.monitoringstarted); //getString(R.string.Working);
										 long when = System.currentTimeMillis();
										 mService.notification = new Notification(icon, "", when);
										 mService.notification.setLatestEventInfo(getApplicationContext(), "OsMoDroid", getString(R.string.Sendcount)+mService.sendcounter+getString(R.string.writen)+mService.writecounter, mService.osmodroidLaunchIntent);
										 mService.mNotificationManager.notify(mService.OSMODROID_ID, mService.notification);
									 }
							}
					}
					
				}
				
			};
			registerReceiver(mIMstatusReciever, new IntentFilter("OsMoDroid"));
	}


	void setupDrawerList() {
		mDrawerItems.clear();
//		if (!OsMoDroid.settings.getString("key", "" ).equals("") ){
			 String[] menu1 = new String[] {
					    getString(R.string.tracker), getString(R.string.stat),getString(R.string.map),
					    getString(R.string.chanals),getString(R.string.devices),
					    getString(R.string.links),
					    getString(R.string.notifications), getString(R.string.tracks) , getString(R.string.exit)};
			if(OsMoDroid.debug){
				 menu1 = new String[] {
					    getString(R.string.tracker), getString(R.string.stat),getString(R.string.map),
					    getString(R.string.chanals),getString(R.string.devices),//
					    getString(R.string.links),
					    getString(R.string.notifications), getString(R.string.tracks) , getString(R.string.exit), "debug"};
				
			}
			
			 
		 for (String s: menu1)
		 {
		 mDrawerItems.add(s);
		 
		 }
		// }
//		else{
//			 String[] menu2=new String[] {
//					    getString(R.string.tracker), getString(R.string.stat),getString(R.string.map),
//					    getString(R.string.tracks),getString(R.string.exit)};
//			 if(OsMoDroid.debug){
//				 menu2 = new String[] {
//						    getString(R.string.tracker), getString(R.string.stat),getString(R.string.map),
//						    getString(R.string.tracks),getString(R.string.exit), "debug"};
//				
//			}
//			for (String s: menu2 ){
//				mDrawerItems.add(s);
//			}
//		}
		
		menuAdapter=new ArrayAdapter<String>(this,R.layout.drawer_list_item, mDrawerItems);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, mDrawerItems));
	}
	 
	
	 @Override
		protected void onPostCreate(Bundle savedInstanceState) {
		 Log.d(this.getClass().getSimpleName(), "onpostcreate gpsclient");	
		 super.onPostCreate(savedInstanceState);
			mDrawerToggle.syncState();
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			Log.d(this.getClass().getSimpleName(), "onconfigchanged gpsclient");
			super.onConfigurationChanged(newConfig);
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
		
		 @Override
			public boolean onOptionsItemSelected(MenuItem item) {
			
				 if(item.getItemId()==android.R.id.home)
				 {
					 if(mDrawerLayout.isDrawerOpen(mDrawerList))
					 {
						 mDrawerLayout.closeDrawer(mDrawerList);
					 }
					 else {
						mDrawerLayout.openDrawer(mDrawerList);
					}
				 }
				return super.onOptionsItemSelected(item);
			}	
	 

	@Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		 Log.d(this.getClass().getSimpleName(), "void onActivityResult"+requestCode+" "+resultCode);
		 updateMainUI();
		if (conn == null || mService == null) {

		} else {
			if(requestCode==0){
				Log.d(this.getClass().getSimpleName(), "void onActivityResult=preference");
			mService.applyPreference();
			}
			if(requestCode==1&&resultCode==Activity.RESULT_OK){
				Log.d(this.getClass().getSimpleName(), "void onActivityResult=auth");
				mService.myIM.stop();
				mService.myIM.start();
			}
			

		}
	   
	  }
	
	
	@Override
	protected void onResume() {
		super.onResume();
		 Log.d(this.getClass().getSimpleName(), "onResume() gpsclient");
		OsMoDroid.gpslocalserviceclientVisible=true;
	
		ReadPref();
		started = checkStarted();
		
		
//		if (hash.equals("") && live) {
//			RequestAuthTask requestAuthTask = new RequestAuthTask();
//			requestAuthTask.execute();
//			
//		}
		

	}



	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Log.d(this.getClass().getSimpleName(), "onresumefragments gpsclient");
		updateMainUI();
		if(!proceednewintent){
		if(mBound){
		intentAction(getIntent());
		
		}
		else{
			needIntent=getIntent();
		}
		}
		proceednewintent=false;
//		if (getIntent().getAction().equals(Intent.ACTION_MAIN)&&checkStarted()){
//			StatFragment stat = new StatFragment();
//			showFragment(stat);
//		}
		
	}


	private void intentAction(Intent intent) {
		Log.d(this.getClass().getSimpleName(), "intentaction ");
		if(intent.getAction()!=null){
		if (intent.getAction().equals("devicechat"))
		{
			//DeviceChatFragment openFragment = new DeviceChatFragment();	
			Bundle bundle = new Bundle();
			bundle.putInt("deviceU", intent.getIntExtra("deviceU", -1));
			//openFragment.setArguments(bundle);
			//showFragment(openFragment,true);
			drawClickListener.selectItem(getString(R.string.devices),bundle);
			Log.d(this.getClass().getSimpleName(), "on new intent=devicechat");
		} else
		if (intent.getAction().equals("notif"))
		{
			Log.d(this.getClass().getSimpleName(), "on new intent=notif");
			NotifFragment notif =new NotifFragment();
			drawClickListener.selectItem(getString(R.string.notifications),null);
			//showFragment(notif,false);
		} else
		if (intent.getAction().equals("channelchat"))
		{
			Log.d(this.getClass().getSimpleName(), "on new intent=channelchat");
			//ChannelDevicesFragment openFragment = new ChannelDevicesFragment();	
			Bundle bundle = new Bundle();
			bundle.putInt("channelpos", intent.getIntExtra("channelpos", -1));
			//openFragment.setArguments(bundle);
			//showFragment(openFragment,true);
			drawClickListener.selectItem(getString(R.string.chanals),bundle);
		} else
		if (intent.getAction().equals(Intent.ACTION_MAIN)){
			if(!LocalService.currentItemName.equals("")){
				drawClickListener.selectItem(LocalService.currentItemName,null);
			}else{
			Log.d(this.getClass().getSimpleName(), "on new intent=MAIN");
			drawClickListener.selectItem(getString(R.string.tracker),null);
			}
		}
		Intent i = getIntent();
		i.setAction(null);
		setIntent(i);
		
		}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		
	try {
		if (OsMoDroid.gpslocalserviceclientVisible){
			Log.d(this.getClass().getSimpleName(), "on new intent="+intent.getIntExtra("deviceU", -1));
			intentAction(intent);
			
		}
		proceednewintent=true;
			super.onNewIntent(intent);
	} catch (IllegalStateException e) {
		Log.d(this.getClass().getSimpleName(), "on new intent bug");
		e.printStackTrace();
	}
		
	}
	
	


	


	void auth() {
		Intent intent = new Intent(this, AuthActivity.class);
	    startActivityForResult(intent, 1);
	}


	boolean checkStarted() {

		// Log.d(this.getClass().getSimpleName(), "oncheckstartedy() gpsclient");
		return OsMoDroid.settings.getBoolean("started", false);

	}

	void ReadPref() {
		 Log.d(this.getClass().getSimpleName(), "readpref() gpsclient");


		speed =  Integer.parseInt(OsMoDroid.settings.getString("speed", "3").equals(
				"") ? "3" : OsMoDroid.settings.getString("speed", "3"));
		period = Integer.parseInt(OsMoDroid.settings.getString("period", "10000").equals(
				"") ? "10000" : OsMoDroid.settings.getString("period", "10000"));
		distance = Integer.parseInt(OsMoDroid.settings.getString("distance", "50")
				.equals("") ? "50" : OsMoDroid.settings.getString("distance", "50"));
		
		speedbearing = Integer.parseInt(OsMoDroid.settings.getString("speedbearing", "2")
				.equals("") ? "2" : OsMoDroid.settings.getString("speedbearing", "2"));
		bearing = Integer.parseInt(OsMoDroid.settings.getString("bearing", "10").equals(
				"") ? "10" : OsMoDroid.settings.getString("bearing", "2"));
		hdop = Integer
				.parseInt(OsMoDroid.settings.getString("hdop", "30").equals("") ? "30"
						: OsMoDroid.settings.getString("hdop", "30"));
		gpx = OsMoDroid.settings.getBoolean("gpx", false);
		live = OsMoDroid.settings.getBoolean("live", true);
		vibrate = OsMoDroid.settings.getBoolean("vibrate", false);
		usecourse = OsMoDroid.settings.getBoolean("usecourse", false);
		vibratetime = Integer.parseInt(OsMoDroid.settings.getString("vibratetime", "200")
				.equals("") ? "200" : OsMoDroid.settings.getString("vibratetime", "0"));
		playsound = OsMoDroid.settings.getBoolean("playsound", false);
		period_gpx = Integer.parseInt(OsMoDroid.settings.getString("period_gpx", "0")
				.equals("") ? "0" : OsMoDroid.settings.getString("period_gpx", "0"));
		distance_gpx = Integer.parseInt(OsMoDroid.settings.getString("distance_gpx", "0")
				.equals("") ? "0" : OsMoDroid.settings.getString("distance_gpx", "0"));
		speedbearing_gpx = Integer.parseInt(OsMoDroid.settings.getString(
				"speedbearing_gpx", "0").equals("") ? "0" : OsMoDroid.settings.getString(
				"speedbearing_gpx", "0"));
		bearing_gpx = Integer.parseInt(OsMoDroid.settings.getString("bearing_gpx", "0")
				.equals("") ? "0" : OsMoDroid.settings.getString("bearing", "0"));
		hdop_gpx = Integer.parseInt(OsMoDroid.settings.getString("hdop_gpx", "30")
				.equals("") ? "30" : OsMoDroid.settings.getString("hdop_gpx", "30"));
		usebuffer = OsMoDroid.settings.getBoolean("usebuffer", false);
		usewake = OsMoDroid.settings.getBoolean("usewake", false);
		notifyperiod = Integer.parseInt(OsMoDroid.settings.getString("notifyperiod",
				"30000").equals("") ? "30000" : OsMoDroid.settings.getString(
				"notifyperiod", "30000"));
		sendsound = OsMoDroid.settings.getBoolean("sendsound", false);
		// pass = OsMoDroid.settings.getString("pass", "");
		
		
		
		
	}

	void startlocalservice(){
		//Intent i = new Intent(this, LocalService.class);
		//startService(i);
		started = true;
		mService.startServiceWork();

	}

	private void bindService() {
		Log.d(this.getClass().getSimpleName(), "bindservice gpsclient");
		Intent i = new Intent("OsMoDroid.local");
		if(!mBound){
		bindService(i, conn, Context.BIND_AUTO_CREATE);
		
		Intent is = new Intent(this, LocalService.class);
		startService(is);
		}
		//updateServiceStatus();
	}

	void stop(Boolean stopsession) {
		Log.d(this.getClass().getSimpleName(), "stop() gpsclient");
		mService.stopServiceWork(stopsession);
		//Intent i = new Intent(this, LocalService.class);
		//stopService(i);

	}

	private void invokeService() {
		Log.d(this.getClass().getSimpleName(), "invokeservice() gpsclient");

		if (conn == null || mService == null) {

		} else {

			mService.refresh();
			updateMainUI();

		}
	}

	

	
	
	
	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getSimpleName(), "onDestroy() gpsclient");
		OsMoDroid.activity=null;
if (mBound) {

			try {
				unbindService(conn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d(this.getClass().getSimpleName(), "Исключение при отсоединении от сервиса");
				e.printStackTrace();
			}
		}

		conn = null;
		// releaseService();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		if (mIMstatusReciever!=null){
			unregisterReceiver(mIMstatusReciever);
		}


		// 
		super.onDestroy();
	}



	public String inputStreamToString(InputStream in) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(in));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line + "\n");
			 if (!bufferedReader.ready()) {
			       break;
			    }
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	public static String bytesToHex(byte[] b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}

	public static String SHA1(String text) {
		//Log.d(this.getClass().getName(), text);
		MessageDigest md;
		byte[] sha1hash = new byte[40];
		try {
			md = MessageDigest.getInstance("SHA-1");

			// md.update(text.getBytes());//, 0, text.length());
			sha1hash = md.digest(text.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bytesToHex(sha1hash);
	}
	
	public String getDeviceName() {
		  String manufacturer = Build.MANUFACTURER;
		  String model = Build.MODEL;
		  if (model.startsWith(manufacturer)) {
		    return capitalize(model);
		  } else {
		    return capitalize(manufacturer) + " " + model;
		  }
		}


		private String capitalize(String s) {
		  if (s == null || s.length() == 0) {
		    return "";
		  }
		  char first = s.charAt(0);
		  if (Character.isUpperCase(first)) {
		    return s;
		  } else {
		    return Character.toUpperCase(first) + s.substring(1);
		  }
		} 
		
				
	
	
	boolean saveSharedPreferencesToFile(File dst) {
	    boolean res = false;
	    ObjectOutputStream output = null;
	    try {
	        output = new ObjectOutputStream(new FileOutputStream(dst));
	        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
	        output.writeObject(pref.getAll());
	        Toast.makeText(this, R.string.prefsaved, Toast.LENGTH_SHORT).show();
	        res = true;
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (output != null) {
	                output.flush();
	                output.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}

	@SuppressWarnings({ "unchecked" }) boolean loadSharedPreferencesFromFile(File src) {
	    boolean res = false;
	    ObjectInputStream input = null;
	    try {
	        input = new ObjectInputStream(new FileInputStream(src));
	        OsMoDroid.editor.clear();
	            Map<String, ?> entries = (Map<String, ?>) input.readObject();
	            for (Entry<String, ?> entry : entries.entrySet()) {
	                Object v = entry.getValue();
	                String key = entry.getKey();

	                if (v instanceof Boolean)
	                	OsMoDroid.editor.putBoolean(key, ((Boolean) v).booleanValue());
	                else if (v instanceof Float)
	                	OsMoDroid.editor.putFloat(key, ((Float) v).floatValue());
	                else if (v instanceof Integer)
	                	OsMoDroid.editor.putInt(key, ((Integer) v).intValue());
	                else if (v instanceof Long)
	                	OsMoDroid.editor.putLong(key, ((Long) v).longValue());
	                else if (v instanceof String)
	                	OsMoDroid.editor.putString(key, ((String) v));
	            }
	            OsMoDroid.editor.commit();
	            Toast.makeText(this, R.string.prefloaded, Toast.LENGTH_SHORT).show();
	            setupDrawerList();
	        res = true;         
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (input != null) {
	                input.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}




}
