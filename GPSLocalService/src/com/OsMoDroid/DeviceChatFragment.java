package com.OsMoDroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.MenuItemCompat;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.OsMoDroid.Netutil.MyAsyncTask;


public class DeviceChatFragment extends Fragment  {
	//ListView lv2;
	//Button sendButton;
	//EditText input;
	 int deviceU=-1;
	
	final private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
	
	Device getDeviceByU (int u){
		Log.d(this.getClass().getSimpleName(), " LocalService.deviceList="+ LocalService.deviceList.toString());
		for (Device dev : LocalService.deviceList){
			if (dev.u==u){
				return dev;
			}
		}
		
		return null;
		
	}
	@Override
	public void onDestroy() {
		
		super.onDestroy();
	}
	@Override
	public void onDestroyView() {
	
		super.onDestroyView();
	}
	
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	
	private GPSLocalServiceClient globalActivity;
	
	
	
	
	 @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         //setRetainInstance(true);
         super.onCreate(savedInstanceState);
     }
	
	@Override
	public void onAttach(Activity activity) {
		globalActivity = (GPSLocalServiceClient)activity;
		
		super.onAttach(activity);
		
	}
	void getDeviceInfo() {
		globalActivity.mService.myIM.sendToServer("IM:"+getDeviceByU(deviceU).u,true);
	}
	@Override
	public void onDetach() {
		LocalService.chatmessagelist.clear();
		LocalService.currentDevice=null;
		globalActivity=null;
		super.onDetach();
	}
	@Override
	public void onResume() {
		globalActivity.actionBar.setTitle(getString(R.string.chatwith)+getDeviceByU(deviceU).name);
		super.onResume();
	}
	
@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
	Log.d(this.getClass().getSimpleName(), "deviceU ="+deviceU+ " mBound="+globalActivity.mBound);
	
	getDeviceInfo();
	
		super.onViewCreated(view, savedInstanceState);
	}
	//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		globalActivity = (GPSLocalServiceClient) getSherlockActivity();
//		super.onActivityCreated(savedInstanceState);
//	}
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		MenuItem refresh = menu.add(0, 2, 0, R.string.refresh);
		MenuCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		refresh.setIcon(android.R.drawable.ic_menu_rotate);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onPrepareOptionsMenu(com.actionbarsherlock.view.Menu)
	 */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId()==2){
			globalActivity.mService.myIM.sendToServer("IM:"+getDeviceByU(deviceU).u,true);
		}
		
		return super.onOptionsItemSelected(item);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.chat, container, false);  
		 
		Bundle bundle = getArguments();
		   if(bundle != null){
		      deviceU = bundle.getInt("deviceU", -1);
		   
		   }
		    
		   //LocalService.currentchanneldeviceList= LocalService.channelList.get(getIntent().getIntExtra("CHANNELPOS", -1)).deviceList;
		   
		   LocalService.currentDevice = getDeviceByU(deviceU);
		   LocalService.chatmessagelist = LocalService.currentDevice.messagesstringList;
		    
	LocalService.chatmessagesAdapter = new DeviceChatAdapter(globalActivity.getApplicationContext(), R.layout.devicechatitem, LocalService.chatmessagelist);

		    final ListView lv2 = (ListView) view.findViewById(R.id.chatmessages);

	LocalService.chatmessagesAdapter.registerDataSetObserver(new DataSetObserver() {
	    @Override
	    public void onChanged() {
	        super.onChanged();
	        lv2.setSelection(LocalService.chatmessagesAdapter.getCount());    
	    }
	});
		    final EditText input = (EditText) view.findViewById(R.id.chateditText);
		    input.requestFocus();
		    
	Button sendButton = (Button) view.findViewById(R.id.chatsendButton);
	sendButton.setOnClickListener(new OnClickListener() {

		public void onClick(View v) {

			if (!(input.getText().toString().equals(""))) {

				JSONObject postjson = new JSONObject();
			try {

//				postjson.put("from", OsMoDroid.settings.getString("device", ""));
//				postjson.put("to", getDeviceByU(deviceU).tracker_id);
				postjson.put("text", input.getText().toString());
				globalActivity.mService.myIM.sendToServer("IMS:"+getDeviceByU(deviceU).u+"|"+postjson.toString(),true);

				} 
			catch (JSONException e) {

					// TODO Auto-generated catch block

					e.printStackTrace();

				}
	input.setText("");
			}


		}});

		      
		       lv2.setAdapter(LocalService.chatmessagesAdapter);
		       if (LocalService.chatmessagesAdapter!=null)
		       	{
		    	   LocalService.chatmessagesAdapter.notifyDataSetChanged();
		       	}
		       //Log.d(this.getClass().getSimpleName(),"getSherlockActivity:"+getSherlockActivity()+" deviceU="+deviceU+" getdevicebyU="+getDeviceByU(deviceU).u);
		    

		


		
		return view;

	}
	
	


	

}
