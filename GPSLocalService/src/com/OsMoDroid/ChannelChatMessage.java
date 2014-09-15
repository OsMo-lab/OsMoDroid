package com.OsMoDroid;

import android.graphics.Color;

public class ChannelChatMessage {
//    u = 468
//    device = 5837
//    text = не ник аккаунта
//    time = 2014-04-18 18:38:22
	public int u;
	public int device;
	public String from;
	public String text;
	public String time;
	public int color;
	public ChannelChatMessage(int u, int device,String from, String text, String time , int color) {
		
		this.u=u;
		this.device=device;
		this.from=from;
		this.text=text;
		this.time=time;
		this.color=color;
	}
	public ChannelChatMessage() {
		// TODO Auto-generated constructor stub
	}
	
}
