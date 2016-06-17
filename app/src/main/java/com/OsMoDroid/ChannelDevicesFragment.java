package com.OsMoDroid;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.text.ClipboardManager;
import android.text.Layout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.OsMoDroid.Netutil.MyAsyncTask;
public class ChannelDevicesFragment extends Fragment implements ResultsListener
    {
        final private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        ListView lv1;
        ListView lv2;
        Button sendButton;
        EditText input;
        /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
         */
        private GPSLocalServiceClient globalActivity;
        private int channelpos;
        private AdapterContextMenuInfo subacmi;
        //ArrayList<MyAsyncTask> t= new ArrayList<Netutil.MyAsyncTask>();
        @Override
        public boolean onContextItemSelected(android.view.MenuItem item)
            {
                final AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
                if (acmi != null)
                    {
                        subacmi = acmi;
                    }



//                if (item.getItemId() == 7)
//                    {
//                        ColorDialog.OnClickListener cl = new ColorDialog.OnClickListener()
//                        {
//                            @Override
//                            public void onClick(Object tag, int color)
//                                {
//                                    LocalService.deviceList.get((int) acmi.id).color = color;
//                                    JSONObject jo = new JSONObject();
//                                    JSONObject jodata = new JSONObject();
//                                    try
//                                        {
//                                            jodata.putOpt("color", "#" + Integer.toHexString(color));
//                                            jo.put("u", LocalService.deviceList.get((int) acmi.id).u);
//                                            jo.put("data", jodata);
//                                        }
//                                    catch (JSONException e)
//                                        {
//                                            // TODO Auto-generated catch block
//                                            e.printStackTrace();
//                                        }
//                                    if (LocalService.deviceList.get((int) acmi.id).subscribed)
//                                        {
//                                            globalActivity.mService.myIM.sendToServer("DSS|" + jo.toString(), true);
//                                        }
//                                    else
//                                        {
//                                            globalActivity.mService.myIM.sendToServer("DS|" + jo.toString(), true);
//                                        }
//                                }
//                        };
//                        ColorDialog dialog = new ColorDialog(globalActivity, false, getView(), LocalService.deviceList.get((int) acmi.id).color, cl, R.drawable.wheel);
//                        dialog.show();
//                    }
                if (item.getItemId() == 8)
                    {
                        //REMOTE_CONTROL:[tracker_id]|DESTROY_DEVICE
                        LocalService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + OsMoDroid.TRACKER_SESSION_START, true);
                    }
                if (item.getItemId() == 9)
                    {
                        //REMOTE_CONTROL:[tracker_id]|DESTROY_DEVICE
                        globalActivity.mService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + OsMoDroid.TRACKER_SESSION_STOP, true);
                    }
                if (item.getItemId() == 10)
                    {
                        //REMOTE_CONTROL:[tracker_id]|DESTROY_DEVICE
                        LinearLayout layout = new LinearLayout(getActivity());
                        layout.setOrientation(LinearLayout.VERTICAL);
                        final TextView txv5 = new TextView(getActivity());
                        txv5.setText("Enter text to TTS");
                        layout.addView(txv5);
                        final EditText inputhash = new EditText(getActivity());
                        layout.addView(inputhash);
                        AlertDialog alertdialog3 = new AlertDialog.Builder(
                                getActivity())
                                .setTitle("Remote TTS")
                                .setView(layout)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton)
                                                {
                                                    if (!(inputhash.getText().toString().equals("")))
                                                        {
                                                            //REMOTE_CONTROL|TTS:Привет жена)
                                                            globalActivity.mService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + "TTS:" + inputhash.getText().toString(), true);
                                                        }
                                                }
                                        })
                                .setNegativeButton(R.string.No,
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton)
                                                {

								/* User clicked cancel so do some stuff */
                                                }
                                        }).create();
                        alertdialog3.show();
                    }
                if (item.getItemId() == 11)
                    {
                        globalActivity.mService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + OsMoDroid.ALARM_ON, true);
                    }
                if (item.getItemId() == 12)
                    {
                        globalActivity.mService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + OsMoDroid.ALARM_OFF, true);
                    }
                if (item.getItemId() == 13)
                    {
                        globalActivity.mService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + OsMoDroid.SIGNAL_ON, true);
                    }
                if (item.getItemId() == 14)
                    {
                        globalActivity.mService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + OsMoDroid.SIGNAL_OFF, true);
                    }
                if (item.getItemId() == 15)
                    {
                        globalActivity.mService.myIM.sendToServer("SRC:" + LocalService.currentchanneldeviceList.get((int) subacmi.id).u + "|" + OsMoDroid.WHERE, true);
                    }


                if (item.getItemId() == 6)
                    {
                        if (LocalService.channelsDevicesAdapter.getItem(acmi.position).lat != 0)
                            {
                                Log.d(getClass().getSimpleName(), "move to map to device");
                                OsMoDroid.editor.putInt("centerlat", (int) ((LocalService.channelsDevicesAdapter.getItem(acmi.position).lat) * 1E6));
                                OsMoDroid.editor.putInt("centerlon", (int) ((LocalService.channelsDevicesAdapter.getItem(acmi.position).lon) * 1E6));
                                OsMoDroid.editor.putInt("zoom", 16);
                                OsMoDroid.editor.putBoolean("isfollow", false);
                                OsMoDroid.editor.commit();
                                globalActivity.drawClickListener.selectItem(OsMoDroid.context.getString(R.string.map), null);
                                LocalService.currentItemName = OsMoDroid.context.getString(R.string.map);
                            }
                        else
                            {
                                Toast.makeText(globalActivity, R.string.unknown_location_now, Toast.LENGTH_SHORT).show();
                            }
                    }
                return super.onContextItemSelected(item);
            }
        @Override
        public void onDestroyView()
            {
                super.onDestroyView();
            }
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenuInfo menuInfo)
            {
                if(LocalService.currentChannel.type==2)
                    {

                        AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

                        SubMenu menu2 = menu.addSubMenu(Menu.NONE, 100, 20, R.string.remote_commands);
                        MenuItem start = menu2.add(0, 8, 8, R.string.start_monitoring);
                        MenuItem stop = menu2.add(0, 9, 9, R.string.stop_monitoring);
                        //MenuItem sendTTS =menu2.add(0, 10, 10, R.string.send_tts);
                        MenuItem alarmon = menu2.add(0, 11, 11, R.string.play_alarm_on);
                        MenuItem alarmoff = menu2.add(0, 12, 12, R.string.play_alarm_off);
                        MenuItem signalon = menu2.add(0, 13, 13, R.string.signalisation_set_on);
                        MenuItem signaloff = menu2.add(0, 14, 14, R.string.signalisation_set_off);
                        MenuItem where = menu2.add(0, 15, 15, R.string.where_);
                        //    menu.add(0, 2, 2, R.string.messages).setIcon(android.R.drawable.ic_menu_delete);
                        //    menu.add(0, 3, 3, R.string.copylink).setIcon(android.R.drawable.ic_menu_edit);
                        //    menu.add(0, 4, 4, R.string.sharelink).setIcon(android.R.drawable.ic_menu_edit);
                        //    menu.add(0, 5, 5, R.string.openinbrowser).setIcon(android.R.drawable.ic_menu_edit);
                        menu.add(0, 6, 6, R.string.showonmap).setIcon(android.R.drawable.ic_menu_edit);
                        //menu.add(0, 7, 7, R.string.color).setIcon(android.R.drawable.ic_menu_edit);
                        super.onCreateContextMenu(menu, v, menuInfo);

                    }
                else
                    {
                        menu.add(0, 6, 6, R.string.showonmap).setIcon(android.R.drawable.ic_menu_edit);
                    }

                super.onCreateContextMenu(menu, v, menuInfo);
            }
        @Override
        public void onDestroy()
            {
                Log.d(getClass().getSimpleName(), "ChannelDevicesFragment onDestroy");
                LocalService.currentchanneldeviceList = null;
                LocalService.currentChannel = null;
                super.onDestroy();
            }
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                Log.d(getClass().getSimpleName(), "ChannelDevicesFragment onCreate");
                super.onCreate(savedInstanceState);
                setHasOptionsMenu(true);
                //setRetainInstance(true);
                super.onCreate(savedInstanceState);
            }
        @Override
        public void onAttach(Activity activity)
            {
                Log.d(getClass().getSimpleName(), "ChannelDevicesFragment onAttach");
                globalActivity = (GPSLocalServiceClient) activity;// TODO Auto-generated method stub
                super.onAttach(activity);
            }
        @Override
        public void onDetach()
            {
                Log.d(getClass().getSimpleName(), "ChannelDevicesFragment onDetach");
                LocalService.chatmessagelist.clear();
                LocalService.currentDevice = null;
                globalActivity = null;
                super.onDetach();
            }
        @Override
        public void onResume()
            {
                Log.d(getClass().getSimpleName(), "ChannelDevicesFragment onResume");
                LocalService.chatVisible = true;
                globalActivity.actionBar.setTitle(getString(R.string.chanal) +' '+ LocalService.currentChannel.name);
                super.onResume();
            }
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
            {
                MenuItem refresh = menu.add(0, 3, 0, R.string.refresh);
                MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                refresh.setIcon(android.R.drawable.ic_menu_rotate);
                MenuItem hideshow = menu.add(0, 4, 0, "HideShow");
                MenuItemCompat.setShowAsAction(hideshow, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                hideshow.setIcon(android.R.drawable.ic_menu_view);
                super.onCreateOptionsMenu(menu, inflater);
            }
        @Override
        public void onPause()
            {
                Log.d(getClass().getSimpleName(), "ChannelDevicesFragment onPause");
                LocalService.chatVisible = false;
                super.onPause();
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                if (item.getItemId() == 3)
                    {
                        //globalActivity.mService.myIM.sendToServer("GROUP_CONNECT:"+LocalService.currentChannel.group_id);
                        globalActivity.mService.myIM.sendToServer("GROUP", true);
                        //globalActivity.mService.myIM.sendToServer("GC:"+LocalService.currentChannel.u);
                    }
                if (item.getItemId() == 4)
                    {
                        if (lv1.getVisibility() == View.VISIBLE)
                            {
                                lv1.setVisibility(View.GONE);
                                OsMoDroid.editor.putBoolean("showdevices", false);
                                OsMoDroid.editor.commit();
                            }
                        else
                            {
                                lv1.setVisibility(View.VISIBLE);
                                OsMoDroid.editor.putBoolean("showdevices", true);
                                OsMoDroid.editor.commit();
                            }
                    }
                return super.onOptionsItemSelected(item);
            }
        /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
            {
                Log.d(getClass().getSimpleName(), "ChannelDevicesFragment onCreateView");
                Bundle bundle = getArguments();
                if (bundle != null)
                    {
                        channelpos = bundle.getInt("channelpos", -1);
                    }
                for (Channel ch : LocalService.channelList)
                    {
                        if (ch.u == channelpos)
                            {
                                LocalService.currentchanneldeviceList = ch.deviceList;
                                LocalService.currentChannel = ch;
                            }
                    }

                View view = inflater.inflate(R.layout.mychannelsdevices, container, false);
                if(LocalService.currentChannel==null)
                    {
                        return view;
                    }
                //LocalService.currentchanneldeviceList= LocalService.channelList.get(channelpos).deviceList;
                //LocalService.currentChannel= LocalService.channelList.get(channelpos);
                LocalService.channelsDevicesAdapter = new ChannelsDevicesAdapter(getActivity(), R.layout.channelsdeviceitem, LocalService.currentchanneldeviceList);
                LocalService.channelsmessagesAdapter = new ChannelChatAdapter(globalActivity, R.layout.devicechatitem, LocalService.currentChannel.messagesstringList);
                //(getSherlockActivity(), R.layout.channelchatitem, LocalService.currentChannel.messagesstringList );
                lv1 = (ListView) view.findViewById(R.id.mychannelsdeviceslistView);
                lv2 = (ListView) view.findViewById(R.id.mychannelsmessages);
                if(OsMoDroid.settings.getBoolean("showdevices",true))
                    {
                        lv1.setVisibility(View.VISIBLE);
                    }
                else
                    {
                        lv1.setVisibility(View.GONE);
                    }

//		Button hideButton = (Button) view.findViewById(R.id.hidebutton);
//		hideButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(lv1.getVisibility()==View.VISIBLE)
//				{
//					lv1.setVisibility(View.GONE);
////					LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) lv1.getLayoutParams();
////					LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) lv2.getLayoutParams();
////					lp1.weight=0;
////					lp2.height=LinearLayout.LayoutParams.FILL_PARENT;
////					lv1.setLayoutParams(lp1);
////					lv2.setLayoutParams(lp2);
//				}
//				else
//				{
//					lv1.setVisibility(View.VISIBLE);
////					LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) lv1.getLayoutParams();
////					lp1.weight=1f;
////					LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) lv2.getLayoutParams();
////					lp2.height=0;
////					lv1.setLayoutParams(lp1);
//				}
//			}
//
//		});
                input = (EditText) view.findViewById(R.id.mychannelsdeviceseditText1);
                input.requestFocus();
                sendButton = (Button) view.findViewById(R.id.mychanneldevicesendButton);
                sendButton.setOnClickListener(new OnClickListener()
                {
                    public void onClick(View v)
                        {
                            if (!(input.getText().toString().equals("")))
                                {
                                    if (globalActivity.mService.myIM.authed)
                                        {
                                            JSONObject postjson = new JSONObject();
                                            try
                                                {
                                                    postjson.put("text", input.getText().toString());
                                                    globalActivity.mService.myIM.sendToServer("GCS:" + LocalService.currentChannel.u + '|' + postjson.toString(), true);
                                                    //http://apim.esya.ru/?key=H8&query=om_channel_chat_post&format=jsonp
                                                    //json={"channel":"51","device":"40","text":"789"}
                                                    //t.add(Netutil.newapicommand((ResultsListener)ChannelDevicesFragment.this,(Context)getSherlockActivity(), "om_channel_chat_post","json="+postjson.toString()));
                                                    input.setText("");
                                                }
                                            catch (JSONException e)
                                                {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }
                                        }
                                    else
                                        {
                                            Toast.makeText(globalActivity, R.string.CheckInternet, Toast.LENGTH_SHORT).show();
                                        }
                                }

                        }
                });
                lv1.setAdapter(LocalService.channelsDevicesAdapter);
                lv2.setAdapter(LocalService.channelsmessagesAdapter);
                if (LocalService.channelsDevicesAdapter != null)
                    {
                        LocalService.channelsDevicesAdapter.notifyDataSetChanged();
                    }
                if (LocalService.channelsmessagesAdapter != null)
                    {
                        LocalService.channelsmessagesAdapter.notifyDataSetChanged();
                    }
                registerForContextMenu(lv1);
                lv1.setOnItemClickListener(new OnItemClickListener()
                {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                        {
                            arg0.showContextMenuForChild(arg1);
                        }
                });
                lv2.setOnItemClickListener(new OnItemClickListener()
                {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                        {
                            ChatMessage m = (ChatMessage) arg0.getItemAtPosition(arg2);
                            if (input.length() == 0 && !(m.name.equals(getString(R.string.iam))))
                                {
                                    input.setText(m.name + ", " + input.getText());
                                    input.setSelection(input.length());
                                    OsMoDroid.inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                                }
                        }
                });
                return view;
            }
        // only will trigger it if no physical keyboard is open
        @Override
        public void onResultsSucceeded(APIComResult result)
            {
                Log.d(getClass().getSimpleName(), "OnResultListener Command:" + result.Command + ",Jo=" + result.Jo);
            }

    }
