package com.OsMoDroid;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
public class ChannelsDevicesAdapter extends ArrayAdapter<Device>
    {
        private TextView channelDeviceName;
        private TextView channelDeviceWhere;
        private TextView channelDeviceSpeed;
        private TextView channelDeviceDistance;
        private Location channelDeviceLocation = new Location("");
        private Context context;
        public ChannelsDevicesAdapter(Context context, int textViewResourceId, List<Device> objects)
            {
                super(context, textViewResourceId, objects);
                this.context = context;
            }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
            {
                View row = convertView;
                if (row == null)
                    {
                        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        row = inflater.inflate(R.layout.channelsdeviceitem, parent, false);
                    }
                Device device = getItem(position);
                Log.d(getClass().getSimpleName(), "channeldevice:" + device.name + " " + device.lat + " " + device.lon);
                channelDeviceName = (TextView) row.findViewById(R.id.txtName);
                channelDeviceWhere = (TextView) row.findViewById(R.id.txtWhere);
                channelDeviceSpeed = (TextView) row.findViewById(R.id.txtSpeed);
                channelDeviceDistance = (TextView) row.findViewById(R.id.TextDistance);
                channelDeviceName.setTextColor(device.color);
                channelDeviceWhere.setTextColor(device.color);
                channelDeviceSpeed.setTextColor(device.color);
                channelDeviceDistance.setTextColor(device.color);
                if (device.name != null)
                    {
                        channelDeviceName.setText(device.name);
                    }
                if (!device.speed.equals(""))
                    {
                        channelDeviceSpeed.setVisibility(View.VISIBLE);
                        channelDeviceSpeed.setText(context.getString(R.string.speed)+':'+device.speed);
                    }
                else
                    {
                        channelDeviceSpeed.setVisibility(View.GONE);
                    }
                if (device.lat != 0f && device.lon != 0f)
                    {

                        if (device.updatated > 0)
                            {
                                channelDeviceWhere.setText(context.getString(R.string.coordinats) + device.lat + " " + device.lon + ' ' + LocalService.formatInterval(System.currentTimeMillis() - device.updatated));
                            }
                        else
                            {
                                channelDeviceWhere.setText(context.getString(R.string.coordinats) + device.lat + " " + device.lon + " -");
                            }
                        if (LocalService.currentLocation != null)
                            {
                                channelDeviceDistance.setVisibility(View.VISIBLE);
                                channelDeviceLocation.setLatitude((device.lat));
                                channelDeviceLocation.setLongitude((device.lon));
                                channelDeviceDistance.setText(context.getString(R.string.Distantion) + Integer.toString((int) LocalService.currentLocation.distanceTo(channelDeviceLocation) / 1000) + context.getString(R.string.Km) + Integer.toString((int) (1000 * (LocalService.currentLocation.distanceTo(channelDeviceLocation) / 1000 - (int) LocalService.currentLocation.distanceTo(channelDeviceLocation) / 1000))) + context.getString(R.string.m));
                            }
                        else
                            {
                                channelDeviceDistance.setVisibility(View.GONE);
                            }
                    }
                else
                    {
                        channelDeviceWhere.setText(context.getString(R.string.unknown_location_now));
                        channelDeviceDistance.setVisibility(View.GONE);
                    }
                return row;
            }
    }
