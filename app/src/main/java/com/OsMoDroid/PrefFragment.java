package com.OsMoDroid;


import android.os.Bundle;
import android.util.Log;


import androidx.annotation.Nullable;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;


public class PrefFragment extends PreferenceFragmentCompat {
    private static final int PERMISSION_REQUEST_CODE = 555;



    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.pref, rootKey);
        Preference pref = findPreference("udpmode");
        if (OsMoDroid.settings.getBoolean("started", false)) {
            pref.setEnabled(false);
            pref.setShouldDisableView(true);


        }
        SeekBarPreference sp = findPreference("period");
        sp.setShowSeekBarValue(true);
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


