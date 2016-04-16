package com.OsMoDroid;import org.osmdroid.views.drawing.OsmBitmapShader;import android.app.Activity;import android.content.Intent;import android.content.SharedPreferences;import android.os.Bundle;import android.preference.PreferenceManager;import android.util.Log;import android.view.View;import android.view.View.OnClickListener;import android.widget.Button;import android.widget.TextView;public class WarnActivity extends Activity    {        @Override        public void onBackPressed()            {                if (!OsMoDroid.gpslocalserviceclientVisible)                    {                        Intent i = new Intent(this, GPSLocalServiceClient.class);                        i.setAction(Intent.ACTION_MAIN);                        i.addCategory(Intent.CATEGORY_LAUNCHER);                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);                        startActivity(i);                    }                super.onBackPressed();            }        @Override        protected void onCreate(Bundle savedInstanceState)            {                Log.d("WarnActivity", getIntent().getStringExtra("info"));                super.onCreate(savedInstanceState);            }        @Override        protected void onDestroy()            {                // TODO Auto-generated method stub                super.onDestroy();                Log.d("WarnActivity", "OnDestroy");            }        @Override        protected void onPause()            {                // TODO Auto-generated method stub                super.onPause();                Log.d("WarnActivity", "OnPause");                finish();            }        @Override        protected void onNewIntent(Intent intent)            {                super.onNewIntent(intent);                setIntent(intent);            }        @Override        protected void onResume()            {                // TODO Auto-generated method stub                super.onResume();                final SharedPreferences settings = PreferenceManager                        .getDefaultSharedPreferences(this);                setContentView(R.layout.warnactivity);                Log.d("WarnActivity", getIntent().getStringExtra("info"));                TextView txv = (TextView) findViewById(R.id.infotextView);                txv.setText(getIntent().getStringExtra("info"));                Button support = (Button) findViewById(R.id.supportbutton);                if (getIntent().getBooleanExtra("supportButton", false))                    {                        support.setOnClickListener(new OnClickListener()                        {                            public void onClick(View v)                                {                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);                                    sendIntent.setType("text/plain");                                    sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"support@osmo.mobi"});                                    sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, getIntent().getStringExtra("info") + " Устройство: " + settings.getString("device", "") + ", newkey: " + settings.getString("newkey", ""));                                    sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Error");                                    startActivity(Intent.createChooser(sendIntent, "Email"));                                }                        });                    }                switch (getIntent().getIntExtra("mode", 0))                    {                        case OsMoDroid.NOTIFY_ERROR_SENDID:                            break;                        case OsMoDroid.NOTIFY_EXPIRY_USER:                            support.setText(R.string.auth);                            support.setOnClickListener(new OnClickListener()                            {                                public void onClick(View v)                                    {                                        Intent i = new Intent(WarnActivity.this, GPSLocalServiceClient.class);                                        i.setAction(Intent.ACTION_MAIN);                                        i.addCategory(Intent.CATEGORY_LAUNCHER);                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);                                        i.putExtra("auth", true);                                        startActivity(i);                                    }                            });                            break;                        case OsMoDroid.NOTIFY_NO_CONNECT:                            support.setText(R.string.understand);                            support.setOnClickListener(new OnClickListener()                            {                                public void onClick(View v)                                    {                                        OsMoDroid.editor.putBoolean("understand", true);                                        OsMoDroid.editor.commit();                                        if (!OsMoDroid.gpslocalserviceclientVisible)                                            {                                                Intent i = new Intent(WarnActivity.this, GPSLocalServiceClient.class);                                                i.setAction(Intent.ACTION_MAIN);                                                i.addCategory(Intent.CATEGORY_LAUNCHER);                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);                                                startActivity(i);                                            }                                        finish();                                    }                            });                            break;                        case OsMoDroid.NOTIFY_NO_DEVICE:                            txv.setText("This device was deleted from OSMO.MOBI\nWill create new, if you have been created links(URL) it will invalid now and need to reenter to groups\n You can contact support if like ");                            break;                        default:                            break;                    }                Log.d("WarnActivity", "OnResume");            }    }