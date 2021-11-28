package com.OsMoDroid;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TrackFileAdapter extends ArrayAdapter<TrackFile> implements ResultsListener
    {
        private TextView fileName;
        private TextView fileDate;
        private TextView fileSize;
        private ImageView imageView;
        private File sdDir = OsMoDroid.osmodirFile;
        Context context;

        public TrackFileAdapter(Context context, int textViewResourceId, List<TrackFile> objects)
            {

                super(context, textViewResourceId, objects);
                TrackFileAdapter.this.context=context;

            }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
            {
                View row = convertView;
                if (row == null)
                    {
                        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        row = inflater.inflate(R.layout.trackfileitem, parent, false);
                    }
                TrackFile trackFile = getItem(position);
                fileName = (TextView) row.findViewById(R.id.fileName);
                fileDate = (TextView) row.findViewById(R.id.fileDate);
                fileSize = (TextView) row.findViewById(R.id.fileSize);
                imageView= (ImageView) row.findViewById(R.id.imageView);
                if(trackFile.fromServer) {
                    imageView.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(trackFile.image)
                            .into(imageView);
                }
                else
                {
                    imageView.setVisibility(View.GONE);
                }
                if(trackFile.fromServer)
                {
                    fileName.setText(trackFile.fileName + '\n' + trackFile.distance + "KM");
                   // fileName.setBackgroundColor(Color.parseColor("#FF8C00"));
                }
                else
                {
                    fileName.setText(trackFile.fileName);
                  //  fileName.setBackgroundColor(Color.TRANSPARENT);
                }

                fileDate.setText(trackFile.fileDate);
                fileSize.setText(trackFile.fileSize);
//                if (trackFile.showedonmap)
//                    {
//                        fileName.setBackgroundColor(Color.GREEN);
//                    }
//                else
//                    {
//                        fileName.setBackgroundColor(Color.TRANSPARENT);
//                    }
                CheckBox checkBox = (CheckBox)row.findViewById(R.id.showTrackCheckBox);
                checkBox.setChecked(trackFile.showedonmap);
                checkBox.setTag(position);
                checkBox.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                            {
//                                ((CheckBox) v).toggle();
                                TrackFile trackFile = getItem((Integer) v.getTag());
                                if(!trackFile.showedonmap)
                                    {
                                        if(trackFile.fromServer)
                                        {

                                            ColoredGPX cgpx = new ColoredGPX(trackFile.u, new File(sdDir, "OsMoDroid/servergpx/" + trackFile.u+".gpx"), "#0000FF", trackFile.url);

                                            cgpx.status = ColoredGPX.Statuses.DOWNLOADING;
                                            Netutil.downloadfile(TrackFileAdapter.this,context, cgpx.url, cgpx);

                                        }
                                        else {
                                            File fileName = new File(sdDir, "OsMoDroid/" + trackFile.fileName);
                                            Log.d(getClass().getSimpleName(), "filename=" + fileName);
                                            ColoredGPX load = new ColoredGPX(0, fileName, "#0000FF", null);
                                            Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                                            boolean exist = false;
                                            while (it.hasNext()) {
                                                ColoredGPX cg = it.next();
                                                if (cg.gpxfile.equals(load.gpxfile)) {
                                                    exist = true;
                                                }
                                            }
                                            if (!exist) {
                                                LocalService.showedgpxList.add(load);
                                                load.initPathOverlay();
                                                trackFile.showedonmap = true;
                                            }
                                        }
                                    }
                                else {
                                    if (trackFile.fromServer)
                                    {
                                        Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                                        while (it.hasNext()) {
                                            ColoredGPX cg = it.next();
                                            if (cg.u==trackFile.u) {
                                                it.remove();
                                            }
                                        }
                                        trackFile.showedonmap = false;
                                    }
                                    else
                                        {
                                        File fileName = new File(sdDir, "OsMoDroid/" + trackFile.fileName);
                                        ColoredGPX load = new ColoredGPX(0, fileName, "#0000FF", null);
                                        Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                                        while (it.hasNext()) {
                                            ColoredGPX cg = it.next();
                                            if (cg.gpxfile.equals(load.gpxfile)) {
                                                it.remove();
                                            }
                                        }
                                        trackFile.showedonmap = false;

                                    }
                                }

                            }
                    });
                return row;
            }

        @Override
        public void onResultsSucceeded(APIComResult result)
        {
            Log.d(getClass().getSimpleName(), "download result=" + result.load);
            Log.d(getClass().getSimpleName(), "add to showlist status"+result.load.status );
            if(result.load.status== ColoredGPX.Statuses.DOWNLOADED)
            {

                LocalService.showedgpxList.add(result.load);
                result.load.initPathOverlay();

                for (TrackFile trackFile : LocalService.trackFileList) {
                    if (trackFile.u == result.load.u) {
                        trackFile.showedonmap = true;
                        trackFile.fileName=result.load.gpxfile.getName();
                    }
                }


            }
            else
            {
                TrackFileAdapter.this.notifyDataSetChanged();
            }
            Collections.sort(LocalService.trackFileList);
        }
    }
