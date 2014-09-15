package com.OsMoDroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.aboutactivity);
		TextView txt= (TextView)findViewById(R.id.infotextView);
		String strVersionName = getString(R.string.Unknow);

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			strVersionName = packageInfo.packageName + ' '
					+ packageInfo.versionName+' '+packageInfo.versionCode;;
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
		}
		txt.setText(strVersionName+'\n'+txt.getText());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		 Log.d("AboutActivity", "OnDestroy");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		 Log.d("AboutActivity", "OnPause");
		 
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
			Log.d("AboutActivity", "OnResume");
	}

	

}
