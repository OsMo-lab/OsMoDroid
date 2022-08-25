package com.OsMoDroid;


import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;


public class NotifyPrefFragment extends PreferenceFragmentCompat {
    private static final int PERMISSION_REQUEST_CODE = 555;



    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.prefnotif, rootKey);


    }

    @Override
        public void onDestroy()
            {
                Log.d(getClass().getSimpleName(), "ondestroy() preffragment");
                super.onDestroy();
            }


    @Override
    public void onResume() {
        super.onResume();

//        com.pavelsikun.seekbarpreference.SeekBarPreference s = (com.pavelsikun.seekbarpreference.SeekBarPreference)findPreference("refreshrateinteger");
//        s.setMaxValue(10+s.getCurrentValue());
    }


}


