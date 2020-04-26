package com.OsMoDroid;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
//import android.util.Log;
public class PrefActivity extends PreferenceActivity  implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final int PERMISSION_REQUEST_CODE = 555;

    @Override
    protected void onStart() {
        super.onStart();


    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Need permission", Toast.LENGTH_LONG).show();
            }
            else
            {
                if(permissions[0].equals("RECEIVE_SMS"))
                {
                    Preference g = findPreference("getsms");
                    g.setEnabled(true);
                }
                if(permissions[0].equals("SEND_SMS"))
                {
                    Preference s = findPreference("sendsms");
                    s.setEnabled(true);
                }
            }

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    Preference.OnPreferenceChangeListener op = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(newValue.toString().equals("true")) {

                if (preference.getKey().equals("sendsms")) {
                    if (ContextCompat.checkSelfPermission(PrefActivity.this,
                            Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PrefActivity.this, new String[]{
                                Manifest.permission.SEND_SMS
                        }, PERMISSION_REQUEST_CODE);
                    }
                }
                if (preference.getKey().equals("getsms")) {
                    if (ContextCompat.checkSelfPermission(PrefActivity.this,
                            Manifest.permission.RECEIVE_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PrefActivity.this, new String[]{
                                Manifest.permission.RECEIVE_SMS
                        }, PERMISSION_REQUEST_CODE);
                    }
                }
            }
            else
            {
                return true;
            }

            return false;
        }
    };

        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                //  Log.d(getClass().getSimpleName(), "oncreate() prefactivity");
                addPreferencesFromResource(R.xml.pref);
                if (true||android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    PreferenceCategory category = (PreferenceCategory) findPreference("smsprefcategory");
                    getPreferenceScreen().removePreference(category);
                }
                else
                {
                    Preference s = findPreference("sendsms");
                    Preference g = findPreference("getsms");

                    s.setOnPreferenceChangeListener(op);
                    g.setOnPreferenceChangeListener(op);


                }



/*
if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
{
Preference prefereces = findPreference("sdpath");
prefereces.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
{
public boolean onPreferenceClick(Preference preference)
{
Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
.addCategory(Intent.CATEGORY_OPENABLE)
.setType("* /*");
startActivityForResult(intent, 1);
return true;
}
});
}
*/
            }
        @Override
        protected void onDestroy()
            {
                //Log.d(getClass().getSimpleName(), "ondestroy() prefactivity");
                super.onDestroy();
            }
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            //get the new value from Intent data
            File t = new File(data.toUri(0));

            OsMoDroid.editor.putString("filePicker", data.getData().toString());
            OsMoDroid.editor.commit();
        }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
//        com.pavelsikun.seekbarpreference.SeekBarPreference s = (com.pavelsikun.seekbarpreference.SeekBarPreference)findPreference("refreshrateinteger");
//        s.setMaxValue(10+s.getCurrentValue());
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("refreshrateinteger"))
        {
//                com.pavelsikun.seekbarpreference.SeekBarPreference s = (com.pavelsikun.seekbarpreference.SeekBarPreference)findPreference("refreshrateinteger");
//                s.setMaxValue(10+2*s.getCurrentValue());

        }
    }
}


