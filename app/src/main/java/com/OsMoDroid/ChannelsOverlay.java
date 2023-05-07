package com.OsMoDroid;

import static com.OsMoDroid.LocalService.myIM;
import static com.OsMoDroid.LocalService.waypointsList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.PointL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class ChannelsOverlay extends Overlay implements RotationGestureDetector.RotationListener
    {
        public static final int showtimeout = 90000;
        private int cluster_radius;
        private final Point mTempPoint1 = new Point();
        private final Point mTempPoint2 = new Point();
        private final Point mCurScreenCoords = new Point();
        boolean widgetmode;
        Paint graypaint = new Paint();
        Paint blackpaint = new Paint();
        Paint redpaint = new Paint();
        MapView map;
        int ten;
        int twenty;
        int followdev = -1;
        private RotationGestureDetector mRotationDetector;
        private float currentAngle = 0f;
        private MapFragment mapFragment;
        int lastclusterid=0;
        class Cluster
            {
               Point p;
                int id;
                int size=0;
            }
        ArrayList<Cluster> clusters=new ArrayList<Cluster>();
        public ChannelsOverlay( MapView map, boolean widgetmode)
            {

                this.map = map;
                this.widgetmode=widgetmode;

                this.mapFragment = mapFragment;
                mRotationDetector = new RotationGestureDetector(this);
                graypaint.setTextSize(twenty);
                graypaint.setDither(true);
                graypaint.setAntiAlias(true);
                graypaint.setTextSize(twenty);
                graypaint.setTypeface(Typeface.DEFAULT_BOLD);
                graypaint.setTextAlign(Paint.Align.CENTER);
                graypaint.setColor(Color.GRAY);
                blackpaint.setTextSize(twenty);
                blackpaint.setDither(true);
                blackpaint.setAntiAlias(true);
                blackpaint.setTextSize(twenty);
                blackpaint.setTypeface(Typeface.DEFAULT_BOLD);
                blackpaint.setTextAlign(Paint.Align.CENTER);
                blackpaint.setColor(Color.BLACK);
                redpaint.setColor(Color.RED);
            }
        int getSP(float px)
            {
                return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, map.getResources().getDisplayMetrics());
            }
        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView)
            {
                if (this.isEnabled())
                    {
                        mRotationDetector.onTouch(event);
                    }
                return super.onTouchEvent(event, mapView);
            }
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow)
            {
                Long curtime = System.currentTimeMillis();
                ten = getSP(OsMoDroid.settings.getInt("pointsize", 8));
                cluster_radius=ten*2;
                twenty = getSP(OsMoDroid.settings.getInt("pointsize", 8) * 2);
                if (shadow)
                    {
                        return;
                    }
                final BoundingBox theBoundingBox = mapView.getBoundingBox();
                final Projection pj = mapView.getProjection();
                final Point scrPoint = new Point();
                final Point scrPoint1 = new Point();


                if(!widgetmode)
                    {
                        for (ColoredGPX gpx : LocalService.showedgpxList)
                            {
                                if (gpx.status.equals(ColoredGPX.Statuses.LOADED))
                                    {
                                        drawGPX(canvas, pj, gpx, theBoundingBox, scrPoint, mapView);
                                    }
                            }
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.send)
                                    {
                                        for (Device d : ch.deviceList)
                                            {
                                                if (d.lat != 0f && d.lon != 0f && d.clusterid == 0)
                                                    {
                                                        //if (OsMoDroid.settings.getBoolean("traces", true))
                                                            {
                                                                drawdevicepath(canvas, pj, d);
                                                            }
                                                    }
                                            }
                                        for (ColoredGPX gpx : ch.gpxList)
                                            {
                                                if (gpx.status.equals(ColoredGPX.Statuses.LOADED))
                                                    {
                                                        drawGPX(canvas, pj, gpx, theBoundingBox, scrPoint, mapView);
                                                    }
                                            }
                                        for (com.OsMoDroid.Channel.Point p : ch.pointList)
                                            {
                                                if (p.clusterid == 0 && theBoundingBox.contains(new GeoPoint(p.lat, p.lon)))
                                                    {
                                                        pj.toPixels(new GeoPoint(p.lat, p.lon), scrPoint);
                                                        if (p.paint == null)
                                                            {
                                                                p.paint = new Paint();
                                                                p.paint.setDither(true);
                                                                p.paint.setAntiAlias(true);
                                                                p.paint.setTextSize(twenty);
                                                                p.paint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                p.paint.setTextAlign(Paint.Align.CENTER);
                                                                try
                                                                    {
                                                                        p.paint.setColor(Color.parseColor(p.color));
                                                                    }
                                                                catch (Exception e)
                                                                    {
                                                                        p.paint.setColor(Color.RED);
                                                                    }
                                                            }
                                                        if (p.paint.getColor() != Color.parseColor(p.color))
                                                            {
                                                                try
                                                                    {
                                                                        p.paint.setColor(Color.parseColor(p.color));
                                                                    }
                                                                catch (Exception e)
                                                                    {
                                                                        e.printStackTrace();
                                                                    }
                                                            }
                                                        canvas.save();
                                                        canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
                                                        canvas.drawRect(scrPoint.x - ten, scrPoint.y - ten, scrPoint.x + ten, scrPoint.y + ten, p.paint);
                                                        blackpaint.setDither(true);
                                                        blackpaint.setAntiAlias(true);
                                                        blackpaint.setTextSize(twenty);
                                                        blackpaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                        blackpaint.setTextAlign(Paint.Align.CENTER);
                                                        if (OsMoDroid.settings.getBoolean("shortname", false))
                                                            {
                                                                Rect textBounds = new Rect();
                                                                String shortname = p.name;
                                                                if (p.name.length() > 3)
                                                                    {
                                                                        shortname = p.name.substring(0, 3);
                                                                    }
                                                                blackpaint.setTextSize(twenty / 2);
                                                                blackpaint.getTextBounds(shortname, 0, shortname.length(), textBounds);
                                                                canvas.drawText(shortname, scrPoint.x, scrPoint.y + textBounds.height() / 2 - ten / 2, blackpaint);
                                                                blackpaint.setTextSize(twenty);
                                                            }
                                                        else
                                                            {
                                                                canvas.drawText(p.name, scrPoint.x, scrPoint.y - ten, blackpaint);
                                                            }
                                                        canvas.restore();
                                                    }
                                            }
                                        for (Device dev : ch.deviceList)
                                            {
                                                if (dev.devpaint == null)
                                                    {
                                                        dev.devpaint = new Paint();
                                                        dev.devpaint.setColor(dev.color);
                                                    }
                                                if (dev.devpaint.getColor() != dev.color)
                                                    {
                                                        dev.devpaint.setColor(dev.color);
                                                    }
                                                if (dev.lat != 0f && dev.lon != 0f && dev.clusterid == 0)
                                                    {
                                                        if (theBoundingBox.contains(new GeoPoint(dev.lat, dev.lon)))
                                                            {
                                                                pj.toPixels(new GeoPoint(dev.lat, dev.lon), scrPoint);
                                                                dev.devpaint.setDither(true);
                                                                dev.devpaint.setAntiAlias(true);
                                                                dev.devpaint.setTextSize(twenty);
                                                                dev.devpaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                dev.devpaint.setTextAlign(Paint.Align.CENTER);
                                                                blackpaint.setDither(true);
                                                                blackpaint.setAntiAlias(true);
                                                                blackpaint.setTextSize(twenty);
                                                                blackpaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                blackpaint.setTextAlign(Paint.Align.CENTER);
                                                                graypaint.setDither(true);
                                                                graypaint.setAntiAlias(true);
                                                                graypaint.setTextSize(twenty);
                                                                graypaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                graypaint.setTextAlign(Paint.Align.CENTER);
                                                                canvas.save();
                                                                canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
                                                                canvas.drawCircle(scrPoint.x, scrPoint.y, ten + ten / 10, graypaint);
                                                                canvas.drawCircle(scrPoint.x, scrPoint.y, ten, dev.devpaint);
                                                                if (dev.u == followdev)
                                                                    {
                                                                        canvas.drawCircle(scrPoint.x, scrPoint.y, ten / 3, redpaint);
                                                                    }
                                                                if (dev.updatated < (curtime - showtimeout))
                                                                    {
                                                                        if (OsMoDroid.settings.getBoolean("shortname", false))
                                                                            {
                                                                                Rect textBounds = new Rect();
                                                                                String shortname = dev.name;
                                                                                if (dev.name.length() > 1)
                                                                                    {
                                                                                        shortname = dev.name.substring(0, 1);
                                                                                    }
                                                                                graypaint.getTextBounds(dev.name, 0, shortname.length(), textBounds);
                                                                                canvas.drawText(shortname, scrPoint.x, scrPoint.y + textBounds.height() / 2, graypaint);
                                                                            }
                                                                        else
                                                                            {
                                                                                if (dev.name.equals(""))
                                                                                    {
                                                                                        canvas.drawText(Integer.toString(dev.u), scrPoint.x, scrPoint.y - ten, graypaint);
                                                                                    }
                                                                                else
                                                                                    {
                                                                                        canvas.drawText(dev.name, scrPoint.x, scrPoint.y - ten, graypaint);
                                                                                    }
                                                                                if (dev.updatated < (curtime - showtimeout))
                                                                                    {
                                                                                        Date resultdate = new Date(dev.updatated);
                                                                                        canvas.drawText(OsMoDroid.sdf.format(dev.updatated), scrPoint.x, scrPoint.y + ten + twenty, graypaint);
                                                                                    }
                                                                                else
                                                                                    {
                                                                                        canvas.drawText(dev.speed, scrPoint.x, scrPoint.y + ten + twenty, graypaint);
                                                                                    }
                                                                            }
                                                                    }
                                                                else
                                                                    {
                                                                        if (OsMoDroid.settings.getBoolean("shortname", false))
                                                                            {
                                                                                Rect textBounds = new Rect();
                                                                                String shortname = dev.name;
                                                                                if (dev.name.length() > 1)
                                                                                    {
                                                                                        shortname = dev.name.substring(0, 1);
                                                                                    }
                                                                                blackpaint.getTextBounds(dev.name, 0, shortname.length(), textBounds);
                                                                                canvas.drawText(shortname, scrPoint.x, scrPoint.y + textBounds.height() / 2, blackpaint);
                                                                            }
                                                                        else
                                                                            {
                                                                                if (dev.name.equals(""))
                                                                                    {
                                                                                        canvas.drawText(Integer.toString(dev.u), scrPoint.x, scrPoint.y - ten, blackpaint);
                                                                                    }
                                                                                else
                                                                                    {
                                                                                        canvas.drawText(dev.name, scrPoint.x, scrPoint.y - ten, blackpaint);
                                                                                    }
                                                                                if (dev.updatated < (curtime - showtimeout))
                                                                                    {
                                                                                        Date resultdate = new Date(dev.updatated);
                                                                                        canvas.drawText(OsMoDroid.sdf.format(dev.updatated), scrPoint.x, scrPoint.y + ten + twenty, blackpaint);
                                                                                    }
                                                                                else
                                                                                    {
                                                                                        canvas.drawText(dev.speed, scrPoint.x, scrPoint.y + ten + twenty, blackpaint);
                                                                                    }
                                                                            }
                                                                    }
                                                                canvas.restore();
                                                            }
                                                    }
                                            }
                                    }
                            }
                        if (LocalService.mydev.devicePath.size() > 2)
                            {
                                drawdevicepath(canvas, pj, LocalService.mydev);
                            }
                    }
                else
                    {
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.type==2)
                                    {
                                        for (com.OsMoDroid.Channel.Point p : ch.pointList)
                                            {
                                                if (p.clusterid == 0 && theBoundingBox.contains(new GeoPoint(p.lat, p.lon)))
                                                    {
                                                        pj.toPixels(new GeoPoint(p.lat, p.lon), scrPoint);
                                                        if (p.paint == null)
                                                            {
                                                                p.paint = new Paint();
                                                                p.paint.setDither(true);
                                                                p.paint.setAntiAlias(true);
                                                                p.paint.setTextSize(twenty);
                                                                p.paint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                p.paint.setTextAlign(Paint.Align.CENTER);
                                                                try
                                                                    {
                                                                        p.paint.setColor(Color.parseColor(p.color));
                                                                    }
                                                                catch (Exception e)
                                                                    {
                                                                        p.paint.setColor(Color.RED);
                                                                    }
                                                            }
                                                        if (p.paint.getColor() != Color.parseColor(p.color))
                                                            {
                                                                try
                                                                    {
                                                                        p.paint.setColor(Color.parseColor(p.color));
                                                                    }
                                                                catch (Exception e)
                                                                    {
                                                                        e.printStackTrace();
                                                                    }
                                                            }
                                                        canvas.save();
                                                        canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
                                                        canvas.drawRect(scrPoint.x - ten, scrPoint.y - ten, scrPoint.x + ten, scrPoint.y + ten, p.paint);
                                                        blackpaint.setDither(true);
                                                        blackpaint.setAntiAlias(true);
                                                        blackpaint.setTextSize(twenty);
                                                        blackpaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                        blackpaint.setTextAlign(Paint.Align.CENTER);

                                                                Rect textBounds = new Rect();
                                                                String shortname = p.name;
                                                                if (p.name.length() > 3)
                                                                    {
                                                                        shortname = p.name.substring(0, 3);
                                                                    }
                                                                blackpaint.setTextSize(twenty / 2);
                                                                blackpaint.getTextBounds(shortname, 0, shortname.length(), textBounds);
                                                                canvas.drawText(shortname, scrPoint.x, scrPoint.y + textBounds.height() / 2 - ten / 2, blackpaint);
                                                                blackpaint.setTextSize(twenty);


                                                        canvas.restore();
                                                    }
                                            }

                                        for (Device dev : ch.deviceList)
                                            {
                                                if (dev.devpaint == null)
                                                    {
                                                        dev.devpaint = new Paint();
                                                        dev.devpaint.setColor(dev.color);
                                                    }
                                                if (dev.devpaint.getColor() != dev.color)
                                                    {
                                                        dev.devpaint.setColor(dev.color);
                                                    }
                                                if (dev.lat != 0f && dev.lon != 0f && dev.clusterid == 0)
                                                    {
                                                        if (theBoundingBox.contains(new GeoPoint(dev.lat, dev.lon)))
                                                            {
                                                                pj.toPixels(new GeoPoint(dev.lat, dev.lon), scrPoint);
                                                                dev.devpaint.setDither(true);
                                                                dev.devpaint.setAntiAlias(true);
                                                                dev.devpaint.setTextSize(twenty);
                                                                dev.devpaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                dev.devpaint.setTextAlign(Paint.Align.CENTER);
                                                                blackpaint.setDither(true);
                                                                blackpaint.setAntiAlias(true);
                                                                blackpaint.setTextSize(twenty);
                                                                blackpaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                blackpaint.setTextAlign(Paint.Align.CENTER);
                                                                graypaint.setDither(true);
                                                                graypaint.setAntiAlias(true);
                                                                graypaint.setTextSize(twenty);
                                                                graypaint.setTypeface(Typeface.DEFAULT_BOLD);
                                                                graypaint.setTextAlign(Paint.Align.CENTER);
                                                                canvas.save();
                                                                canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
                                                                canvas.drawCircle(scrPoint.x, scrPoint.y, ten + ten / 10, graypaint);
                                                                canvas.drawCircle(scrPoint.x, scrPoint.y, ten, dev.devpaint);
                                                                Rect textBounds = new Rect();
                                                                String shortname = dev.name;
                                                                if (dev.name.length() > 1)
                                                                    {
                                                                        shortname = dev.name.substring(0, 1);
                                                                    }
                                                                blackpaint.getTextBounds(dev.name, 0, shortname.length(), textBounds);
                                                                canvas.drawText(shortname, scrPoint.x, scrPoint.y + textBounds.height() / 2, blackpaint);

                                                                canvas.restore();
                                                            }
                                                    }
                                            }
                                    }
                            }

                    }
            }

        private void drawdevicepath(Canvas canvas, Projection pj, Device dev)
            {
                if(dev.pathpaint==null)
                    {
                        dev.pathpaint=new Paint();
                        dev.pathpaint.setColor(dev.color);
                        dev.pathpaint.setAlpha(128);
                        dev.pathpaint.setAntiAlias(true);
                        dev.pathpaint.setStrokeCap(Cap.ROUND);
                        dev.pathpaint.setStrokeJoin(Join.ROUND);
                        dev.pathpaint.setStyle(Style.STROKE);

                    }
                if(dev.pathpaint.getColor()!=dev.color)
                    {
                        dev.pathpaint.setColor(dev.color);
                        dev.pathpaint.setAlpha(128);
                    }
                dev.pathpaint.setStrokeWidth(getSP(OsMoDroid.settings.getInt("pointsize", 8))/2);
                if (dev.devicePath.size() > 2)
                    {

                        while (dev.iprecomputed < dev.devicePath.size())
                            {
                                final SerPoint pt = dev.devicePath.get(dev.iprecomputed);
                                pj.toProjectedPixels(pt.point.x, pt.point.y, pt.point);
                                dev.iprecomputed++;
                            }
                       Point screenPoint = pj.toPixelsFromProjected(dev.devicePath.get(dev.devicePath.size()-1).point, this.mTempPoint1);
                        if(true
                                //OsMoDroid.settings.getBoolean("longpath",true)
                        )
                            {
                                for (int i = dev.devicePath.size() - 2; i >= 0; i--)
                                    {
                                        Point screenPoint1 = pj.toPixelsFromProjected(dev.devicePath.get(i + 1).point, this.mTempPoint2);
                                        if (Math.abs(screenPoint.x - screenPoint1.x) + Math.abs(screenPoint.y - screenPoint1.y) > ten)
                                            {
                                                if (Math.abs(screenPoint.x - screenPoint1.x) + Math.abs(screenPoint.y - screenPoint1.y) < twenty * 15)
                                                    {
                                                        canvas.drawLine(screenPoint1.x, screenPoint1.y, screenPoint.x, screenPoint.y, dev.pathpaint);
                                                    }
                                                else
                                                    {
                                                        canvas.drawPoint(screenPoint.x, screenPoint.y, dev.pathpaint);
                                                    }
                                                screenPoint.set(screenPoint1.x, screenPoint1.y);
                                            }
                                    }
                            }
                        else
                            {
                                int k = dev.devicePath.size()-30;
                                if (k<0)
                                    {
                                        k=0;
                                    }
                                for (int i = dev.devicePath.size() - 2; i >=k; i--)
                                    {
                                        Point screenPoint1 = pj.toPixelsFromProjected(dev.devicePath.get(i + 1).point, this.mTempPoint2);
                                        if (Math.abs(screenPoint.x - screenPoint1.x) + Math.abs(screenPoint.y - screenPoint1.y) > ten)
                                            {
                                                if (Math.abs(screenPoint.x - screenPoint1.x) + Math.abs(screenPoint.y - screenPoint1.y) < twenty * 15)
                                                    {
                                                        canvas.drawLine(screenPoint1.x, screenPoint1.y, screenPoint.x, screenPoint.y, dev.pathpaint);
                                                    }
                                                else
                                                    {
                                                        canvas.drawPoint(screenPoint.x, screenPoint.y, dev.pathpaint);
                                                    }
                                                screenPoint.set(screenPoint1.x, screenPoint1.y);
                                            }
                                    }
                            }
                    }
            }
        private void drawGPX(Canvas canvas, final Projection pj, ColoredGPX gpx, BoundingBox theBoundingBox, Point scrPoint, MapView mapView)
            {
                if(gpx.paint==null)
                    {
                        gpx.paint= new Paint();
                        gpx.paint.setColor(gpx.color);
                        gpx.paint.setAlpha(128);
                        gpx.paint.setStyle(Style.STROKE);
                        gpx.paint.setAntiAlias(true);
                        gpx.paint.setStrokeCap(Cap.ROUND);
                        gpx.paint.setStrokeJoin(Join.ROUND);

                    }
                if(gpx.paint.getColor()!=gpx.color)
                    {
                        gpx.paint.setColor(gpx.color);
                        gpx.paint.setAlpha(128);
                    }

                gpx.paint.setStrokeWidth(getSP(OsMoDroid.settings.getInt("pointsize", 8))/2);
                if(gpx.wpPaint==null)
                    {
                        gpx.wpPaint=new Paint();
                        gpx.wpPaint.setColor(gpx.color);
                        gpx.wpPaint.setDither(true);
                        gpx.wpPaint.setAntiAlias(true);
                        gpx.wpPaint.setTextSize(twenty);
                        gpx.wpPaint.setTypeface(Typeface.DEFAULT_BOLD);
                        gpx.wpPaint.setTextAlign(Paint.Align.CENTER);
                    }
                if(gpx.wpPaint.getColor()!=gpx.color)
                    {
                        gpx.wpPaint.setColor(gpx.color);
                    }
                int size = gpx.points.size();
                if (size > 2)
                    {
                        // precompute new points to the intermediate projection.
                        while (gpx.mPointsPrecomputed < size)
                            {
                                final PointL pt = gpx.points.get(gpx.mPointsPrecomputed);
                                pj.toProjectedPixels(pt.x, pt.y, pt);
                                gpx.mPointsPrecomputed++;
                            }
                        Point screenPoint0 = null; // points on screen
                        Point screenPoint1;
                        Point lastArrowPoint = new Point(0,0);
                        SegmentPoint projectedPoint0; // points from the points list
                        SegmentPoint projectedPoint1;
                        // clipping rectangle in the intermediate projection, to avoid performing projection.
                        BoundingBox boundingBox = pj.getBoundingBox();
                        PointL topLeft = pj.toProjectedPixels(boundingBox.getLatNorth(),
                                boundingBox.getLonWest(), null);
                        PointL bottomRight = pj.toProjectedPixels(boundingBox.getLatSouth(),
                                boundingBox.getLonEast(), null);
                        projectedPoint0 = gpx.points.get(size - 1);
                        boolean firstpointofsegment=true;
                        for (int i = size - 2; i >= 0; i--)
                            {
                                // compute next points
                                projectedPoint1 = gpx.points.get(i);
                                if ((projectedPoint0.y>bottomRight.y||projectedPoint0.y<topLeft.y)&&(projectedPoint0.x>bottomRight.x||projectedPoint0.x<topLeft.x)
                                &&(projectedPoint1.y>bottomRight.y||projectedPoint1.y<topLeft.y)&&(projectedPoint1.x>bottomRight.x||projectedPoint1.x<topLeft.x)
                                ) // проверяем что все точки снаружи
                                    {
                                        // skip this line, move to next point
                                        projectedPoint0 = projectedPoint1;
                                        screenPoint0 = null;
                                        continue;
                                    }
                                if(projectedPoint0.segment!=projectedPoint1.segment)
                                    {
                                        projectedPoint0 = projectedPoint1;
                                        screenPoint0 = null;
                                        firstpointofsegment=true;
                                        continue;
                                    }
                                if (screenPoint0 == null)
                                    {
                                        screenPoint0 = pj.toPixelsFromProjected(projectedPoint0, this.mTempPoint1);
                                    }
                                screenPoint1 = pj.toPixelsFromProjected(projectedPoint1, this.mTempPoint2);
                                // skip this point, too close to previous point
                                if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= ten &&!firstpointofsegment )
                                    {
                                        continue;
                                    }
                                firstpointofsegment=false;
                                if(OsMoDroid.settings.getBoolean("fullgpx",true)
                                        ||(Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y))<twenty*15
                                )
                                    {
                                        canvas.drawLine(screenPoint0.x, screenPoint0.y, screenPoint1.x, screenPoint1.y, gpx.paint);
                                        //if(OsMoDroid.settings.getBoolean("arrows",true))
                                            {
                                                float angleRad = (float) Math.atan2(screenPoint0.y - screenPoint1.y, screenPoint0.x - screenPoint1.x);
                                                float angle = (float) (angleRad * 180 / Math.PI) + 90f;
                                                float angleRadLastArrow = (float) Math.atan2(screenPoint1.y - lastArrowPoint.y, screenPoint1.x - lastArrowPoint.x);
                                                float angleLastArrow = (float) (angleRadLastArrow * 180 / Math.PI) + 90f;
                                                if (
                                                        Math.sqrt((screenPoint0.x - lastArrowPoint.x) * (screenPoint0.x - lastArrowPoint.x) + (screenPoint0.y - lastArrowPoint.y) * (screenPoint0.y - lastArrowPoint.y)) > twenty * 10f
                                                                || Math.abs(angleLastArrow - angle) > 200f
                                                        )
                                                    {
                                                        lastArrowPoint.x = screenPoint0.x;
                                                        lastArrowPoint.y = screenPoint0.y;
                                                        canvas.save();
                                                        canvas.rotate(angle, screenPoint0.x, screenPoint0.y);
                                                        canvas.drawLine(screenPoint0.x + twenty / 3, screenPoint0.y + twenty/2, screenPoint0.x, screenPoint0.y, gpx.paint);
                                                        canvas.drawLine(screenPoint0.x, screenPoint0.y, screenPoint0.x - twenty / 3, screenPoint0.y + twenty/2, gpx.paint);
                                                        canvas.restore();
                                                    }
                                            }
                                    }
                                else
                                    {
                                        canvas.drawPoint(screenPoint1.x, screenPoint1.y, gpx.paint);
                                    }

                                projectedPoint0 = projectedPoint1;
                                screenPoint0.x = screenPoint1.x;
                                screenPoint0.y = screenPoint1.y;
                            }

                    }
                for (Channel.Point p : gpx.waypoints)
                    {
                        if (theBoundingBox.contains(new GeoPoint(p.lat, p.lon)))
                            {
                                pj.toPixels(new GeoPoint(p.lat, p.lon), scrPoint);
                                canvas.save();
                                canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
                                canvas.drawRect(scrPoint.x - ten, scrPoint.y - ten, scrPoint.x + ten, scrPoint.y + ten, gpx.wpPaint);
                                if(OsMoDroid.settings.getBoolean("shortname",false))
                                    {
                                        Rect textBounds = new Rect();
                                        String shortname = p.name;
                                        if(p.name.length()>3)
                                            {
                                                shortname = p.name.substring(0, 3);
                                            }
                                        blackpaint.getTextBounds(shortname, 0,shortname.length(), textBounds);
                                        blackpaint.setTextSize(twenty/2);
                                        canvas.drawText(shortname, scrPoint.x, scrPoint.y + textBounds.height()/2- ten/2 , blackpaint);
                                        blackpaint.setTextSize(twenty);

                                    }
                                else
                                    {
                                                canvas.drawText(p.name, scrPoint.x, scrPoint.y - ten, blackpaint);
                                    }
                                canvas.restore();
                            }
                    }
            }
        @Override
        public void onRotate(float deltaAngle)
            {
               // if (OsMoDroid.settings.getBoolean("rotation", false))
                    {
                        map.setMapOrientation(map.getMapOrientation() + deltaAngle);
                    }
            }
        @Override
        public boolean onLongPress(MotionEvent e, MapView mapView)
            {
                if(LocalService.channelList.size()>0 ||OsMoDroid.settings.getBoolean("started",false))
                    {
                        Projection proj = mapView.getProjection();
                        IGeoPoint p = proj.fromPixels((int) e.getX(), (int) e.getY());
                        final JSONObject jo = new JSONObject();
                        try
                            {
                                jo.put("lat", p.getLatitude());
                                jo.put("lon", p.getLongitude());
                            }
                        catch (JSONException e1)
                            {
                                e1.printStackTrace();
                            }
                        LinearLayout layout = new LinearLayout(map.getContext());
                        layout.setOrientation(LinearLayout.VERTICAL);
                        final TextView txv5 = new TextView(map.getContext());
                        txv5.setText(R.string.point_name_);
                        layout.addView(txv5);
                        final EditText pointName = new EditText(map.getContext());
                        layout.addView(pointName);
                        final TextView txv6 = new TextView(map.getContext());
                        txv6.setText(R.string.chanal);
                        layout.addView(txv6);
                        final Spinner groupSpinner = new Spinner(map.getContext());
                        layout.addView(groupSpinner);
                        List<Channel> activeChannelList = new ArrayList<Channel>();
                        Channel currentTrackChannel = new Channel();
                        currentTrackChannel.u = -1;
                        currentTrackChannel.name = map.getContext().getString(R.string.currtrack);
                        if(OsMoDroid.settings.getBoolean("started",false))
                        {
                            activeChannelList.add(currentTrackChannel);
                        }
                        for(Channel ch: LocalService.channelList)
                            {
                                if(ch.send)
                                    {
                                        activeChannelList.add(ch);
                                    }
                            }
                        ArrayAdapter<Channel> dataAdapter = new ArrayAdapter<Channel>(map.getContext(), R.layout.spinneritem, activeChannelList);
                        groupSpinner.setAdapter(dataAdapter);
                        if(LocalService.lastGroupSpinnerSelectedPosition<=activeChannelList.size())groupSpinner.setSelection(LocalService.lastGroupSpinnerSelectedPosition);
                        AlertDialog alertdialog1 = new AlertDialog.Builder(map.getContext()).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                    {
                                    }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                    {
                                    }
                            }).create();
                        alertdialog1.setView(layout);
                        alertdialog1.setTitle(map.getContext().getString(R.string.point_create));
                        alertdialog1.setMessage(map.getContext().getString(R.string.point_create_description));

                        alertdialog1.show();
                        alertdialog1.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new CustomListener(alertdialog1)
                            {
                                @Override
                                public void onClick(View v)
                                    {
                                        if (LocalService.myIM.authed)
                                            {
                                                if(groupSpinner.getSelectedItem()!=null)
                                                    {
                                                        try
                                                            {
                                                                jo.put("name", pointName.getText().toString());
                                                                jo.put("group", ((Channel) groupSpinner.getSelectedItem()).u);
                                                                waypointsList.add(jo);
                                                            }
                                                        catch (JSONException e1)
                                                            {
                                                                e1.printStackTrace();
                                                            }
                                                        LocalService.myIM.sendToServer("GPA|" + jo.toString(), true);
                                                        LocalService.lastGroupSpinnerSelectedPosition =  groupSpinner.getSelectedItemPosition();
                                                        super.dialog.dismiss();
                                                    }
                                                else
                                                    {
                                                        Toast.makeText(map.getContext(), R.string.needselectpoint, Toast.LENGTH_SHORT).show();
                                                    }
                                            }
                                        else
                                            {
                                                OsMoDroid.saveObject(myIM.localService,  waypointsList, "waypointsList");
                                                Toast.makeText(map.getContext(), R.string.CheckInternet, Toast.LENGTH_SHORT).show();

                                            }
                                    }
                            });


                    }
                else
                    {
                        Toast.makeText(map.getContext(), R.string.nogroupstosendpoint, Toast.LENGTH_SHORT).show();
                    }
                        return true;

            }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView)
            {
                final Projection pj = mapView.getProjection();
                final Rect screenRect = pj.getIntrinsicScreenRect();

//                for(Cluster c:clusters)
//                    {
//
//                        if (c.p.x <= (e.getX() + 2 * ten) && c.p.x >= (e.getX() - 2 * ten)
//                                && c.p.y <= (e.getY() + ten) && c.p.y >= (e.getY() - ten))
//                            {
//                                map.getController().zoomInFixing(c.p.x,c.p.y);
//                            }
//                    }
                for (Channel ch : LocalService.channelList)
                    {

                        if (ch.send)
                            {
                                for(Channel.Point p: ch.pointList)
                                    {
                                        if(p.clusterid==0)
                                            {
                                                pj.toPixels(new GeoPoint(p.lat, p.lon), mCurScreenCoords);
                                                if (mCurScreenCoords.x <= (e.getX() + 2 * ten) && mCurScreenCoords.x >= (e.getX() - 2 * ten)
                                                        && mCurScreenCoords.y <= (e.getY() + ten) && mCurScreenCoords.y >= (e.getY() - ten))
                                                    {
                                                        LinearLayout layout = new LinearLayout(map.getContext());
                                                        layout.setOrientation(LinearLayout.VERTICAL);
                                                        final TextView txv5 = new TextView(map.getContext());
                                                        txv5.setText(p.description+'\n'+p.url);
                                                        Linkify.addLinks(txv5,Linkify.WEB_URLS);
                                                        layout.addView(txv5);
                                                        final TextView txv6 = new TextView(map.getContext());
                                                        txv6.setText(p.time);
                                                        layout.addView(txv6);
                                                        AlertDialog alertdialog1 = new AlertDialog.Builder(map.getContext()).create();
                                                        alertdialog1.setView(layout);
                                                        alertdialog1.setTitle(p.name);
                                                        alertdialog1.setMessage(ch.name);
                                                        alertdialog1.show();
                                                        if(LocalService.myIM!=null)
                                                        {
                                                            LocalService.myIM.sendToServer("GPV:"+p.u,false);
                                                        }
                                                    }
                                            }
                                    }
                                    for (Device dev : ch.deviceList)
                                        {
                                            if(dev.clusterid==0)
                                                {
                                                    pj.toPixels(new GeoPoint(dev.lat, dev.lon), mCurScreenCoords);
                                                    if (mCurScreenCoords.x <= (e.getX() + 2 * ten) && mCurScreenCoords.x >= (e.getX() - 2 * ten)
                                                            && mCurScreenCoords.y <= (e.getY() + ten) && mCurScreenCoords.y >= (e.getY() - ten))
                                                        {
                                                            if (followdev != dev.u)
                                                                {
                                                                    Toast.makeText(mapView.getContext(), map.getContext().getString(R.string.follow_) + ' '+dev.name, Toast.LENGTH_SHORT).show();
                                                                    LocalService.myIM.sendToServer("SP:"+dev.u+"|1", false);
                                                                    followdev = dev.u;

                                                                }
                                                            else
                                                                {
                                                                    Toast.makeText(mapView.getContext(), map.getContext().getString(R.string.no_follow_) + ' '+dev.name, Toast.LENGTH_SHORT).show();
                                                                    LocalService.myIM.sendToServer("SP:"+dev.u+"|0", false);
                                                                    followdev = -1;
                                                                }
                                                            mapView.invalidate();
                                                            return true;
                                                        }
                                                }
                                        }
                                    for (ColoredGPX cg : ch.gpxList)
                                        {
                                            for (Channel.Point p : cg.waypoints)
                                                {
                                                    pj.toPixels(new GeoPoint(p.lat, p.lon), mCurScreenCoords);
                                                    if (mCurScreenCoords.x <= (e.getX() + 2 * ten) && mCurScreenCoords.x >= (e.getX() - 2 * ten)
                                                            && mCurScreenCoords.y <= (e.getY() + ten) && mCurScreenCoords.y >= (e.getY() - ten))
                                                        {
                                                            Toast.makeText(mapView.getContext(), p.name, Toast.LENGTH_SHORT).show();
                                                        }
                                                }
                                        }
                                }
                            }
                return true;
            }
    }
