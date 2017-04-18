package com.OsMoDroid;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.mapzen.tangram.HttpHandler;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.MapView;
import com.mapzen.tangram.Marker;
import com.mapzen.tangram.MarkerPickResult;
import com.mapzen.tangram.TouchInput;
import com.mapzen.tangram.geometry.Polyline;

import static com.OsMoDroid.LocalService.addlog;
public class MapFragment extends Fragment implements DeviceChange,  LocationListener, MapController.MarkerPickListener
    {
        MapView mMapView;

        @Override
        public void onLowMemory()
            {
                if(mMapView!=null)
                    {
                        mMapView.onLowMemory();
                    }

                super.onLowMemory();
            }
        @Override
        public void onDestroy()
            {
                if(mMapView!=null)
                    {
                        mMapView.onDestroy();
                    }
                super.onDestroy();
            }
        static MapController mapController;
        View view;
        private Marker myLocationMarker;
        public static final String DEFAULT_STYLE = "style: 'points', interactive: true,  size: [20px, 20px], collide: false";
        public static final String LOCATION_STYLE = "style: 'points',  size: [36px, 36px], collide: false";
        private boolean isFollow=true;
        private Location center;
        private ImageButton centerImageButton;
        private GPSLocalServiceClient globalActivity;
        private TextView speddTextView;
//        MapData  mapData;
        private Marker myTraceMarker;
        private Polyline myTracePolyline = new Polyline(new ArrayList<LngLat>(), null);


//        private MapData myTraceMapData;
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
            {
                super.onCreateOptionsMenu(menu, inflater);
//                MenuItem shortname = menu.add(0, 17, 0, R.string.shortname);
//                MenuItem fullgpx = menu.add(0, 16, 0, R.string.fullgpx);
//                MenuItem longpath = menu.add(0, 15, 0, R.string.longpath);
//                MenuItem arrows = menu.add(0, 14, 0, R.string.show_arrows);
//                MenuItem traces = menu.add(0, 1, 0, R.string.showtraces);
//                MenuItem rotation = menu.add(0, 2, 0, R.string.enable_manual_rotation);
//                courserotation = menu.add(0, 3, 0, R.string.enable_course_rotation);
//                traces.setCheckable(true);
//                rotation.setCheckable(true);
//                courserotation.setCheckable(true);
//                arrows.setCheckable(true);
//                longpath.setCheckable(true);
//                fullgpx.setCheckable(true);
//                shortname.setCheckable(true);
//                arrows.setChecked(OsMoDroid.settings.getBoolean("arrows", false));
//                traces.setChecked(OsMoDroid.settings.getBoolean("traces", true));
//                rotation.setChecked(OsMoDroid.settings.getBoolean("rotation", false));
//                longpath.setChecked(OsMoDroid.settings.getBoolean("longpath", true));
//                fullgpx.setChecked(OsMoDroid.settings.getBoolean("fullgpx", true));
//                shortname.setChecked(OsMoDroid.settings.getBoolean("shortname",false));
//
//                courserotation.setChecked(OsMoDroid.settings.getBoolean("courserotation",false));
                SubMenu menu2 = menu.addSubMenu(Menu.NONE, 4, 4, R.string.map);
                MenuItem buublewrap = menu2.add(0, 5, 1, "Bubble Wrap");
                MenuItem Outwalk = menu2.add(0, 6, 2, "WalkAbout");
                MenuItem cinnabar = menu2.add(0, 7, 3, "Cinnabar");
                MenuItem zinc = menu2.add(0, 8, 4, "Zinc");
                MenuItem refill = menu2.add(0, 9, 5, "Refill");


                //menu.add(0, 11, 1, R.string.size_of_point);
               
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                switch (item.getItemId())
                    {
                        case 5:
                            mapController.loadSceneFile("bubble-wrap/bubble-wrap.yaml");
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 1);
                            OsMoDroid.editor.commit();
                            break;
                        case 6:
                            mapController.loadSceneFile("walkabout-style-more-labels/walkabout-style-more-labels.yaml");
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 2);
                            OsMoDroid.editor.commit();
                            break;
                        case 7:
                            mapController.loadSceneFile("cinnabar-more-labels/cinnabar-style-more-labels.yaml");
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 3);
                            OsMoDroid.editor.commit();
                            break;
                        case 8:
                            mapController.loadSceneFile("zinc-style-more-labels/zinc-style-more-labels.yaml");
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 4);
                            OsMoDroid.editor.commit();
                            break;
                        case 9:
                            mapController.loadSceneFile("refill-more-labels/refill-style-more-labels.yaml");
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 5);
                            OsMoDroid.editor.commit();
                            break;

                        default:
                            break;
                    }
                return super.onOptionsItemSelected(item);
            }
        @Override
        public void onMarkerPick(MarkerPickResult markerPickResult, float positionX, float positionY)
            {
                if (markerPickResult != null)
                    {
                        for (MarkerToU m : markerToUs)
                            {
                                if(m.mId==markerPickResult.getMarker().getMarkerId())
                                    {
                                        int u =m.u;
                                        for (Channel ch : LocalService.channelList)
                                            {
                                                if (ch.send)
                                                    {
                                                        for (final Device d : ch.deviceList)
                                                            {
                                                                if (d.u == u)
                                                                    {
                                                                        globalActivity.runOnUiThread(new Runnable()
                                                                            {
                                                                                @Override
                                                                                public void run()
                                                                                    {
                                                                                        Toast.makeText(MapFragment.this.getContext(), d.name, Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                            });

                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                            }


                    }
                else
                    {
                        Log.d(getClass().getSimpleName(), "on marker click null" );
                    }
            }
        static  class MarkerToU
            {
                Marker marker;

                Marker textMarker;
                public int u;
                public long mId;
                public long tId;
                public MarkerToU(Device dev)
                    {

                        this.u=dev.u;
                        marker= mapController.addMarker();
                        this.mId=marker.getMarkerId();
                        textMarker=mapController.addMarker();
                        this.tId=textMarker.getMarkerId();
                        marker.setDrawOrder(2000);
                        textMarker.setStylingFromString("{ style: 'text', text_wrap: 18, max_lines: 3 ,text_source: \"function() { return '"+ dev.name +"'; }\", collide: false,offset: [0px, -12px] ,font: { size: 10px, fill: '#ffffff', stroke: { color: '#000000', width: 2px } } }");
                        marker.setStylingFromString("{ " + DEFAULT_STYLE + ", color: '" + String.format("#%06X", (0xFFFFFF & dev.color)) + "' }");
                        textMarker.setPointEased(new LngLat((double) dev.lon,(double)dev.lat),200, MapController.EaseType.CUBIC);
                        marker.setPointEased(new LngLat((double) dev.lon,(double)dev.lat),200, MapController.EaseType.CUBIC);

                    }
                @Override
                public boolean equals(Object o)
                    {
                        if ((o instanceof Device) && this.u == ((Device) o).u && this.u != 0)
                            {
                                return true;
                            }
                        else
                        if ((o instanceof MarkerToU) && this.u != 0 && this.u == ((MarkerToU) o).u)
                            {
                                return true;
                            }
                        else
                        if ( this.mId != 0 && (this.mId == (Long)o||this.tId==(Long)o))
                        {
                            return true;
                        }
                            return false;

                    }
            }
        ArrayList<MarkerToU> markerToUs = new ArrayList<>();
        @Override
        public void onResume()
            {

                super.onResume();
                if(mMapView!=null)
                    {
                        mMapView.onResume();
                    }
                globalActivity.actionBar.setTitle(getString(R.string.map));
                Criteria c = new Criteria();
                c.setAccuracy(Criteria.ACCURACY_FINE);
                try
                    {
                        LocalService.myManager.requestLocationUpdates(0,0f,c,(LocationListener) MapFragment.this,MapFragment.this.getActivity().getMainLooper());
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                MapFragment.this.onChannelListChange();
            }
        @Override
        public void onPause()
            {
                if(mMapView!=null)
                    {
                        mMapView.onPause();
                    }
                LocalService.myManager.removeUpdates(this);
                super.onPause();
            }
        HttpHandler getHttpHandler()
            {
                File cacheDir = OsMoDroid.context.getExternalCacheDir();
                if (cacheDir != null && cacheDir.exists())
                    {
                        return new HttpHandler(new File(cacheDir, "tile_cache"), 100 * 1024 * 1024);
                    }
                return new HttpHandler();
            }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



            view = inflater.inflate(R.layout.glmap, null);
                    centerImageButton = (ImageButton) view.findViewById(R.id.imageButtonCenter);
            ImageButton zi=(ImageButton)view.findViewById(R.id.zoomInButton);
            ImageButton zo=(ImageButton)view.findViewById(R.id.zoomOutButton);
            zi.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                        {
                            if(mapController!=null)
                                {
                                    mapController.setZoom(mapController.getZoom() + 1.0f);
                                }
                        }
                });
            zo.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                        {
                            if(mapController!=null)
                                {
                                    mapController.setZoom(mapController.getZoom() - 1.0f);
                                }
                        }
                });
                    mMapView = (MapView) view.findViewById(R.id.glMapView);
                    mMapView.setKeepScreenOn(true);
                    speddTextView = (TextView) view.findViewById(R.id.mapSpeedtextView);

                    mMapView.getMapAsync(new MapView.OnMapReadyCallback()
                        {
                            @Override
                            public void onMapReady(MapController mc)
                                {

                                  //  mzmap.setZoomButtonsEnabled(true);
                                    mapController = mc;
                                    mapController.addDataLayer("osmo");
                                    mapController.setHttpHandler(getHttpHandler());
                                    mapController.setCameraType(MapController.CameraType.ISOMETRIC);
                                    centerImageButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                                {
                                                    if (center != null && mapController != null)
                                                        {
                                                            mapController.setPositionEased(new LngLat(center.getLongitude(), center.getLatitude()), 200, MapController.EaseType.CUBIC);
                                                        }
                                                    isFollow = true;
                                                }
                                        });
                                    //mapzenMap.setStyle(new BubbleWrapStyle());
                                    switch (OsMoDroid.settings.getInt("selectedTileSourceInt",1))
                                        {
                                            case 1:
                                                mapController.loadSceneFile("bubble-wrap/bubble-wrap.yaml");
                                                break;
                                            case 2:
                                                mapController.loadSceneFile("walkabout-style-more-labels/walkabout-style-more-labels.yaml");
                                               break;
                                            case 3:
                                                mapController.loadSceneFile("cinnabar-more-labels/cinnabar-style-more-labels.yaml");
                                                break;
                                            case 4:
                                                mapController.loadSceneFile("zinc-style-more-labels/zinc-style-more-labels.yaml");
                                                break;
                                            case 5:
                                                mapController.loadSceneFile("refill-more-labels/refill-style-more-labels.yaml");
                                                break;
                                            default:
                                                break;
                                        }



                                    mapController.setPickRadius(3);
                                    mapController.setTapResponder(new TouchInput.TapResponder()
                                        {
                                            @Override
                                            public boolean onSingleTapUp(float x, float y)
                                                {
                                                    return false;
                                                }
                                            @Override
                                            public boolean onSingleTapConfirmed(float x, float y)
                                                {
//                                            PointF touchPoint = mapController.lngLatToScreenPosition();
                                                    mapController.pickMarker(x, y);
                                                    return true;
                                                }
                                        });
                                    mapController.setMarkerPickListener(MapFragment.this);


                                    MapFragment.this.onChannelListChange();
                                    createMarker();
//                                    Criteria c = new Criteria();
//                                    c.setAccuracy(Criteria.ACCURACY_FINE);
                                    Location location = LocalService.myManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if (location != null)
                                        mapController.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()), 200);
                                    if (mapController.getZoom() < 12)
                                        mapController.setZoomEased(12, 1000);
                                    //LocalService.myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MapFragment.this);
//                                    try
//                                        {
//                                            LocalService.myManager.requestLocationUpdates(0,0f,c,(LocationListener) MapFragment.this,MapFragment.this.getActivity().getMainLooper());
//                                        }
//                                    catch (Exception e)
//                                        {
//                                            e.printStackTrace();
//                                        }
                                    mapController.setPanResponder(new TouchInput.PanResponder()
                                        {
                                            @Override
                                            public boolean onPan(float startX, float startY, float endX, float endY)
                                                {
                                                    isFollow = false;
                                                    return false;
                                                }
                                            @Override
                                            public boolean onFling(float posX, float posY, float velocityX, float velocityY)
                                                {
                                                    return false;
                                                }
                                        });
                                    mapController.setDoubleTapResponder(new TouchInput.DoubleTapResponder()
                                        {
                                            @Override
                                            public boolean onDoubleTap(float x, float y)
                                                {
                                                    LngLat tappedPos = mapController.screenPositionToLngLat(new PointF(x, y));
                                                    LngLat currentPos = mapController.getPosition();
                                                    mapController.setZoom(mapController.getZoom() + 1.0f);
                                                    mapController.setPosition(new LngLat(0.5f * (tappedPos.longitude + currentPos.longitude), 0.5f * (tappedPos.latitude + currentPos.latitude)));
                                                    return true;
                                                }
                                        });
                                }
                        },"bubble-wrap/bubble-wrap.yaml");

            return view;
        }
        private void createMarker() {


            myLocationMarker = mapController.addMarker();
//            for(SerPoint serPoint:LocalService.mydev.devicePath)
//                {
//                    myTracePolyline.getCoordinates().add((new LngLat(serPoint.point.y/1000000.0D,serPoint.point.x/1000000.0D)));
//                }
//            if(myTracePolyline.getCoordinates().size()>1)
//                {
//                   myTraceMapData= mzmap.addPolyline(myTracePolyline);
//                }

            //MapData markers = mapController.addDataLayer("touch");
            ArrayList<LngLat> lngLats = new ArrayList<>();
            lngLats.add(new LngLat(37.0,55.0));
            lngLats.add(new LngLat(35.0,58.0));
            Marker m = mapController.addMarker();

            Map<String, String> props = new HashMap<>();
            props.put("type", "line");
            props.put("color", "black");
            m.setPolyline(new Polyline(lngLats,props));
            m.setDrawOrder(20000);

           // markers.addPolyline(lngLats,props);


            myLocationMarker.setDrawOrder(2000);
            myLocationMarker.setStylingFromString("{ " + LOCATION_STYLE + ",color : 'white' }");

            myLocationMarker.setDrawable(R.drawable.myloc);
            Location location = LocalService.myManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null)
                {
                    myLocationMarker.setPoint(new LngLat(location.getLongitude(), location.getLatitude()));
                    if(isFollow)
                        {
                            mapController.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()), 200, MapController.EaseType.CUBIC);
                        }
                }
        }


//        private static BitmapDrawable getBitmapDrawable(VectorDrawable vectorDrawable) {
//            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
//                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
//            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//            vectorDrawable.draw(canvas);
//            return new BitmapDrawable(bitmap);
//        }

        @Override
        public void onLocationChanged(Location location)
            {
                if(speddTextView!=null)
                    {
                        if(isFollow)
                            {
                                if(!OsMoDroid.settings.getBoolean("imperial",false))
                                    {
                                        speddTextView.setText(OsMoDroid.df0.format(location.getSpeed() * 3.6));
                                    }
                                else
                                    {
                                        speddTextView.setText(OsMoDroid.df0.format(location.getSpeed() * 3.6*0.621371));
                                    }
                            }
                        else
                            {
                                speddTextView.setText("");
                            }

                        if(location.getSpeed()*3.6<2&&(int)location.getAccuracy()<Integer.parseInt(OsMoDroid.settings.getString("hdop_gpx", "30").equals("") ? "30" : OsMoDroid.settings.getString("hdop_gpx", "30")))
                            {
                                speddTextView.setText("");
                            }
                    }
                if(center==null)
                    {
                        center=new Location(location);
                    }
                center.set(location);
                if(isFollow&&mapController!=null)
                    {
                        mapController.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()), 200, MapController.EaseType.CUBIC);
                    }
                if(myLocationMarker!=null)
                    {
                        myLocationMarker.setPointEased(new LngLat(location.getLongitude(), location.getLatitude()), 200, MapController.EaseType.CUBIC);
                        if (location.hasBearing())
                            {
                                addlog("has bearing");
                                addlog( "on bearing: "+"{ " + DEFAULT_STYLE + ", angle: " + (int) location.getBearing() + " }");
                                myLocationMarker.setStylingFromString("{ " + DEFAULT_STYLE + ", angle: " + (int) location.getBearing() + " }");
                            }
                    }

            }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
            {
            }
        @Override
        public void onProviderEnabled(String provider)
            {
            }
        @Override
        public void onProviderDisabled(String provider)
            {
            }
        @Override
        public void onDeviceChange(Device dev)
            {
                for (MarkerToU m: markerToUs)
                    {
                        if(m.u==dev.u)
                            {
                                m.marker.setPointEased(new LngLat((double) dev.lon,(double)dev.lat),5000, MapController.EaseType.CUBIC);
                                m.textMarker.setPointEased(new LngLat((double) dev.lon,(double)dev.lat),5000, MapController.EaseType.CUBIC);

                            }
                    }
//                if(myTracePolyline!=null)
//                    {
//
//                        myTracePolyline.getCoordinates().add(new LngLat(LocalService.mydev.devicePath.get(LocalService.mydev.devicePath.size()).point.y/1000000.0D,LocalService.mydev.devicePath.get(LocalService.mydev.devicePath.size()).point.x/1000000.0D));
//
//                    }
//                if(myTraceMapData==null)
//                    {
//                        if(myTracePolyline.getCoordinates().size()>1)
//                            {
//                                myTraceMapData= mzmap.addPolyline(myTracePolyline);
//                            }
//                    }
            }
        @Override
        public void onChannelListChange()
            {
                if(mapController!=null)
                    {
                        for (MarkerToU m : markerToUs)
                            {
                                mapController.removeMarker(m.marker);
                                mapController.removeMarker(m.textMarker);
                            }
                        Log.d(getClass().getSimpleName(), "link enter");
                        for (Channel ch : LocalService.channelList)
                            {
                                if (ch.send)
                                    {
                                        for (Device d : ch.deviceList)
                                            {
                                                if (!markerToUs.contains(d))
                                                    {
                                                        Log.d(getClass().getSimpleName(), "link succusuful u=" + d.u);
                                                        markerToUs.add(new MarkerToU(d));
                                                    }
                                            }
                                    }
                            }
                    }

            }

        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                Log.d(getClass().getSimpleName(), "map oncreate");
                super.onCreate(savedInstanceState);
                setHasOptionsMenu(true);
                //setRetainInstance(true);
                LocalService.devlistener = this;
                super.onCreate(savedInstanceState);
            }
        @Override
        public void onAttach(Activity activity)
            {
                Log.d(getClass().getSimpleName(), "map onattach");
                globalActivity = (GPSLocalServiceClient) activity;
                super.onAttach(activity);
            }
        @Override
        public void onDetach()
            {
                globalActivity = null;
                LocalService.devlistener = null;
                Log.d(getClass().getSimpleName(), "map ondetach");
                super.onDetach();
            }

    }
