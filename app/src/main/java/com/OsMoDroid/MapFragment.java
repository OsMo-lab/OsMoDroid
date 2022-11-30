package com.OsMoDroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

//import org.osmdroid.views.overlay.PathOverlay;

public class MapFragment extends Fragment implements DeviceChange, IMyLocationProvider, LocationListener {
    Handler mHandler = new Handler();

    MapView mMapView;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMapView != null) {
                mMapView.invalidate();
            }
            mHandler.postDelayed(mRunnable, 5000);
        }
    };
    private IMapController mController;
    private MyLocationNewOverlay myLoc;

    //private PathOverlay myTracePathOverlay;
    private GPSLocalServiceClient globalActivity;
    private IMyLocationConsumer myLocationConumer;
    private long lastgpslocation = 0;
    private MenuItem courserotation;
    private MAPSurferTileSource mapSurferTileSource;
    private BingMapTileSource bingTileSource;
    private ITileSource sputnikTileSource;
    private ITileSource outdoorTileSource;
    private ITileSource chepeTileSource;
    private ITileSource mtbTileSource;
    private ITileSource wikiTileSource;
    private ITileSource hdMapnikTileSource;
    private ITileSource topoTileSource;
    private ChannelsOverlay choverlay;
    private TextView speddTextView;
    private MapListener wrappedListener;
    private GeoPoint prevGeoPoint;
    private ArrayList<ITileSource> jsonTileSourceArrayList;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem shortname = menu.add(0, 17, 0, R.string.shortname);
        MenuItem fullgpx = menu.add(0, 16, 0, R.string.fullgpx);
        //MenuItem longpath = menu.add(0, 15, 0, R.string.longpath);
        //MenuItem arrows = menu.add(0, 14, 0, R.string.show_arrows);
        //MenuItem traces = menu.add(0, 1, 0, R.string.showtraces);
        //MenuItem rotation = menu.add(0, 2, 0, R.string.enable_manual_rotation);
        courserotation = menu.add(0, 3, 0, R.string.enable_course_rotation);
        //traces.setCheckable(true);
        //rotation.setCheckable(true);
        courserotation.setCheckable(true);
        //arrows.setCheckable(true);
        //longpath.setCheckable(true);
        fullgpx.setCheckable(true);
        shortname.setCheckable(true);
        //arrows.setChecked(OsMoDroid.settings.getBoolean("arrows", false));
        //traces.setChecked(OsMoDroid.settings.getBoolean("traces", true));
        //rotation.setChecked(OsMoDroid.settings.getBoolean("rotation", false));
        //longpath.setChecked(OsMoDroid.settings.getBoolean("longpath", true));
        fullgpx.setChecked(OsMoDroid.settings.getBoolean("fullgpx", true));
        shortname.setChecked(OsMoDroid.settings.getBoolean("shortname", false));

        courserotation.setChecked(OsMoDroid.settings.getBoolean("courserotation", false));
        SubMenu menu2 = menu.addSubMenu(Menu.NONE, 4, 4, R.string.map);
        //MenuItem mapsurfer = menu2.add(0, 5, 1, "MapSurfer");
        MenuItem mapnik = menu2.add(0, 6, 2, "Mapnik");
        MenuItem bing = menu2.add(0, 7, 3, "Microsoft Bing");
        MenuItem binglabels = menu2.add(0, 8, 4, "Microsoft Bing with Labels");
        MenuItem adjdpi = menu.add(0, 9, 1, R.string.adjust_to_dpi);
        adjdpi.setCheckable(true);
        adjdpi.setChecked(OsMoDroid.settings.getBoolean("adjust_to_dpi", true));
        // MenuItem sputnik = menu2.add(0, 10, 1, "Sputnik");

        //MenuItem wiki = menu2.add(0, 20, 1, "Wiki");
        //MenuItem mapnikHD = menu2.add(0, 21, 1, "MapnikHD");
        //MenuItem chepe = menu2.add(0, 18, 1, "HotMap");
        //menu2.add(0, 22, 1, "Opentopomap");
        //  MenuItem mtb = menu2.add(0, 19, 1, "MTB");
        menu.add(0, 11, 1, R.string.size_of_point);

            for(int i = 0; i < jsonTileSourceArrayList.size(); i++)
        {
            menu2.add(0,100 + i,i, jsonTileSourceArrayList.get(i).name());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        IGeoPoint c = mMapView.getMapCenter();
        switch (item.getItemId()) {
            case 1:
                item.setChecked(!item.isChecked());
                OsMoDroid.editor.putBoolean("traces", item.isChecked());
                OsMoDroid.editor.commit();
                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 2:
                item.setChecked(!item.isChecked());
                OsMoDroid.editor.putBoolean("rotation", item.isChecked());
                OsMoDroid.editor.commit();
                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 3:
                item.setChecked(!item.isChecked());

                OsMoDroid.editor.putBoolean("courserotation", item.isChecked());
                OsMoDroid.editor.commit();
                mMapView.setMapOrientation(0);
                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 6:

                mMapView.setTileSource(TileSourceFactory.MAPNIK);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 1);
                OsMoDroid.editor.commit();
                reinitchoverlay();

                break;
            case 5:
                mMapView.setTileSource(mapSurferTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 2);
                OsMoDroid.editor.commit();

                reinitchoverlay();
                break;
            case 7:
                org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource.retrieveBingKey(globalActivity);
                bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
                mMapView.setTileSource(bingTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 3);
                OsMoDroid.editor.commit();

                reinitchoverlay();
                break;
            case 8:
                BingMapTileSource.retrieveBingKey(globalActivity);
                bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
                mMapView.setTileSource(bingTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 4);
                OsMoDroid.editor.commit();
                reinitchoverlay();
                break;
            case 9:
                item.setChecked(!item.isChecked());
                OsMoDroid.editor.putBoolean("adjust_to_dpi", item.isChecked());
                OsMoDroid.editor.commit();
                IGeoPoint g = mMapView.getMapCenter();
                mMapView.setTilesScaledToDpi(item.isChecked());
                mMapView.invalidate();
                mController.setCenter(g);
                reinitchoverlay();
                break;
            case 10:
                mMapView.setTileSource(sputnikTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 6);
                OsMoDroid.editor.commit();
                reinitchoverlay();
                break;
            case 11:
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                final SeekBar input = new SeekBar(getActivity());
                input.setMax(30);
                input.setProgress(OsMoDroid.settings.getInt("pointsize", 8));
                input.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        OsMoDroid.editor.putInt("pointsize", input.getProgress());
                        OsMoDroid.editor.commit();
                        mMapView.invalidate();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }
                });
                layout.addView(input);
                AlertDialog alertdialog3 = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.size_of_point)
                        .setView(layout)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        OsMoDroid.editor.putInt("pointsize", input.getProgress());
                                        OsMoDroid.editor.commit();
                                        mMapView.invalidate();
                                    }
                                })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).create();
                alertdialog3.show();
                break;
            case 12:
                mMapView.setTileSource(outdoorTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 7);
                OsMoDroid.editor.commit();

                reinitchoverlay();
                break;
            case 14:
                item.setChecked(!item.isChecked());
                OsMoDroid.editor.putBoolean("arrows", item.isChecked());
                OsMoDroid.editor.commit();
                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 15:
                item.setChecked(!item.isChecked());
                OsMoDroid.editor.putBoolean("longpath", item.isChecked());
                OsMoDroid.editor.commit();
                mMapView.invalidate();
                break;
            case 16:
                item.setChecked(!item.isChecked());
                OsMoDroid.editor.putBoolean("fullgpx", item.isChecked());
                OsMoDroid.editor.commit();
                mMapView.invalidate();
                break;
            case 17:
                item.setChecked(!item.isChecked());
                OsMoDroid.editor.putBoolean("shortname", item.isChecked());
                OsMoDroid.editor.commit();
                mMapView.invalidate();
                if (OsMoDroid.settings.getBoolean("shortname", false)) {
                    //       RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)speddTextView.getLayoutParams();
                    //     params.addRule(RelativeLayout.RIGHT_OF, R.id.imageButtonCenter);
                    //    speddTextView.setLayoutParams(params);
                    speddTextView.setTextSize(20);
                } else {
                    //  RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)speddTextView.getLayoutParams();
                    //  params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    //  speddTextView.setLayoutParams(params);
                    speddTextView.setTextSize(100);
                }
                break;
            case 18:
                mMapView.setTileSource(chepeTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 8);
                OsMoDroid.editor.commit();

                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 19:
                mMapView.setTileSource(mtbTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 9);
                OsMoDroid.editor.commit();

                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 20:
                mMapView.setTileSource(wikiTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 10);
                OsMoDroid.editor.commit();

                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 21:
                mMapView.setTileSource(hdMapnikTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 11);
                OsMoDroid.editor.commit();
                reinitchoverlay();
                mMapView.invalidate();
                break;
            case 22:
                mMapView.setTileSource(topoTileSource);
                OsMoDroid.editor.putInt("selectedTileSourceInt", 12);
                OsMoDroid.editor.commit();
                reinitchoverlay();
                mMapView.invalidate();
                break;
            default:
                break;
        }
        for(int i = 0; i < jsonTileSourceArrayList.size(); i++)
        {
            if(item.getItemId()==(i+100))
            {
                mMapView.setTileSource(jsonTileSourceArrayList.get(i));
                OsMoDroid.editor.putInt("selectedTileSourceInt", i+100);
                OsMoDroid.editor.commit();
                reinitchoverlay();
                mMapView.invalidate();
                break;
            }

        }
        mController.setCenter(c);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "map oncreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //setRetainInstance(true);
        LocalService.devlistener = this;
        jsonTileSourceArrayList = new ArrayList<ITileSource>();
        try {
            JSONArray mapJSON = new JSONArray(readMapJson());
            for (int i=0; i < mapJSON.length(); i++) {
                JSONObject jo = mapJSON.getJSONObject(i);
                JSONArray jsonArray = jo.getJSONArray("url");
                String[] arr=new String[jsonArray.length()];
                for(int k=0; k<arr.length; k++) {
                    arr[k]=jsonArray.optString(k,"");
                }
                final ITileSource tileSource = new customTileSource(
                        jo.optString("name",""),
                        jo.optInt("min_zoom"),
                        jo.optInt("max_zoom",18),
                        jo.optInt("tile_size",256),
                        jo.optString("file_ext",".png"),
                        arr);
               jsonTileSourceArrayList.add(tileSource);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(getClass().getSimpleName(), "map onattach");
        globalActivity = (GPSLocalServiceClient) activity;

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        LocalService.devlistener = null;

        mMapView = null;
        mController = null;
        myLoc.disableMyLocation();
        myLoc = null;
        //myTracePathOverlay=null;
        globalActivity = null;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.d(getClass().getSimpleName(), "map onpause");
        mMapView.getOverlays().remove(myLoc);
        myLoc.disableMyLocation();
        mMapView.removeMapListener(wrappedListener);
        LocalService.myIM.sendToServer("SP:" + choverlay.followdev + "|0", false);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(getClass().getSimpleName(), "map onResume");
        OsMoDroid.mFirebaseAnalytics.logEvent("MAP_OPEN", null);
        globalActivity.actionBar.setTitle(getString(R.string.map));
        mMapView.getOverlays().add(myLoc);
        myLoc.enableMyLocation();
        sendcentercoords();
        prevGeoPoint = (GeoPoint) mMapView.getMapCenter();
        wrappedListener = new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {

                GeoPoint mapCenter = (GeoPoint) scrollEvent.getSource().getMapCenter();

                if (prevGeoPoint != null && mapCenter.distanceToAsDouble(prevGeoPoint) > 100000 / (scrollEvent.getSource().getZoomLevel() + 1)) {

                    sendcentercoords();
                    prevGeoPoint = mapCenter;
                }
                ;

                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                sendcentercoords();
                return false;
            }
        };
        mMapView.addMapListener(wrappedListener);
        if (LocalService.myIM != null && LocalService.myIM.authed) {
            LocalService.myIM.sendToServer("SP:" + choverlay.followdev + "|1", false);
        }
        super.onResume();
    }

    private void sendcentercoords() {
        if (LocalService.myIM != null && LocalService.myIM.authed) {

            JSONObject json = new JSONObject();
            try {
                json.put("lat", OsMoDroid.df6.format(mMapView.getMapCenter().getLatitude()));
                json.put("lon", OsMoDroid.df6.format(mMapView.getMapCenter().getLongitude()));
                json.put("zoom", mMapView.getZoomLevel());

                json.put("bbox", "N:" + OsMoDroid.df6.format(mMapView.getBoundingBox().getLatNorth()) + "; E:" + OsMoDroid.df6.format(mMapView.getBoundingBox().getLonEast()) + "; S:" + OsMoDroid.df6.format(mMapView.getBoundingBox().getLatSouth()) + "; W:" + OsMoDroid.df6.format(mMapView.getBoundingBox().getLonWest()));
                json.put("mapid", OsMoDroid.settings.getInt("selectedTileSourceInt", 1));
            } catch (JSONException e) {

            }
            globalActivity.mService.myIM.sendToServer("SM|" + json.toString(), false);
        }
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
        Log.d(getClass().getSimpleName(), "map oncreateview");
        Configuration.getInstance().setMapViewHardwareAccelerated(true);

        final String name = "MapSurfer";
        final int aZoomMinLevel = 0;
        final int aZoomMaxLevel = 18;
        final int aTileSizePixels = 256;
        final String aImageFilenameEnding = ".png";
        final String[] aBaseUrl = new String[]{"http://korona.geog.uni-heidelberg.de/tiles/roads/"};
        final String[] sputnikURL = new String[]{"http://b.tiles.maps.sputnik.ru/"};
        final String[] outdoorURL = new String[]{"http://tile.thunderforest.com/outdoors/"};
        final String[] chepeURL = new String[]{"https://tile-a.openstreetmap.fr/hot/"};
        final String[] mtbURL = new String[]{"http://tile.mtbmap.cz/mtbmap_tiles/"};
        final String[] wikiURL = new String[]{"https://maps.wikimedia.org/osm-intl/"};
        final String[] hdMapnikURL = new String[]{"https://osm.rrze.fau.de/osmhd/"};
        final String[] topoURL = new String[]{"https://tile.opentopomap.org/"};

        bingTileSource = new BingMapTileSource(null);
        //sputnikTileSource = new SputnikTileSource("Sputnik",  aZoomMinLevel, aZoomMaxLevel, 512, aImageFilenameEnding, sputnikURL);
        outdoorTileSource = new OutdoorTileSource("OutDoor", aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, outdoorURL);
        mtbTileSource = new OutdoorTileSource("MTB", aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, mtbURL);
        chepeTileSource = new OutdoorTileSource("Chepe", aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, chepeURL);
        mapSurferTileSource = new MAPSurferTileSource(name, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
        wikiTileSource = new OutdoorTileSource("Wiki", aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, wikiURL);
        hdMapnikTileSource = new OutdoorTileSource("MapnikHD", aZoomMinLevel, aZoomMaxLevel, 512, aImageFilenameEnding, hdMapnikURL);
        topoTileSource = new OutdoorTileSource("Topo", aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, topoURL);
        View view = inflater.inflate(R.layout.map, container, false);
        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.relative);
        CustomTileProvider customTileProvider = new CustomTileProvider(getActivity());

        mMapView = new MapView(getActivity(), customTileProvider);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mMapView.setLayoutParams(lp);
        mMapView.setTilesScaledToDpi(OsMoDroid.settings.getBoolean("adjust_to_dpi", true));

        //mMapView.setTilesScaledToDpi(true);
        rl.addView(mMapView, 0);
        ImageButton centerImageButton = (ImageButton) view.findViewById(R.id.imageButtonCenter);
        Button rotateButton = (Button) view.findViewById(R.id.buttonRotate);
        speddTextView = (TextView) view.findViewById(R.id.mapSpeedtextView);
        if (OsMoDroid.settings.getBoolean("shortname", false)) {
            //                   RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)speddTextView.getLayoutParams();
            //                 params.addRule(RelativeLayout.RIGHT_OF, R.id.imageButtonCenter);
            //               speddTextView.setLayoutParams(params);
            speddTextView.setTextSize(20);
        } else {
            //                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)speddTextView.getLayoutParams();
            ///                   params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            //                speddTextView.setLayoutParams(params);
            speddTextView.setTextSize(100);
        }

        switch (OsMoDroid.settings.getInt("selectedTileSourceInt", 1)) {
            case 1:

                mMapView.setTileSource(TileSourceFactory.MAPNIK);
                break;
            case 2:
                mMapView.setTileSource(mapSurferTileSource);
                break;
            case 3:
                BingMapTileSource.retrieveBingKey(globalActivity);
                bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
                mMapView.setTileSource(bingTileSource);
                break;
            case 4:
                BingMapTileSource.retrieveBingKey(globalActivity);
                bingTileSource.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
                mMapView.setTileSource(bingTileSource);
                break;
            case 6:
                mMapView.setTileSource(sputnikTileSource);
                break;
            case 7:
                mMapView.setTileSource(outdoorTileSource);
                break;
            case 8:
                mMapView.setTileSource(chepeTileSource);
                break;
            case 9:
                mMapView.setTileSource(mtbTileSource);
                break;
            case 10:
                mMapView.setTileSource(wikiTileSource);
                break;
            case 11:
                mMapView.setTileSource(hdMapnikTileSource);
                break;
            case 12:
                mMapView.setTileSource(topoTileSource);
                break;
            default:
                break;
        }
        for(int i = 0; i < jsonTileSourceArrayList.size(); i++)
        {
            if(OsMoDroid.settings.getInt("selectedTileSourceInt", 1)==(i+100))
            {
                mMapView.setTileSource(jsonTileSourceArrayList.get(i));
                break;
            }

        }
//			if(myTracePathOverlay==null)
//			{
//				myTracePathOverlay=new PathOverlay(Color.RED, 10, mResourceProxy);
//				myTracePathOverlay.addPoints(LocalService.traceList);
//			} else
//			{
//				myTracePathOverlay.clearPath();
//				myTracePathOverlay.addPoints(LocalService.traceList);
//			}
        //mMapView.getOverlays().add(myTracePathOverlay);
        myLoc = new MyLocationNewOverlay(this, mMapView);
        Bitmap bitmapNotMoving = BitmapFactory.decodeResource(getResources(), R.drawable.twotone_navigation_black_48);
        Bitmap bitmapMoving = BitmapFactory.decodeResource(getResources(), R.drawable.twotone_navigation_black_48);
        myLoc.setDirectionArrow(bitmapNotMoving, bitmapMoving);
        myLoc.setOptionsMenuEnabled(true);

        if (OsMoDroid.settings.getBoolean("isfollow", true)) {
            myLoc.enableFollowLocation();
        }
        //mMapView.getOverlays().add(myLoc);
        mMapView.setBuiltInZoomControls(true);
        mMapView.getZoomController().getDisplay().setBitmaps(
                ((BitmapDrawable) mMapView.getResources().getDrawable(R.drawable.ic_zoom_in)).getBitmap(),
                ((BitmapDrawable) mMapView.getResources().getDrawable(R.drawable.ic_zoom_in)).getBitmap(),
                ((BitmapDrawable) mMapView.getResources().getDrawable(R.drawable.ic_zoom_out)).getBitmap(),
                ((BitmapDrawable) mMapView.getResources().getDrawable(R.drawable.ic_zoom_out)).getBitmap()
        );
        mMapView.getZoomController().getDisplay().setAdditionalPixelMargins(10, 10, 10, 10);
        mMapView.setMultiTouchControls(true);
        mController = mMapView.getController();
        Bundle bundle = getArguments();
        if (OsMoDroid.settings.getInt("centerlat", -1) != -1 && bundle == null) {
            if (mController != null) {
                mController.setZoom(OsMoDroid.settings.getInt("zoom", 10));
                mController.setCenter(new GeoPoint(OsMoDroid.settings.getInt("centerlat", 0), OsMoDroid.settings.getInt("centerlon", 0)));
                Log.d(this.getClass().getName(), "Center map on =" + OsMoDroid.settings.getInt("centerlat", 0) + OsMoDroid.settings.getInt("centerlon", 0));
            }
            new Handler(Looper.getMainLooper()).post(
                    new Runnable() {
                        public void run() {

                        }
                    }
            );
        } else {
            mController.setZoom(10);
        }
        centerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location center = MapFragment.this.getLastKnownLocation();
                if (center != null) {
                    mController.setCenter(new GeoPoint(MapFragment.this.getLastKnownLocation()));
                    // mController.setZoom(16);
                }
                myLoc.enableFollowLocation();
            }
        });
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.setMapOrientation(0);
                Log.d(getClass().getSimpleName(), "map click on compas");
                OsMoDroid.editor.putBoolean("courserotation", !OsMoDroid.settings.getBoolean("courserotation", false));
                OsMoDroid.editor.commit();
                courserotation.setChecked(OsMoDroid.settings.getBoolean("courserotation", false));
            }
        });
        CompassOverlay compas = new CompassOverlay(getActivity(), mMapView);
        choverlay = new ChannelsOverlay(mMapView, false);
        mMapView.getOverlays().add(choverlay);
        mMapView.getOverlays().add(compas);
        compas.enableCompass();
        mMapView.setKeepScreenOn(true);

        if (bundle != null) {
            mController.setCenter(new GeoPoint(bundle.getFloat("lat"), bundle.getFloat("lon")));
            ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            items.add(new OverlayItem("", "", new GeoPoint(bundle.getFloat("lat"), bundle.getFloat("lon"))));
            ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, getContext());
            mOverlay.setFocusItemsOnTap(true);
            mMapView.getOverlays().add(mOverlay);
        }

        return view;
    }

    private void reinitchoverlay() {
        mMapView.getOverlays().remove(choverlay);
        choverlay = new ChannelsOverlay(mMapView, false);
        mMapView.getOverlays().add(choverlay);
    }

    @Override
    public void onDeviceChange(Device dev) {
        Log.d(getClass().getSimpleName(), "ondevicechange");
        if (choverlay != null && dev.u == choverlay.followdev) {
            mController.setCenter(new GeoPoint(dev.lat, dev.lon));
        }
        mMapView.invalidate();
    }

    @Override
    public void onChannelListChange() {
        mMapView.invalidate();
        LocalService.channelsupdated = true;
    }

    @Override
    public Location getLastKnownLocation() {
        Location forcelocation = null;
        Location forcenetworklocation = null;
        try {
            forcelocation = LocalService.myManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            forcenetworklocation = LocalService.myManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (forcelocation != null) {
            if (forcenetworklocation != null) {
                if (forcenetworklocation.getTime() > forcelocation.getTime()) {
                    return forcenetworklocation;
                }
            }
            return forcelocation;
        } else {
            return forcenetworklocation;
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer locationConsumer) {
        myLocationConumer = locationConsumer;
        boolean result = false;
        if (LocalService.myManager != null) {
            for (final String provider : LocalService.myManager.getProviders(true)) {
                if (LocationManager.GPS_PROVIDER.equals(provider) || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                    result = true;
                    try {
                        LocalService.myManager.requestLocationUpdates(provider, 0, 0, this);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void stopLocationProvider() {
        if (LocalService.myManager != null) {
            LocalService.myManager.removeUpdates(this);
        }
        myLocationConumer = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (myLoc != null && myLoc.isFollowLocationEnabled() && location.hasBearing() && OsMoDroid.settings.getBoolean("courserotation", false) && location.getSpeed() > 1) {
            mMapView.setMapOrientation(-location.getBearing());
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            lastgpslocation = System.currentTimeMillis();
        }
        if (myLocationConumer != null) {
            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                if (System.currentTimeMillis() > lastgpslocation + 30000) {
                    myLocationConumer.onLocationChanged(location, this);
                }
            } else {
                myLocationConumer.onLocationChanged(location, this);
            }
        }
        if (speddTextView != null) {
            if (myLoc != null && myLoc.isFollowLocationEnabled()) {
                if (!OsMoDroid.settings.getBoolean("imperial", false)) {
                    speddTextView.setText(OsMoDroid.df0.format(location.getSpeed() * 3.6));
                } else {
                    speddTextView.setText(OsMoDroid.df0.format(location.getSpeed() * 3.6 * 0.621371));
                }
            } else {
                speddTextView.setText("");
            }

            if (location.getSpeed() * 3.6 < 2 && (int) location.getAccuracy() < Integer.parseInt(OsMoDroid.settings.getString("hdop_gpx", "30").equals("") ? "30" : OsMoDroid.settings.getString("hdop_gpx", "30"))) {
                speddTextView.setText("");
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

    class MAPSurferTileSource extends OnlineTileSourceBase {
        MAPSurferTileSource(String aName, int aZoomMinLevel,
                            int aZoomMaxLevel, int aTileSizePixels,
                            String aImageFilenameEnding, String... aBaseUrl) {
            super(aName, aZoomMinLevel, aZoomMaxLevel,
                    aTileSizePixels, aImageFilenameEnding, aBaseUrl);
        }

        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "z=" + MapTileIndex.getZoom(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&x=" + MapTileIndex.getX(pMapTileIndex)
                    + mImageFilenameEnding;
        }


    }

    //        class SputnikTileSource extends OnlineTileSourceBase
//            {
//                SputnikTileSource(String aName,  int aZoomMinLevel,
//                                  int aZoomMaxLevel, int aTileSizePixels,
//                                  String aImageFilenameEnding, String... aBaseUrl)
//                    {
//                        super(aName,  aZoomMinLevel, aZoomMaxLevel,
//                                aTileSizePixels, aImageFilenameEnding, aBaseUrl);
//                    }
//                @Override
//                public String getTileURLString(MapTile aTile)
//                    {
//                        //	x=710&y=381&z=10
//                        return getBaseUrl() + aTile.getZoomLevel() + '/' + aTile.getX() + '/' + aTile.getY() + ".png";
//                    }
//            }

    class customTileSource extends  OnlineTileSourceBase{

        public customTileSource(String aName, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
            super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
        }

        @Override
        public String getTileURLString(long pMapTileIndex) {
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);

            formatter.format(getBaseUrl(),
                    MapTileIndex.getZoom(pMapTileIndex),
                    MapTileIndex.getX(pMapTileIndex),
                    MapTileIndex.getY(pMapTileIndex),
                    imageFilenameEnding());
            return sb.toString();
        }
    }
    class OutdoorTileSource extends OnlineTileSourceBase {
        OutdoorTileSource(String aName, int aZoomMinLevel,
                          int aZoomMaxLevel, int aTileSizePixels,
                          String aImageFilenameEnding, String... aBaseUrl) {
            super(aName, aZoomMinLevel, aZoomMaxLevel,
                    aTileSizePixels, aImageFilenameEnding, aBaseUrl);
        }

        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + '/' + MapTileIndex.getX(pMapTileIndex) + '/' + MapTileIndex.getY(pMapTileIndex) + imageFilenameEnding();
        }
    }

    public class CustomTileProvider extends MapTileProviderBasic {
        public CustomTileProvider(Context pContext) {
            this(new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
                    TileSourceFactory.DEFAULT_TILE_SOURCE);
        }

        public CustomTileProvider(IRegisterReceiver pRegisterReceiver, INetworkAvailablityCheck aNetworkAvailablityCheck,
                                  ITileSource pTileSource) {

            super(pRegisterReceiver, aNetworkAvailablityCheck, pTileSource, getActivity(), null);
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

    public String readMapJson() {
        try {
            File yourFile = new File(OsMoDroid.osmodirFile, "maps.json");
            FileInputStream stream = null;
            String jsonStr = null;
            try {
                stream = new FileInputStream(yourFile);
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                jsonStr = Charset.defaultCharset().decode(bb).toString();
                return jsonStr;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
