package com.OsMoDroid;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
public class TrackStatFragment extends Fragment
    {
        private File sdDir = android.os.Environment.getExternalStorageDirectory();
        private LineChart mChart;
        LineData speedLineData;
        LineDataSet speedDataSet;
        LineDataSet avgspeedDataSet;
        LineDataSet eleDataSet;
        double prevlat;
        double prevlon;
        public  ArrayList<Entry> speeddistanceEntryList = new ArrayList<Entry>();
        public  ArrayList<Entry> avgspeeddistanceEntryList = new ArrayList<Entry>();
        public  ArrayList<Entry> eledistanceEntryList = new ArrayList<Entry>();
        public  ArrayList<String> distanceStringList = new ArrayList<String>();
        class StatfromFile extends AsyncTask<File,Void,Void>
            {
                ProgressDialog dialog;
                @Override
                protected void onPreExecute()
                    {
                        super.onPreExecute();
                       dialog = ProgressDialog.show(TrackStatFragment.this.getContext(), getString(R.string.loading), getString(R.string.pleasewait), true);
                    }
                @Override
                protected void onProgressUpdate(Void... values)
                    {

                        super.onProgressUpdate(values);
                    }
                @Override
                protected void onPostExecute(Void aVoid)
                    {
                        speedLineData.notifyDataChanged();
                        speedDataSet.notifyDataSetChanged();
                        eleDataSet.notifyDataSetChanged();
                        mChart.notifyDataSetChanged();
                        mChart.invalidate();
                        dialog.dismiss();

                    }
                @Override
                protected Void doInBackground(File... params)
                    {
                        float workdistance=0;
                        long firsttime=0;
                        try {
                            FileInputStream is = new FileInputStream(params[0]);
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                        parser.setInput(is, null);
                        parser.nextTag();
                        while (parser.next() != XmlPullParser.END_DOCUMENT) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            String name = parser.getName();
                            // Starts by looking for the entry tag
                            if (name.equals("trkpt"))
                                {
                                    Double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                                    Double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                                    GeoPoint curGeoPoint = new GeoPoint(lat,lon);
                                    if(prevlat!=0&&prevlon!=0)
                                        {
                                            GeoPoint prevGeoPoint = new GeoPoint(prevlat, prevlon);
                                            workdistance = workdistance + curGeoPoint.distanceTo(prevGeoPoint);
                                        }
                                    prevlat=lat;
                                    prevlon=lon;

                                    String speed="";
                                    String ele="";
                                    String time="";
                                    while (!(parser.next() == XmlPullParser.END_TAG && "trkpt".equals(parser.getName())))
                                        {
                                            if (parser.getEventType() == XmlPullParser.START_TAG && ("speed".equals(parser.getName())||"ele".equals(parser.getName())||"time".equals(parser.getName())) )
                                                {
                                                    if("speed".equals(parser.getName()))
                                                        {
                                                            while (parser.next() != XmlPullParser.END_TAG && !"speed".equals(parser.getName()))
                                                                {
                                                                    if (parser.getText() != null)
                                                                        {
                                                                            speed = speed + parser.getText();
                                                                        }
                                                                }
                                                        }

                                                    if("ele".equals(parser.getName()))
                                                        {
                                                            while (parser.next() != XmlPullParser.END_TAG && !"ele".equals(parser.getName()))
                                                                {
                                                                    if (parser.getText() != null)
                                                                        {
                                                                            ele = ele + parser.getText();
                                                                        }
                                                                }
                                                        }

                                                    if("time".equals(parser.getName()))
                                                        {
                                                            while (parser.next() != XmlPullParser.END_TAG && !"time".equals(parser.getName()))
                                                                {
                                                                    if (parser.getText() != null)
                                                                        {
                                                                            time = time + parser.getText();
                                                                        }
                                                                }
                                                        }

                                                }
                                        }
                                    for (int index = distanceStringList.size(); index <= (int) workdistance; index++)
                                        {
                                            distanceStringList.add(Integer.toString(index/1000)+','+Integer.toString(index%1000));
                                        }
                                    Entry e = new Entry(Float.parseFloat(speed)* 3.6f,(int) workdistance);
                                    speeddistanceEntryList.add(e);
                                    Entry elee = new Entry(Float.parseFloat(ele),(int) workdistance);
                                    eledistanceEntryList.add(elee);

                                    long curtime = OsMoDroid.sdf1.parse(time).getTime();
                                    if(firsttime==0)
                                        {
                                            firsttime=curtime;
                                        }
                                    else
                                        {
                                            Entry avgspeedentry= new Entry( 3600f*workdistance / (curtime - firsttime),(int) workdistance );
                                            avgspeeddistanceEntryList.add(avgspeedentry);
                                        }




                                    //2010-02-11T21:06:33Z

                                }

                        }


                        is.close();
                    }

                catch (Exception e)
                {
                    e.printStackTrace();
                    LocalService.addlog("Error reading gpx "+e.getMessage());

                    //Toast.makeText(OsMoDroid.context,"Error loading gpx track", Toast.LENGTH_LONG).show();
                }
                        return null;
                    }
            }

        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                Log.d(getClass().getSimpleName(), "TrackStatFragment onCreate");

            }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
            {


                Log.d(getClass().getSimpleName(), "TrackStatFragment onCreateView");
                final View view = inflater.inflate(R.layout.fragment_track_stat, container, false);
                mChart = (LineChart) view.findViewById(R.id.tracklineChart);
                speedDataSet = new LineDataSet(speeddistanceEntryList,  getString(R.string.speed));
                avgspeedDataSet = new LineDataSet(avgspeeddistanceEntryList, getString(R.string.average));
                eleDataSet = new LineDataSet(eledistanceEntryList,  getString(R.string.altitude));
                speedDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                speedDataSet.setColor(Color.RED);
                //speedDataSet.setDrawCubic(true);
                speedDataSet.setDrawFilled(true);
                speedDataSet.setDrawCircles(false);
                speedDataSet.setDrawValues(false);
                avgspeedDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                avgspeedDataSet.setColor(Color.GREEN);
                //avgspeedDataSet.setDrawCubic(true);
                avgspeedDataSet.setDrawFilled(true);
                avgspeedDataSet.setDrawCircles(false);
                avgspeedDataSet.setDrawValues(false);
                eleDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                eleDataSet.setColor(Color.BLUE);
                //eleDataSet.setDrawCubic(true);
                eleDataSet.setDrawFilled(true);
                eleDataSet.setDrawCircles(false);
                eleDataSet.setDrawValues(false);

//                mChart.setOnChartGestureListener(this);
//                mChart.setOnChartValueSelectedListener(this);
                mChart.setDrawGridBackground(false);
                mChart.setDescription("");
                mChart.setNoDataTextDescription("You need to provide data for the chart.");
                mChart.setTouchEnabled(true);
                mChart.setDragEnabled(true);
                mChart.setScaleEnabled(true);
                mChart.setPinchZoom(true);
                YAxis leftAxis = mChart.getAxisLeft();
                leftAxis.removeAllLimitLines();
                leftAxis.setAxisMinValue(0f);
                YAxis rightAxis = mChart.getAxisRight();
                leftAxis.removeAllLimitLines();
                leftAxis.setAxisMinValue(0f);
                speedLineData = new LineData(distanceStringList);
                speedLineData.addDataSet(speedDataSet);
                speedLineData.addDataSet(avgspeedDataSet);
                speedLineData.addDataSet(eleDataSet);
                mChart.setData(speedLineData);
                mChart.invalidate();
                Bundle bundle = getArguments();
                if (bundle != null)
                    {

                        File file = new File(sdDir, "OsMoDroid/" + bundle.getString("file"));
                        StatfromFile s = new StatfromFile();
                        s.execute(file);
                    }
                return view;
            }

        @Override
        public void onAttach(Context context)
            {
                super.onAttach(context);

            }
        @Override
        public void onDetach()
            {
                super.onDetach();

            }

    }
