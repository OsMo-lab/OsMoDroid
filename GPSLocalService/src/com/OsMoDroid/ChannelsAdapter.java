package com.OsMoDroid;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ChannelsAdapter extends ArrayAdapter<Channel> {

	//private TextView channelName;
	//private TextView channelCreated;
	//ToggleButton tg;
	LocalService localservice;
	public Context context;
	
	
	public ChannelsAdapter(Context context, int textViewResourceId, List<Channel> objects, LocalService localservice) {
		super(context, textViewResourceId, objects);
		this.localservice=localservice;
		
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		 if (row == null) {
  LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         row = inflater.inflate(R.layout.channelsitem, parent, false);
		
		        }
		         Channel channel = getItem(position);
		        TextView channelName = (TextView) row.findViewById(R.id.txtName);
		        TextView channelCreated = (TextView) row.findViewById(R.id.txtCreated);
		        ToggleButton tg = (ToggleButton) row.findViewById(R.id.toggleButton1);
		        
		        tg.setOnClickListener(myCheckChangList);
		        tg.setTag(position);
		        if (channel.name!=null)
		        	{   
		        		channelName.setText(channel.name);
		        	}
		        else 
		        	{
		        		channelName.setText(channel.group_id);
		        	}
		        if (channel.created!=null){channelCreated.setText(channel.group_id);}
		        if (channel.send!=null){tg.setChecked(channel.send);
		          channelName.setTextColor(Color.BLACK);      
		        
		        }
		     
		        return row;
	
}
	OnClickListener myCheckChangList = new OnClickListener() {
		public void onClick(View v) {
			((ToggleButton) v).toggle();
			 Channel channel = getItem((Integer)v.getTag());
			 String boolglobalsend =channel.send ? "0" : "1";
			//Netutil.newapicommand((ResultsListener)localservice,context, "om_device_channel_active:"+OsMoDroid.settings.getString("device", "")+","+channel.u+","+boolglobalsend);

				
								}
							};
	
	
}
