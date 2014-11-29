package com.OsMoDroid;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.OsMoDroid.MapFragment.MAPSurferTileSource;


public class MapFragment extends Fragment implements DeviceChange, IMyLocationProvider,LocationListener {
		Handler mHandler = new Handler();
		private Runnable mRunnable = new Runnable()
			{
				@Override
				public void run() {
					if(mMapView!=null)
						{
							mMapView.invalidate();
						}
					mHandler.postDelayed(mRunnable, 5000);
				}
			};
		ResourceProxyImpl mResourceProxy;
	 	MapView mMapView;
		private IMapController mController;
		private MyLocationNewOverlay myLoc;
		private PathOverlay myTracePathOverlay;
		private GPSLocalServiceClient globalActivity;
		//private View view;
		boolean rotate=false;
		//private Context context;
		//ArrayList<PathOverlay> paths = new ArrayList<PathOverlay>();
		private IMyLocationConsumer myLocationConumer;
		private long lastgpslocation=0;
		private MenuItem courserotation;
		private MAPSurferTileSource mapSurferTileSource;
		private BingMapTileSource bingTileSource;
		private MAPSurferTileSource mapSurferTileSourceZ;
		private ITileSource sputnikTileSource;
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			MenuItem traces = menu.add(0, 1, 0, R.string.showtraces);
			MenuItem rotation = menu.add(0, 2, 0, R.string.enable_manual_rotation);
			courserotation = menu.add(0, 3, 0, R.string.enable_course_rotation);
			traces.setCheckable(true);
			rotation.setCheckable(true);
			courserotation.setCheckable(true);
			traces.setChecked(OsMoDroid.settings.getBoolean("traces", true));
			rotation.setChecked(OsMoDroid.settings.getBoolean("rotation", false));
			courserotation.setChecked(rotate);
			SubMenu menu2 = menu.addSubMenu(Menu.NONE, 4, 4, R.string.map);
			MenuItem mapsurfer = menu2.add(0, 5, 1, "MapSurfer");
			MenuItem mapnik = menu2.add(0, 6, 2, "Mapnik");
			MenuItem bing = menu2.add(0, 7, 3, "Microsoft Bing");
			MenuItem binglabels = menu2.add(0, 8, 4, "Microsoft Bing with Labels");
			MenuItem mapsurferz = menu2.add(0, 9, 1, "MapSurfer ZOOM");
			MenuItem sputnik = menu2.add(0, 10, 1, "Sputnik");
			menu.add(0,11,1,R.string.size_of_point);
			super.onCreateOptionsMenu(menu, inflater);
			
		}



		
		
	@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case 1:
				item.setChecked(!item.isChecked());
				OsMoDroid.editor.putBoolean("traces", item.isChecked());
				OsMoDroid.editor.commit();
				mMapView.invalidate();
			break;
			case 2:
				item.setChecked(!item.isChecked());
				OsMoDroid.editor.putBoolean("rotation", item.isChecked());
				OsMoDroid.editor.commit();
				mMapView.invalidate();
			break;	
			case 3:
				item.setChecked(!item.isChecked());
				rotate=!rotate;
				mMapView.setMapOrientation(0);
				mMapView.invalidate();
			break;
			case 6:
				mMapView.setTileSource(TileSourceFactory.MAPNIK);
				LocalService.selectedTileSourceInt=1;
				break;
			case 5:
				mMapView.setTileSource(mapSurferTileSource);
				LocalService.selectedTileSourceInt=2;
				break;
			case 7:
				BingMapTileSource.retrieveBingKey(globalActivity);
				bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
				mMapView.setTileSource(bingTileSource);
				LocalService.selectedTileSourceInt=3;
				break;
			case 8:
				BingMapTileSource.retrieveBingKey(globalActivity);
				bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
				mMapView.setTileSource(bingTileSource);
				LocalService.selectedTileSourceInt=4;
				break;
			case 9:
				mMapView.setTileSource(mapSurferTileSourceZ);
				LocalService.selectedTileSourceInt=5;
				break;
			case 10:
				mMapView.setTileSource(sputnikTileSource);
				LocalService.selectedTileSourceInt=6;
				break;	
			case 11:
				 
					  LinearLayout layout = new LinearLayout(getActivity());
					  layout.setOrientation(LinearLayout.VERTICAL);
					  final SeekBar  input = new SeekBar(getActivity());
					  input.setMax(30);
					  input.setProgress(OsMoDroid.settings.getInt("pointsize", 8));
					  layout.addView(input);
					  AlertDialog alertdialog3 = new AlertDialog.Builder(getActivity())
								.setTitle(R.string.size_of_point)
								.setView(layout)
								.setPositiveButton(R.string.yes,
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,	int whichButton) {
												
													OsMoDroid.editor.putInt("pointsize", input.getProgress());
													OsMoDroid.editor.commit();
													mMapView.invalidate();
												
											}
										})
								.setNegativeButton(R.string.cancel,	new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int whichButton) 
									{
												}
										}).create();
						alertdialog3.show();
				break;
			default:
				break;
			}
			return super.onOptionsItemSelected(item);
		}





	@Override
     public void onCreate(Bundle savedInstanceState) {
		Log.d(getClass().getSimpleName(), "map oncreate"); 
		super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         //setRetainInstance(true);
         LocalService.devlistener=this;
         super.onCreate(savedInstanceState);
     }
	
	
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(getClass().getSimpleName(), "map onattach");	
		globalActivity = (GPSLocalServiceClient)activity;
		
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach() {
		LocalService.devlistener=null;
		mResourceProxy=null;
		mMapView=null;
		mController=null;
		myLoc.disableMyLocation();
		myLoc=null;
		myTracePathOverlay=null;
		globalActivity=null;
		Log.d(getClass().getSimpleName(), "map ondetach");
		super.onDetach();
	}
	@Override
	public void onDestroyView() {
		Log.d(getClass().getSimpleName(), "map ondestroyview");
		OsMoDroid.editor.putInt("centerlat", mMapView.getMapCenter().getLatitudeE6());
		OsMoDroid.editor.putInt("centerlon", mMapView.getMapCenter().getLongitudeE6());
		OsMoDroid.editor.putInt("zoom", mMapView.getZoomLevel());
		OsMoDroid.editor.putBoolean("isfollow", myLoc.isFollowLocationEnabled());
		OsMoDroid.editor.commit();
		//ch.map=null;
	
		super.onDestroyView();
	}
	
	
	class MAPSurferTileSource extends OnlineTileSourceBase {

		MAPSurferTileSource(String aName, string aResourceId, int aZoomMinLevel,
                        int aZoomMaxLevel, int aTileSizePixels,
                        String aImageFilenameEnding, String... aBaseUrl) {
                super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel,
                                aTileSizePixels, aImageFilenameEnding, aBaseUrl);
        }
		@Override
		public String getTileURLString(MapTile aTile) {
		//	x=710&y=381&z=10
			return getBaseUrl() + "x="+ aTile.getX() + "&y="
                    + aTile.getY()+ "&z="+aTile.getZoomLevel() ;
			
		}
}
	class SputnikTileSource extends OnlineTileSourceBase {

		SputnikTileSource(String aName, string aResourceId, int aZoomMinLevel,
                        int aZoomMaxLevel, int aTileSizePixels,
                        String aImageFilenameEnding, String... aBaseUrl) {
                super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel,
                                aTileSizePixels, aImageFilenameEnding, aBaseUrl);
        }
		@Override
		public String getTileURLString(MapTile aTile) {
		//	x=710&y=381&z=10
			return getBaseUrl() + aTile.getZoomLevel()+'/'+aTile.getX() +'/' + aTile.getY()+".png" ;
			
		}
}

public class CustomTileProvider extends MapTileProviderBasic {

    public CustomTileProvider(Context pContext) {
        this(new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
                TileSourceFactory.DEFAULT_TILE_SOURCE);
    }

    public CustomTileProvider(IRegisterReceiver pRegisterReceiver, INetworkAvailablityCheck aNetworkAvailablityCheck,
            ITileSource pTileSource) {
        super(pRegisterReceiver, aNetworkAvailablityCheck, pTileSource);

        mTileProviderList.set(0, new CustomMapTileFilesystemProvider(pRegisterReceiver, pTileSource));
    }

    @Override
    public void setTileSource(ITileSource aTileSource) {
        super.setTileSource(aTileSource);
    }
}

public class CustomMapTileFilesystemProvider extends MapTileFilesystemProvider {

    public CustomMapTileFilesystemProvider(IRegisterReceiver pRegisterReceiver, ITileSource pTileSource) {
        super(pRegisterReceiver, pTileSource, OpenStreetMapTileProviderConstants.ONE_DAY * 30);
    }

}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	



	@Override
	public void onPause() {
		Log.d(getClass().getSimpleName(), "map onpause");
		mMapView.getOverlays().remove(myLoc);
		myLoc.disableMyLocation();
		
		super.onPause();
	}





	@Override
	public void onResume() {
		Log.d(getClass().getSimpleName(), "map onResume");
		globalActivity.actionBar.setTitle(getString(R.string.map));
		mMapView.getOverlays().add(myLoc);
		myLoc.enableMyLocation();
		super.onResume();
	}





	@Override
	public void onStart() {
		Log.d(getClass().getSimpleName(), "map onstart");
		
		super.onStart();
	}





	@Override
	public void onStop() {
		Log.d(getClass().getSimpleName(), "map onstop");
		mHandler.removeCallbacks(mRunnable);
		super.onStop();
	}





	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(getClass().getSimpleName(), "map onviewcreated");
		mHandler.postDelayed(mRunnable, 5000);
//		if(!LocalService.channelsupdated&&!OsMoDroid.settings.getString("key", "").equals(""))
//		{
//			Log.d(getClass().getSimpleName(), "map request channels");
//			Netutil.newapicommand((ResultsListener)LocalService.serContext, (Context)getSherlockActivity(), "om_device_channel_adaptive:"+OsMoDroid.settings.getString("device", ""));
//		}
		
		super.onViewCreated(view, savedInstanceState);
	}





	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		Log.d(getClass().getSimpleName(), "map onviewstaterestored");
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			//SherlockFragmentActivity context = getSherlockActivity();
			
			Log.d(getClass().getSimpleName(), "map oncreateview");
			mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
			final String name = "MapSurfer";
			final int aZoomMinLevel=0; 
			final int aZoomMaxLevel=18;
			final int aTileSizePixels=256;
			final String aImageFilenameEnding = ".png";
			final String[] aBaseUrl=new String[] {"http://openmapsurfer.uni-hd.de/tiles/roads/"};
			final String[] sputnikURL = new String[] {"http://b.tiles.maps.sputnik.ru/"};
			bingTileSource = new BingMapTileSource(null);
			sputnikTileSource = new SputnikTileSource("Sputnik", string.unknown, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding,sputnikURL);
			mapSurferTileSource = new MAPSurferTileSource(name, string.unknown, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
			mapSurferTileSourceZ = new MAPSurferTileSource(name, string.unknown, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels*2, aImageFilenameEnding, aBaseUrl);
			View view = inflater.inflate(R.layout.map, container, false);
			RelativeLayout rl = (RelativeLayout)view.findViewById(R.id.relative);
			//mMapView = (MapView)view.findViewById(R.id.mapview);
			CustomTileProvider customTileProvider = new CustomTileProvider(getActivity());
			
			mMapView = new MapView(getActivity(), 256, mResourceProxy, customTileProvider);
			//<!--         android:layout_width="fill_parent" -->
			//<!--         android:layout_height="fill_parent" -->
			//<!--         android:layout_alignParentLeft="true" -->
			//<!--         android:layout_alignParentTop="true" > -->
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT	, RelativeLayout.LayoutParams.FILL_PARENT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mMapView.setLayoutParams(lp);
			rl.addView(mMapView,0);
			
			ImageButton centerImageButton = (ImageButton)view.findViewById(R.id.imageButtonCenter);
			Button rotateButton = (Button)view.findViewById(R.id.buttonRotate);
			switch (LocalService.selectedTileSourceInt) {
			case 1:
				mMapView.setTileSource(TileSourceFactory.MAPNIK);
				break;
			case 2:
				mMapView.setTileSource(mapSurferTileSource);
				break;
			case 3:
				bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
				mMapView.setTileSource(bingTileSource);
				break;
			case 4:
				bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
				mMapView.setTileSource(bingTileSource);
				break;
			case 5:
				mMapView.setTileSource(mapSurferTileSourceZ);
				break;
			case 6:
				mMapView.setTileSource(sputnikTileSource);
				break;
			default:
				break;
			}
			 
			
			
			
			if(myTracePathOverlay==null)
			{
				myTracePathOverlay=new PathOverlay(Color.RED, 10, mResourceProxy);
				myTracePathOverlay.addPoints(LocalService.traceList);
			} else
			{
				myTracePathOverlay.clearPath();
				myTracePathOverlay.addPoints(LocalService.traceList);
			}
	
			
			
			
            mMapView.getOverlays().add(myTracePathOverlay);
            
            myLoc = new MyLocationNewOverlay (getActivity(),this, mMapView);
            
            myLoc.setOptionsMenuEnabled(true);
            if(OsMoDroid.settings.getBoolean("isfollow", true))
            {
            myLoc.enableFollowLocation();
            }
            //mMapView.getOverlays().add(myLoc);
            mMapView.setBuiltInZoomControls(true);
            mMapView.setMultiTouchControls(true);
            mController = mMapView.getController();
            if(OsMoDroid.settings.getInt("centerlat", -1)!=-1){
            	new Handler(Looper.getMainLooper()).post(
            		    new Runnable() {
            		        public void run() {
            		        	if(mController!=null)
            		        		{
            		        			mController.setZoom(OsMoDroid.settings.getInt("zoom",10));
            		        			mController.animateTo(new GeoPoint(OsMoDroid.settings.getInt("centerlat", 0), OsMoDroid.settings.getInt("centerlon", 0)));
            		        		}
            		        }
            		    }
            		);
            	
            	//mController.setCenter(new GeoPoint(OsMoDroid.settings.getInt("centerlat", 0), OsMoDroid.settings.getInt("centerlon", 0)));
            	
            	
            } 
            else
            {
            	mController.setZoom(10);
            }
            centerImageButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Location center = MapFragment.this.getLastKnownLocation();
					if (center!=null){
					mController.setCenter(new GeoPoint(MapFragment.this.getLastKnownLocation()));
					mController.setZoom(16);
					}
					myLoc.enableFollowLocation();
					
				}
			});
           rotateButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
					mMapView.setMapOrientation(0);
					Log.d(getClass().getSimpleName(), "map click on compas");
					rotate=!rotate;
					courserotation.setChecked(rotate);
			}
		});
           		
			CompassOverlay compas = new CompassOverlay(getActivity(), mMapView);
			ChannelsOverlay choverlay = new ChannelsOverlay(mResourceProxy, mMapView);
			mMapView.getOverlays().add(choverlay);
			mMapView.getOverlays().add(compas);
			compas.enableCompass();
			mMapView.setKeepScreenOn(true);
			return view;
	}



	

	@Override
	public void onDeviceChange(Device dev) {
		 Log.d(getClass().getSimpleName(), "ondevicechange");
		 
		 mMapView.invalidate();
			
	}


	@Override
	public void onChannelListChange() {
		
		mMapView.invalidate();
		LocalService.channelsupdated=true;
		
	}


	@Override
	public Location getLastKnownLocation() {
		Location forcelocation = LocalService.myManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location forcenetworklocation = LocalService.myManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(forcelocation!=null){
			if (forcenetworklocation!=null){
				if(forcenetworklocation.getTime()>forcelocation.getTime()){
					return forcenetworklocation;
				}
			}
			return forcelocation;
		}
		else{
			return forcenetworklocation;	
		}
		
	}





	@Override
	public boolean startLocationProvider(IMyLocationConsumer locationConsumer) {
		 myLocationConumer=locationConsumer;
         boolean result = false;
         for (final String provider : LocalService.myManager.getProviders(true)) {
                 if (LocationManager.GPS_PROVIDER.equals(provider)
                                 || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                         result = true;
                         if(LocalService.myManager!=null)
                         {
                        	 LocalService.myManager.requestLocationUpdates(provider, 0,0, this);
                         }
                 }
         }
         return result;

		
	}





	@Override
	public void stopLocationProvider() {
		LocalService.myManager.removeUpdates(this);
		myLocationConumer=null;
		
	}





	@Override
	public void onLocationChanged(Location location) {
		if(myLoc!=null&&myLoc.isFollowLocationEnabled()&&location.hasBearing()&&rotate&&location.getSpeed()>1){
		mMapView.setMapOrientation(-location.getBearing());
		}
		if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
			lastgpslocation=System.currentTimeMillis();
		} 
		//Log.d(getClass().getSimpleName(), "onlocchange mapfrag rotate="+rotate);
		//Log.d(getClass().getSimpleName(), "onlocchange mapfrag bearing="+Float.toString(location.getBearing()));
		//Log.d(getClass().getSimpleName(), "onlocchange mapfrag hasbearing="+location.hasBearing());
		if (myLocationConumer != null){
			if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
				if(System.currentTimeMillis()>lastgpslocation+30000){
					myLocationConumer.onLocationChanged(location, this);
				}
			}else
			{
				myLocationConumer.onLocationChanged(location, this);
			}
		}

		
	}





	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void onNewPoint(GeoPoint geopoint) {
		Log.d(getClass().getSimpleName(), "map on new point");
		myTracePathOverlay.addPoint(geopoint);
		
	}
	
	
	}
