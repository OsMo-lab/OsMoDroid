package com.OsMoDroid;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


//import com.mapzen.tangram.HttpHandler;
import com.mapzen.tangram.CameraUpdateFactory;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.MapView;
import com.mapzen.tangram.Marker;
import com.mapzen.tangram.MarkerPickListener;
import com.mapzen.tangram.MarkerPickResult;
import com.mapzen.tangram.SceneError;
import com.mapzen.tangram.SceneUpdate;
import com.mapzen.tangram.TouchInput;
import com.mapzen.tangram.geometry.Polyline;
import com.mapzen.tangram.networking.DefaultHttpHandler;
import com.mapzen.tangram.networking.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Cache;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.OsMoDroid.LocalService.addlog;
public class MapFragment extends Fragment implements DeviceChange,  LocationListener, MarkerPickListener, MapController.SceneLoadListener, MapView.MapReadyCallback,
        TouchInput.TapResponder,
        TouchInput.DoubleTapResponder, TouchInput.LongPressResponder  {
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
                if(mapController!=null)
                    {
                        OsMoDroid.editor.putInt("centerlat", (int) (mapController.getCameraPosition().latitude * 1000000));
                        OsMoDroid.editor.putInt("centerlon", (int) (mapController.getCameraPosition().longitude * 1000000));
                        OsMoDroid.editor.putInt("zoom", (int) mapController.getCameraPosition().zoom);
                        OsMoDroid.editor.commit();
                        mapController=null;
                    }
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
        public static final String DEFAULT_STYLE = "style: 'points', interactive: true,  size: [20px, 20px], collide: false, order: 2000";
        public static final String LOCATION_STYLE = "style: 'points',  size: [36px, 36px], collide: false, color : 'white'";

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
                MenuItem zinc = menu2.add(0, 8, 4, "Default");
                MenuItem refill = menu2.add(0, 9, 5, "Refill");
                MenuItem tron = menu2.add(0, 10, 6, "Tron");


                //menu.add(0, 11, 1, R.string.size_of_point);
               
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                String sceneYaml;
                switch (item.getItemId())
                    {
                        case 5:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            //mapController.loadSceneFile("asset:///bubble-wrap-style.zip", sceneUpdates);
                            sceneYaml = "import: [asset:///bubble-wrap-style.zip, asset:///label-11.zip]";
                            mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 1);
                            OsMoDroid.editor.commit();
                            break;
                        case 6:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            //mapController.loadSceneFile("asset:///walkabout-style.zip", sceneUpdates);
                            sceneYaml = "import: [asset:///walkabout-style.zip, asset:///label-11.zip]";
                            mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 2);
                            OsMoDroid.editor.commit();
                            break;
                        case 7:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            //mapController.loadSceneFile("asset:///cinnabar-style.zip", sceneUpdates);
                            sceneYaml = "import: [asset:///cinnabar-style.zip, asset:///label-11.zip]";
                            mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 3);
                            OsMoDroid.editor.commit();
                            break;
                        case 8:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            //mapController.loadSceneFile("asset:///sdk-default-style.zip", sceneUpdates);
                            sceneYaml = "import: [asset:///sdk-default-style.zip, asset:///label-11.zip]";
                            mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 4);
                            OsMoDroid.editor.commit();
                            break;
                        case 9:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            //mapController.loadSceneFile("asset:///refill-style.zip", sceneUpdates);
                            sceneYaml = "import: [asset:///refill-style.zip, asset:///label-11.zip]";
                            mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                            createMarker();
                            MapFragment.this.onChannelListChange();
                            OsMoDroid.editor.putInt("selectedTileSourceInt", 5);
                            OsMoDroid.editor.commit();
                            break;
                        case 10:
                            markerToUs.clear();
                            allTracksWayPoints.clear();
                            //mapController.loadSceneFile("asset:///tron-style.zip", sceneUpdates);
                            sceneYaml = "import: [asset:///trone-style.zip, asset:///label-11.zip]";
                            mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
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
                Log.d(getClass().getSimpleName(), "onMarkerPick: ");
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
                                                        //Toast.makeText(getContext(),m.getUserData().toString(), Toast.LENGTH_SHORT).show();
                                                        //Toast.makeText(MapFragment.this.getContext(), OsMoDroid.sdf.format(d.updatated), Toast.LENGTH_SHORT).show();
                                                        LinearLayout layout = new LinearLayout(getContext());
                                                        layout.setOrientation(LinearLayout.VERTICAL);
                                                        final TextView txv5 = new TextView(getContext());
                                                        txv5.setText(((Channel.Point)m.getUserData()).description+'\n'+((Channel.Point)m.getUserData()).url);
                                                        Linkify.addLinks(txv5, Linkify.WEB_URLS);
                                                        layout.addView(txv5);
                                                        final TextView txv6 = new TextView(getContext());
                                                        txv6.setText(((Channel.Point)m.getUserData()).time);
                                                        layout.addView(txv6);
                                                        AlertDialog alertdialog1 = new AlertDialog.Builder(getContext()).create();
                                                        alertdialog1.setView(layout);
                                                        alertdialog1.setTitle(((Channel.Point)m.getUserData()).name);
                                                        alertdialog1.show();
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

        @Override
        public void onSceneReady(int sceneId, SceneError sceneError) {
            if (sceneError == null)
            {
                Toast.makeText(getContext(), "Scene ready: " + sceneId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Scene load error: " + sceneId + " "
                        + sceneError.getSceneUpdate().toString()
                        + " " + sceneError.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onMapReady(@Nullable MapController m) {
            this.mapController=m;
            showTracks();
            mapController.setCameraType(MapController.CameraType.ISOMETRIC);
            markerToUs.clear();
            allTracksWayPoints.clear();
            String sceneYaml;
            switch (OsMoDroid.settings.getInt("selectedTileSourceInt",1))
            {
                case 1:
                    //mapController.loadSceneFile("asset:///bubble-wrap-style.zip", sceneUpdates);
                    sceneYaml = "import: [asset:///bubble-wrap-style.zip, asset:///label-11.zip]";
                    mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                    break;
                case 2:
                    //mapController.loadSceneFile("asset:///walkabout-style.zip", sceneUpdates);
                    sceneYaml = "import: [asset:////walkabout-style.zip, asset:///label-11.zip]";
                    mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                    break;
                case 3:
                    //mapController.loadSceneFile("asset:///cinnabar-style.zip", sceneUpdates);
                    sceneYaml = "import: [asset:///cinnabar-style.zip, asset:///label-11.zip]";
                    mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                    break;
                case 4:
                    //mapController.loadSceneFile("asset:///sdk-default-style.zip", sceneUpdates);
                    sceneYaml = "import: [asset:///sdk-default-style.zip, asset:///label-11.zip]";
                    mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                    break;
                case 5:
                    //mapController.loadSceneFile("asset:///refill-style", sceneUpdates);
                    sceneYaml = "import: [asset:///refill-style.zip, asset:///label-11.zip]";
                    mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                    break;
                case 6:
                    //mapController.loadSceneFile("asset:///tron-style.zip", sceneUpdates);
                    sceneYaml = "import: [asset:///tron-style.zip, asset:///label-11.zip]";
                    mapController.loadSceneYaml(sceneYaml, "", sceneUpdates);
                    break;
                default:
                    break;
            }
            //mapController. loadSceneFile("asset:///label-11.zip", sceneUpdates);
            Location location = LocalService.myManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null)
                //mapController.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()), 200);
                mapController.updateCameraPosition(CameraUpdateFactory.setPosition(new LngLat(location.getLongitude(), location.getLatitude())), 200);

            if (mapController.getCameraPosition().zoom < 12)
                mapController.updateCameraPosition(CameraUpdateFactory.setZoom(12),1000);
            mapController.addDataLayer("osmo");
            mapController.setPickRadius(3);
            mapController.setMarkerPickListener(MapFragment.this);
            final TouchInput touchInput = mapController.getTouchInput();
            final TouchInput.PanResponder defPanResponed = mapController.getPanResponder();
            touchInput.setTapResponder(this);
            touchInput.setDoubleTapResponder(this);
            touchInput.setLongPressResponder(this);
            touchInput.setPanResponder(new TouchInput.PanResponder() {
                @Override
                public boolean onPanBegin() {
                    defPanResponed.onPanBegin();

                    return false;
                }

                @Override
                public boolean onPan(float startX, float startY, float endX, float endY) {
                    isFollow = false;
                    defPanResponed.onPan( startX,  startY,  endX,  endY);
                    return false;
                }

                @Override
                public boolean onPanEnd() {
                    defPanResponed.onPanEnd();
                    return false;
                }

                @Override
                public boolean onFling(float posX, float posY, float velocityX, float velocityY) {
                    defPanResponed.onFling(posX,posY,velocityX,velocityY);
                    return false;
                }

                @Override
                public boolean onCancelFling() {
                    defPanResponed.onCancelFling();
                    return false;
                }
            });



            createMarker();
            onChannelListChange();
            Bundle bundle = getArguments();
            if (OsMoDroid.settings.getInt("centerlat", -1) != -1&&bundle==null)
            {
                if (mapController != null)
                {
                    // mapController.setZoom(OsMoDroid.settings.getInt("zoom", 10));
                    mapController.updateCameraPosition(CameraUpdateFactory.setZoom(OsMoDroid.settings.getInt("zoom", 10)));
                    // mapController.setPosition(new LngLat( OsMoDroid.settings.getInt("centerlon", 0)/(double)1000000, OsMoDroid.settings.getInt("centerlat", 0)/(double)1000000));
                    mapController.updateCameraPosition(CameraUpdateFactory.setPosition(new LngLat( OsMoDroid.settings.getInt("centerlon", 0)/(double)1000000, OsMoDroid.settings.getInt("centerlat", 0)/(double)1000000)),1000, MapController.EaseType.CUBIC);
                    Log.d(this.getClass().getName(), "Center map on ="+OsMoDroid.settings.getInt("centerlat", 0)+ OsMoDroid.settings.getInt("centerlon", 0));
                    isFollow=false;
                }
            }


        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            LngLat tappedPos = mapController.screenPositionToLngLat(new PointF(x, y));
            mapController.updateCameraPosition(CameraUpdateFactory.newLngLatZoom(tappedPos,mapController.getCameraPosition().zoom+1f), 50);
            
            return false;

        }

        @Override
        public void onLongPress(float x, float y) {

            if(LocalService.channelList.size()>0)
            {
                LngLat l =mapController.screenPositionToLngLat(new PointF(x,y));
                final JSONObject jo = new JSONObject();
                try
                {
                    jo.put("lat", l.latitude);
                    jo.put("lon", l.longitude);
                }
                catch (JSONException e1)
                {
                    e1.printStackTrace();
                }
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                final TextView txv5 = new TextView(getContext());
                txv5.setText(R.string.point_name_);
                layout.addView(txv5);
                final EditText pointName = new EditText(getContext());
                layout.addView(pointName);
                final TextView txv6 = new TextView(getContext());
                txv6.setText(R.string.chanal);
                layout.addView(txv6);
                final Spinner groupSpinner = new Spinner(getContext());
                layout.addView(groupSpinner);
                List<Channel> activeChannelList = new ArrayList<Channel>();
                for(Channel ch: LocalService.channelList)
                {
                    if(ch.send)
                    {
                        activeChannelList.add(ch);
                    }
                }
                ArrayAdapter<Channel> dataAdapter = new ArrayAdapter<Channel>(getContext(), R.layout.spinneritem, activeChannelList);
                groupSpinner.setAdapter(dataAdapter);
                AlertDialog alertdialog1 = new AlertDialog.Builder(getContext()).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                }).create();
                alertdialog1.setView(layout);
                alertdialog1.setTitle(getContext().getString(R.string.point_create));
                alertdialog1.setMessage(getContext().getString(R.string.point_create_description));

                alertdialog1.show();
                alertdialog1.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new CustomListener(alertdialog1)
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (LocalService.myIM.authed)
                        {
                            if(groupSpinner.getSelectedItem()!=null)
                            {
                                try
                                {
                                    jo.put("name", pointName.getText().toString());
                                    jo.put("group", ((Channel) groupSpinner.getSelectedItem()).u);
                                }
                                catch (JSONException e1)
                                {
                                    e1.printStackTrace();
                                }
                                LocalService.myIM.sendToServer("GPA|" + jo.toString(), true);
                                super.dialog.dismiss();
                            }
                            else
                            {
                                Toast.makeText(getContext(), R.string.needselectpoint, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getContext(), R.string.CheckInternet, Toast.LENGTH_SHORT).show();

                        }
                    }
                });


            }
            else
            {
                Toast.makeText(getContext(), R.string.nogroupstosendpoint, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public boolean onSingleTapUp(float x, float y) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(float x, float y) {
            mapController.pickMarker(x, y);
            return false;

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
        HttpHandler getHttpHandler() {
            return new DefaultHttpHandler() {
                @Override
                protected void configureClient(OkHttpClient.Builder builder) {
                    File cacheDir = getContext().getExternalCacheDir();
                    if (cacheDir != null && cacheDir.exists()) {
                        builder.cache(new Cache(cacheDir, 16 * 1024 * 1024));
                    }
                }
                CacheControl tileCacheControl = new CacheControl.Builder().maxStale(7, TimeUnit.DAYS).build();
                @Override
                protected void configureRequest(HttpUrl url, Request.Builder builder) {
                    if ("tile.nextzen.com".equals(url.host())) {
                        builder.cacheControl(tileCacheControl);
                    }
                }
            };
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
                                    mapController.updateCameraPosition(CameraUpdateFactory.zoomIn());
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
                                    mapController.updateCameraPosition(CameraUpdateFactory.zoomOut());
                                }
                        }
                });
                    mMapView = (MapView) view.findViewById(R.id.glMapView);
                    mMapView.setKeepScreenOn(true);
                    speddTextView = (TextView) view.findViewById(R.id.mapSpeedtextView);
            sceneUpdates.add(new SceneUpdate("global.sdk_api_key", MAPZEN_API_KEY));




            mMapView.getMapAsync(this, getHttpHandler());
                                    centerImageButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                                {
                                                    if (center != null && mapController != null)
                                                        {
                                                           // mapController.setPositionEased(new LngLat(center.getLongitude(), center.getLatitude()), 200, MapController.EaseType.CUBIC);
                                                            mapController.updateCameraPosition(CameraUpdateFactory.setPosition(new LngLat(center.getLongitude(), center.getLatitude())), 200, MapController.EaseType.CUBIC);
                                                        }
                                                    isFollow = true;
                                                    followdev=-1;
                                                }
                                        });



                                    MapFragment.this.onChannelListChange();
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
            myLocationMarker.setStylingFromString("{ " + LOCATION_STYLE + " }");
            myLocationMarker.setDrawable(R.drawable.myloc);
            Location location = LocalService.myManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null&&mapController!=null)
                {
                    myLocationMarker.setPoint(new LngLat(location.getLongitude(), location.getLatitude()));
                    if(isFollow)
                        {
                           mapController.updateCameraPosition(CameraUpdateFactory.setPosition(new LngLat(location.getLongitude(), location.getLatitude())),1000, MapController.EaseType.CUBIC);
                        }
                }
        }
         void showTracks()
            {
                if(mapController!=null) {
                    mapData = mapController.addDataLayer("touch");
                    mapData.clear();
                    for (Marker m : allTracksWayPoints) {
                        mapController.removeMarker(m);
                    }
                    allTracksWayPoints.clear();
                    for (Channel ch : LocalService.channelList) {
                        if (ch.send) {
                            for (ColoredGPX cg : ch.gpxList) {
                                Log.d(getClass().getSimpleName(), "for coloredgpx");
                                if (cg.status == ColoredGPX.Statuses.LOADED) {
                                    int currentSegment = -1;
                                    Log.d(getClass().getSimpleName(), "for loaded coloredgpx size " + cg.points.size());
                                    ArrayList<LngLat> lngLats = new ArrayList<>();
                                    for (SegmentPoint sp : cg.points) {
                                        if (sp.segment == currentSegment) {
                                            Log.d(getClass().getSimpleName(), "for segment=currentsegment");
                                            //Log.d(getClass().getSimpleName(), "for segemntpoint " + sp.y / (double) 1000000 + ' ' + sp.x / (double) 1000000);
                                            lngLats.add(new LngLat(sp.y / (double) 1000000, sp.x / (double) 1000000));
                                        } else {
                                            if (lngLats.size() > 0) {
                                                Log.d(getClass().getSimpleName(), "for lngLats size>0");
                                                Map<String, String> props = new HashMap<>();
                                                props.put("type", "line");
                                                props.put("color", String.format("#%06X", (0xFFFFFF & cg.color)));
                                                Log.d(getClass().getSimpleName(), "for color= " + String.format("#%06X", (0xFFFFFF & cg.color)) + ' ' + lngLats.size());
                                                mapData.addPolyline(lngLats, props);
                                            }
                                            currentSegment = sp.segment;
                                            lngLats = new ArrayList<>();
                                        }
                                    }
                                    Log.d(getClass().getSimpleName(), "for lngLats size>0");
                                    Map<String, String> props = new HashMap<>();
                                    props.put("type", "line");
                                    props.put("color", String.format("#%06X", (0xFFFFFF & cg.color)));
                                    Log.d(getClass().getSimpleName(), "for color= " + String.format("#%06X", (0xFFFFFF & cg.color)) + ' ' + lngLats.size());
                                    mapData.addPolyline(lngLats, props);
                                    for (Channel.Point p : cg.waypoints) {
                                        Marker m = mapController.addMarker();
                                        m.setUserData(p);
                                        m.setPoint(new LngLat(p.lon, p.lat));
                                        m.setStylingFromString("{ " + DEFAULT_STYLE + ", color: '" + String.format("#%06X", (0xFFFFFF & cg.color)) + "' }");
                                        allTracksWayPoints.add(m);
//                                                        Marker t = mapController.addMarker();
//                                                        t.setPoint(new LngLat(p.lon, p.lat));
//                                                        String name = p.name.replace('/',' ');
//                                                        //t.setStylingFromString("{ style: 'text', text_wrap: 18, max_lines: 3 ,text_source: \"function() { return '"+ name +"'; }\", collide: true,offset: [0px, -12px] ,font: { size: 10px, fill: '#ffffff', stroke: { color: '#000000', width: 2px } } }");
//                                                        t.setStylingFromString("{ style: 'text', text_source: '\"function() { return '" + name+ "'; }\", collide: true,offset: [0px, -12px] ,font: { size: 10px, fill: '#ffffff', stroke: { color: '#000000', width: 2px } } }");
                                        //allTracksWayPoints.add(t);

                                    }

                                }
                            }
                            for (Channel.Point p : ch.pointList) {
                                Marker m = mapController.addMarker();
                                m.setUserData(p);
                                m.setPoint(new LngLat(p.lon, p.lat));
                                m.setStylingFromString("{ " + DEFAULT_STYLE + ", color: '" + String.format("#%06X", (0xFFFFFF & Color.parseColor(p.color))) + "' }");
                                allTracksWayPoints.add(m);
                            }
                        }
                    }
                    for (ColoredGPX cg : LocalService.showedgpxList) {
                        //   Log.d(getClass().getSimpleName(), "for coloredgpx");
                        if (cg.status == ColoredGPX.Statuses.LOADED) {
                            int currentSegment = -1;
                            // Log.d(getClass().getSimpleName(), "for loaded coloredgpx size "+cg.points.size());
                            ArrayList<LngLat> lngLats = new ArrayList<>();
                            for (SegmentPoint sp : cg.points) {
                                if (sp.segment == currentSegment) {
                                    //  Log.d(getClass().getSimpleName(), "for segment=currentsegment");
                                    //Log.d(getClass().getSimpleName(), "for segemntpoint " + sp.y / (double) 1000000 + ' ' + sp.x / (double) 1000000);
                                    lngLats.add(new LngLat(sp.y / (double) 1000000, sp.x / (double) 1000000));
                                } else {
                                    if (lngLats.size() > 0) {
                                        //   Log.d(getClass().getSimpleName(), "for lngLats size>0");
                                        Map<String, String> props = new HashMap<>();
                                        props.put("type", "line");
                                        props.put("color", String.format("#%06X", (0xFFFFFF & cg.color)));
                                        // Log.d(getClass().getSimpleName(), "for color= " + String.format("#%06X", (0xFFFFFF & cg.color)) + ' ' + lngLats.size());
                                        mapData.addPolyline(lngLats, props);
                                    }

                                    currentSegment = sp.segment;
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
                      //  mapController.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()), 200, MapController.EaseType.CUBIC);
                        mapController.updateCameraPosition(CameraUpdateFactory.setPosition(new LngLat(location.getLongitude(), location.getLatitude())),200, MapController.EaseType.CUBIC);
                    }
                if(myLocationMarker!=null)
                    {
                        try {
                            myLocationMarker.setPointEased(new LngLat(location.getLongitude(), location.getLatitude()), 200, MapController.EaseType.CUBIC);
                        } catch (RuntimeException e) {
                            addlog("tangram pointer to locationmarker is null, panic");
                        }
                        if (location.hasBearing())
                            {
//                                addlog("has bearing");
//                                addlog( "on bearing: "+"{ " + LOCATION_STYLE + ", angle: " + ((int) location.getBearing()) + " }");
                                //myLocationMarker.setDrawable(R.drawable.myloc);
                                myLocationMarker.setDrawOrder(2000);
                                myLocationMarker.setStylingFromString("{ " + LOCATION_STYLE + ", angle: " + ((int) location.getBearing()) + " }");

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
                Log.d(getClass().getSimpleName(), "map on device change");
                for (MarkerToU m: markerToUs)
                    {
                        if(m.u==dev.u)
                            {
                                Log.d(getClass().getSimpleName(), "map on device change - move");
                                LngLat point = new LngLat((double) dev.lon, (double) dev.lat);
                                m.marker.setPointEased(point,6000, MapController.EaseType.SINE);
                                m.textMarker.setPointEased(point,6000, MapController.EaseType.SINE);
                                if(dev.u==followdev)
                                    {
                                   //     mapController.setPositionEased(point,200, MapController.EaseType.SINE);
                                        mapController.updateCameraPosition(CameraUpdateFactory.setPosition(point),200,MapController.EaseType.SINE);
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
