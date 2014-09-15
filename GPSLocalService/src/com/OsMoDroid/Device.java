package com.OsMoDroid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import android.util.Log;

public class Device implements Comparable<Device> , Serializable{
	public int u;
	public String tracker_id;
	public String name;
	public String app;
	public String last;
	public String url;
	public String where;
	public float lat;
	public float lon;
	public int online=0;
	public int state=0;
	public String uid;
	public String speed="";
	public String color="#AAAAAA";
	public String ch;
	public boolean subscribed=false;
	public long updatated=0;
	public List<IGeoPoint> devicePath= new ArrayList<IGeoPoint>();
	//public PathOverlay p;
	public Device(){
		
	}
	
//	public Device( String tracker_id,
//	 String name,
//	 String app,
//	 String last,
//	 String url,
//	 String where,
//	 String lat,
//	 String lon,
//	 String online,
//	 String state,
//	 String uid
//	 
//	 ){
//		Log.d(getClass().getSimpleName(), "tracker_id="+tracker_id+" "+"name="+name+"app="+app+" "+"last="
//	 +last+" "+"url="+url+" "+"where="+where+" "+"lat="+lat+" "+"lon="+lon+" "+"online="+state+" "+"uid="+uid+" ");
//		 this.tracker_id=tracker_id;
//		 this.name=name;
//		  this.app=app;
//		  this.last=last;
//		  this.url=url;
//		  this.where=where;
//		   try {
//			if (lat!=null){ this.lat=  Float.parseFloat(lat);}
//			   if (lon!=null){ this.lon=  Float.parseFloat(lon);}
//		} catch (NumberFormatException e) {
//			Log.d(getClass().getSimpleName(),e+" "+ e.getMessage());
//			e.printStackTrace();
//		}
//		  this.online=online;
//		  this.state=state;
//		  this.uid=uid;
//		 
//		}
	
//	public Device( String tracker_id,
//			 String name,
//			 String app,
//			 String last,
//			 String url,
//			 String where,
//			 String lat,
//			 String lon,
//			 String online,
//			 String state,
//			 String uid,
//			 String color,
//			 String ch){
//				Log.d(getClass().getSimpleName(), "tracker_id="+tracker_id+" "+"name="+name+"app="+app+" "+"last="
//			 +last+" "+"url="+url+" "+"where="+where+" "+"lat="+lat+" "+"lon="+lon+" "+"online="+state+" "+"uid="+uid+" ");
//				 this.tracker_id=tracker_id;
//				 this.name=name;
//				  this.app=app;
//				  this.last=last;
//				  this.url=url;
//				  this.where=where;
//				   try {
//					if (lat!=null){ this.lat=  Float.parseFloat(lat);}
//					   if (lon!=null){ this.lon=  Float.parseFloat(lon);}
//				} catch (NumberFormatException e) {
//					Log.d(getClass().getSimpleName(),e+" "+ e.getMessage());
//					e.printStackTrace();
//				}
//				  this.online=online;
//				  this.state=state;
//				  this.uid=uid;
//				  if (!color.equals("")){this.color=color;}
//				}
	
	

//	public Device(String tracker_id, String name, String online, String uid) {
//		 this.tracker_id=tracker_id;
//		 this.name=name;
//		 this.uid=uid;
//		 this.online=online;
//	}

	public Device(String trid, String name, String color) {
		this.name=name;
		this.tracker_id=trid;
		this.color=color;
	}

	@Override
	public String toString() {
		
		
		return "Device:tracker_id="+tracker_id+",name="+name+",app="+app+",last="+last+",url="+url+",where="+where+",lat="+lat+",lon="+lon
				+",online="+online+",state="+state+",uid="+uid + ",speed="+speed+" color="+color;
	}

	@Override
    public int compareTo(Device dev) {
            // TODO Auto-generated method stub
            return -dev.name.compareTo(this.name);
    }

}
