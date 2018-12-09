package com.OsMoDroid;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.util.Linkify;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static com.OsMoDroid.LocalService.addlog;

public class SosActivity extends Activity
    {
        SoundPool s;
        long[] pattern = {0, 100, 100, 200, 100, 300};

        Vibrator vibrator;
        private final BroadcastReceiver finisReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        private int alarmStreamId;
        private int sound=1;
        private Ringtone r;
        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                 s= new SoundPool(1,AudioManager.STREAM_ALARM, 0);
                registerReceiver(finisReceiver,new IntentFilter("closesos"));
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                final Window win = getWindow();
                win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                setContentView(R.layout.activity_sos);
                TextView txt = (TextView) findViewById(R.id.textViewSOS);
                RelativeLayout rl = (RelativeLayout)findViewById(R.id.fon);
                if (getIntent().hasExtra("message"))
                    {
                        txt.setText(getIntent().getStringExtra("message"));
                    }




                Button b1 = (Button)findViewById(R.id.buttonSOS1);
                Button b2 = (Button)findViewById(R.id.buttonSOS2);
                Button b3 = (Button)findViewById(R.id.buttonSOS3);
                if(getIntent().hasExtra("jo"))
                    {
                        try
                            {
                                JSONObject jo = new JSONObject(getIntent().getStringExtra("jo"));
                                String title=jo.optString("title");
                                String message=jo.optString("message");
                                sound= jo.optInt ("sound");//0/1/2) - звук (нет/СОС сирена/уведомления (стандартный звук нотификации))
                                String button1=jo.optString("button1");// - текст первой кнопки (не выводить кнопку если нет такого свойства или пустое)
                                String button2= jo.optString("button2");// текст второй кнопки (не выводить кнопку если нет такого свойства или пустое)
                                String button3= jo.optString("button3");// текст второй кнопки (не выводить кнопку если нет такого свойства или пустое)
                                txt.setText(title+'\n'+message);
                                final String group=jo.optString("group");
                                Linkify.addLinks(txt, Linkify.ALL);
                                if(jo.has("color"))
                                    {
                                        try {
                                            rl.setBackgroundColor(Color.parseColor(jo.optString("color")));
                                        } catch (Exception e) {
                                            addlog("invalid color SOS");
                                        }

                                    }
                                if(button1.equals(""))
                                    {
                                        b1.setVisibility(View.GONE);
                                    }
                                else
                                    {
                                        b1.setText(button1);
                                        b1.setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                    {
                                                        LocalService.myIM.sendToServer("BT:"+group+"|1",true);
                                                        finish();
                                                    }
                                            });
                                    }
                                if(button2.equals(""))
                                    {
                                        b2.setVisibility(View.GONE);
                                    }
                                else
                                    {
                                        b2.setText(button2);
                                        b2.setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                    {
                                                        LocalService.myIM.sendToServer("BT:"+group+"|2",true);
                                                        finish();
                                                    }
                                            });
                                    }
                                if(button3.equals(""))
                                {
                                    b3.setVisibility(View.GONE);
                                }
                                else
                                {
                                    b3.setText(button3);
                                    b3.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            LocalService.myIM.sendToServer("BT:"+group+"|3",true);
                                            finish();
                                        }
                                    });
                                }




                            }
                        catch (JSONException e)
                            {
                                e.printStackTrace();
                            }

                    }
                else
                    {
                        b2.setVisibility(View.GONE);
                        b3.setVisibility(View.GONE);
                        b1.setText(R.string.closesos);
                        b1.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                    {
                                        finish();
                                    }
                            });
                    }
                int alarmsound = s.load(this, R.raw.sos, 1);
                SoundPool.OnLoadCompleteListener l = new SoundPool.OnLoadCompleteListener()
                    {
                        @Override
                        public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
                            {
                                if(sound==1)
                                    {
                                        vibrator.vibrate(pattern, 0);
                                        alarmStreamId = s.play(sampleId, 1f, 1f, 1, -1, 1f);
                                    }
                                if(sound==2)
                                    {
                                        vibrator.vibrate(pattern, -1);
                                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                        r.setStreamType(AudioManager.STREAM_ALARM);
                                        r.play();

                                    }
                            }
                    };
                s.setOnLoadCompleteListener(l);

            }
        @Override
        protected void onDestroy()
            {
                unregisterReceiver(finisReceiver);
                if(r!=null)
                {
                    r.stop();
                }
                vibrator.cancel();
                s.stop(alarmStreamId);

                super.onDestroy();
            }
    }
