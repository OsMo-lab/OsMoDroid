package com.OsMoDroid;
import java.text.SimpleDateFormat;

import android.util.Log;
public class TrackFile implements Comparable<TrackFile>
    {
        final private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        public String fileName;
        public String name;
        public String fileDate;
        public String fileSize;
        public boolean showedonmap = false;
        public boolean fromServer = false;
        public String url;
        public String distantion;
        public String image;
        int u;
        public TrackFile(String fileName, long date, long size)
            {
                this.fileName = fileName;
                if(fileName.equals("null"))
                {
                    this.fileName="";
                }
                this.fileDate = sdf1.format(date);
                this.fileSize = Long.toString(size / 1024) + " Kb";
            }
        @Override
        public int compareTo(TrackFile file)
            {
                // TODO Auto-generated method stub
                return file.fileDate.compareTo(this.fileDate);
            }
    }