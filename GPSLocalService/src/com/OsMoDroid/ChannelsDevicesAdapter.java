package com.OsMoDroid;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelsDevicesAdapter extends ArrayAdapter<Device> {

	private TextView channelDeviceName;
	private TextView channelDeviceWhere;
	private TextView channelDeviceSpeed;
	private TextView channelDeviceDistance;
	private Location channelDeviceLocation =new Location("");
	private Context context;
	public ChannelsDevicesAdapter(Context context, int textViewResourceId, List<Device> objects) {
		super(context, textViewResourceId, objects);
		this.context=context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		 if (row == null) {
  LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         row = inflater.inflate(R.layout.channelsdeviceitem, parent, false);
		
		        }
		        Device device = getItem(position);
		        Log.d(getClass().getSimpleName(), "channeldevice:"+device.name+" "+device.lat+" "+device.lon);
		        channelDeviceName = (TextView) row.findViewById(R.id.txtName);
		        channelDeviceWhere = (TextView) row.findViewById(R.id.txtWhere);
		        channelDeviceSpeed = (TextView) row.findViewById(R.id.txtSpeed);
		        channelDeviceDistance = (TextView) row.findViewById(R.id.TextDistance);
		        channelDeviceName.setTextColor(Color.parseColor(device.color));
		        channelDeviceWhere.setTextColor(Color.parseColor(device.color));
		        channelDeviceSpeed.setTextColor(Color.parseColor(device.color));
		        channelDeviceDistance.setTextColor(Color.parseColor(device.color));
		        if (device.name!=null){   channelDeviceName.setText(device.name);}
		        if (device.speed!=null){   channelDeviceSpeed.setText(device.speed);}
		        //if (device.lat!=null&device.lon!=null){
		        if(device.updatated>0)
		        	{
		        		channelDeviceWhere.setText(context.getString(R.string.coordinats)+device.lat+" "+device.lon+' '+LocalService.formatInterval(System.currentTimeMillis()-device.updatated));
		        	}
		        else
		        	{
		        		channelDeviceWhere.setText(context.getString(R.string.coordinats)+device.lat+" "+device.lon+" -");
		        	}
		        	//}
		        if (LocalService.currentLocation!=null){
		        	channelDeviceLocation.setLatitude((device.lat));
		        	channelDeviceLocation.setLongitude((device.lon));
		        	channelDeviceDistance.setText(context.getString(R.string.Distantion)+Integer.toString((int)LocalService.currentLocation.distanceTo(channelDeviceLocation)/1000)+context.getString(R.string.Km)+Integer.toString((int) (1000*(LocalService.currentLocation.distanceTo(channelDeviceLocation)/1000 -(int)LocalService.currentLocation.distanceTo(channelDeviceLocation)/1000)) )+context.getString(R.string.m));	
		        	
		        }
		        return row;

	}

}
