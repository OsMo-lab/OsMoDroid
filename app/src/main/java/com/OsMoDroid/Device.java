package com.OsMoDroid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
public class Device implements Comparable<Device>, Serializable
    {
        public int u;
        public int gu;
        public String tracker_id;
        public String name="";
        public String app;
        public String last;
        public String url;
        public String where;
        public float lat;
        public float lon;
        public int online = 0;
        public int state = 0;
        public String uid;
        public String speed = "";
        public int color = Color.RED;
        public String ch;
        public boolean subscribed = false;
        public long updatated = 0;
        public List<SerPoint> devicePath = new ArrayList<SerPoint>();
        public int iprecomputed=0;
        public List<ChatMessage> messagesstringList = new ArrayList<ChatMessage>();
        int clusterid=0;
        public Paint devpaint;
        public Paint pathpaint;

        //public PathOverlay p;
        public Device()
            {
            }
        public Device(int u, String name, String color, int state)
            {
                if(name!=null)
                    {
                        this.name = name;
                    }
                this.u = u;
                this.state=state;
                try
                    {
                        this.color = Color.parseColor(color);

                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }

            }
        @Override
        public String toString()
            {
                return "Device:tracker_id=" + tracker_id + ",name=" + name + ",app=" + app + ",last=" + last + ",url=" + url + ",where=" + where + ",lat=" + lat + ",lon=" + lon
                        + ",online=" + online + ",state=" + state + ",uid=" + uid + ",speed=" + speed + " color=" + color+" gu="+gu;
            }
        @Override
        public boolean equals(Object o)
            {
                if ((o instanceof Device) && this.u == ((Device) o).u && this.u != 0)
                    {
                        return true;
                    }
                else
                    {
                        if ((o instanceof Device) && this.u == 0 && this.tracker_id.equals(((Device) o).tracker_id))
                            {
                                return true;
                            }
                        return false;
                    }
            }
        @Override
        public int compareTo(Device dev)
            {
                // TODO Auto-generated method stub
                return -dev.name.compareTo(this.name);
            }
    }
