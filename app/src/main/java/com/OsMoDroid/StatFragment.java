package com.OsMoDroid;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
public class StatFragment extends Fragment
    {
        private GPSLocalServiceClient globalActivity;
        private BroadcastReceiver receiver;
        @Override
        public void onDestroy()
            {
                super.onDestroy();
            }
        @Override
        public void onAttach(Activity activity)
            {
                globalActivity = (GPSLocalServiceClient) activity;
                super.onAttach(activity);
            }
        @Override
        public void onResume()
            {
                globalActivity.actionBar.setTitle(getString(R.string.stat));
                super.onResume();
            }
        @Override
        public void onDetach()
            {
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
                setHasOptionsMenu(true);
                //setRetainInstance(true);
                super.onCreate(savedInstanceState);
            }
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
            {
                MenuItem bind = menu.add(0, 1, 0, R.string.reset);
                MenuItemCompat.setShowAsAction(bind, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                bind.setIcon(android.R.drawable.ic_menu_revert);
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
                                                globalActivity.mService.workdistance = 0;
                                                globalActivity.mService.timeperiod = 0;
                                                globalActivity.mService.workmilli = System.currentTimeMillis();
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
                        default:
                            break;
                    }
                return super.onOptionsItemSelected(item);
            }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
            {
                final View view = inflater.inflate(R.layout.status, container, false);
                if (globalActivity.mService != null)
                    {
                        TextView currentSpeedTextView = (TextView) view.findViewById(R.id.CurrentSpeedTextView);
                        TextView maxSpeedTextView = (TextView) view.findViewById(R.id.MaxSpeedTextView);
                        TextView avgSpeedTextView = (TextView) view.findViewById(R.id.AvgSpeedtextView);
                        TextView workDistanceTextView = (TextView) view.findViewById(R.id.WorkDistanceTextView);
                        TextView timeperiodTextView = (TextView) view.findViewById(R.id.timeperiodTextView);
                        TextView sendTextView = (TextView) view.findViewById(R.id.sendTextView);
                        TextView writeTextView = (TextView) view.findViewById(R.id.WriteTextView);
                        currentSpeedTextView.setText(OsMoDroid.df0.format(globalActivity.mService.currentspeed * 3.6));
                        maxSpeedTextView.setText(OsMoDroid.df1.format(globalActivity.mService.maxspeed * 3.6));
                        avgSpeedTextView.setText(OsMoDroid.df1.format(globalActivity.mService.avgspeed * 3600));
                        workDistanceTextView.setText(OsMoDroid.df2.format(globalActivity.mService.workdistance / 1000));
                        timeperiodTextView.setText(LocalService.formatInterval(globalActivity.mService.timeperiod));
                        sendTextView.setText(Integer.toString(globalActivity.mService.sendcounter));
                        writeTextView.setText(Integer.toString(globalActivity.mService.writecounter));
                    }
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
                        }
                };
                getActivity().registerReceiver(receiver, new IntentFilter("OsMoDroid"));
                return view;
            }
    }
