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
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class StatFragment extends Fragment {
	private GPSLocalServiceClient globalActivity;
	private BroadcastReceiver receiver;
	@Override
	public void onDestroy() {
		
		super.onDestroy();
	}
	@Override
	public void onAttach(Activity activity) {
		globalActivity = (GPSLocalServiceClient) activity;
		super.onAttach(activity);
	}
	 @Override
	public void onResume() {
		 globalActivity.actionBar.setTitle(getString(R.string.stat));
		super.onResume();
	}
	@Override
	public void onDetach() {
		if (receiver != null) {
			globalActivity.unregisterReceiver(receiver);
		}
		globalActivity=null;
		super.onDetach();
	}
	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         //setRetainInstance(true);
         super.onCreate(savedInstanceState);
     }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 final View view =  inflater.inflate(R.layout.status,container, false);
		 if (globalActivity.mService!=null){
			 TextView currentSpeedTextView = (TextView) view.findViewById(R.id.CurrentSpeedTextView);
				TextView maxSpeedTextView = (TextView) view.findViewById(R.id.MaxSpeedTextView);
				TextView avgSpeedTextView = (TextView) view.findViewById(R.id.AvgSpeedtextView);
				TextView workDistanceTextView = (TextView) view.findViewById(R.id.WorkDistanceTextView);
				TextView timeperiodTextView = (TextView) view.findViewById(R.id.timeperiodTextView);
				TextView sendTextView = (TextView) view.findViewById(R.id.sendTextView);
				TextView writeTextView = (TextView) view.findViewById(R.id.WriteTextView);
				currentSpeedTextView.setText(LocalService.df0.format(globalActivity.mService.currentspeed*3.6));
				maxSpeedTextView.setText(LocalService.df1.format(globalActivity.mService.maxspeed*3.6));
				avgSpeedTextView.setText(LocalService.df1.format(globalActivity.mService.avgspeed*3600));
				workDistanceTextView.setText( LocalService.df2.format(globalActivity.mService.workdistance/1000));
				timeperiodTextView.setText(LocalService.formatInterval(globalActivity.mService.timeperiod));
				sendTextView.setText(Integer.toString(globalActivity.mService.sendcounter));
				writeTextView.setText(Integer.toString(globalActivity.mService.writecounter));
		 }
		 receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, final Intent intent) {
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
					sendTextView.setText(Integer.toString(intent.getIntExtra("sendcounter",0)));
					writeTextView.setText(Integer.toString(intent.getIntExtra("writecounter",0)));
								
				}


			};

			getActivity().registerReceiver(receiver, new IntentFilter("OsMoDroid"));
		 return view;
	}

}
