
package com.OsMoDroid;import java.io.BufferedReader;import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.IOException;import java.io.InputStream;import java.io.InputStreamReader;import java.io.PrintWriter;import java.net.HttpURLConnection;import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.InetSocketAddress;import java.net.Proxy;import java.net.URL;import java.net.UnknownHostException;import java.text.SimpleDateFormat;import java.util.ArrayList;import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Date;import java.util.Iterator;import java.util.Map.Entry;
import org.json.JSONArray;import org.json.JSONException;import org.json.JSONObject;import org.osmdroid.util.GeoPoint;

import com.OsMoDroid.IM.IMWriter;
import com.OsMoDroid.Netutil.MyAsyncTask;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;import android.app.PendingIntent;import android.app.PendingIntent.CanceledException;import android.content.BroadcastReceiver;import android.content.SharedPreferences;
import android.content.Context;import android.content.Intent;import android.content.IntentFilter;import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.NetworkInfo;import android.net.Uri;
import android.os.Bundle;import android.os.Environment;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Handler;import android.os.Message;import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;import android.telephony.TelephonyManager;
import android.util.Log;import android.widget.Toast;
/**
 * @author dfokin
 *Class for work with LongPolling
 */
public class IM implements ResultsListener {	
	private IMWriter iMWriter;
	private IMReader iMReader;	private static int RECONNECT_TIMEOUT = 1000*30;
	private static final int KEEP_ALIVE = 1000*270;
	private static final long ERROR_RECONNECT_TIMEOUT = 5*1000;
	private static final String RECONNECT_INTENT = "com.osmodroid.reconnect";
	private static final String GET_TOKEN_TIMEOUT_INTENT = "com.osmodroid.gettokentimeout";
	private static final String KEEPALIVE_INTENT = "com.osmodroid.keepalive";
	AlarmManager manager;
    PendingIntent reconnectPIntent;
    PendingIntent keepAlivePIntent;
    PendingIntent getTokenTimeoutPIntent;
	volatile protected  boolean running       = false;	//protected boolean autoReconnect = true;	//protected Integer timeout       = 0;	//String adr;	//String lcursor="";	//int pingTimeout=900;	Thread connectThread;	volatile private boolean gettokening=false;	Context parent;	private String token="";	//String myLongPollCh;
	int mestype=0;	LocalService localService;	FileOutputStream fos;
	ObjectOutputStream output = null;	final private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static String SERVER_IP;// = "osmo.mobi";
	static int SERVERPORT;// = 5757;
	volatile protected boolean connOpened=false;
	volatile protected boolean connecting=false;
	final boolean  log=true;
	public Socket socket;
	volatile public boolean authed=false;
	public BufferedReader rd;
	public PrintWriter wr;
	long sendBytes=0;
	long recievedBytes=0;
	volatile public boolean needopensession=false;
	volatile public boolean needclosesession=false;
	private Thread readerThread;
	private Thread writerThread;
	private int workserverint=-1;
	private String workservername="";
	private MyAsyncTask sendidtask;
	private ArrayList<String> ExecutedCommandArryaList = new ArrayList<String>();
	public IM(String server, int port, LocalService service){
		RECONNECT_TIMEOUT=Integer.parseInt(OsMoDroid.settings.getString("timeout", "30"))*1000;
		localService=service;
		parent=service;
		manager = (AlarmManager)(parent.getSystemService( Context.ALARM_SERVICE ));
		reconnectPIntent = PendingIntent.getBroadcast( parent, 0, new Intent(RECONNECT_INTENT), 0 );
		keepAlivePIntent = PendingIntent.getBroadcast( parent, 0, new Intent(KEEPALIVE_INTENT), 0 );
		getTokenTimeoutPIntent = PendingIntent.getBroadcast( parent, 0, new Intent(GET_TOKEN_TIMEOUT_INTENT), 0 );
		SERVER_IP=server;
		SERVERPORT=port;
		addlog("IM create");
		iMWriter=new IMWriter();
		writerThread = new Thread(iMWriter,"writer");
		writerThread.start();
//		if(!OsMoDroid.settings.getString("newkey", "").equals("")){
//		start();
//		}
	}
	public void sendToServer(String str)
		{
			Message msg =new Message();
			Bundle b =new Bundle();
			b.putString("write",str);
			msg.setData(b);
			if (iMWriter.handler!=null){
				String[] data = str.split("\\=");
				for (int index =0; index < data.length; index++)
					{
						if(data[index].contains("|"))
							{  
								data[index] = data[index].substring(0,data[index].indexOf('|'));
							}
					}
				ExecutedCommandArryaList.addAll(Arrays.asList(data));
				addlog("add to command order "+Arrays.asList(data));
				iMWriter.handler.sendMessage(msg);	
			}
			else {
				addlog("panic! handler is null ");
				 if(log)Log.d(this.getClass().getName(), " handler is null!!!");
			}
				
			
		}
	  BroadcastReceiver getTokenTimeoutReceiver = new BroadcastReceiver() {
          @Override public void onReceive( Context context, Intent _ )
          {
        		context.unregisterReceiver( this );
        		 if(log)Log.d(this.getClass().getName(), "gettoken timeout reciever trigged");
        		addlog("gettoken timeout reciever trigged");
        	  	sendidtask.cancel(true);
        	  	gettokening=false;
        	  	stop();
        	  	start();
        	   
          }
      };
	
	  BroadcastReceiver reconnectReceiver = new BroadcastReceiver() {
          @Override public void onReceive( Context context, Intent _ )
          {
        	  addlog("websocket reconnect reciever trigged");
        	  localService.alertHandler.post(new Runnable()
  			{
  				
  				@Override
  				public void run()
  					{
  						ondisconnect();
  						
  					}
  			});
        	  disablekeepAliveAlarm();
        	  

        	  stop();
        	  localService.alertHandler.post(new Runnable()
  				{
  					
  					@Override
  					public void run()
  						{
  							if(log)Log.d(this.getClass().getName(), "void IM.stop");
  							localService.internetnotify(false);
  						}
  				});
    			localService.refresh();
        	  start();
              context.unregisterReceiver( this ); 
          }
      };
      BroadcastReceiver keepAliveReceiver = new BroadcastReceiver() {
          @Override public void onReceive( Context context, Intent _ )
          {
              if(connOpened){
            	  addlog("websocket sendPing");
            	  if(log)Log.d(this.getClass().getName(), " send ping");
            	  sendToServer("P");
            	
              }
             
          }
      };
      
      void addlog(final String str){
    	  	localService.alertHandler.post(new Runnable()
				{
					
					@Override
					public void run()
						{
							//if(OsMoDroid.debug)ExceptionHandler.reportOnlyHandler(parent.getApplicationContext()).uncaughtException(Thread.currentThread(), new Throwable(str));
				    	  	if(OsMoDroid.debug)
				    	  		{
				    	  			LocalService.debuglist.add( sdf1.format(new Date(System.currentTimeMillis()))+" "+str+" S="+sendBytes+ " R="+recievedBytes);
				    	  			if(LocalService.debuglist.size()>1500)
				    	  				{
				    	  					LocalService.debuglist.remove(0);
				    	  				}
				    	  		}
				  			if(LocalService.debugAdapter!=null){LocalService.debugAdapter.notifyDataSetChanged();}
							
						}
				});
    	  	
    	  
      }
      
      
     
	 
      public void setkeepAliveAlarm(){
    	  if(log)Log.d(this.getClass().getName(), "void setKeepAliveAlarm");
    	  addlog("websocket void setkeepalive");
    	  parent.registerReceiver(keepAliveReceiver, new IntentFilter(KEEPALIVE_INTENT));
    	  manager.cancel(keepAlivePIntent);
    	  manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+KEEP_ALIVE, KEEP_ALIVE, keepAlivePIntent);
      }
      
      public void disablekeepAliveAlarm(){
    	  if(log)Log.d(this.getClass().getName(), "void disableKeepAliveAlarm");
    	  addlog("websocket void disablekeepalive");
    	  try {
			parent.unregisterReceiver(keepAliveReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	  manager.cancel(keepAlivePIntent);
      }
      
    synchronized  public void setReconnectAlarm() 
	    {	
    	  if(log)Log.d(this.getClass().getName(), "void setReconnectAlarm");
    	  
    	  localService.alertHandler.post(new Runnable(){
				 @Override
				public void run()
					{
						addlog("websocket setReconnectAlarn");
						
					}
			 });
    	  parent.registerReceiver( reconnectReceiver, new IntentFilter(RECONNECT_INTENT) );
    	  manager.cancel(reconnectPIntent);
    	  manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, reconnectPIntent );
	    }
	

	
	
	
				public void addtoDeviceChat(String message) {String u = "";			try {
				MyMessage mes =new MyMessage( new JSONObject(message));
				if(log)Log.d(this.getClass().getName(), "MyMessage,from "+mes.from);
				if(log)Log.d(this.getClass().getName(), "DeviceList= "+LocalService.deviceList);
if (mes.from.equals(OsMoDroid.settings.getString("device", ""))){
	for (Device dev : LocalService.deviceList){
		if((dev.tracker_id).equals(mes.to)){
			u=dev.tracker_id;
		}
	}
} else {
				for (Device dev : LocalService.deviceList){
					if((dev.tracker_id).equals(mes.from)){
						u=dev.tracker_id;
					}
				}
}		
				
				if (LocalService.currentDevice!=null&& u ==LocalService.currentDevice.tracker_id){
					boolean contains = false;
					for (MyMessage mes1: LocalService.chatmessagelist){
						
						
						if(mes1.u==mes.u){
							 contains = true;
							
						}
						
					}
					if (!contains){
						LocalService.chatmessagelist.add(mes);
						Collections.sort(LocalService.chatmessagelist);
						
						localService.alertHandler.post(new Runnable(){
							public void run() {
								if (LocalService.chatmessagesAdapter!=null){
									LocalService.chatmessagesAdapter.notifyDataSetChanged();
								}
							}
						});
						
						
					}
				
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!u.equals("")){
			Message msg = new Message();			Bundle b = new Bundle();			b.putString("deviceU", u);			msg.setData(b);			localService.alertHandler.sendMessage(msg);		}			}	private BroadcastReceiver bcr = new BroadcastReceiver() {		@Override		public void onReceive(Context context, Intent intent) {			addlog("network broadcast recive");		//	if(log)Log.d(this.getClass().getName(), "BCR"+this);		//	if(log)Log.d(this.getClass().getName(), "BCR"+this+" Intent:"+intent);			if (intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION)) {				Bundle extras = intent.getExtras();			//	if(log)Log.d(this.getClass().getName(), "BCR"+this+ " "+intent.getExtras());				if(extras.containsKey("networkInfo")) {					NetworkInfo netinfo = (NetworkInfo) extras.get("networkInfo");				//	if(log)Log.d(this.getClass().getName(), "BCR"+this+ " "+netinfo);				//	if(log)Log.d(this.getClass().getName(), "BCR"+this+ " "+netinfo.getType());					if(netinfo.isConnected()) {						if(log)Log.d(this.getClass().getName(), "BCR Network is connected");						if(log)Log.d(this.getClass().getName(), "Running:"+running);						// Network is connected
						addlog("webcoket Network is connected, running="+running);						if(!running ) {							//SetAlarm();
							start();
							addlog("webcoket start by broadcast because no running");
						}											}					else {						if(log)Log.d(this.getClass().getName(), "BCR Network is not connected");						if(log)Log.d(this.getClass().getName(), "Running:"+running);
						addlog("webcoket Network is not connected, running="+running);						if (running)
						{							addlog("webcoket stop by broadcast because running");
							localService.alertHandler.post(new Runnable()
								{
									
									@Override
									public void run()
										{
											if(log)Log.d(this.getClass().getName(), "void IM.stop");
											localService.internetnotify(false);
										}
								});
							stop();
						}
											}				}				else if(extras.containsKey("noConnectivity")) {					if(log)Log.d(this.getClass().getName(), "BCR Network is noConnectivity");					if(log)Log.d(this.getClass().getName(), "Running:"+running);
					addlog("webcoket Network is not connected, running="+running);					if (running)
					{						addlog("webcoket stop by broadcast because running");
						localService.alertHandler.post(new Runnable()
							{
								
								@Override
								public void run()
									{
										if(log)Log.d(this.getClass().getName(), "void IM.stop");
										localService.internetnotify(false);
									}
							});
						stop();
					}
									}		    }		}	};
	
	
	

	

	

	


	





	 /**
	 * Выключает IM
	 */
	void close(){		sendToServer("=BYE");
		if(log)Log.d(this.getClass().getName(), "void IM.close");
		addlog("webcoket void close");
		try {
			parent.unregisterReceiver(bcr);
		} catch (Exception e) {
		
			e.printStackTrace();
		}
		try {
	    	  parent.unregisterReceiver( reconnectReceiver);
		}
		catch (Exception e) {
			
		}
		try {
	    	  parent.unregisterReceiver( keepAliveReceiver);
		}
		catch (Exception e) {
			
		}
		try {
	    	  parent.unregisterReceiver( getTokenTimeoutReceiver);
		}
		catch (Exception e) {
			
		}		stop();	};
	public void gettoken()
	
	{
		addlog("Start gettoket"+", key="+OsMoDroid.settings.getString("newkey", ""));
		if(!gettokening){
		gettokening=true;
		if(log)Log.d(getClass().getSimpleName(), "http://api.osmo.mobi/prepare"+", key="+OsMoDroid.settings.getString("newkey", ""));
		APIcomParams params = null;    
		//APIcomParams params = new APIcomParams("http://api.osmo.mobi/prepare","key="+OsMoDroid.settings.getString("newkey", "")+"&protocol=1","gettoken");
		//{"android_id":"660d7b862282066f","android_model":"HTC One S","imei":"0","android_product":"htc_europe","client":"OsmAnd~ 1.8.3","osmand":"OsmAnd~ 1.8.3"}
	        if(OsMoDroid.settings.getString("p", "").equals("")){
	        	params = new APIcomParams("http://api.osmo.mobi/prepare?key="+OsMoDroid.settings.getString("newkey", "")
	        			+"&protocol=1"+"&app=OsMoDroid"+"&version="+localService.getversion()
	        			+"&android_model="+localService.getDeviceName()
	        			+"&android_product"+android.os.Build.PRODUCT,"","gettoken");
	        }
	        else
	        {
	        	params = new APIcomParams("http://api.osmo.mobi/prepare?key="+OsMoDroid.settings.getString("newkey", "")
	        			+"&protocol=1&auth="+OsMoDroid.settings.getString("p", "")
	        			+"&app=OsMoDroid"+"&version="+localService.getversion()
	        			+"&android_model="+localService.getDeviceName()
	        			+"&android_product"+android.os.Build.PRODUCT,"","gettoken");	
	        }
	        sendidtask = new Netutil.MyAsyncTask(this);
	        
			sendidtask.execute(params) ;
	        Log.d(getClass().getSimpleName(), "gettoken start to execute");
	        parent.registerReceiver(getTokenTimeoutReceiver,new IntentFilter(GET_TOKEN_TIMEOUT_INTENT));
	        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + RECONNECT_TIMEOUT, getTokenTimeoutPIntent );
		}

	}
	
		 void start(){		if(log)Log.d(this.getClass().getName(), "void IM.start");
		addlog("webcoket void start");
		running = true;		connecting=true;
		localService.refresh();
	
		iMReader=new IMReader();
		
		connectThread = new Thread(new IMConnect(),"connecter");
		readerThread = new Thread(iMReader,"reader");
		connectThread.setPriority(Thread.MIN_PRIORITY);
		readerThread.setPriority(Thread.MIN_PRIORITY);
		writerThread.setPriority(Thread.MIN_PRIORITY);
		gettoken();
		
		
		parent.registerReceiver(bcr, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		  }
	 public class IMWriter implements Runnable{
		 boolean error=false;
		
		 public Handler handler;
		@Override
		public void run()
			
			{
				
				 Looper.prepare();
				   
				    handler = new Handler(){

						@Override
						public void handleMessage(Message msg)
							{
								Bundle b = msg.getData();
								if(running){
									if (socket!=null&&socket.isConnected()&&wr!=null){
										 setReconnectAlarm(); 
										 try
												{
													
													Thread.sleep(0);
												} catch (InterruptedException e)
												{
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
										 wr.println('='+b.getString("write"));
										 
										 error=wr.checkError();
										
										 if(log)Log.d(this.getClass().getName(), "wr write "+b.getString("write")+" error="+error);
										 addlog("wr write "+b.getString("write")+" error="+error);
										 if(error){
											 if(running){setReconnectOnError();}
											 //Looper.myLooper().quit();
										 }
										 else{
											 sendBytes=sendBytes+b.getString("write").getBytes().length;
										 }
										 }
								}
								else {
									addlog("not connected now");
								}
							
								super.handleMessage(msg);
							}
				    	
				    };
				  
				    Looper.loop();
				    
			
	 }
	 
	
	 }
	 private class IMReader implements Runnable{
		
		private StringBuilder stringBuilder = new StringBuilder(1024);
		private String str;
		 @Override
		public void run()
			{
				
					while (connOpened && !Thread.currentThread().isInterrupted()){
						 try
							{
								stringBuilder.setLength(0);
								int c = 0;
								int i=0;
								while (!(c==10)  && !Thread.currentThread().isInterrupted()) {
								c = rd.read();
								if (!(c==-1))
									{stringBuilder.append((char) c);
								} 
								else {
									 if(log)Log.d(this.getClass().getName(), "inputstream c=-1 ");
									 addlog("inputstream c=-1 ");
									 setReconnectOnError();
									 break;
								}
								i=i+1;
								}
								if (stringBuilder.length()!=0&&connOpened){
									str=stringBuilder.toString();
									recievedBytes=recievedBytes+str.getBytes().length;
									Message msg =new Message();
									Bundle b =new Bundle();
									b.putString("read",str);
									msg.setData(b);
									if (localService.alertHandler!=null){
										localService.alertHandler.sendMessage(msg);	
									}
									else {
										addlog("panic!alert handler is null ");
										 if(log)Log.d(this.getClass().getName(), " alert handler is null!!!");
									}
								
								
								
								}
							} catch (IOException e)
							{
								 if(running){setReconnectOnError();}
								e.printStackTrace();
							}
						
					
					 
					}
				}
				
			}
	 
	 
	 private class IMConnect implements Runnable {
		 
		 
		@Override
		public void run()
			{
				SocketAddress sockAddr;
				 try {
					 if(workservername.equals(""))
						 {
							 InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
							 sockAddr = new InetSocketAddress(serverAddr, SERVERPORT);
						 }
					 else
						 {
							 InetAddress serverAddr = InetAddress.getByName(workservername);
							 sockAddr = new InetSocketAddress(serverAddr, workserverint);
						 }
					 workserverint=-1;
					 workservername="";
					 socket=new Socket();
					 //socket.setTcpNoDelay(true);
					
					 //addlog("TCP_NODELAY="+Boolean.toString(socket.getTcpNoDelay()));
					socket.connect(sockAddr, 5000);
					 connOpened=true;
					 connecting=false;
					 
					 localService.alertHandler.post(new Runnable(){
						 @Override
						public void run()
							{
								addlog("TCP Connected");
								localService.refresh();
								
							}
					 });
					 setReconnectAlarm();
					 rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					 wr =new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"),true);
					  readerThread.start();
					 
					 
				 } catch (final Exception e1) {
					 e1.printStackTrace();
					 connecting=false;
					 setReconnectOnError();
					 localService.alertHandler.post(new Runnable(){
						 @Override
						public void run()
							{
								 addlog("could no conenct to socket"+e1.getMessage());
								 							}
					 });
					 
					
				 }
								
			}
		 
	 }
	 
	 void stop (){
		 if(log)Log.d(this.getClass().getName(), "void IM.stop");
		 addlog("webcoket void stop");
		 ExecutedCommandArryaList.clear();
		 running = false;
		 connOpened=false;
		 authed=false;
		 localService.alertHandler.post(new Runnable()
	  			{
	  				
	  				@Override
	  				public void run()
	  					{
	  						ondisconnect();
	  						
	  					}
	  			});

		 if(socket!=null){
		 try
			{
				socket.close();
			} catch (IOException e)
			{
				addlog("exeption close socket "+e.getMessage());
				e.printStackTrace();
			}
		 }
		 
		 manager.cancel(getTokenTimeoutPIntent);
		 manager.cancel(reconnectPIntent);		 

			}

	 	 

	private void addToChannelChat(String toParse, String topic) {
		if(log)Log.d(this.getClass().getName(), "type=chch");
		if(log)Log.d(this.getClass().getName(), "Сообщение в чат канала " + toParse);
		ChannelChatMessage m =new ChannelChatMessage();
		String fromDevice="Незнамо кто";
		//09-16 18:25:41.057: D/com.OsMoDroid.IM(1474):     "data": "0|40+\u041e\u043f\u0430\u0441\u043d\u043e +2013-09-16 22:25:44"
		//"data": "0|40|cxbcxvbcxvbcxvb|2013-03-14 22:42:34"
		String[] data = toParse.split("\\|");
		//if(log)Log.d(this.getClass().getName(), "data[0]=" + data[0] + " data[1]=" + data[1] + " data[2]=" + data[2]);
		String[] datanew = data[1].split("\\+");
		//if(log)Log.d(this.getClass().getName(), "datanew[0]=" + datanew[0] + " datanew[1]=" + datanew[1] + " datanew[2]=" + datanew[2]);
		for (final Channel channel : LocalService.channelList) {
			if(log)Log.d(this.getClass().getName(), "chanal nest" + channel.name);
			if (topic.equals(channel.group_id+"_chat")){
						
			
			for (Device device : channel.deviceList) {
				if(log)Log.d(this.getClass().getName(), "device nest" + device.name + " " + device.tracker_id);
				if (datanew[0].equals((device.tracker_id))) {
					if(log)Log.d(this.getClass().getName(), "Сообщение от устройства в канале " + device.toString());
					fromDevice = device.name;
				}
				if (datanew[0].equals(OsMoDroid.settings.getString("device", ""))){
					fromDevice=localService.getString(R.string.iam);
					if(log)Log.d(this.getClass().getName(), "Сообщение от устройства в канале от меня ");
				}
				if (datanew[0].equals("0")){
				fromDevice=localService.getString(R.string.observers);
				}
			}
			Intent intent =new Intent(localService, GPSLocalServiceClient.class).putExtra("channelpos", channel.u);
			intent.setAction("channelchat");
			PendingIntent contentIntent = PendingIntent.getActivity(localService,333,intent, PendingIntent.FLAG_CANCEL_CURRENT);
			Long when=System.currentTimeMillis();
		 	NotificationCompat.Builder notificationBuilder =new NotificationCompat.Builder(
					localService.getApplicationContext())
			    	.setWhen(when)
			    	.setContentText(channel.name+" "+fromDevice+": "+Netutil.unescape(datanew[1]))
			    	.setContentTitle("OsMoDroid")
			    	.setSmallIcon(android.R.drawable.ic_menu_send)
			    	.setAutoCancel(true)
			    	.setDefaults(Notification.DEFAULT_LIGHTS)
			    	.setContentIntent(contentIntent);
	
	if (!OsMoDroid.settings.getBoolean("silentnotify", false)){
			 notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE| Notification.DEFAULT_SOUND);
			    	}
				Notification notification = notificationBuilder.build();
				LocalService.mNotificationManager.notify(OsMoDroid.mesnotifyid, notification);
		
		if (LocalService.channelsmessagesAdapter!=null&& LocalService.currentChannel != null&&LocalService.currentChannel.u==channel.u&&LocalService.chatVisible ){
			LocalService.mNotificationManager.cancel(OsMoDroid.mesnotifyid);
		}
	
			
			
			
					channel.messagesstringList.clear();
					m.text=Netutil.unescape(datanew[1]);
					m.time=datanew[2];
					channel.messagesstringList.add(m);
					localService.alertHandler.post(new Runnable(){
						public void run() {
							if (LocalService.channelsmessagesAdapter!=null&& LocalService.currentChannel != null&&LocalService.currentChannel.u==channel.u ){
								LocalService.currentChannel.messagesstringList.addAll(channel.messagesstringList);
								LocalService.channelsmessagesAdapter.notifyDataSetChanged();
							}
						}
					});
			}
		}
	}

	
	synchronized void parseEx (String toParse){
		//addlog("recieve "+toParse);
		
		if(log)Log.d(this.getClass().getName(), "recive "+toParse);
	
		
		if(!running)
		{
			running=true;
		}
		
	if(toParse.equals("P|")){
		addlog("recieve pong");
		
		return;
		}
	JSONObject jsonObject;
	JSONObject jo = new JSONObject();
	JSONArray ja = new JSONArray();
	String c="";
	String d="";
	try
		{
			c = toParse.substring(0, toParse.indexOf('|'));
			d = toParse.substring(toParse.indexOf('|')+1);
		} catch (Exception e1)
		{
			c=toParse;
			
		}
	Iterator<String> comIter = ExecutedCommandArryaList.iterator();
	while (comIter.hasNext()) {
	   String str = comIter.next();
	   if(log)Log.d(this.getClass().getName(), "ExecutedListItem: "+str);
	   if(str.equals(c))
		   {
		   comIter.remove();
		   if(log)Log.d(this.getClass().getName(), "ExecutedListItem removed: "+str);
		   addlog("ExecutedListItem removed: "+str);
		   }
	   
	}
	if(ExecutedCommandArryaList.size()==0)
		{
			addlog("cancel recconect alarm - no commands in order");
			manager.cancel(reconnectPIntent);
		}
	
	try
		{
			
		 jo = new JSONObject(d);
		}
	catch (JSONException e) {
		try {
			if(log)Log.d(this.getClass().getName(), "не JSONO ");
			ja=new JSONArray(d);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			if(log)Log.d(this.getClass().getName(), "не JSONA ");
		} 
	}
	if(jo.has("error"))
	{
		final String str = jo.optString("error_description");
		Toast.makeText(localService, str, Toast.LENGTH_SHORT).show();
		
	}
	
	if(c.equals("NEED_AUTH")){
		sendToServer( "AUTH|{\"key\":\""+OsMoDroid.settings.getString("newkey", "")+"\"}");
	}
	if(c.contains("NEED_TOKEN")){
		sendToServer( "TOKEN|"+token);
	}
	
	
	if(c.equals("TOKEN")){
		if(!jo.has("error")){
			OsMoDroid.editor.putString("device", jo.optString("group_tracker_id"));
			OsMoDroid.editor.putString("tracker_id", jo.optString("tracker_id"));
			OsMoDroid.editor.commit();
			authed=true;
			if(needopensession){
				sendToServer("TRACKER_SESSION_OPEN");
			}
			if(needclosesession){
				sendToServer("TRACKER_SESSION_CLOSE");
			}
			if(OsMoDroid.gpslocalserviceclientVisible){
				sendToServer("MOTD");
			}
			if(LocalService.channelList.isEmpty())
			{
			sendToServer("GROUP_GET_ALL");
			}
			else
				{
					for (Channel ch : LocalService.channelList)
					{
						sendToServer("GROUP_CONNECT:"+ch.group_id);
					}
				}
			
			if(LocalService.deviceList.isEmpty()){
				sendToServer("DEVICE_GET_ALL");
			}
			else
				{
					String listen = "";
					for (Device dev : LocalService.deviceList){
						if(!dev.tracker_id.equals(OsMoDroid.settings.getString("tracker_id", "")))
						{
							listen=listen+("L:"+dev.tracker_id)+"=";
						}
						
					}
					if(!listen.equals(""))
						{
							sendToServer(listen);
						}
				}
			
			setkeepAliveAlarm();
			
			
			
			localService.internetnotify(true);
			
		}
		localService.refresh();
	}
	//GROUP_CREATE|{"u":247,"group_id":"IEIFLWQGHSQRBG","name":"Meps","policy":"","description":null}

	if(c.equals("GROUP_CREATE"))
	{
		
		sendToServer("GROUP_JOIN:"+jo.optString("group_id")+"|"+OsMoDroid.settings.getString("u", "Creator"));
	}
	
	if(c.equals("TRACKER_SESSION_OPEN")){
		localService.sessionstarted=true;
		sendBytes=0;
		recievedBytes=0;
		needopensession=false;
		OsMoDroid.editor.putString("viewurl","http://osmo.mobi/u/"+jo.optString("url"));
		OsMoDroid.editor.commit();
		localService.refresh();
	}
	else
		if(c.equals("TRACKER_SESSION_CLOSE")){
			localService.sessionstarted=false;
			needclosesession=false;
			OsMoDroid.editor.putString("viewurl","");
			OsMoDroid.editor.commit();
			localService.refresh();
			}
	
	if(c.equals("T")){
		localService.sendcounter++;
		localService.sending="";
		if (localService.sendsound && !localService.mayak) {
			localService.soundPool.play(localService.sendpalyer, 1f, 1f, 1, 0, 1f);
			localService.mayak = false;
		}
		String time = localService.sdf3.format(new Date(System.currentTimeMillis()));
		localService.sendresult = time + " " + localService.getString(R.string.succes);
		localService.refresh();
		return;
	}
	if(c.contains("REMOTE_CONTROL"))
	{
		if(d.equals("PP"))
		{
			sendToServer("PP");
		}
		if(d.equals("TRACKER_SESSION_START")){
			localService.startServiceWork();
		}
		if(d.equals("TRACKER_SESSION_STOP")){
			localService.stopServiceWork(false);
		}
		if(d.contains("TTS:")){
			if(localService.tts!=null){localService.tts.speak(d , TextToSpeech.QUEUE_ADD, null);}
		}
		if(d.equals("ALARM_ON"))
			{
				localService.playAlarmOn();
			}
		if(d.equals("ALARM_OFF"))
			{
				localService.playAlarmOff();
			}
		if(d.equals("SIGNAL_ON"))
			{
				localService.enableSignalisation();
			}
		if(d.equals("SIGNAL_OFF"))
			{
				localService.disableSignalisation();
			}
	}
	
	
	
	
	if(c.equals("MOTD")){
		localService.motd=LocalService.unescape(d);
		OsMoDroid.editor.putString("startmessage", LocalService.unescape(d));
		OsMoDroid.editor.commit();
		localService.refresh();
	}
	if(c.contains("GROUP_JOIN")){
		sendToServer("GROUP_CONNECT"+c.substring(c.indexOf(":")));
	}
	if(c.contains("SUBSCRIBE")&&!c.contains("UNSUBSCRIBE")){
		sendToServer("DEVICE_GET_ALL");
		//		sendToServer("L:"+c.substring(c.indexOf(':')+1, c.indexOf('|')-1));
		
	}
	if(c.contains("DEVICE_SET")){
		sendToServer("DEVICE_GET_ALL");
	}
	if(c.contains("UNSUBSCRIBE")){
		sendToServer("DEVICE_GET_ALL");
		//		sendToServer("UL:"+c.substring(c.indexOf(':')+1, c.length()));
//		
//				Iterator<Device> i = LocalService.deviceList.iterator();
//				while (i.hasNext()) {
//				   Device dev = i.next(); // must be called before you can call i.remove()
//				   if(dev.tracker_id.equals(c.substring(c.indexOf(':')+1, c.length())))
//				   {
//					   i.remove();   
//				   }
//				   
//				}
//		
//		  if(LocalService.deviceAdapter!=null)
//			{
//				LocalService.deviceAdapter.notifyDataSetChanged();
//			}
		
	}
	
	if(c.contains("DEVICE_GET_ALL")){
		String str="";
		Iterator<Device> i = LocalService.deviceList.iterator();
		while (i.hasNext()) {
		   Device dev = i.next(); // must be called before you can call i.remove()
		   boolean exist=false;
		   for (int k = 0; k < ja.length(); k++) {
	 			
				try {
					jsonObject = ja.getJSONObject(k);
					if(dev.u==jsonObject.optInt("u")){
						exist=true;
						dev.name=jsonObject.optString("name");
						dev.tracker_id=jsonObject.optString("tracker_id");
						dev.subscribed=jsonObject.has("subscribe");
						dev.u=jsonObject.optInt("u");
						dev.online=jsonObject.optInt("online");
						dev.state=jsonObject.optInt("state");
						if(jsonObject.has("data")){
							if (jsonObject.optJSONObject("data")!=null&&!jsonObject.optJSONObject("data").optString("color").equals("")){
								String color = jsonObject.optJSONObject("data").optString("color");
								dev.color=color;
								if(log)Log.d(this.getClass().getName(), "detected color "+color);
							}
						}
					}
					
//					LocalService.deviceList.add(dev);
//					if(!dev.tracker_id.equals(OsMoDroid.settings.getString("tracker_id", ""))){
//					str="=L:"+dev.tracker_id;
//					}
					
								}
				catch (Exception e) {
					e.printStackTrace();
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					addlog(exceptionAsString);
				}
				
				
		}
		   if(!exist){
			   i.remove();
			   str=str+"=UL:"+dev.tracker_id;
		   }
		   
		}
			for (int n = 0; n < ja.length(); n++) {
	 			
				try {
					jsonObject = ja.getJSONObject(n);
					boolean exist=false;
					Device newdev = new Device();
					newdev.name=jsonObject.optString("name");
					newdev.tracker_id=jsonObject.optString("tracker_id");
					newdev.subscribed=jsonObject.has("subscribe");
					newdev.u=jsonObject.optInt("u");
					newdev.online=jsonObject.optInt("online");
					newdev.state=jsonObject.optInt("state");
					if(jsonObject.has("data")){
						if (jsonObject.optJSONObject("data")!=null&&!jsonObject.optJSONObject("data").optString("color").equals("")){
							String color = jsonObject.optJSONObject("data").optString("color");
							newdev.color=color;
							if(log)Log.d(this.getClass().getName(), "detected color "+color);
						}
					}
					for (Device dev : LocalService.deviceList){
						if(newdev.u==dev.u){
							exist=true;
						}
					}
					if(!exist){
					LocalService.deviceList.add(newdev);
					if(!newdev.tracker_id.equals(OsMoDroid.settings.getString("tracker_id", ""))){
					str=str+"=L:"+newdev.tracker_id;
					}
					}
								}
				catch (Exception e) {
					e.printStackTrace();
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					addlog(exceptionAsString);
				}
				
				
		}
			if(!str.equals(""))
			{
				sendToServer(str);
			}
			Collections.sort(LocalService.deviceList);
			if(LocalService.deviceAdapter!=null)
			{
				LocalService.deviceAdapter.notifyDataSetChanged();
			
			}
			
						
	}
	
	if(c.contains("GROUP_CONNECT"))
		{
		if(!jo.has("error")&&jo.has("group")){
			String listen="";
			Channel ch = new Channel(jo, localService);
			
			if(!LocalService.channelList.contains(ch))
			{
				LocalService.channelList.add(ch);
				
			}
			else 
			{	
				 
				for(Device dev: LocalService.channelList.get(LocalService.channelList.indexOf(ch)).deviceList){
					if (!dev.tracker_id.equals(OsMoDroid.settings.getString("device", "")))
						{
					listen=listen+("UL:"+dev.tracker_id)+"=";
						}
					
				}
				
				LocalService.channelList.remove(ch);
				LocalService.channelList.add(ch);
			}
			addlog(ch.deviceList.toString());
			
					if (LocalService.channelsAdapter!=null )
					{
						LocalService.channelsAdapter.notifyDataSetChanged();
					}
					for(Device dev: ch.deviceList){
						if (!dev.tracker_id.equals(OsMoDroid.settings.getString("device", "")))
							{
								listen=listen+("L:"+dev.tracker_id)+"=";
							}
					}
					if(!listen.equals(""))
					{
						sendToServer(listen);
					}		
			}
		else 
		{
			Toast.makeText(localService, "No group", Toast.LENGTH_SHORT).show();
		}
		//localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
		}
	if(c.contains("GROUP_GET_ALL"))
	{
		LocalService.channelList.clear();
		String str="";
			for (int i = 0; i < ja.length(); i++) {
	 			
				try {
					jsonObject = ja.getJSONObject(i);
					if(!jsonObject.getString("group_id").equals("null"))
					{
					str =str+"=GROUP_CONNECT:"+jsonObject.getString("group_id"); 
					}
					
								}
				catch (Exception e) {
					e.printStackTrace();
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					addlog(exceptionAsString);
				}
				
				
		}
			if(!str.equals(""))
			{
				sendToServer(str);
			}
			
						LocalService.channelsAdapter.notifyDataSetChanged();
		}
	if(c.contains("GROUP_LEAVE"))
		{
			Channel chToDel=null;
			for(Channel ch:LocalService.channelList)
			{
				if(ch.group_id.equals(c.substring(c.indexOf(":")+1, c.length())))
				{
					chToDel=ch;
					
				}
			}
			if(chToDel!=null)
			{
				LocalService.channelList.remove(chToDel);
			}
			
						LocalService.channelsAdapter.notifyDataSetChanged();
			
			//localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
		}
	// recive LINK_GET_ALL|[{"u":"962","uid":"0","device":"7665","url":"LAvP1jaqPaJtvs4jfGWeC5el","general_id":"k4bMooJU9AFam8fproUX","from":"0000-00-00 00:00:00","to":"0000-00-00 00:00:00","created":"2014-08-14 23:39:11","limit":"-1","until":"0","active":"1"},{"u":"963","uid":"0","device":"7665","url":"gZ012cpDOrL1gGT0tjQMFu2G","general_id":"uqupQcQ2pC8H820LAX4S","from":"0000-00-00 00:00:00","to":"0000-00-00 00:00:00","created":"2014-08-14 23:57:39","limit":"-1","until":"0","active":"1"},{"u":"964","uid":"0","device":"7665","url":"u3KO0mEDmTrKmLhFU1COh6Lk","general_id":"pRICSHb7UXsTeCkTcaf2","from":"0000-00-00 00:00:00","to":"0000-00-00 00:00:00","created":"2014-08-14 23:59:04","limit":"-1","until":"0","active":"1"},{"u":"965","uid":"0","device":"7665","url":"xikg52FsZhUaGK9u9sKKPY4u","general_id":"PVRbAiIhLhXzGnCfA62G","from":"0000-00-00 00:00:00","to":"0000-00-00 00:00:00","created":"2014-08-14 23:59:06","limit":"-1","until":"0","active":"1"},{"u":"966","uid":"0","device":"7665","url":"UiGxNaKc19UpCvq2CFuk6xhl","general_id":"Ir86ShJU1qxdQxMWWMG0","from":"0000-00-00 00:00:00","to":"0000-00-00 00:00:00","created":"2014-08-14 23:59:07","limit":"-1","until":"0","active":"1"},{"u":"967","uid":"0","device":"7665","url":"AM3tXlP5U59B7KuTgjJ8QSr1","general_id":"tai6egJmMFyZMLtEdszU","from":"0000-00-00 00:00:00","to":"0000-00-00 00:00:00","created":"2014-08-14 23:59:07","limit":"-1","until":"0","active":"1"}]

	if(c.equals("LINK_GET_ALL"))
	{
		LocalService.simlimkslist.clear();
		for (int i = 0; i < ja.length(); i++) {
			
				try {
					jsonObject = ja.getJSONObject(i);
					PermLink pl = new PermLink();
					pl.u=jsonObject.getInt("u");
					pl.url="http://osmo.mobi/u/"+jsonObject.optString("url");
					LocalService.simlimkslist.add(pl);
				} catch (JSONException e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					String exceptionAsString = sw.toString();
					addlog(exceptionAsString);
					e.printStackTrace();
				}
		}
		if(LocalService.simlinksadapter!=null)
		{
			LocalService.simlinksadapter.notifyDataSetChanged();
		}
		
	}
	
	// recive LINK_DEL:977|1
	if(c.contains("LINK_DEL"))
	{
		int positiontodel=-1;
		for (PermLink pl : LocalService.simlimkslist)
		{
			
			if(Integer.parseInt(c.substring(c.indexOf(":")+1, c.length()))==pl.u)
			{
				positiontodel=LocalService.simlimkslist.indexOf(pl);
			}
		}
		if(positiontodel!=-1)
		{
			LocalService.simlimkslist.remove(positiontodel);
			if(LocalService.simlinksadapter!=null)
			{
			LocalService.simlinksadapter.notifyDataSetChanged();
			}
		}
	}
	
	if(c.contains("LINK_ADD"))
	{
		PermLink pl = new PermLink();
		try {
			pl.u=jo.getInt("u");
			pl.url="http://osmo.mobi/u/"+jo.getString("url");
			LocalService.simlimkslist.add(pl);
			if(LocalService.simlinksadapter!=null)
			{
				LocalService.simlinksadapter.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			addlog(exceptionAsString);
		}
		
		
		
	}
	
	

	//GP:MT|{"users":[{"name":"Dddddd","group_tracker_id":"WSlRasAgyD","color":"#ff9900"}]}
		//GP:MT|{"users":[{"name":"Dddddd","group_tracker_id":"WSlRasAgyD","deleted":"yes"}]}
		if (c.contains("GP:"))
		{
			for (Channel ch : LocalService.channelList)
			{
				if(ch.group_id.equals(c.substring(c.indexOf(":")+1, c.length())))
				{
					 JSONArray a =jo.optJSONArray("users");
						for (int i = 0; i < a.length(); i++) {
					 			
								try {
									jsonObject = a.getJSONObject(i);
						try {
							if(jsonObject.has("deleted"))
							{
								Device deviceToDel = null;
								for (Device dev : ch.deviceList){
									if(dev.tracker_id.equals(jsonObject.opt("group_tracker_id"))&&!jsonObject.opt("group_tracker_id").equals(OsMoDroid.settings.getString("device", "")))
									{
										sendToServer("UL:"+dev.tracker_id);
										deviceToDel=dev;
									}
								}
								if(deviceToDel!=null)
								{
									ch.deviceList.remove(deviceToDel);
								}
							}
							else 
							{
								boolean exist=false;
								for (Device dev : ch.deviceList){
									if(dev.tracker_id.equals(jsonObject.opt("group_tracker_id")))
									{
										exist=true;
									}
								}
								if(!exist&&!jsonObject.opt("group_tracker_id").equals(OsMoDroid.settings.getString("device", "")))
								{
									try {
										ch.deviceList.add(new Device(jsonObject.getString("group_tracker_id"),jsonObject.getString("name"), jsonObject.getString("color") ) );
										sendToServer("L:"+jsonObject.getString("group_tracker_id"));
									} catch (JSONException e) {
										StringWriter sw = new StringWriter();
										e.printStackTrace(new PrintWriter(sw));
										String exceptionAsString = sw.toString();
										addlog(exceptionAsString);
										e.printStackTrace();
									}
								}
							}
						} catch (NumberFormatException e) {
							Log.d(getClass().getSimpleName(),"Wrong device info");
							e.printStackTrace();
							StringWriter sw = new StringWriter();
							e.printStackTrace(new PrintWriter(sw));
							String exceptionAsString = sw.toString();
							addlog(exceptionAsString);
						}
					
						
					} catch (JSONException e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String exceptionAsString = sw.toString();
						addlog(exceptionAsString);
						e.printStackTrace();
					}   
						

					//channelList.add(chanitem);
//					netutil.newapicommand((ResultsListener)serContext, "om_channel_user:"+chanitem.u);


					 		 }
					
					
				}
			}
			if(LocalService.channelsDevicesAdapter!=null)
			{
				LocalService.channelsDevicesAdapter.notifyDataSetChanged();
			}
			//localService.saveObject(LocalService.channelList, OsMoDroid.CHANNELLIST);
		}
	//LT:fI8qCrlvw6j0dEKZtB9h|L59.252465:30.324515S20.3A124.3H2.5C235
	if(c.length()>2&&c.substring(0, 2).contains("LT"))
		
		{
		for (Channel ch : LocalService.channelList)
			{
			for (final Device dev : ch.deviceList){
			updateCoordinates(c, d, dev);
				}
			}
		for (Device dev: LocalService.deviceList)
		{
			updateCoordinates(c, d, dev);
		}
		if (LocalService.deviceAdapter!=null)
			{
				LocalService.deviceAdapter.notifyDataSetChanged();
			}
		if (LocalService.channelsDevicesAdapter!=null&&LocalService.currentChannel!=null)
		{
			 if(log)Log.d(this.getClass().getName(), "Adapter:"+ LocalService.channelsDevicesAdapter.toString());
			 LocalService.channelsDevicesAdapter.notifyDataSetChanged();
		}
		}

	
	}
	private void updateCoordinates(String c, String d, final Device dev) {
		if (c.substring(c.indexOf(":")+1, c.length()).equals(dev.tracker_id)){
			
			dev.lat=Float.parseFloat(d.substring(d.indexOf("L")+1, d.indexOf(":")));
			for (int i = d.indexOf(":")+1; i <= d.length(); i++) {
				if(!(d.charAt(i)=='-')&&!Character.isDigit(d.charAt(i))){
					if(!Character.toString(d.charAt(i)).equals(".")){
						dev.lon=Float.parseFloat(d.substring(d.indexOf(":")+1, i));
						dev.updatated=System.currentTimeMillis();
						break;
					}
				}
			
			}
			for (int i = d.indexOf("S")+1; i <= d.length(); i++) {
				if(!Character.isDigit(d.charAt(i))){
					if(!Character.toString(d.charAt(i)).equals(".")){
						dev.speed=LocalService.df0.format((((Float.parseFloat(d.substring(d.indexOf("S")+1, i))*3.6))));
						break;
					}
				}
			
			}
			
			
				localService.alertHandler.post(new Runnable() {
					
					@Override
					public void run() {
						if(LocalService.devlistener!=null)
						{
						dev.devicePath.add(new GeoPoint(dev.lat, dev.lon));
						LocalService.devlistener.onDeviceChange(dev);
						}
					}
				});
				
				
			}
	}

	void ondisconnect(){
		
	}
	
	private void setReconnectOnError()
		{	
			
			try
				{
					if(socket!=null){socket.close();}
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			disablekeepAliveAlarm();
			authed=false;
			connecting=false;
			connOpened=false;
			localService.alertHandler.post(new Runnable()
				{
					
					@Override
					public void run()
						{
							localService.internetnotify(false);
						}
				});
			running=false;
			localService.refresh();
			if(localService.isOnline()){
				 
		    	  
		    	  localService.alertHandler.post(new Runnable(){
						 @Override
						public void run()
							{
								addlog("setReconnectAlarm on error");
								
							}
					 });
		    	  parent.registerReceiver( reconnectReceiver, new IntentFilter(RECONNECT_INTENT) );
		    	  manager.cancel(reconnectPIntent);
		    	  manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ERROR_RECONNECT_TIMEOUT, reconnectPIntent );
			 }
		}

	@Override
	public void onResultsSucceeded(APIComResult result) {
		gettokening=false;
		manager.cancel(getTokenTimeoutPIntent);
		if(log)Log.d(getClass().getSimpleName(),"OnResultSucceded "+result.rawresponse);
		if(result.Command.equals("gettoken")&&!(result.Jo==null)){
			addlog("Recieve token "+result.Jo.toString());
			if(log)Log.d(getClass().getSimpleName(),"gettoken response:"+result.Jo.toString());
			//Toast.makeText(localService,result.Jo.optString("state")+" "+ result.Jo.optString("error_description"),5).show();
			if(result.Jo.has("token")){
				try {
					token=result.Jo.getString("token");
					workservername=result.Jo.optString("address").substring(0, result.Jo.optString("address").indexOf(':'));
					workserverint=Integer.parseInt(result.Jo.optString("address").substring( result.Jo.optString("address").indexOf(':')+1));
					connectThread.start();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if(result.Jo.has("error")&&result.Jo.optInt("error")==100){
				localService.alertHandler.post(new Runnable()
					{
						
						@Override
						public void run()
							{
								if(log)Log.d(this.getClass().getName(), "void IM.stop");
								localService.internetnotify(false);
							}
					});
				stop();
				localService.sendid();
				}
				else
				{
					close();
					localService.notifywarnactivity(LocalService.unescape(result.Jo.optString("error_description")), false);
					localService.motd=LocalService.unescape(result.Jo.optString("error_description"));
					localService.refresh();
					
				}
			}
			
		} else {
			addlog("Recieve token error - shall reconnecting ");
			setReconnectOnError();
		}
		
	}}
