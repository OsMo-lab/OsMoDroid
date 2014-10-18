package com.OsMoDroid;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.overlay.PathOverlay;

import com.OsMoDroid.ColoredGPX.Statuses;
import com.OsMoDroid.Netutil.InitTask;

import android.graphics.Color;
import android.location.Address;
import android.os.AsyncTask;
import android.provider.Settings.Global;
import android.util.Log;

public class Channel implements Serializable {
	
	public ArrayList<ColoredGPX> gpxList = new ArrayList<ColoredGPX>();
	public String name;
	public String description;
	public String myNameInGroup;
	public String general_id;
	public int u;
	public String created;
	public String group_id;
	public String url;
	public List<Device> deviceList= new ArrayList<Device>();
	public List<ChannelChatMessage> messagesstringList= new ArrayList<ChannelChatMessage>();
	//public List<PathOverlay> paths = new ArrayList<PathOverlay>();
	ArrayList<Point> pointList= new ArrayList<Channel.Point>();
	public boolean connected=false;
	public boolean send=false;
	LocalService localService;
	boolean active;
	//MapFragment map;
	 File sdDir = android.os.Environment.getExternalStorageDirectory();
	 File fileName = new File (sdDir, "OsMoDroid/channelsgpx/");
	
	ResultsListener gpxdownloadListener = new ResultsListener() {
	
		@Override
		public void onResultsSucceeded(APIComResult result) {
			Log.d(getClass().getSimpleName(),"download result="+result.load);
			result.load.initPathOverlay();
					}
	};
	
	public boolean chatconnected=false;
	class Point {
		int u;
		float lat;
		float lon;
		String name;
		String description; 
		String color;
		Point(JSONObject json) throws JSONException{
		u=json.getInt("u");
		lat=Float.parseFloat(json.getString("lat"));
		lon=Float.parseFloat(json.getString("lon"));
		description=json.getString("description");
		color=json.optString("color");
		name=json.getString("name");
		}
	}
	public Channel(){
		
	}
	
	
//	public void addtrack(ColoredGPX load){
//		if (!gpxList.contains(load)){
//			gpxList.add(load);
//			load.initPathOverlay();
//			
//			Log.d(getClass().getSimpleName(),"add download result to gpxlist="+gpxList.size());
//		}
//	}
	
//	public Channel( 
//	 String name,
//	 String u,
//	 String created){
//		 this.u=Integer.parseInt(u);
//		 this.name=name;
//		 this.created=created;
//		}
	//{"uid":"173","lon":"30","count":"0","zoom":"14","track":"0","code":"zxc","url":"ByIWINB22Fj74452is","main":"0","private":"0","u":"51","created":"2013-02-20 12:54:18","color":"0","name":"zxc","me":{"channel_name":"zxc","uid":"173","icon":"1","u":"19","channel_url":"ByIWINB22Fj74452is","added":"2013-02-24 13:40:27","color":"FFFFFF","name":"TEST2","device":"1436","active":"1","role":"0","channel":"51"},"ch":"omc_RAzHkQ9JzY5","user":[{"uid":"173","icon":"1","lon":"41.332442","u":"1436","color":"FFFFFF","name":"TEST2","state":"0","active":"1","role":"0","lat":"54.95365","online":"1"},{"uid":"173","icon":"1","lon":"37.685934","u":"40","color":"FFFFFF","name":"Денис","state":"0","active":"1","role":"0","lat":"55.682043","online":"1"}],"lat":"60","zone":"0"},{"uid":"173","lon":"30","count":"0","zoom":"14","track":"0","code":"123","url":"w2GjFV1BcviZz6qo83","main":"0","private":"0","u":"54","created":"2013-02-25 14:46:35","color":"0","name":"Каналья","me":{"channel_name":"Каналья","uid":"173","icon":"1","u":"21","channel_url":"w2GjFV1BcviZz6qo83","added":"2013-02-25 18:48:42","color":"FFFFFF","name":"TEST2","device":"1436","active":"1","role":"0","channel":"54"},"ch":"omc_qg9q1W06aB6","user":[{"uid":"173","icon":"1","lon":"41.332442","u":"1436","color":"FFFFFF","name":"TEST2","state":"0","active":"1","role":"0","lat":"54.95365","online":"1"}],"lat":"60","zone":"0"}]}
	
	public void updChannel(JSONObject jo, LocalService localService)
		{
			this.localService=localService;
			this.name=jo.optJSONObject("group").optString("name");
			this.u=Integer.parseInt(jo.optJSONObject("group").optString("u"));
			this.created=jo.optJSONObject("group").optString("created");
			this.group_id=jo.optJSONObject("group").optString("group_id");
			this.url="http://osmo.mobi/g/"+jo.optJSONObject("group").optString("url");

			JSONArray users =jo.optJSONArray("users");
			ArrayList<Device> recieveddeviceList= new ArrayList<Device>();
			for (int i = 0; i < users.length(); i++) 
				{
		 			JSONObject jsonObject;
					try {
						jsonObject = users.getJSONObject(i);
						try {
							recieveddeviceList.add(new Device(jsonObject.getString("group_tracker_id"),jsonObject.getString("name"), jsonObject.getString("color") ) );
						} catch (NumberFormatException e) {
							Log.d(getClass().getSimpleName(),"Wrong device info");
							e.printStackTrace();
						}
						
					} catch (JSONException e) {
						e.printStackTrace();
					}   
				}
			ArrayList<Device> deleteDeviceList= new ArrayList<Device>(this.deviceList);
			this.localService.myIM.addlog("reciveddevicelist:"+recieveddeviceList.toString());
			this.localService.myIM.addlog("deletedevicelist:"+deleteDeviceList.toString());
			this.localService.myIM.addlog("this.devicelist:"+this.deviceList.toString());
			deleteDeviceList.removeAll(recieveddeviceList);
			this.localService.myIM.addlog("reciveddevicelist:"+recieveddeviceList.toString());
			this.localService.myIM.addlog("deletedevicelist:"+deleteDeviceList.toString());
			this.localService.myIM.addlog("this.devicelist:"+this.deviceList.toString());
			recieveddeviceList.removeAll(this.deviceList);
			this.localService.myIM.addlog("reciveddevicelist:"+recieveddeviceList.toString());
			this.localService.myIM.addlog("deletedevicelist:"+deleteDeviceList.toString());
			this.localService.myIM.addlog("this.devicelist:"+this.deviceList.toString());
			this.localService.myIM.addlog("reciveddevicelist:"+recieveddeviceList.toString());
			String str = "";
			for (Device dev:deleteDeviceList)
				{
					str=str+"UL:"+dev.tracker_id+'=';
				}
			if(!str.equals(""))
				{
					localService.myIM.sendToServer(str);
				}
			this.deviceList.addAll(recieveddeviceList);
			this.localService.myIM.addlog("reciveddevicelist:"+recieveddeviceList.toString());
			this.localService.myIM.addlog("deletedevicelist:"+deleteDeviceList.toString());
			this.localService.myIM.addlog("this.devicelist:"+this.deviceList.toString());
			this.deviceList.removeAll(deleteDeviceList);
			this.localService.myIM.addlog("reciveddevicelist:"+recieveddeviceList.toString());
			this.localService.myIM.addlog("deletedevicelist:"+deleteDeviceList.toString());
			this.localService.myIM.addlog("this.devicelist:"+this.deviceList.toString());
			str="";
			for (Device dev:this.deviceList)
				{
					if(!dev.tracker_id.equals(OsMoDroid.settings.getString("device", ""))){
						str=str+"L:"+dev.tracker_id+'=';
					}
				}
			if(!str.equals(""))
				{
					localService.myIM.sendToServer(str);
				}
			Collections.sort(this.deviceList);
			
			 JSONArray points =jo.optJSONArray("point");
			 this.pointList.clear();
				for (int i = 0; i < points.length(); i++)
					{
			 			JSONObject jsonObject;
						try {
							jsonObject = points.getJSONObject(i);
							this.pointList.add(new Point(jsonObject));
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					}
				
				JSONArray tracks =jo.optJSONArray("track");
				ArrayList<ColoredGPX> recievedgpxList = new ArrayList<ColoredGPX>();
				for (int i = 0; i < tracks.length(); i++)
					{
						JSONObject jsonObject;
						try {
							jsonObject = tracks.getJSONObject(i);
							fileName.mkdirs();
							Log.d(getClass().getSimpleName(),"filename="+fileName);
							recievedgpxList.add(new ColoredGPX(jsonObject.getInt("u"), new File(sdDir, "OsMoDroid/channelsgpx/"+jsonObject.getString("u")+".gpx"), jsonObject.getString("color"), jsonObject.getString("url")));
							//this.downloadgpx(jsonObject.getString("url"), jsonObject.getString("u"),jsonObject.getString("color"));
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					
					}
				ArrayList<ColoredGPX> deletegpxList = new ArrayList<ColoredGPX>(gpxList);
				deletegpxList.removeAll(recievedgpxList);
				recievedgpxList.removeAll(gpxList);
				gpxList.addAll(recievedgpxList);
				gpxList.removeAll(deletegpxList);
				for(ColoredGPX cgpx: gpxList)
					{
					 if(cgpx.status==ColoredGPX.Statuses.EMPTY)
					 	{
						 cgpx.status=Statuses.DOWNLOADING;
						 Netutil.downloadfile(gpxdownloadListener, cgpx.url, cgpx);
					 	}	
					 else if (cgpx.status==ColoredGPX.Statuses.DOWNLOADED)
					 		
					 	{
					 		cgpx.initPathOverlay();
					 	}	
					}
				
		}
	
//	public Channel(JSONObject jo, LocalService localService)
//	{
//		this.localService=localService;
//		
//		this.name=jo.optJSONObject("group").optString("name");
//		this.u=Integer.parseInt(jo.optJSONObject("group").optString("u"));
//		this.created=jo.optJSONObject("group").optString("created");
//		this.group_id=jo.optJSONObject("group").optString("group_id");
//		this.url="http://osmo.mobi/g/"+jo.optJSONObject("group").optString("url");
//		JSONArray users =jo.optJSONArray("users");
//		for (int i = 0; i < users.length(); i++) 
//			{
//	 			JSONObject jsonObject;
//				try {
//					jsonObject = users.getJSONObject(i);
//					try {
//						this.deviceList.add(new Device(jsonObject.getString("group_tracker_id"),jsonObject.getString("name"), jsonObject.getString("color") ) );
//					} catch (NumberFormatException e) {
//						Log.d(getClass().getSimpleName(),"Wrong device info");
//						e.printStackTrace();
//					}
//					Collections.sort(this.deviceList);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}   
//			}
//
//		
//	
//		 
//		 JSONArray points =jo.optJSONArray("point");
//			for (int i = 0; i < points.length(); i++) {
//		 			JSONObject jsonObject;
//					try {
//						jsonObject = points.getJSONObject(i);
//						this.pointList.add(new Point(jsonObject));
//					}
//					catch (JSONException e) {
//						e.printStackTrace();
//					}
//					
//			}
//			JSONArray tracks =jo.optJSONArray("track");
//			for (int i = 0; i < tracks.length(); i++) {
//	 			JSONObject jsonObject;
//				try {
//					jsonObject = tracks.getJSONObject(i);
//	//				this.downloadgpx(jsonObject.getString("url"), jsonObject.getString("u"),jsonObject.getString("color"));
//				}
//				catch (JSONException e) {
//					e.printStackTrace();
//				}
//				
//		}
//
//				}
	
	@Override
	public boolean equals(Object o) {
		
		 if((o instanceof Channel) && this.u == ((Channel)o).u )  
	        {    
	            return true;    
	        }  
	        else  
	        {  
	            return false;  
	        }  
	}

		
//	public void downloadgpx(String url, String u, String color){
//		 File sdDir = android.os.Environment.getExternalStorageDirectory();
//			 File fileName = new File (sdDir, "OsMoDroid/channelsgpx/");
//			 fileName.mkdirs();
//			 fileName = new File(sdDir, "OsMoDroid/channelsgpx/"+u+".gpx");
//			 Log.d(getClass().getSimpleName(),"filename="+fileName);
//			 ColoredGPX load = new ColoredGPX(fileName,color);
//			 if(!fileName.exists())
//			 	{
//				 Netutil.downloadfile(gpxdownloadListener, url, load);
//			 	}	else
//			 	{
//			 		addtrack(load);
//			 	}
//	}
	
	@Override
	public String toString() {
		return name;
	}

	public void getPointList(JSONArray jsonArray) {
		pointList.clear();
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				pointList.add(new Point(jsonArray.getJSONObject(i)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}

	

}
