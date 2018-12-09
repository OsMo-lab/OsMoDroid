package com.OsMoDroid;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.w3c.dom.Text;
public class StatFragment extends Fragment implements OnChartGestureListener,OnChartValueSelectedListener
    {
        private GPSLocalServiceClient globalActivity;
        private BroadcastReceiver receiver;
        private LineChart mChart;
        LineData speedLineData;
        LineDataSet speedDataSet;
        LineDataSet avgspeedDataSet;
        LineDataSet altitudeDataSet;
        private View l1;
        private View l2;
        private View l3;
        private View l4;
        private MenuItem plot;
        @Override
        public void onDestroy()
            {
                if(OsMoDroid.debug) Log.d(getClass().getSimpleName(), "StatFragment onDestroy");
                super.onDestroy();
            }
        @Override
        public void onAttach(Activity activity)
            {
                if(OsMoDroid.debug) Log.d(getClass().getSimpleName(), "StatFragment onAttach");
                globalActivity = (GPSLocalServiceClient) activity;
                super.onAttach(activity);
            }
        @Override
        public void onResume()
            {
                if(OsMoDroid.debug) Log.d(getClass().getSimpleName(), "StatFragment onResume");
                globalActivity.actionBar.setTitle(getString(R.string.stat));
                super.onResume();
            }
        @Override
        public void onDetach()
            {
                if(OsMoDroid.debug) Log.d(getClass().getSimpleName(), "StatFragment onDetach");
                if (receiver != null)
                    {
                        globalActivity.unregisterReceiver(receiver);
                    }
                globalActivity = null;
                super.onDetach();
            }
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                if(OsMoDroid.debug) Log.d(getClass().getSimpleName(), "StatFragment onCreate");
                setHasOptionsMenu(true);
                //setRetainInstance(true);
                super.onCreate(savedInstanceState);
            }
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
            {
                MenuItem bind = menu.add(0, 1, 0, R.string.reset);
                plot = menu.add(0, 2, 0, R.string.plot);
                MenuItemCompat.setShowAsAction(bind, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                MenuItemCompat.setShowAsAction(plot, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                bind.setIcon(android.R.drawable.ic_menu_revert);
                if(mChart!=null&&mChart.getVisibility()==View.GONE)
                    {
                        plot.setIcon(android.R.drawable.ic_menu_gallery);
                    }
                else
                    {
                        plot.setIcon(android.R.drawable.ic_menu_info_details);
                    }
                super.onCreateOptionsMenu(menu, inflater);
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                switch (item.getItemId())
                    {
                        case 1:
                            AlertDialog alertdialog1 = new AlertDialog.Builder(
                                    getActivity()).create();
                            alertdialog1.setTitle(getActivity().getString(R.string.confirm_reset_staticstic));
                            alertdialog1
                                    .setMessage(getActivity()
                                            .getString(R.string.please_confirm_reset_statistic));
                            alertdialog1.setButton(getString(R.string.yes),
                                    new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                            {
                                                globalActivity.mService.avgspeed = 0;
                                                globalActivity.mService.maxspeed = 0;
                                                globalActivity.mService.intKM = 0;
                                                globalActivity.mService.totalclimb = 0;
                                                globalActivity.mService.workdistance = 0;
                                                globalActivity.mService.timeperiod = 0;
                                                globalActivity.mService.workmilli = System.currentTimeMillis();
                                                LocalService.distanceStringList.clear();
                                                LocalService.speeddistanceEntryList.clear();
                                                LocalService.avgspeeddistanceEntryList.clear();
                                                LocalService.altitudedistanceEntryList.clear();
                                                speedLineData.clearValues();
                                                speedDataSet.clear();
                                                avgspeedDataSet.clear();
                                                altitudeDataSet.clear();
                                                speedLineData.addDataSet(speedDataSet);
                                                speedLineData.addDataSet(avgspeedDataSet);
                                                speedLineData.addDataSet(altitudeDataSet);
                                                mChart.notifyDataSetChanged();
                                                globalActivity.mService.refresh();
                                                return;
                                            }
                                    });
                            alertdialog1.setButton2(getString(R.string.No),
                                    new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                            {
                                                return;
                                            }
                                    });
                            alertdialog1.show();
                            break;
                        case 2:
                            if(mChart.getVisibility()==View.VISIBLE)
                                {
                                    mChart.setVisibility(View.GONE);
                                    l1.setVisibility(View.VISIBLE);
                                    l2.setVisibility(View.VISIBLE);
                                    l3.setVisibility(View.VISIBLE);
                                    l4.setVisibility(View.VISIBLE);
                                    plot.setIcon(android.R.drawable.ic_menu_gallery);
                                }
                            else
                                {
                                    mChart.setVisibility(View.VISIBLE);
                                    l1.setVisibility(View.GONE);
                                    l2.setVisibility(View.GONE);
                                    l3.setVisibility(View.GONE);
                                    l4.setVisibility(View.GONE);
                                    plot.setIcon(android.R.drawable.ic_menu_info_details);

                                }
                            getActivity().supportInvalidateOptionsMenu();
                        default:
                            break;
                    }
                return super.onOptionsItemSelected(item);
            }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
            {
                if(OsMoDroid.debug) Log.d(getClass().getSimpleName(), "StatFragment onCreateView");
                final View view = inflater.inflate(R.layout.status, container, false);
                l1=view.findViewById(R.id.l1);
                l2=view.findViewById(R.id.l2);
                l3=view.findViewById(R.id.l3);
                l4=view.findViewById(R.id.l4);
                if (globalActivity.mService != null)
                    {
                        TextView currentSpeedTextView = (TextView) view.findViewById(R.id.CurrentSpeedTextView);
                        TextView maxSpeedTextView = (TextView) view.findViewById(R.id.MaxSpeedTextView);
                        TextView avgSpeedTextView = (TextView) view.findViewById(R.id.AvgSpeedtextView);
                        TextView workDistanceTextView = (TextView) view.findViewById(R.id.WorkDistanceTextView);
                        TextView timeperiodTextView = (TextView) view.findViewById(R.id.timeperiodTextView);
                        TextView sendTextView = (TextView) view.findViewById(R.id.sendTextView);
                        TextView writeTextView = (TextView) view.findViewById(R.id.WriteTextView);
                        TextView altTextView = (TextView)view.findViewById(R.id.altTextView);
                        TextView climbTextView = (TextView) view.findViewById(R.id.climbTextView);
                        currentSpeedTextView.setText(OsMoDroid.df0.format(globalActivity.mService.currentspeed * 3.6));
                        maxSpeedTextView.setText(OsMoDroid.df1.format(globalActivity.mService.maxspeed * 3.6));
                        avgSpeedTextView.setText(OsMoDroid.df1.format(globalActivity.mService.avgspeed * 3600));
                        workDistanceTextView.setText(OsMoDroid.df2.format(globalActivity.mService.workdistance / 1000));
                        timeperiodTextView.setText(LocalService.formatInterval(globalActivity.mService.timeperiod));
                        sendTextView.setText(Integer.toString(globalActivity.mService.sendcounter));
                        writeTextView.setText(Integer.toString(globalActivity.mService.writecounter));
                        if(globalActivity.mService.altitude!=Integer.MIN_VALUE)
                            {
                                altTextView.setText(Integer.toString(globalActivity.mService.altitude));
                            }

                        climbTextView.setText(Integer.toString(globalActivity.mService.totalclimb));

                    }

                mChart = (LineChart) view.findViewById(R.id.lineChart1);
                mChart.setVisibility(View.GONE);


                speedDataSet = new LineDataSet(LocalService.speeddistanceEntryList,  getString(R.string.speed));
                avgspeedDataSet = new LineDataSet(LocalService.avgspeeddistanceEntryList, getString(R.string.average));
                altitudeDataSet = new LineDataSet(LocalService.altitudedistanceEntryList , globalActivity.getString(R.string.altitude));
                speedDataSet.setLineWidth(3);
                avgspeedDataSet.setLineWidth(3);
                altitudeDataSet.setLineWidth(3);

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

                altitudeDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                altitudeDataSet.setColor(Color.BLUE);
                //altitudeDataSet.setDrawCubic(true);
                altitudeDataSet.setDrawFilled(true);
                altitudeDataSet.setDrawCircles(false);
                altitudeDataSet.setDrawValues(false);



                mChart.setOnChartGestureListener(this);
                mChart.setOnChartValueSelectedListener(this);
                mChart.setDrawGridBackground(false);

                // no description text
//                mChart.setDescription("");
                mChart.getDescription().setEnabled(false);
                mChart.setNoDataText("You need to provide data for the chart.");

                // enable touch gestures
                mChart.setTouchEnabled(true);

                // enable scaling and dragging
                mChart.setDragEnabled(true);
                mChart.setScaleEnabled(true);
                // mChart.setScaleXEnabled(true);
                // mChart.setScaleYEnabled(true);

                // if disabled, scaling can be done on x- and y-axis separately
                mChart.setPinchZoom(true);




                YAxis leftAxis = mChart.getAxisLeft();
                leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines

                //leftAxis.setAxisMaxValue(50);
                leftAxis.setAxisMinValue(0f);
                YAxis rightAxis = mChart.getAxisRight();
                rightAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines

                //leftAxis.setAxisMaxValue(50);
                rightAxis.setAxisMinValue(0f);

                //speedLineData = new LineData(LocalService.distanceStringList);
                speedLineData = new LineData();
                speedLineData.addDataSet(speedDataSet);
                speedLineData.addDataSet(avgspeedDataSet);
                speedLineData.addDataSet(altitudeDataSet);

                speedDataSet.setDrawValues(false);
                avgspeedDataSet.setDrawValues(false);
                altitudeDataSet.setDrawValues(false);
                mChart.setData(speedLineData);


                mChart.invalidate();
                receiver = new BroadcastReceiver()
                    {
                        @Override
                        public void onReceive(Context context, final Intent intent)
                            {
                                TextView currentSpeedTextView = (TextView) view.findViewById(R.id.CurrentSpeedTextView);
                                TextView maxSpeedTextView = (TextView) view.findViewById(R.id.MaxSpeedTextView);
                                TextView avgSpeedTextView = (TextView) view.findViewById(R.id.AvgSpeedtextView);
                                TextView workDistanceTextView = (TextView) view.findViewById(R.id.WorkDistanceTextView);
                                TextView timeperiodTextView = (TextView) view.findViewById(R.id.timeperiodTextView);
                                TextView sendTextView = (TextView) view.findViewById(R.id.sendTextView);
                                TextView writeTextView = (TextView) view.findViewById(R.id.WriteTextView);
                                currentSpeedTextView.setText(intent.getStringExtra("currentspeed"));
                                maxSpeedTextView.setText(intent.getStringExtra("maxspeed"));
                                avgSpeedTextView.setText(intent.getStringExtra("avgspeed"));
                                workDistanceTextView.setText(intent.getStringExtra("workdistance"));
                                timeperiodTextView.setText(intent.getStringExtra("timeperiod"));
                                sendTextView.setText(Integer.toString(intent.getIntExtra("sendcounter", 0)));
                                writeTextView.setText(Integer.toString(intent.getIntExtra("writecounter", 0)));

                                TextView altTextView = (TextView)view.findViewById(R.id.altTextView);
                                TextView climbTextView = (TextView) view.findViewById(R.id.climbTextView);

                                        altTextView.setText(intent.getStringExtra("altitude"));

                                climbTextView.setText(intent.getStringExtra("totalclimb"));
                                if(mChart!=null)
                                    {
                                        try
                                            {
                                                speedDataSet.setDrawValues(true);
                                                avgspeedDataSet.setDrawValues(true);
                                                altitudeDataSet.setDrawValues(true);
                                                speedDataSet.notifyDataSetChanged();
                                                avgspeedDataSet.notifyDataSetChanged();
                                                altitudeDataSet.notifyDataSetChanged();
                                                speedLineData.notifyDataChanged();
                                                mChart.notifyDataSetChanged();
                                                mChart.invalidate();
                                            }
                                            catch (IllegalArgumentException e)
                                                {
                                                    LocalService.addlog("strange exception "+speedDataSet.getEntryCount() +' '+avgspeedDataSet.getEntryCount()+' '+altitudeDataSet.getEntryCount()+' ');
                                                }
                                    }
                            }
                    };
                getActivity().registerReceiver(receiver, new IntentFilter("OsMoDroid"));
                return view;
            }
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
            {
            }
        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
            {
            }
        @Override
        public void onChartLongPressed(MotionEvent me)
            {
            }
        @Override
        public void onChartDoubleTapped(MotionEvent me)
            {
            }
        @Override
        public void onChartSingleTapped(MotionEvent me)
            {
            }
        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY)
            {
            }
        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY)
            {
            }
        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY)
            {
            }

        @Override
        public void onValueSelected(Entry e, Highlight h) {

        }

        @Override
        public void onNothingSelected()
            {
            }

    }
