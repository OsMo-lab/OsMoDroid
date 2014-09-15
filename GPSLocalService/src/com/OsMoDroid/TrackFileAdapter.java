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

public class TrackFileAdapter extends ArrayAdapter<TrackFile> {

	private TextView fileName;
	private TextView fileDate;
	private TextView fileSize;
	public TrackFileAdapter(Context context, int textViewResourceId, List<TrackFile> objects) {
		super(context, textViewResourceId, objects);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		 if (row == null) {
      LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      row = inflater.inflate(R.layout.trackfileitem, parent, false);
		        }
		        TrackFile trackFile = getItem(position);
		        fileName = (TextView) row.findViewById(R.id.fileName);
		        fileDate = (TextView) row.findViewById(R.id.fileDate);
		        fileSize = (TextView) row.findViewById(R.id.fileSize);
		        fileName.setText(trackFile.fileName);
		        fileDate.setText(trackFile.fileDate);
		        fileSize.setText(trackFile.fileSize);
		        return row;

	}

}
