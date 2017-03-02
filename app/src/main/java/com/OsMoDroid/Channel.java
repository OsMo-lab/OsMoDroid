package com.OsMoDroid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.overlay.PathOverlay;

import com.OsMoDroid.ColoredGPX.Statuses;
import com.OsMoDroid.Netutil.InitTask;

import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.os.AsyncTask;
import android.provider.Settings.Global;
import android.util.Log;
public class Channel implements Serializable, ResultsListener
    {
        public ArrayList<ColoredGPX> gpxList = new ArrayList<ColoredGPX>();
        public String name;
        public String description;
        public String myNameInGroup;
        public int u;
        public int gu;
        public String created;

        public String url;
        public String browseurl;

        public List<Device> deviceList = new ArrayList<Device>();
        public List<ChatMessage> messagesstringList = new ArrayList<ChatMessage>();
        public boolean send = false;
        public int type=0;
        public boolean chatconnected = false;
        ArrayList<Point> pointList = new ArrayList<Channel.Point>();
        File sdDir = android.os.Environment.getExternalStorageDirectory();
        File fileName = new File(sdDir, "OsMoDroid/channelsgpx/");
        public Channel()
            {
            }
        public void updChannel(JSONObject jo)
            {
                this.name = jo.optString("name");
                this.u = jo.optInt("u");
                this.created = jo.optString("created");

                this.browseurl = "https://osmo.mobi/g/" + jo.optString("url");
                this.url = "https://api.osmo.mobi/s?g="+ jo.optString("url")+"&c=OsMoDroid";
                this.myNameInGroup = jo.optString("nick");
                this.gu=jo.optInt("gu");
                this.type=jo.optInt("type");
                if (jo.optInt("active") == 1)
                    {
                        this.send = true;
                    }
                else
                    {
                        this.send = false;
                    }
                JSONArray users = jo.optJSONArray("users");
                ArrayList<Device> recieveddeviceList = new ArrayList<Device>();
                for (int i = 0; i < users.length(); i++)
                    {
                        JSONObject jsonObject;
                        try
                            {
                                jsonObject = users.getJSONObject(i);
                                try
                                    {
                                        Device dev = new Device(jsonObject.getInt("u"), jsonObject.getString("name"), jsonObject.getString("color"),jsonObject.getInt("state"));
                                        if (jsonObject.has("lat") && jsonObject.has("lon"))
                                            {
                                                dev.lat = Float.parseFloat(jsonObject.getString("lat"));
                                                dev.lon = Float.parseFloat(jsonObject.getString("lon"));
                                            }
                                                dev.updatated=  1000*jsonObject.optLong("time");



                                        //dev.updatated=System.currentTimeMillis();
                                        IM.getDevtrace(jsonObject,dev);
                                        recieveddeviceList.add(dev);

                                    }
                                catch (NumberFormatException e)
                                    {
                                        Log.d(getClass().getSimpleName(), "Wrong device info");
                                        e.printStackTrace();
                                    }
                            }
                        catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                    }
                this.deviceList.retainAll(recieveddeviceList);
                for (Device dev : this.deviceList)
                    {
                        dev.color = recieveddeviceList.get(recieveddeviceList.indexOf(dev)).color;
                        dev.name = recieveddeviceList.get(recieveddeviceList.indexOf(dev)).name;
                        dev.updatated=recieveddeviceList.get(recieveddeviceList.indexOf(dev)).updatated;
                        dev.lat=recieveddeviceList.get(recieveddeviceList.indexOf(dev)).lat;
                        dev.lon=recieveddeviceList.get(recieveddeviceList.indexOf(dev)).lon;
                        dev.state=recieveddeviceList.get(recieveddeviceList.indexOf(dev)).state;
                        dev.online=recieveddeviceList.get(recieveddeviceList.indexOf(dev)).online;

                    }
                recieveddeviceList.removeAll(this.deviceList);
                this.deviceList.addAll(recieveddeviceList);

                Collections.sort(this.deviceList);
                JSONArray points = jo.optJSONArray("point");
                if (points != null)
                    {
                        this.pointList.clear();
                        for (int i = 0; i < points.length(); i++)
                            {
                                JSONObject jsonObject;
                                try
                                    {
                                        jsonObject = points.getJSONObject(i);
                                        this.pointList.add(new Point(jsonObject));
                                    }
                                catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                            }
                    }
                JSONArray tracks = jo.optJSONArray("track");
                if (tracks != null)
                    {
                        ArrayList<ColoredGPX> recievedgpxList = new ArrayList<ColoredGPX>();
                        for (int i = 0; i < tracks.length(); i++)
                            {
                                JSONObject jsonObject;
                                try
                                    {
                                        jsonObject = tracks.getJSONObject(i);
                                        fileName.mkdirs();
                                        Log.d(getClass().getSimpleName(), "filename=" + fileName);
                                        recievedgpxList.add(new ColoredGPX(jsonObject.getInt("u"), new File(sdDir, "OsMoDroid/channelsgpx/" + jsonObject.getString("u") + ".gpx"), jsonObject.getString("color"), jsonObject.getString("url")));

                                    }
                                catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }
                            }
                        gpxList.retainAll(recievedgpxList);
                        recievedgpxList.removeAll(gpxList);
                        gpxList.addAll(recievedgpxList);

                        for (ColoredGPX cgpx : gpxList)
                            {
                                if (cgpx.status == ColoredGPX.Statuses.EMPTY)
                                    {
                                        cgpx.status = Statuses.DOWNLOADING;
                                        Netutil.downloadfile(this, cgpx.url, cgpx);
                                    }
                                else if (cgpx.status == ColoredGPX.Statuses.DOWNLOADED)
                                    {
                                        cgpx.initPathOverlay();
                                    }
                            }
                    }
            }

        @Override
        public boolean equals(Object o)
            {
                if ((o instanceof Channel) && this.u == ((Channel) o).u)
                    {
                        return true;
                    }
                else
                    {
                        return false;
                    }
            }

        @Override
        public String toString()
            {
                return name;
            }
        public void getPointList(JSONArray jsonArray)
            {
                pointList.clear();
                for (int i = 0; i < jsonArray.length(); i++)
                    {
                        try
                            {
                                pointList.add(new Point(jsonArray.getJSONObject(i)));
                            }
                        catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                    }
            }
        @Override
        public void onResultsSucceeded(APIComResult result)
            {
                Log.d(getClass().getSimpleName(), "download result=" + result.load);
                result.load.initPathOverlay();
            }
        static class Point implements Serializable
            {
                int u;
                float lat;
                float lon;
                String name;
                String description;
                String color;
                int clusterid=0;
                String url="";
                String time;
                Paint paint;
                Point()
                    {
                        name = "";
                        description = "";
                        url="";
                    }
                Point(JSONObject json) throws JSONException
                    {
                        u = json.getInt("u");
                        lat = Float.parseFloat(json.getString("lat"));
                        lon = Float.parseFloat(json.getString("lon"));
                        description = json.optString("description");
                        color = json.optString("color");
                        name = json.getString("name");
                        url=json.optString("url");
                        time= OsMoDroid.sdf.format(new Date(json.optLong("time")*1000));
                    }
            }
    }
