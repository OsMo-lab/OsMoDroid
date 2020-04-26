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
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.OsMoDroid.LocalService.LocalBinder;
import com.OsMoDroid.Netutil.MyAsyncTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.SubMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
public class GPSLocalServiceClient extends AppCompatActivity implements ResultsListener
    {
        private static final int PERMISSION_REQUEST_CODE = 777;
        //SharedPreferences OsMoDroid.settings;
        public ActionBar actionBar;
        boolean messageShowed = false;
        int speedbearing_gpx;
        int bearing_gpx;
        int hdop_gpx;
        int period_gpx;
        int distance_gpx;
        boolean started = false;
        LocalService mService;
        boolean mBound = false;
        int speedbearing;
        int bearing;
        boolean live = OsMoDroid.settings.getBoolean("live", true);;
        int hdop;
        int period;
        int distance;
        String position;
        String sendresult;
        BroadcastReceiver receiver;
        int sendcounter;
        int buffercounter = 0;
        File fileName = null;
        PowerManager pm;
        WakeLock wakeLock;// = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyWakeLock");
        String version = "Unknown";
        DrawerLayout mDrawerLayout;
        ListView mDrawerList;
        FragmentManager fMan;
        DrawerItemClickListener drawClickListener;
        upd mainUpdListener;
        private int speed;
        private long notifyperiod = 30000;
        private boolean sendsound = false;
        private boolean playsound = false;
        private boolean vibrate;
        private boolean usecourse;
        private int vibratetime;
        private boolean gpx = false;
        private int n;
        private boolean usebuffer = false;
        private boolean usewake = false;
        private ArrayList<String> mDrawerItems = new ArrayList<String>();
        private ActionBarDrawerToggle mDrawerToggle;
        private CharSequence mTitle;
        private CharSequence mDrawerTitle;
        private BroadcastReceiver mIMstatusReciever;
        //private boolean afterrotate=false;
        private Intent needIntent;
        ServiceConnection conn = new ServiceConnection()
        {
            public void onServiceConnected(ComponentName className, IBinder service)
                {
                    Log.d(this.getClass().getSimpleName(), "onserviceconnected gpsclient");
                    LocalBinder binder = (LocalBinder) service;
                    mService = binder.getService();
                    mBound = true;
                    invokeService();
                    started = true;
                    updateMainUI();
                    if (needIntent != null)
                        {
                            intentAction(needIntent);
                            needIntent = null;
                        }
                    if(OsMoDroid.settings.getBoolean("autostartsession", false))
                    {
                        if(!mService.state)
                        {
                            mService.startServiceWork(true);
                        }
                    }
                    if (mService.myIM != null)
                        {
                            mService.bitmapmapview();
                            if(!mService.myIM.start&&OsMoDroid.settings.getBoolean("live", true))
                                {
                                    mService.myIM.start();
                                }
                            if (mService.myIM.connOpened && !mService.myIM.connecting)
                                {
                                    actionBar.setIcon(R.drawable.eyeo);
                                }
                            else if (mService.myIM.connecting)
                                {
                                    actionBar.setIcon(R.drawable.eyeu);
                                }
                            else
                                {
                                    actionBar.setIcon(R.drawable.eyen);
                                }
                            if (!OsMoDroid.settings.getBoolean("subscribebackground", false) && mBound)
                                {
                                    if(mService.myIM.authed)
                                        {
                                            mService.myIM.sendToServer("PG:1", false);
                                        }
                                    // mService.myIM.sendToServer("PD:1", false);
                                }
                        }
                }
            public void onServiceDisconnected(ComponentName arg0)
                {
                    mBound = false;
                    Log.d(this.getClass().getSimpleName(), "onservicedisconnected gpsclient");
                }
        };
        private boolean proceednewintent = false;
        private ArrayAdapter<String> menuAdapter;
        public static String bytesToHex(byte[] b)
            {
                char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                        'a', 'b', 'c', 'd', 'e', 'f'};
                StringBuffer buf = new StringBuffer();
                for (int j = 0; j < b.length; j++)
                    {
                        buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
                        buf.append(hexDigit[b[j] & 0x0f]);
                    }
                return buf.toString();
            }
        public static String SHA1(String text)
            {
                //Log.d(this.getClass().getName(), text);
                MessageDigest md;
                byte[] sha1hash = new byte[40];
                try
                    {
                        md = MessageDigest.getInstance("SHA-1");
                        // md.update(text.getBytes());//, 0, text.length());
                        sha1hash = md.digest(text.getBytes());
                    }
                catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                return bytesToHex(sha1hash);
            }
        //void showFragment(SherlockFragment fragment)
//	{
//		FragmentTransaction ft = fMan.beginTransaction();
//		ft.replace(R.id.fragment_container, fragment);
//		ft.commit();
//	}
        void showFragment(Fragment fragment, boolean backstack)
            {
                if( fragment instanceof TrackStatFragment)
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                else
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                FragmentTransaction ft = fMan.beginTransaction();
                if (backstack)
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
        protected void updateMainUI()
            {
                Log.d(this.getClass().getSimpleName(), "updateMainUI gpsclient");
                if (mainUpdListener != null)
                    {
                        Log.d(this.getClass().getSimpleName(), "updateMainUI not null gpsclient");
                        mainUpdListener.update();
                    }
            }
        @Override
        protected void onPause()
            {
                Log.d(this.getClass().getSimpleName(), "onPause() gpsclient");
                if (!OsMoDroid.settings.getBoolean("subscribebackground", false) && mBound)
                    {
                       // mService.myIM.sendToServer("PD:-1", false);
                        mService.myIM.sendToServer("PG:-1", false);
                    }
                if (!(wakeLock == null) && wakeLock.isHeld())
                    {
                        wakeLock.release();
                    }
                super.onPause();
            }
        @Override
        protected void onStop()
            {
                Log.d(this.getClass().getSimpleName(), "onStop() gpsclient");
                OsMoDroid.gpslocalserviceclientVisible = false;
                super.onStop();
            }
        @Override
        protected void onSaveInstanceState(Bundle outState)
            {
                //outState.putString("currentItem", drawClickListener.currentItemName);
                Log.d(this.getClass().getSimpleName(), "onsave gpsclient");
                super.onSaveInstanceState(outState);
            }
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                Log.d(this.getClass().getSimpleName(), "onCreate() gpsclient");
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED  ||ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE

                    },PERMISSION_REQUEST_CODE);
                }
                LocalService.alertHandler.removeCallbacksAndMessages(null);
                super.onCreate(savedInstanceState);
                actionBar = getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
                //actionBar.setDisplayShowHomeEnabled(true);
                //actionBar.setDisplayUseLogoEnabled(true);
                //actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.eyeo);
                OsMoDroid.activity = this;
                PreferenceManager.setDefaultValues(this, R.xml.pref, true);
                //ReadPref();
                String sdState = android.os.Environment.getExternalStorageState();
                if (sdState.equals(android.os.Environment.MEDIA_MOUNTED))
                    {
                        File sdDir = android.os.Environment.getExternalStorageDirectory();
                        fileName = new File(sdDir, "OsMoDroid/");
                        fileName.mkdirs();
                        fileName = new File(sdDir, "OsMoDroid/settings.dat");
                    }
                pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                String strVersionName = getString(R.string.Unknow);
                try
                    {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(
                                getPackageName(), 0);
                        strVersionName = packageInfo.packageName + " "
                                + packageInfo.versionName;
                        version = packageInfo.versionName;
                    }
                catch (NameNotFoundException e)
                    {
                        //e.printStackTrace();
                    }
                setContentView(R.layout.activity_main);
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerList = (ListView) findViewById(R.id.left_drawer);
                // Set the notificationStringsAdapter for the list view
                setupDrawerList();
                mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
                //mDrawerLayout.setBackgroundColor(Color.WHITE);
                mDrawerList.setCacheColorHint(0);
                if (OsMoDroid.settings.getBoolean("darktheme", true))
                    {
                        setTheme(R.style.Theme_Osmodroid);
                        mDrawerLayout.setBackgroundColor(Color.TRANSPARENT);
                    }
                else
                    {
                        setTheme(R.style.Theme_AppCompat_Light);
                        mDrawerLayout.setBackgroundColor(Color.WHITE);
                    }

                mTitle = mDrawerTitle = getTitle();
                mDrawerToggle = new ActionBarDrawerToggle(
                        this,                  /* host Activity */
                        mDrawerLayout,         /* DrawerLayout object */
                        R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                        R.string.drawer_open,  /* "open drawer" description for accessibility */
                        R.string.drawer_close  /* "close drawer" description for accessibility */
                )
                {
                    public void onDrawerClosed(View view)
                        {
                            super.onDrawerClosed(view);
                        }
                    public void onDrawerOpened(View drawerView)
                        {
                            super.onDrawerOpened(drawerView);
                        }
                };
                mDrawerLayout.setDrawerListener(mDrawerToggle);
                fMan = getSupportFragmentManager();
                drawClickListener = new DrawerItemClickListener(fMan, mDrawerList, mDrawerLayout, this);
                mDrawerList.setOnItemClickListener(drawClickListener);
                bindService();
                if (savedInstanceState != null)
                    {
                        //	afterrotate=true;
                        //drawClickListener.selectItem(savedInstanceState.getString("currentItem"));
                    }
                else
                    {
                        //drawClickListener.selectItem(getString(R.string.tracker));
                    }
                mIMstatusReciever = new BroadcastReceiver()
                {
                    @Override
                    public void onReceive(Context context, Intent intent)
                        {
                            int icon = 0;
                            if (intent.hasExtra("connecting") && intent.hasExtra("connect"))
                                {
                                    if (intent.getBooleanExtra("connect", false) && !intent.getBooleanExtra("connecting", false))
                                        {
                                            actionBar.setIcon(R.drawable.eyeo);
                                            actionBar.setHomeAsUpIndicator(R.drawable.eyeo);
                                            if(intent.hasExtra("executedlistsize"))
                                                {
                                                    if(intent.getIntExtra("executedlistsize",0)>0)
                                                        {
                                                            actionBar.setIcon(R.drawable.anim);
                                                            actionBar.setHomeAsUpIndicator(R.drawable.anim);
                                                        }
                                                }
                                        }
                                    else if (intent.getBooleanExtra("connecting", false))
                                        {
                                            actionBar.setIcon(R.drawable.eyeu);
                                            actionBar.setHomeAsUpIndicator(R.drawable.eyeu);
                                        }
                                    else
                                        {
                                            actionBar.setIcon(R.drawable.eyen);
                                            actionBar.setHomeAsUpIndicator(R.drawable.eyen);
                                        }
                                }

                        }
                };
                registerReceiver(mIMstatusReciever, new IntentFilter("OsMoDroid"));
                if (mService.myIM != null)
                    {
                        if (!mService.myIM.start&&OsMoDroid.settings.getBoolean("live", true))
                            {
                                mService.myIM.start();
                            }
                    }

            }
        void setupDrawerList()
            {
                mDrawerItems.clear();
//		if (!OsMoDroid.settings.getString("key", "" ).equals("") ){
                String[] menu1 = new String[]{
                        getString(R.string.tracker), getString(R.string.stat), getString(R.string.map),
                        getString(R.string.chanals), getString(R.string.devices),
                        getString(R.string.links),
                        //getString(R.string.notifications),
                        getString(R.string.tracks), getString(R.string.settings), getString(R.string.exit)};
                if (OsMoDroid.debug)
                    {
                        menu1 = new String[]{
                                getString(R.string.tracker), getString(R.string.stat), getString(R.string.map),
                                getString(R.string.chanals), getString(R.string.devices),//
                                getString(R.string.links),
                                //getString(R.string.notifications),
                                getString(R.string.tracks), getString(R.string.settings), getString(R.string.exit), "debug"};
                    }
                for (String s : menu1)
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
                menuAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerItems);
                mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerItems));
            }
        @Override
        protected void onPostCreate(Bundle savedInstanceState)
            {
                Log.d(this.getClass().getSimpleName(), "onpostcreate gpsclient");
                super.onPostCreate(savedInstanceState);
                mDrawerToggle.syncState();
            }
        @Override
        public void onConfigurationChanged(Configuration newConfig)
            {
                Log.d(this.getClass().getSimpleName(), "onconfigchanged gpsclient");
                super.onConfigurationChanged(newConfig);
                mDrawerToggle.onConfigurationChanged(newConfig);
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                if (item.getItemId() == android.R.id.home)
                    {
                        if (mDrawerLayout.isDrawerOpen(mDrawerList))
                            {
                                mDrawerLayout.closeDrawer(mDrawerList);
                            }
                        else
                            {
                                mDrawerLayout.openDrawer(mDrawerList);
                            }
                    }
                return super.onOptionsItemSelected(item);
            }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data)
            {
                Log.d(this.getClass().getSimpleName(), "void onActivityResult" + requestCode + " " + resultCode);
                updateMainUI();
                if (conn == null || mService == null)
                    {
                    }
                else
                    {
                        if (requestCode == 0)
                            {
                                Log.d(this.getClass().getSimpleName(), "void onActivityResult=preference");
                                mService.applyPreference();
                                Intent intent = new Intent(this, GPSLocalServiceClient.class);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                finish();
                                startActivity(intent);
                                if ( mBound)
                                    {
                                        if(mService.myIM.authed)

                                            {
                                                mService.myIM.sendToServer("PG:1", false);
                                            }
                                       // mService.myIM.sendToServer("PD:1", false);
                                    }
                                mService.myIM.needtosendpreference=true;
                                if(mService.myIM.authed)
                                    {
                                        mService.myIM.sendToServer("DCU",false);
                                    }

                            }
                        if (requestCode == 1 && resultCode == Activity.RESULT_OK)
                            {
                                Log.d(this.getClass().getSimpleName(), "void onActivityResult=reg");
                                if (live&& mBound)
                                    {
                                        mService.myIM.close();
                                        LocalService.channelList.clear();

                                        mService.myIM.needtosendpreference=true;
                                        mService.myIM.start();

                                    }
                            }
                    }
            }
        @Override
        protected void onStart()
            {
                super.onStart();
                Log.d(this.getClass().getSimpleName(), "onStart() gpsclient");
                OsMoDroid.gpslocalserviceclientVisible = true;
                //ReadPref();
                started = checkStarted();
                if(mBound)
                    {
                        mService.bitmapmapview();
                    }
                if (!OsMoDroid.settings.getBoolean("subscribebackground", false) && mBound)
                    {
                        if(mService.myIM.authed)
                            {
                                mService.myIM.sendToServer("PG:1", false);
                            }

                        // mService.myIM.sendToServer("PD:1", false);
                    }
                if (mService.myIM != null)
                    {
                        mService.myIM.checkalarmindozemode();
                        if (!mService.myIM.start)
                            {
                                mService.myIM.start();
                            }
                    }

            }
        @Override
        protected void onResume()
            {
                super.onResume();
                OsMoDroid.mFirebaseAnalytics.logEvent("APP_OPEN",null);
                if(OsMoDroid.settings.getBoolean("usewake",false))
                    {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                else
                    {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                Log.d(this.getClass().getSimpleName(), "onResume() gpsclient");

//		if (hash.equals("") && live) {
//			RequestAuthTask requestAuthTask = new RequestAuthTask();
//			requestAuthTask.execute();
//
//		}
//                if(!isGooglePlayServicesAvailable(this))
//                {
//                    //Toast.makeText(this, R.string.bogoogleplay, Toast.LENGTH_LONG).show();
//                };
            }
        @Override
        protected void onResumeFragments()
            {
                super.onResumeFragments();
                Log.d(this.getClass().getSimpleName(), "onresumefragments gpsclient");
                updateMainUI();
                if (!proceednewintent)
                    {
                        if (mBound)
                            {
                                intentAction(getIntent());
                            }
                        else
                            {
                                needIntent = getIntent();
                            }
                    }
                proceednewintent = false;

            }
        private void intentAction(Intent intent)
            {
                Log.d(this.getClass().getSimpleName(), "intentaction ");
                if (intent.getAction() != null)
                    {
                        if (intent.getAction().equals("devicechat"))
                            {
                                //DeviceChatFragment openFragment = new DeviceChatFragment();
                                Bundle bundle = new Bundle();
                                bundle.putInt("deviceU", intent.getIntExtra("deviceU", -1));
                                //openFragment.setArguments(bundle);
                                //showFragment(openFragment,true);
                                drawClickListener.selectItem(getString(R.string.devices), bundle);
                                Log.d(this.getClass().getSimpleName(), "on new intent=devicechat");
                            }
                        else if (intent.getAction().equals("notif"))
                            {
                                Log.d(this.getClass().getSimpleName(), "on new intent=notif");
                              //  NotifFragment notif = new NotifFragment();
                              //  drawClickListener.selectItem(getString(R.string.notifications), null);
                                //showFragment(notif,false);
                            }
                        else if (intent.getAction().equals("mapfromwidget"))
                            {
                                Log.d(this.getClass().getSimpleName(), "on new intent=mapfromwidget");
                                //MapFragment notif = new MapFragment();
                                drawClickListener.selectItem(getString(R.string.map), null);
                                //showFragment(notif,false);
                            }
                        else if (intent.getAction().equals("channelchat"))
                            {
                                Log.d(this.getClass().getSimpleName(), "on new intent=channelchat");
                                //ChannelDevicesFragment openFragment = new ChannelDevicesFragment();
                                Bundle bundle = new Bundle();
                                bundle.putInt("channelpos", intent.getIntExtra("channelpos", -1));
                                //openFragment.setArguments(bundle);
                                //showFragment(openFragment,true);
                                drawClickListener.selectItem(getString(R.string.chanals), bundle);
                            }
                        else if (intent.getAction().equals(Intent.ACTION_MAIN))
                            {
                                if (!LocalService.currentItemName.equals(""))
                                    {
                                        drawClickListener.selectItem(LocalService.currentItemName, null);
                                    }
                                else
                                    {
                                        Log.d(this.getClass().getSimpleName(), "on new intent=MAIN");
                                        drawClickListener.selectItem(getString(R.string.tracker), null);
                                    }
                            }
                        else if (intent.getAction().equals(Intent.ACTION_VIEW)&&intent.getScheme().equals("https"))
                            {
                                Bundle bundle = new Bundle();
                                String groupurl = intent.getData().toString();
                                int trimInt=groupurl.indexOf("?");
                                if(trimInt>0)
                                    {
                                        groupurl=groupurl.substring(0,trimInt);
                                    }
                                bundle.putString("groupurl", groupurl);
                                drawClickListener.selectItem(getString(R.string.chanals), bundle);
                                Log.d(this.getClass().getSimpleName(), "on new intent=cation_view");
                            }
                        else if (intent.getAction().equals(Intent.ACTION_VIEW)&&intent.getScheme().equals("geo"))
                            {
                                //2016-10-24 20:42:57 geo=59.988184,30.426023?z=18&q=59.988184,30.426023 S=1564 R=47190 overall by netstat=11022
                                //2016-10-24 20:42:57 cant parse geo  S=1564 R=47190 overall by netstat=11022

                                Log.d(this.getClass().getSimpleName(), "on new intent=cation_view geo");
                                Bundle b = new Bundle();
                                try
                                    {
                                        LocalService.addlog("geo="+intent.getData().getSchemeSpecificPart());
                                        b.putFloat("lat",Float.parseFloat(intent.getData().getSchemeSpecificPart().split("=")[1].split(",")[0]));
                                        b.putFloat("lon",Float.parseFloat(intent.getData().getSchemeSpecificPart().split("=")[1].split(",")[1]));
                                    }
                                catch (Exception e)

                                    {
                                        try
                                            {
                                                String s = intent.getData().getSchemeSpecificPart();
                                                b.putFloat("lat",Float.parseFloat(s.substring(s.indexOf("q=")+2,s.indexOf("q=")+21).split(",")[0]));
                                                b.putFloat("lon",Float.parseFloat(s.substring(s.indexOf("q=")+2,s.indexOf("q=")+21).split(",")[1]));

                                            }
                                            catch (Exception e1)
                                                {

                                                }
                                        LocalService.addlog("cant parse geo ");
                                        e.printStackTrace();
                                    }
                                drawClickListener.selectItem(getString(R.string.map),b);
                            }
                        Intent i = getIntent();
                        i.setAction(null);
                        setIntent(i);
                    }
            }
        @Override
        protected void onNewIntent(Intent intent)
            {
                if (intent.hasExtra("enter"))
                    {
                        signin();
                    }
                try
                    {
                        if (OsMoDroid.gpslocalserviceclientVisible)
                            {
                                Log.d(this.getClass().getSimpleName(), "on new intent=" + intent.getIntExtra("deviceU", -1));
                                intentAction(intent);
                            }
                        else
                        {
                            if(intent.getAction().equals(Intent.ACTION_VIEW))
                            {
                                intentAction(intent);
                            }
                        }
                        proceednewintent = true;
                        super.onNewIntent(intent);
                    }
                catch (IllegalStateException e)
                    {
                        Log.d(this.getClass().getSimpleName(), "on new intent bug");
                        e.printStackTrace();
                    }
            }
//        void reg()
//            {
////                Intent intent = new Intent(this, AuthActivity.class);
////                startActivityForResult(intent, 1);
////First ask reg or sign
//                final AlertDialog askRegorSign = new AlertDialog.Builder(this)
//                 .setTitle(R.string.alreadyregistered)
//                    .setPositiveButton(R.string.yes,
//                            new DialogInterface.OnClickListener()
//                                {
//                                    public void onClick(DialogInterface dialog, int whichButton)
//                                        {
//                                            signin();
//                                        }
//                                })
//                    .setNegativeButton(R.string.No,
//                            new DialogInterface.OnClickListener()
//                                {
//                                    public void onClick(DialogInterface dialog, int whichButton)
//                                        {
//                                            regin();
//                                        }
//                                }).create();
//                askRegorSign.show();
//
//            }
        void regin()
            {
                ScrollView scrollView = new ScrollView(this);
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                scrollView.addView(layout);
                final TextView nickTextView = new TextView(this);
                nickTextView.setText(R.string.nick);
                layout.addView(nickTextView);
                final EditText nickEditText = new EditText(this);
                nickEditText.setHint("Superman");
                layout.addView(nickEditText);

                final TextView genderTypeTextView = new TextView(this);
                genderTypeTextView.setText(R.string.gender);
                layout.addView(genderTypeTextView);
                final Spinner genderTypeSpinner = new Spinner(this);
                layout.addView(genderTypeSpinner);
                List<String> typeList = new ArrayList<String>();
                typeList.add(getString(R.string.male));
                typeList.add(getString(R.string.female));

                final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinneritem, typeList);
                genderTypeSpinner.setAdapter(dataAdapter);

                final TextView email = new TextView(this);
                email.setText(R.string.Email);
                layout.addView(email);
                final EditText inputEmail = new EditText(this);
                layout.addView(inputEmail);

                final TextView invite = new TextView(this);
                invite.setText(R.string.invite);
                layout.addView(invite);
                final EditText inputInvite = new EditText(this);
                layout.addView(inputInvite);
//                final TextView passwordTextView = new TextView(this);
//                passwordTextView.setText(R.string.password);
//                layout.addView(passwordTextView);
//                final EditText passwordEditText = new EditText(this);
//                passwordEditText.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
//                passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
//                layout.addView(passwordEditText);
//
//                final TextView rpasswordTextView = new TextView(this);
//                rpasswordTextView.setText(R.string.repeatpassword);
//                layout.addView(rpasswordTextView);
//                final EditText rpasswordEditText = new EditText(this);
//                rpasswordEditText.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
//                rpasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
//                layout.addView(rpasswordEditText);



                final AlertDialog alertdialog4 = new AlertDialog.Builder(this)
                        .setTitle(R.string.registration)
                        .setView(scrollView)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int whichButton)
                                            {

                                            }
                                    })
                        .setNegativeButton(R.string.No,
                                new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int whichButton)
                                            {
                                            }
                                    }).create();
                alertdialog4.show();
                Button theButton = alertdialog4.getButton(DialogInterface.BUTTON_POSITIVE);
                theButton.setOnClickListener(new CustomListener(alertdialog4)
                    {
                        @Override
                        public void onClick(View v)
                            {
                                if (mService.myIM.authed)
                                    {
                                        String email = inputEmail.getText().toString();
//                                        String password = passwordEditText.getText().toString();
//                                        String rpassword = rpasswordEditText.getText().toString();
                                        String nick = nickEditText.getText().toString();
                                        String invite = inputInvite.getText().toString();
                                        int gender=-1;
                                        switch (genderTypeSpinner.getSelectedItemPosition())
                                            {
                                                case 0:
                                                    gender = 1;
                                                    break;
                                                case 1:
                                                    gender = 0;
                                                    break;
                                            }



                                        if (!email.equals("")
                                                //&&!password.equals("")
                                                &&!nick.equals(""))
                                            {
                                                //if(password.equals(rpassword))
                                                    {
                                                        APIcomParams params = new APIcomParams("https://api2.osmo.mobi/signup", "key=" + OsMoDroid.settings.getString("newkey", "") + "&email=" + email +
                                                                //"&password=" + password+
                                                                "&nick=" + nick+ "&gender=" + gender + "&invite="+invite, "SIGNUP");
                                                        MyAsyncTask sendidtask = new Netutil.MyAsyncTask(GPSLocalServiceClient.this,GPSLocalServiceClient.this);
                                                        sendidtask.execute(params);
                                                        Log.d(getClass().getSimpleName(), "signin start to execute");
                                                        super.dialog.dismiss();
                                                        ;
                                                    }
                                                //else
                                                    {
                                                  //      Toast.makeText(GPSLocalServiceClient.this, R.string.passdontequal, Toast.LENGTH_SHORT).show();
                                                    }
                                            }
                                        else
                                            {
                                                Toast.makeText(GPSLocalServiceClient.this,R.string.noallenter, Toast.LENGTH_SHORT).show();
                                            }


                                    }
                                else
                                    {
                                        Toast.makeText(GPSLocalServiceClient.this, R.string.CheckInternet, Toast.LENGTH_SHORT).show();
                                    }
                            }

                    });
            }
        void signin()
            {
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final TextView email = new TextView(this);
                email.setText(R.string.Email);
                layout.addView(email);
                final EditText inputEmail = new EditText(this);
                layout.addView(inputEmail);
                final TextView passwordTextView = new TextView(this);
                passwordTextView.setText(R.string.password);
                layout.addView(passwordTextView);
                final EditText passwordEditText = new EditText(this);
                passwordEditText.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
                passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
                layout.addView(passwordEditText);

                final TextView forgotpasswordTextView = new TextView(this);
                forgotpasswordTextView.setText(Html.fromHtml("<a href=https://osmo.mobi/forgot> "+ "Forgot password"));
                forgotpasswordTextView.setMovementMethod(LinkMovementMethod.getInstance());
                layout.addView(forgotpasswordTextView);



                final AlertDialog alertdialog4 = new AlertDialog.Builder(this)
                        .setTitle(R.string.enterloginpassword)
                        .setView(layout)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int whichButton)
                                            {

                                            }
                                    })
                        .setNegativeButton(R.string.No,
                                new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int whichButton)
                                            {
                                            }
                                    }).create();
                alertdialog4.show();
                Button theButton = alertdialog4.getButton(DialogInterface.BUTTON_POSITIVE);
                theButton.setOnClickListener(new CustomListener(alertdialog4)
                    {
                        @Override
                        public void onClick(View v)
                            {
                                if (mService.myIM.authed)
                                    {
                                        String email = inputEmail.getText().toString();
                                        String password = passwordEditText.getText().toString();

                                        if (!email.equals("")&&!password.equals(""))
                                            {
                                                APIcomParams params = new APIcomParams("https://api2.osmo.mobi/signin", "key="+OsMoDroid.settings.getString("newkey","")+"&email="+email+"&password="+password,"SIGNIN");
                                                MyAsyncTask sendidtask = new Netutil.MyAsyncTask(GPSLocalServiceClient.this,GPSLocalServiceClient.this);
                                                sendidtask.execute(params);
                                                Log.d(getClass().getSimpleName(), "signin start to execute");

                                                super.dialog.dismiss();;
                                            }
                                        else
                                            {
                                                Toast.makeText(GPSLocalServiceClient.this,R.string.noallenter, Toast.LENGTH_SHORT).show();
                                            }


                                    }
                                else
                                    {
                                        Toast.makeText(GPSLocalServiceClient.this, R.string.CheckInternet, Toast.LENGTH_SHORT).show();
                                    }
                            }

                    });
            }


        boolean checkStarted()
            {
                // Log.d(this.getClass().getSimpleName(), "oncheckstartedy() gpsclient");
                return OsMoDroid.settings.getBoolean("started", false);
            }
//        void ReadPref()
//            {
//                Log.d(this.getClass().getSimpleName(), "readpref() gpsclient");
//                speed = Integer.parseInt(OsMoDroid.settings.getString("speed", "3").equals(
//                        "") ? "3" : OsMoDroid.settings.getString("speed", "3"));
//                period = Integer.parseInt(OsMoDroid.settings.getString("period", "10000").equals(
//                        "") ? "10000" : OsMoDroid.settings.getString("period", "10000"));
//                distance = Integer.parseInt(OsMoDroid.settings.getString("distance", "50")
//                        .equals("") ? "50" : OsMoDroid.settings.getString("distance", "50"));
//                speedbearing = Integer.parseInt(OsMoDroid.settings.getString("speedbearing", "2")
//                        .equals("") ? "2" : OsMoDroid.settings.getString("speedbearing", "2"));
//                bearing = Integer.parseInt(OsMoDroid.settings.getString("bearing", "10").equals(
//                        "") ? "10" : OsMoDroid.settings.getString("bearing", "2"));
//                hdop = Integer
//                        .parseInt(OsMoDroid.settings.getString("hdop", "30").equals("") ? "30"
//                                : OsMoDroid.settings.getString("hdop", "30"));
//                gpx = OsMoDroid.settings.getBoolean("gpx", false);
//                //live = OsMoDroid.settings.getBoolean("live", true);
//                vibrate = OsMoDroid.settings.getBoolean("vibrate", false);
//                usecourse = OsMoDroid.settings.getBoolean("usecourse", false);
//                vibratetime = Integer.parseInt(OsMoDroid.settings.getString("vibratetime", "200")
//                        .equals("") ? "200" : OsMoDroid.settings.getString("vibratetime", "0"));
//                playsound = OsMoDroid.settings.getBoolean("playsound", false);
//                period_gpx = Integer.parseInt(OsMoDroid.settings.getString("period_gpx", "0")
//                        .equals("") ? "0" : OsMoDroid.settings.getString("period_gpx", "0"));
//                distance_gpx = Integer.parseInt(OsMoDroid.settings.getString("distance_gpx", "0")
//                        .equals("") ? "0" : OsMoDroid.settings.getString("distance_gpx", "0"));
//                speedbearing_gpx = Integer.parseInt(OsMoDroid.settings.getString(
//                        "speedbearing_gpx", "0").equals("") ? "0" : OsMoDroid.settings.getString(
//                        "speedbearing_gpx", "0"));
//                bearing_gpx = Integer.parseInt(OsMoDroid.settings.getString("bearing_gpx", "0")
//                        .equals("") ? "0" : OsMoDroid.settings.getString("bearing", "0"));
//                hdop_gpx = Integer.parseInt(OsMoDroid.settings.getString("hdop_gpx", "30")
//                        .equals("") ? "30" : OsMoDroid.settings.getString("hdop_gpx", "30"));
//                usebuffer = OsMoDroid.settings.getBoolean("usebuffer", false);
//                usewake = OsMoDroid.settings.getBoolean("usewake", false);
//                notifyperiod = Integer.parseInt(OsMoDroid.settings.getString("notifyperiod",
//                        "30000").equals("") ? "30000" : OsMoDroid.settings.getString(
//                        "notifyperiod", "30000"));
//                sendsound = OsMoDroid.settings.getBoolean("sendsound", false);
//                // pass = OsMoDroid.settings.getString("pass", "");
//            }
        void startlocalservice()
            {
                //Intent i = new Intent(this, LocalService.class);
                //startService(i);
                started = true;
                mService.startServiceWork(true);
            }
        private void bindService()
            {
                Log.d(this.getClass().getSimpleName(), "bindservice gpsclient");
                Intent i = new Intent(this, LocalService.class);
                i.setAction("OsMoDroid.local");
                //Intent i = new Intent("OsMoDroid.local");
                if (!mBound)
                    {
                        bindService(i, conn, Context.BIND_AUTO_CREATE);
                        Intent is = new Intent(this, LocalService.class);
                        startService(is);
                    }
                //updateServiceStatus();
            }
        void stop(Boolean stopsession)
            {
                Log.d(this.getClass().getSimpleName(), "stop() gpsclient");
                mService.stopServiceWork(stopsession);
                //Intent i = new Intent(this, LocalService.class);
                //stopService(i);
            }
        private void invokeService()
            {
                Log.d(this.getClass().getSimpleName(), "invokeservice() gpsclient");
                if (conn == null || mService == null)
                    {
                    }
                else
                    {
                        mService.refresh();
                        updateMainUI();
                    }
            }
        @Override
        protected void onDestroy()
            {
                LocalService.addlog("gpslocalserviceclient ondestroy");
                Log.d(this.getClass().getSimpleName(), "onDestroy() gpsclient");
                OsMoDroid.activity = null;
                if (mBound)
                    {
                        try
                            {
                                unbindService(conn);
                            }
                        catch (Exception e)
                            {
                                // TODO Auto-generated catch block
                                Log.d(this.getClass().getSimpleName(), "Р�СЃРєР»СЋС‡РµРЅРёРµ РїСЂРё РѕС‚СЃРѕРµРґРёРЅРµРЅРёРё РѕС‚ СЃРµСЂРІРёСЃР°");
                                e.printStackTrace();
                            }
                    }
                conn = null;
                // releaseService();
                if (receiver != null)
                    {
                        unregisterReceiver(receiver);
                    }
                if (mIMstatusReciever != null)
                    {
                        unregisterReceiver(mIMstatusReciever);
                    }
                //
                final Field sHelperField;
                try
                    {
                        Class<?> bubbleClass = Class.forName("android.widget.BubblePopupHelper");
                        sHelperField = bubbleClass.getDeclaredField("sHelper");
                        sHelperField.setAccessible(true);
                        sHelperField.set(null, null);
                        Log.d(this.getClass().getName(), "LG Bubble clearing succes" );
                    }
                catch (Exception ignored)
                    {
                        Log.d(this.getClass().getName(), "LG Bubble clearing exception" + ignored.getMessage());
                    }

                getSupportActionBar().setCustomView(null);
                super.onDestroy();
            }
        public String inputStreamToString(InputStream in) throws IOException
            {
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(in));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null)
                    {
                        stringBuilder.append(line + "\n");
                        if (!bufferedReader.ready())
                            {
                                break;
                            }
                    }
                bufferedReader.close();
                return stringBuilder.toString();
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
        boolean saveSharedPreferencesToFile(File dst)
            {
                boolean res = false;
                ObjectOutputStream output = null;
                try
                    {
                        output = new ObjectOutputStream(new FileOutputStream(dst));
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                        output.writeObject(pref.getAll());
                        Toast.makeText(this, R.string.prefsaved, Toast.LENGTH_SHORT).show();
                        res = true;
                    }
                catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                finally
                    {
                        try
                            {
                                if (output != null)
                                    {
                                        output.flush();
                                        output.close();
                                    }
                            }
                        catch (IOException ex)
                            {
                                ex.printStackTrace();
                            }
                    }
                return res;
            }
        @SuppressWarnings({"unchecked"})
        boolean loadSharedPreferencesFromFile(File src)
            {
                boolean res = false;
                ObjectInputStream input = null;
                try
                    {
                        input = new ObjectInputStream(new FileInputStream(src));
                        OsMoDroid.editor.clear();
                        Map<String, ?> entries = (Map<String, ?>) input.readObject();
                        for (Entry<String, ?> entry : entries.entrySet())
                            {
                                Object v = entry.getValue();
                                String key = entry.getKey();
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
                        OsMoDroid.editor.remove("GCMregId");
                        OsMoDroid.editor.commit();
                        Toast.makeText(this, R.string.prefloaded, Toast.LENGTH_SHORT).show();
                        setupDrawerList();
                        res = true;
                    }
                catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                catch (ClassNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                finally
                    {
                        try
                            {
                                if (input != null)
                                    {
                                        input.close();
                                    }
                            }
                        catch (IOException ex)
                            {
                                ex.printStackTrace();
                            }
                    }
                return res;
            }
        @Override
        public void onBackPressed()
            {
                int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
                if (backStackEntryCount == 0)
                    {
                        if (drawClickListener.currentItem != 0)
                            {
                                drawClickListener.selectItem(getString(R.string.tracker), null);
                                LocalService.currentItemName = OsMoDroid.context.getString(R.string.tracker);
                            }
                        else
                            {
                                super.onBackPressed();
                            }
                    }
                else
                    {

                        super.onBackPressed();
                    }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        @Override
        public void onResultsSucceeded(APIComResult result)
            {

                        Log.d(getClass().getSimpleName(), "notifwar1 Команда:" + result.Command + " Ответ сервера:" + result.rawresponse + getString(R.string.query) + result.url);
                if(result.Jo!=null&&!result.Jo.has("error"))
                    {
                        if (result.Command.equals("SIGNIN"))
                            {
                                OsMoDroid.editor.putString("u", result.Jo.optString("nick"));
                                OsMoDroid.editor.commit();
                                mService.refresh();

                            }
                        if (result.Command.equals("SIGNUP"))
                            {
                                OsMoDroid.editor.putString("u", result.Jo.optString("nick"));
                                OsMoDroid.editor.commit();
                                mService.refresh();
                            }
                        updateMainUI();
                    }
                else
                    {
                        if(result.Jo==null)
                            {
                                Toast.makeText(GPSLocalServiceClient.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        else
                            {
                                Toast.makeText(GPSLocalServiceClient.this, result.Jo.optString("error")+" "+result.Jo.optString("error_description"), Toast.LENGTH_SHORT).show();
                            }
                    }

            }
        public interface upd
            {
                void update();
            }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 2) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.requiregps, Toast.LENGTH_LONG).show();
                }
                if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    Toast t = Toast.makeText(this, R.string.requireSTORAGE, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER,0,0);
                    t.show();
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }


        public boolean isGooglePlayServicesAvailable(Activity activity) {
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
            if (status != ConnectionResult.SUCCESS) {
                if (googleApiAvailability.isUserResolvableError(status)) {
                    googleApiAvailability.getErrorDialog(activity, status, 2404).show();
                }
                return false;
            }
            return true;
        }
    }
