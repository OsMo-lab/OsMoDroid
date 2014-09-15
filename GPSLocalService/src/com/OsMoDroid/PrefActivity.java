package com.OsMoDroid;

import com.OsMoDroid.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
//import android.util.Log;

public class PrefActivity extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
   
	  super.onCreate(savedInstanceState);
	//  Log.d(getClass().getSimpleName(), "oncreate() prefactivity");
    addPreferencesFromResource(R.xml.pref);
   
  }

  

@Override
protected void onDestroy() {
	//Log.d(getClass().getSimpleName(), "ondestroy() prefactivity");
	
	super.onDestroy();
	
 
}
}


