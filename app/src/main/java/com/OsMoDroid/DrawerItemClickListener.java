package com.OsMoDroid;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class DrawerItemClickListener implements OnItemClickListener
    {
        MainFragment main;
        StatFragment stat;
        MapFragment map;
        ChannelsFragment chans;
        SimLinksFragment links;
        NotifFragment notif;
        TracFileListFragment trac;
        ChannelDevicesFragment chandev;
        TrackStatFragment trackStatFragment;
        DebugFragment debug;
        int currentItem = 0;
        ListView mDrawerList;
        DrawerLayout mDrawerLayout;
        private FragmentManager fMan;
        private GPSLocalServiceClient activity;
        public DrawerItemClickListener(FragmentManager fMan, ListView mDrawerList, DrawerLayout mDrawerLayout, GPSLocalServiceClient activity)
            {
                this.fMan = fMan;
                this.mDrawerList = mDrawerList;
                this.mDrawerLayout = mDrawerLayout;
                this.activity = activity;
            }
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                Log.d(this.getClass().getSimpleName(), "Drawer onitemclick " + (String) arg0.getAdapter().getItem(arg2));
                if (!((String) arg0.getAdapter().getItem(arg2)).equals(OsMoDroid.context.getString(R.string.settings)))
                    {
                        LocalService.currentItemName = (String) arg0.getAdapter().getItem(arg2);
                    }
                selectItem((String) arg0.getAdapter().getItem(arg2), null);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        public void selectItem(String name, Bundle bundle)
            {
                Log.d(this.getClass().getSimpleName(), "Drawer selectItem " + name);
                final FragmentTransaction ft = fMan.beginTransaction();
//getString(R.string.tracker), getString(R.string.stat),getString(R.string.map),getString(R.string.chanals)
//getString(R.string.devices),getString(R.string.links), getString(R.string.notifications), getString(R.string.tracks)            
                // Locate Position
                if (name.equals(OsMoDroid.context.getString(R.string.tracker)))
                    {
                        if (main == null)
                            {
                                main = new MainFragment();
                            }
                        ft.replace(R.id.fragment_container, main);
                        currentItem = 0;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.stat)))
                    {
                        if (stat == null)
                            {
                                stat = new StatFragment();
                            }
                        ft.replace(R.id.fragment_container, stat);
                        currentItem = 1;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.map)))
                    {
                        if (map == null)
                            {
                                map = new MapFragment();
                                if(bundle!=null&&bundle.size()>0)
                                    {
                                        map.setArguments(bundle);
                                    }
                                else
                                    {
                                        map.setArguments(null);
                                    }
                            }

                        ft.replace(R.id.fragment_container, map);
                        currentItem = 2;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.chanals)))
                    {
                        if (chans == null)
                            {
                                chans = new ChannelsFragment();
                            }
                        if (bundle != null)
                            {
                                chans.channelpos = bundle.getInt("channelpos", -1);
                                chans.groupurl=bundle.getString("groupurl");
                            }
                        ft.replace(R.id.fragment_container, chans);
                        currentItem = 3;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.devices)))
                    {
//                        if (devs == null)
//                            {
//                                devs = new DevicesFragment();
//                            }
//                        if (bundle != null)
//                            {
//                                //devs.setArguments(bundle.geti);
//                                devs.deviceU = bundle.getInt("deviceU");
//                            }
                        for (Channel ch:LocalService.channelList)
                            {
                                if(ch.type==2)
                                    {
                                      chandev = new ChannelDevicesFragment();
                                        Bundle b = new Bundle();
                                        b.putInt("channelpos", ch.u);
                                        chandev.setArguments(b);
                                        ft.replace(R.id.fragment_container, chandev);
                                        break;
                                    }
                            }

                           Toast.makeText(activity, activity.getString(R.string.familygroupabsent), Toast.LENGTH_SHORT).show();

//                        ft.replace(R.id.fragment_container, devs);
                        currentItem = 3;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.links)))
                    {
                        if (links == null)
                            {
                                links = new SimLinksFragment();
                            }
                        ft.replace(R.id.fragment_container, links);
                        currentItem = 5;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.notifications)))
                    {
                        if (notif == null)
                            {
                                notif = new NotifFragment();
                            }
                        ft.replace(R.id.fragment_container, notif);
                        currentItem = 6;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.tracks)))
                    {
                        if (trac == null)
                            {
                                trac = new TracFileListFragment();
                            }
                        ft.replace(R.id.fragment_container, trac);
                        currentItem = 7;
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.settings)))
                    {
                        Intent intent = new Intent();
                        intent.setClass(activity, PrefActivity.class);
                        activity.startActivityForResult(intent, 0);
                    }
                else if (name.equals(OsMoDroid.context.getString(R.string.exit)))
                    {
                        //   android.os.Process.killProcess(android.os.Process.myPid());
                        if(activity.mBound&&LocalService.myIM!=null&&LocalService.myIM.authed&&activity.mService.state)
                            {
                                activity.mService.stopServiceWork(true);
                            }
                        LocalService.alertHandler.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                    {

                                        Intent i = new Intent(activity, LocalService.class);
                                        LocalService.serContext.stopService(i);
                                    }
                            },5000);
                        LocalService.currentItemName = "";
                        activity.finish();
                    }
                else if (name.equals("debug"))
                    {
                        if (debug == null)
                            {
                                debug = new DebugFragment();
                            }
                        ft.replace(R.id.fragment_container, debug);
                        currentItem = 8;
                    }
                //ft.addToBackStack("").commit();
                //GPSLocalServiceClient.mDrawerList.setItemChecked(currentItem, true);
                try
                    {
                        fMan.popBackStack();
                        ft.commit();
                    }
                catch (IllegalStateException e)
                    {
                        Log.d(this.getClass().getSimpleName(), "Illegal state exception ignoring");
                        try
                            {
                                ft.commitAllowingStateLoss();
                            }
                        catch (Exception e1)
                            {
                                Log.d(this.getClass().getSimpleName(), "Illegal state exception ignoring dons works");
                                e1.printStackTrace();
                            }
                    }
                mDrawerLayout.closeDrawer(mDrawerList);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            	
//            }
//        }, 100);
                //setTitle(myfriendname[position]);
            }
    }
