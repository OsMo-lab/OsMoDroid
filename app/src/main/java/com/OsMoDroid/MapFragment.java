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
import com.mapzen.tangram.SceneUpdate;
import com.mapzen.tangram.TouchInput;
import com.mapzen.tangram.geometry.Polyline;

import static com.OsMoDroid.LocalService.addlog;
public class MapFragment extends Fragment implements DeviceChange,  LocationListener, MapController.MarkerPickListener
    {
        MapView mMapView;
        private ArrayList<Marker> allTracksWayPoints= new ArrayList<>();
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
                Log.d(getClass().getSimpleName(), "map ondestroyview");
                OsMoDroid.editor.putInt("centerlat", (int)(mapController.getPosition().latitude*1000000));
                OsMoDroid.editor.putInt("centerlon", (int)(mapController.getPosition().longitude*1000000));
                OsMoDroid.editor.putInt("zoom",(int)mapController.getZoom());

                OsMoDroid.editor.commit();
                if(mMapView!=null)
                    {
                        mMapView.onDestroy();
                    }

                super.onDestroy();
            }
        static MapController mapController;
        View view;
        int followdev = -1;
        private Marker myLocationMarker;
        public static final String DEFAULT_STYLE = "style: 'points', interactive: true,  size: [20px, 20px], collide: false";
        public static final String LOCATION_STYLE = "style: 'points',  size: [36px, 36px], collide: false";
        private boolean isFollow=true;
        private Location center;
        private ImageButton centerImageButton;
        private GPSLocalServiceClient globalActivity;
        private TextView speddTextView;
        MapData  mapData;
        private Marker myTraceMarker;
        private Polyline myTracePolyline = new Polyline(new ArrayList<LngLat>(), null);
        private static final String MAPZEN_API_KEY = BuildConfig.MAPZEN_API_KEY;
        private ArrayList<SceneUpdate> sceneUpdates = new ArrayList<>();


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
                MenuItem tron = menu2.add(0, 10, 6, "Tron");


                //menu.add(0, 11, 1, R.string.size_of_point);
               
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                switch (item.getItemId())
                    {
                        case 5:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            mapController.loadSceneFile("asset:///bubble-wrap-style-more-labels.zip", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 1);
                            OsMoDroid.editor.commit();
                            break;
                        case 6:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            mapController.loadSceneFile("asset:///walkabout-style-more-labels.zip", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 2);
                            OsMoDroid.editor.commit();
                            break;
                        case 7:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            mapController.loadSceneFile("asset:///cinnabar-style-more-labels.zip", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 3);
                            OsMoDroid.editor.commit();
                            break;
                        case 8:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            mapController.loadSceneFile("asset:///zinc-style-more-labels.zip", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 4);
                            OsMoDroid.editor.commit();
                            break;
                        case 9:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            mapController.loadSceneFile("asset:///refill-style-more-labels.zip", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 5);
                            OsMoDroid.editor.commit();
                            break;
                        case 10:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            mapController.loadSceneFile("asset:///tron-style-more-labels.zip", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 6);
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
                                                                if(u==d.u)
                                                                    {
                                                                        if (followdev != d.u)
                                                                            {
                                                                                globalActivity.runOnUiThread(new Runnable()
                                                                                    {
                                                                                        @Override
                                                                                        public void run()
                                                                                            {
                                                                                                Toast.makeText(getContext(), getContext().getString(R.string.follow_) + ' ' + d.name, Toast.LENGTH_SHORT).show();
                                                                                                //Toast.makeText(MapFragment.this.getContext(), OsMoDroid.sdf.format(d.updatated), Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                    });
                                                                                followdev = d.u;
                                                                            }
                                                                        else
                                                                            {
                                                                                globalActivity.runOnUiThread(new Runnable()
                                                                                    {
                                                                                        @Override
                                                                                        public void run()
                                                                                            {
                                                                                                Toast.makeText(getContext(), getContext().getString(R.string.no_follow_) + ' ' + d.name, Toast.LENGTH_SHORT).show();
                                                                                                //Toast.makeText(MapFragment.this.getContext(), OsMoDroid.sdf.format(d.updatated), Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                    });
                                                                                followdev = -1;
                                                                            }
                                                                    }
//                                                                if (d.u == u)
//                                                                    {
//                                                                        globalActivity.runOnUiThread(new Runnable()
//                                                                            {
//                                                                                @Override
//                                                                                public void run()
//                                                                                    {
//                                                                                        Toast.makeText(MapFragment.this.getContext(), OsMoDroid.sdf.format(d.updatated), Toast.LENGTH_SHORT).show();
//                                                                                    }
//                                                                            });
//
//                                                                    }
                                                            }
                                                    }
                                            }
                                    }
                            }
                        for(final Marker m: allTracksWayPoints)
                            {
                                if(m.getMarkerId()==markerPickResult.getMarker().getMarkerId())
                                    {
                                        globalActivity.runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                    {
                                                        Toast.makeText(getContext(),m.getUserData().toString(), Toast.LENGTH_SHORT).show();
                                                        //Toast.makeText(MapFragment.this.getContext(), OsMoDroid.sdf.format(d.updatated), Toast.LENGTH_SHORT).show();
                                                    }
                                            });
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
                        textMarker.setStylingFromString("{ style: 'text', text_wrap: 18, max_lines: 3 ,text_source: \"function() { return '"+ dev.name +"'; }\", collide: true,offset: [0px, -12px] ,font: { size: 10px, fill: '#ffffff', stroke: { color: '#000000', width: 2px } } }");
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
            sceneUpdates.add(new SceneUpdate("global.sdk_mapzen_api_key", MAPZEN_API_KEY));

                    mMapView.getMapAsync(new MapView.OnMapReadyCallback()
                        {
                            @Override
                            public void onMapReady(MapController mc)
                                {

                                  //  mzmap.setZoomButtonsEnabled(true);
                                    mapController = mc;
                                    showTracks();

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
                                                    followdev=-1;
                                                }
                                        });
                                    //mapzenMap.setStyle(new BubbleWrapStyle());
                                    markerToUs.clear();
                                    allTracksWayPoints.clear();
                                    switch (OsMoDroid.settings.getInt("selectedTileSourceInt",1))
                                        {
                                            case 1:
                                                mapController.loadSceneFile("asset:///bubble-wrap-style-more-labels.zip", sceneUpdates);
                                                break;
                                            case 2:
                                                mapController.loadSceneFile("asset:///walkabout-style-more-labels.zip", sceneUpdates);
                                                break;
                                            case 3:
                                                mapController.loadSceneFile("asset:///cinnabar-style-more-labels.zip", sceneUpdates);
                                                break;
                                            case 4:
                                                mapController.loadSceneFile("asset:///zinc-style-more-labels.zip", sceneUpdates);
                                                break;
                                            case 5:
                                                mapController.loadSceneFile("asset:///refill-style-more-labels.zip", sceneUpdates);
                                                break;
                                            case 6:
                                                mapController.loadSceneFile("asset:///tron-style-more-labels.zip", sceneUpdates);
                                                break;
                                            default:
                                                break;
                                        }


                                    mapController.addDataLayer("osmo");
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
                                    Bundle bundle = getArguments();
                                    if (OsMoDroid.settings.getInt("centerlat", -1) != -1&&bundle==null)
                                        {
                                            if (mapController != null)
                                                {
                                                    mapController.setZoom(OsMoDroid.settings.getInt("zoom", 10));
                                                    mapController.setPosition(new LngLat( OsMoDroid.settings.getInt("centerlon", 0)/(double)1000000, OsMoDroid.settings.getInt("centerlat", 0)/(double)1000000));
                                                    Log.d(this.getClass().getName(), "Center map on ="+OsMoDroid.settings.getInt("centerlat", 0)+ OsMoDroid.settings.getInt("centerlon", 0));
                                                    isFollow=false;
                                                }
                                        }
                                }
                        },"asset:///cinnabar-style-more-labels.zip", sceneUpdates);

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

            //ArrayList<LngLat> lngLats = new ArrayList<>();
            //lngLats.add(new LngLat(37.0,55.0));
            //lngLats.add(new LngLat(35.0,58.0));
            //lngLats.add(new LngLat(-74.00976419448854, 40.70532700869127));
            //Marker m = mapController.addMarker();


            //m.setDrawOrder(20000);

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
                            mapController.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()), 1000, MapController.EaseType.CUBIC);
                        }
                }
        }
         void showTracks()
            {
                mapData = mapController.addDataLayer("touch");
                mapData.clear();
                for(Marker m: allTracksWayPoints)
                    {
                        mapController.removeMarker(m);
                    }
                allTracksWayPoints.clear();
                for(Channel ch: LocalService.channelList)
                    {
                        for(ColoredGPX cg:ch.gpxList)
                            {
                                Log.d(getClass().getSimpleName(), "for coloredgpx");
                                if(cg.status== ColoredGPX.Statuses.LOADED)
                                    {
                                        int currentSegment=-1;
                                       Log.d(getClass().getSimpleName(), "for loaded coloredgpx size "+cg.points.size());
                                        ArrayList<LngLat> lngLats = new ArrayList<>();
                                        for (SegmentPoint sp : cg.points)
                                            {
                                                if(sp.segment==currentSegment)
                                                    {
                                                        Log.d(getClass().getSimpleName(), "for segment=currentsegment");
                                                        //Log.d(getClass().getSimpleName(), "for segemntpoint " + sp.y / (double) 1000000 + ' ' + sp.x / (double) 1000000);
                                                        lngLats.add(new LngLat(sp.y / (double) 1000000, sp.x / (double) 1000000));
                                                    }
                                                else
                                                    {
                                                        if(lngLats.size()>0)
                                                            {
                                                                Log.d(getClass().getSimpleName(), "for lngLats size>0");
                                                                Map<String, String> props = new HashMap<>();
                                                                props.put("type", "line");
                                                                props.put("color", String.format("#%06X", (0xFFFFFF & cg.color)));
                                                                Log.d(getClass().getSimpleName(), "for color= " + String.format("#%06X", (0xFFFFFF & cg.color)) + ' ' + lngLats.size());
                                                                mapData.addPolyline(lngLats, props);
                                                            }

                                                        currentSegment=sp.segment;
                                                        lngLats = new ArrayList<>();

                                                    }
                                            }
                                        Log.d(getClass().getSimpleName(), "for lngLats size>0");
                                        Map<String, String> props = new HashMap<>();
                                        props.put("type", "line");
                                        props.put("color", String.format("#%06X", (0xFFFFFF & cg.color)));
                                        Log.d(getClass().getSimpleName(), "for color= " + String.format("#%06X", (0xFFFFFF & cg.color)) + ' ' + lngLats.size());
                                        mapData.addPolyline(lngLats, props);
                                        for (Channel.Point p:cg.waypoints)
                                            {
                                                Marker m =mapController.addMarker();
                                                m.setUserData(p.description);
                                                m.setPoint(new LngLat(p.lon,p.lat));
                                                m.setStylingFromString("{ " + DEFAULT_STYLE + ", color: '" + String.format("#%06X", (0xFFFFFF & cg.color)) + "' }");
                                                allTracksWayPoints.add(m);
                                              Marker t=mapController.addMarker();
                                                t.setPoint(new LngLat(p.lon,p.lat));
                                                t.setStylingFromString("{style: 'text'}");
                                                //t.setStylingFromString("{ style: 'text', text_wrap: 18, max_lines: 3 ,text_source: \"function() { return '"+ p.name +"'; }\", collide: true,offset: [0px, -12px] ,font: { size: 10px, fill: '#ffffff', stroke: { color: '#000000', width: 2px } } }");
                                                allTracksWayPoints.add(t);
                                            }


                                    }
                            }
                    }
                for(ColoredGPX cg:LocalService.showedgpxList)
                    {
                     //   Log.d(getClass().getSimpleName(), "for coloredgpx");
                        if(cg.status== ColoredGPX.Statuses.LOADED)
                            {
                                int currentSegment=-1;
                               // Log.d(getClass().getSimpleName(), "for loaded coloredgpx size "+cg.points.size());
                                ArrayList<LngLat> lngLats = new ArrayList<>();
                                for (SegmentPoint sp : cg.points)
                                    {
                                        if(sp.segment==currentSegment)
                                            {
                                              //  Log.d(getClass().getSimpleName(), "for segment=currentsegment");
                                                //Log.d(getClass().getSimpleName(), "for segemntpoint " + sp.y / (double) 1000000 + ' ' + sp.x / (double) 1000000);
                                                lngLats.add(new LngLat(sp.y / (double) 1000000, sp.x / (double) 1000000));
                                            }
                                        else
                                            {
                                                if(lngLats.size()>0)
                                                    {
                                                     //   Log.d(getClass().getSimpleName(), "for lngLats size>0");
                                                        Map<String, String> props = new HashMap<>();
                                                        props.put("type", "line");
                                                        props.put("color", String.format("#%06X", (0xFFFFFF & cg.color)));
                                                       // Log.d(getClass().getSimpleName(), "for color= " + String.format("#%06X", (0xFFFFFF & cg.color)) + ' ' + lngLats.size());
                                                        mapData.addPolyline(lngLats, props);
                                                    }

                                                currentSegment=sp.segment;
                                                lngLats = new ArrayList<>();

                                            }
                                    }
                                Log.d(getClass().getSimpleName(), "for lngLats size>0");
                                Map<String, String> props = new HashMap<>();
                                props.put("type", "line");
                                props.put("color", String.format("#%06X", (0xFFFFFF & cg.color)));
                                //Log.d(getClass().getSimpleName(), "for color= " + String.format("#%06X", (0xFFFFFF & cg.color)) + ' ' + lngLats.size());
                                mapData.addPolyline(lngLats, props);


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
                                myLocationMarker.setStylingFromString("{ " + LOCATION_STYLE + ", angle: " + (int) location.getBearing() + " }");
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
                                LngLat point = new LngLat((double) dev.lon, (double) dev.lat);
                                m.marker.setPointEased(point,6000, MapController.EaseType.SINE);
                                m.textMarker.setPointEased(point,6000, MapController.EaseType.SINE);
                                if(dev.u==followdev)
                                    {
                                        mapController.setPositionEased(point,200, MapController.EaseType.SINE);
                                    }

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
                        showTracks();
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
