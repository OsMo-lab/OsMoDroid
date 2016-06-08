package com.OsMoDroid;
import com.OsMoDroid.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
//import android.util.Log;
public class PrefActivity extends PreferenceActivity
    {
        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                //  Log.d(getClass().getSimpleName(), "oncreate() prefactivity");
                addPreferencesFromResource(R.xml.pref);
//                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
//                    {
//                        Preference prefereces = findPreference("sdpath");
//                        prefereces.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
//                            {
//                                public boolean onPreferenceClick(Preference preference)
//                                    {
//                                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
//                                                .addCategory(Intent.CATEGORY_OPENABLE)
//                                                .setType("*/*");
//                                        startActivityForResult(intent, 1);
//                                        return true;
//                                    }
//                            });
//                    }
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
    }


