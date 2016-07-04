package com.OsMoDroid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import com.OsMoDroid.Netutil.InitTask;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
public class ColoredGPX implements Serializable
    {
        public int u;
        public int mPointsPrecomputed;
        File gpxfile;
        int color;
        Paint paint;
        Paint wpPaint;
        String url;
        Statuses status = Statuses.EMPTY;
        transient List<Point> points;// = new ArrayList<Point>(3000);
        List<Channel.Point> waypoints = new ArrayList<Channel.Point>(100);
        transient Rect mLineBounds;// = new Rect();
        transient IGeoPoint centerGeoPoint;

        public ColoredGPX(int u, File fileName, String scolor, String url)
            {
                gpxfile = fileName;
                this.url = url;
                this.u = u;
                try
                    {
                        color = Color.parseColor(scolor);
                    }
                catch (Exception e)
                    {
                        color = Color.MAGENTA;
                    }
            }
        public void initPathOverlay()
            {
                Log.d(this.getClass().getName(), "colored gpx initpath");
                try
                    {
//				PathOverlay path = new PathOverlay(cg.color, 10, mResourceProxy);
//				paths.add(path);
                        mPointsPrecomputed = 0;
                        mLineBounds = new Rect();
                        points = new ArrayList<Point>(3000);
                        FileInputStream is = new FileInputStream(this.gpxfile);
                        Netutil.InitTask initTask = new InitTask(this);
                        status = Statuses.LOADING;
                        initTask.execute(is);
                    }
                catch (FileNotFoundException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
        @Override
        public boolean equals(Object o)
            {
                if ((o instanceof ColoredGPX) && this.u == (((ColoredGPX) o).u))
                    {
                        return true;
                    }
                else
                    {
                        return false;
                    }
            }
        enum Statuses
            {
                EMPTY, DOWNLOADING, DOWNLOADED, LOADING, LOADED
            }
    }
