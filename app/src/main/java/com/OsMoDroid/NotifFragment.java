package com.OsMoDroid;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
public class NotifFragment extends Fragment
    {
        ArrayList<String> list;
        //EditText toAppText;
        //EditText toUserText;
        //EditText sendText;
        private GPSLocalServiceClient globalActivity;
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setHasOptionsMenu(true);
                //setRetainInstance(true);
                super.onCreate(savedInstanceState);
            }
        @Override
        public void onDestroy()
            {
                LocalService.notificationStringsAdapter =null;
                super.onDestroy();
            }
        @Override
        public void onResume()
            {
                globalActivity.actionBar.setTitle(getString(R.string.notifications));
                super.onResume();
            }
        @Override
        public void onAttach(Activity activity)
            {
                globalActivity = (GPSLocalServiceClient) activity;
                super.onAttach(activity);
            }
        @Override
        public void onDetach()
            {
                globalActivity = null;
                super.onDetach();
            }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
            {
                final View view = inflater.inflate(R.layout.meslist, container, false);
                final ListView lv1 = (ListView) view.findViewById(R.id.meslistView);
                list = new ArrayList<String>();
                list.clear();
                list.addAll(LocalService.messagelist);
                LocalService.notificationStringsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list)
                    {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent)
                            {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                Linkify.addLinks(text,Linkify.ALL);
                                return view;
                            }
                    }
                ;
                lv1.setAdapter(LocalService.notificationStringsAdapter);
                LocalService.notificationStringsAdapter.notifyDataSetChanged();
                LocalService.numberofnotif=0;
                return view;
            }

    }
