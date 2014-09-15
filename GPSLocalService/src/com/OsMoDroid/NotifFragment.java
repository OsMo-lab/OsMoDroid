package com.OsMoDroid;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;



public class NotifFragment extends Fragment {
	 @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         //setRetainInstance(true);
         super.onCreate(savedInstanceState);
     }
	private ArrayAdapter<String> adapter;
	ArrayList<String> list;
	//EditText toAppText;
	//EditText toUserText;
	//EditText sendText;
	private GPSLocalServiceClient globalActivity;
	
	@Override
	public void onResume() {
		 globalActivity.actionBar.setTitle(getString(R.string.notifications));
		super.onResume();
	}
	
	@Override
	public void onAttach(Activity activity) {
		globalActivity = (GPSLocalServiceClient) activity;
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		globalActivity=null;
		super.onDetach();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.meslist, container, false);  
		final ListView lv1 = (ListView) view.findViewById(R.id.meslistView);
        list = new ArrayList<String>();
        list.clear();
        list.addAll(LocalService.messagelist);
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, list);
        lv1.setAdapter(adapter);
        adapter.notifyDataSetChanged();
       	return view;
	}
}
