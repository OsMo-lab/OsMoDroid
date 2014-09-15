package com.OsMoDroid;

import org.json.JSONException;
import org.json.JSONObject;

public class MyMessage implements Comparable<MyMessage>{
	//"data": "{\"u\":33424,\"from\":\"173\",\"from_app\":0,\"for\":\"173\",\"for_app\":184,\"trig\":\"173-173\",\"trig_app\":\"0-184\",\
	//"text\":\"hjutrrr\",\"time\":\"2013-03-21 21:40:51\",\"readed\":\"0000-00-00 00:00:00\",\"from_name\":\"tox\",\"from_addr\":\"tox\"}"
	
	//{"u":"33369","from":"173","from_app":"0","for":"173","for_app":"184"
	//,"trig":"173-173","trig_app":"0-184","text":"32132132","time":"2013-03-14 21:16:51","readed":"2013-03-15 00:00:04"}
	public int u;
	public String from;
	public String from_app;
	public String for_user;
	public String for_app;
	public String trig;
	public String trig_app;
	public String text;
	public String time;
	public String readed;
	public String from_name;
	public String from_addr;
	public String to;
	
	public MyMessage (JSONObject jo){
		
			this.u=jo.optInt("u");
			this.from=jo.optString("from");
			this.from_app=jo.optString("from_app");
			this.for_user=jo.optString("for");
			this.for_app=jo.optString("for_app");
			this.trig=jo.optString("trig");
			this.trig_app=jo.optString("trig_app");
			this.text=jo.optString("text");
			this.time=jo.optString("time");
			this.readed=jo.optString("readed");
			this.from_name=jo.optString("from_name");
			this.from_addr=jo.optString("from_addr");
			this.to=jo.optString("to");
		
		if (from_name.equals("")||from_name.equals("null")){
			for (Device dev : LocalService.deviceList){
				if ((dev.tracker_id).equals(from)){
					from_name=dev.name;
				}
			}
		}
		
	}

	

	



	public int compareTo(MyMessage another) {
		// TODO Auto-generated method stub
		return this.u-another.u;
	}
	

}
