package com.OsMoDroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import com.OsMoDroid.Netutil.InitTask;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class ColoredGPX {
	File gpxfile;
	int color;
	//PathOverlay path;
	List<Point> points = new ArrayList<Point>();
	public int mPointsPrecomputed;
	public Path mPath = new Path();
	Rect mLineBounds = new Rect();
	
	public ColoredGPX(File fileName, String scolor) {
		gpxfile=fileName;
		try {
			color= Color.parseColor("#" + scolor);	
		} catch (Exception e) {
			color=Color.MAGENTA;
		}
		
		
	}
	
	public void initPathOverlay(){
		Log.d(this.getClass().getName(), "colored gpx initpath");
			try {
//				PathOverlay path = new PathOverlay(cg.color, 10, mResourceProxy);
//				paths.add(path);
				FileInputStream is = new FileInputStream(this.gpxfile);
				Netutil.InitTask initTask = new InitTask(this);
				initTask.execute(is);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
	
	@Override
	public boolean equals(Object o) {
		 if((o instanceof ColoredGPX) && this.gpxfile.getName().equals(((ColoredGPX)o).gpxfile.getName() ))  
	        {    
	            return true;    
	        }  
	        else  
	        {  
	            return false;  
	        }  
	}
}
