package com.OsMoDroid;

import java.io.Serializable;

import android.graphics.Color;

public class ChatMessage implements Comparable<ChatMessage>, Serializable{
//    u = 468
//    device = 5837
//    text = не ник аккаунта
//    time = 2014-04-18 18:38:22
	public int u;
	public int device;
	public String name;
	public String from;
	public String text;
	public String time;
	public int color;
	public ChatMessage(int u, int device,String from, String text, String time , int color) {
		
		this.u=u;
		this.device=device;
		this.from=from;
		this.text=text;
		this.time=time;
		this.color=color;
	}
	public ChatMessage() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean equals(Object o) {
		
		 if((o instanceof ChatMessage) && this.u == ((ChatMessage)o).u &&this.u!=0)  
	        {    
	            return true;    
	        }  
	        else  
	        {
	        	return false;  
	        }  
	}
	@Override
	public int compareTo(ChatMessage another) {
		
		return this.u-another.u;
	}
	
}
