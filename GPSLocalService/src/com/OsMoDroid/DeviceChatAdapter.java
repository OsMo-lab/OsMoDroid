package com.OsMoDroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DeviceChatAdapter extends ArrayAdapter<MyMessage> {

	private TextView txtFromAddr;
	private TextView txtFromName;
	private TextView txtText;
	private TextView txtTime;
	public DeviceChatAdapter(Context context, int textViewResourceId, List<MyMessage> objects) {
		super(context, textViewResourceId, objects);
		
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		 if (row == null) {

		            
		
		            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		            row = inflater.inflate(R.layout.devicechatitem, parent, false);
		
		            
	
		        }
		
		 
		
		
		        MyMessage message = getItem(position);
		
		      
		
		       // DeviceonlineIcon = (ImageView) row.findViewById(R.id.country_icon);
		
		     
		
		        txtFromAddr = (TextView) row.findViewById(R.id.txtFromAddr);
		        txtFromName = (TextView) row.findViewById(R.id.txtFrom);
		        txtText = (TextView) row.findViewById(R.id.txtText);
		        txtTime = (TextView) row.findViewById(R.id.txtTime);
		        txtFromAddr.setText(message.from_addr);
		        txtFromName.setText(message.from_name);
		        txtText.setText(message.text);
		        txtTime.setText(message.time);
		        txtText.setTextColor(Color.BLACK);
		        if (message.from.equals(OsMoDroid.settings.getString("uid", ""))){
		        	 
		        	txtFromName.setTextColor(Color.GREEN);
		        }
		        else 
		        {
		        	txtFromName.setTextColor(Color.BLACK);
		        }
		       
		       
		        				
		
		        return row;

	}

}
