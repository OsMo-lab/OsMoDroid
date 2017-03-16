package com.OsMoDroid;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;


import org.w3c.dom.Text;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
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


import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
public class MapFragment extends Fragment implements DeviceChange,  LocationListener
    {
        com.mapbox.mapboxsdk.maps.MapView mMapView;
        View convertView;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            convertView = inflater.inflate(R.layout.glmap, null);

            mMapView = (MapView) convertView.findViewById(R.id.glMapView);
            mMapView.setStyle(Style.MAPBOX_STREETS);
            mMapView.onCreate(savedInstanceState);
            return convertView;
        }

        @Override
        public void onLocationChanged(Location location)
            {
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
            }
        @Override
        public void onChannelListChange()
            {
            }

    }
