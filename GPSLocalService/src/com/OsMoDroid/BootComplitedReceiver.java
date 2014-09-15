package com.OsMoDroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootComplitedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		PreferenceManager.setDefaultValues(arg0, R.xml.pref, true);
		  SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(arg0);
		  if (settings.getBoolean("autostart", false))
		  {
			  Intent is = new Intent(arg0, LocalService.class);
				arg0.startService(is);
		  }

	}

}
