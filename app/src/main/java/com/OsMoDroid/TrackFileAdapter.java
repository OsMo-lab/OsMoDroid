package com.OsMoDroid;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
public class TrackFileAdapter extends ArrayAdapter<TrackFile>
    {
        private TextView fileName;
        private TextView fileDate;
        private TextView fileSize;
        private File sdDir = android.os.Environment.getExternalStorageDirectory();
        public TrackFileAdapter(Context context, int textViewResourceId, List<TrackFile> objects)
            {
                super(context, textViewResourceId, objects);

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
                fileName.setText(trackFile.fileName);
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
                                        File fileName = new File(sdDir, "OsMoDroid/" + trackFile.fileName);
                                        Log.d(getClass().getSimpleName(), "filename=" + fileName);
                                        ColoredGPX load = new ColoredGPX(0, fileName, "#0000FF", null);
                                        Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                                        boolean exist = false;
                                        while (it.hasNext())
                                            {
                                                ColoredGPX cg = it.next();
                                                if (cg.gpxfile.equals(load.gpxfile))
                                                    {
                                                        exist = true;
                                                    }
                                            }
                                        if (!exist)
                                            {
                                                LocalService.showedgpxList.add(load);
                                                load.initPathOverlay();
                                                trackFile.showedonmap = true;
                                            }
                                    }
                                else
                                    {
                                        File fileName = new File(sdDir, "OsMoDroid/" + trackFile.fileName);
                                        ColoredGPX load = new ColoredGPX(0, fileName, "#0000FF", null);
                                        Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                                        while (it.hasNext())
                                            {
                                                ColoredGPX cg = it.next();
                                                if (cg.gpxfile.equals(load.gpxfile))
                                                    {
                                                        it.remove();
                                                    }
                                            }
                                        trackFile.showedonmap = false;

                                    }

                            }
                    });
                return row;
            }
    }
