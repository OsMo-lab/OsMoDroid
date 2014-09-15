package com.OsMoDroid;

import org.osmdroid.util.GeoPoint;

public interface DeviceChange {
	public void onDeviceChange (Device dev);
	public void onChannelListChange();
	public void onNewPoint(GeoPoint geopoint);
	
}
