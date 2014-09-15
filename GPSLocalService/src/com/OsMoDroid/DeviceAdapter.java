package com.OsMoDroid;

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

public class DeviceAdapter extends ArrayAdapter<Device> {

	private TextView deviceName;
	private TextView deviceWhere;
	private TextView deviceLast;
	Context ctx;
	public DeviceAdapter(Context context, int textViewResourceId, List<Device> objects) {
		super(context, textViewResourceId, objects);
		this.ctx=context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		 if (row == null) {

		            
		
		            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		            row = inflater.inflate(R.layout.deviceitem, parent, false);
		
		            
	
		        }
		
		 
		
		
		        Device device = getItem(position);
		
		      
		
		       // DeviceonlineIcon = (ImageView) row.findViewById(R.id.country_icon);
		
		     
		
		        deviceName = (TextView) row.findViewById(R.id.txtName);
		        deviceWhere = (TextView) row.findViewById(R.id.txtWhere);
		        deviceLast = (TextView) row.findViewById(R.id.txtLast);
		        if(!device.name.equals(""))
		        	{
		        		deviceName.setText(device.name);
		        	}
		        else
		        	{
		        		deviceName.setText("Unknown");
		        	}
		        
		        deviceWhere.setText(device.tracker_id);
		        if(device.updatated>0)
		        	{
		        		deviceLast.setText(Float.toString(device.lat)+' '+Float.toString(device.lon)+' '+device.speed +' '+LocalService.formatInterval(System.currentTimeMillis()-device.updatated));		
		        	}
		        else
		        	{
		        		deviceLast.setText(Float.toString(device.lat)+' '+Float.toString(device.lon)+' '+device.speed +" -");
		        	}
		        
		        deviceName.setTextColor(Color.parseColor(device.color));
		        deviceWhere.setTextColor(Color.parseColor(device.color));
		        //Log.d(getClass().getSimpleName(),"device.name="+device.name.toString());
		        //Log.d(getClass().getSimpleName(),"device.online="+device.online.toString());
		        //Log.d(getClass().getSimpleName(),"device.state="+device.state.toString());
		        if (device.online==1){
		        	deviceLast.setText(deviceLast.getText()+ctx.getString(R.string.online));
		        }
		        else 
		        {
		        	deviceLast.setText(deviceLast.getText()+ctx.getString(R.string.offline));
		        }
		        if (device.state==1){
		        	deviceLast.setText(deviceLast.getText()+", "+ctx.getString(R.string.monitoring_on));
		        }
		        else
		        {
		        	deviceLast.setText(deviceLast.getText()+", "+ctx.getString(R.string.monitoring_off));
		        }
		       
		        				
		
		        return row;

	}

}
