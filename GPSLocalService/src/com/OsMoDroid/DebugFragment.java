package com.OsMoDroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;


public class DebugFragment extends Fragment {
	private GPSLocalServiceClient globalActivity;
	
	
	final private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
	
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.simlinks, container, false);
		final ListView lv1 = (ListView) view.findViewById(R.id.listView1);
       lv1.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL); 
        LocalService.debugAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, LocalService.debuglist);
        lv1.setAdapter(LocalService.debugAdapter);
       
		return view;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		globalActivity=(GPSLocalServiceClient) getActivity();
		super.onActivityCreated(savedInstanceState);
	}
	 @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         //setRetainInstance(true);
         super.onCreate(savedInstanceState);
     }
	
	@Override
	public void onResume() {
		globalActivity.actionBar.setTitle("debug");
		
		super.onResume();
	}
	@Override
	public void onDetach() {
		globalActivity=null;
		super.onDetach();
	}

	@Override
	public void onAttach(Activity activity) {
		globalActivity = (GPSLocalServiceClient)activity;// TODO Auto-generated method stub
		super.onAttach(activity);
	}
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem clear = menu.add(0, 1, 0, "Очистить");
		MenuItem share = menu.add(0, 2, 0, "Отправить журнал");
		super.onCreateOptionsMenu(menu, inflater);
	}
	
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case 1:
		LocalService.debuglist.clear();
		LocalService.debugAdapter.notifyDataSetChanged();
		break;
	case 2:
		StringBuilder sb = new StringBuilder();
		if(LocalService.debuglist.size()>1000){
		for (String s : LocalService.debuglist.subList(LocalService.debuglist.size()-1000, LocalService.debuglist.size()))
		{
		    sb.append(s);
		    sb.append("\n");
		}
		}
		else
		{
			for (String s : LocalService.debuglist)
			{
			    sb.append(s);
			    sb.append("\n");
			}
		}
		String sendtext = sb.toString();
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "developers@osmodroid.ru" } );
		sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, sendtext );
		sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Debug log");
		startActivity(Intent.createChooser(sendIntent, "Email"));
	default:
		break;
	}	
		return super.onOptionsItemSelected(item);
	}



}
