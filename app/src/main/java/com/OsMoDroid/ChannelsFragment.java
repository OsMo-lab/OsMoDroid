package com.OsMoDroid;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.text.ClipboardManager;
import android.text.InputType;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import static android.text.InputType.TYPE_CLASS_PHONE;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
public class ChannelsFragment extends Fragment
    {
        //private ListView lv1;
        protected String canalid;
        int channelpos = -1;
        String groupurl;
        private GPSLocalServiceClient globalActivity;
        private String u;
        /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
            {
                globalActivity = (GPSLocalServiceClient) getActivity();
                super.onActivityCreated(savedInstanceState);
            }
        @Override
        public void onResume()
            {
                globalActivity.actionBar.setTitle(R.string.chanals);
                if (channelpos != -1)
                    {
                        openChannelChat(channelpos);
                    }
                if(groupurl!=null)
                {
                    boolean exist =false;
                    for(Channel ch : LocalService.channelList)
                        {
                            if(ch.url.equals(groupurl))
                                {
                                    if(!ch.send)
                                        {
                                            globalActivity.mService.myIM.sendToServer("GA:" + ch.u, true);
                                        }
                                    groupurl=null;
                                    globalActivity.drawClickListener.selectItem(getString(R.string.map), null);
                                    exist=true;
                                    break;
                                }

                        }
                    if(!exist)
                        {
                            enterchanal(groupurl);
                        }
                }
                super.onResume();
            }
        @Override
        public void onDetach()
            {
                globalActivity = null;
                super.onDetach();
            }
        @Override
        public void onPause()
            {
                channelpos = -1;
                super.onPause();
            }
        /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem)
         */
        @Override
        public boolean onContextItemSelected(android.view.MenuItem item)
            {
                final AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
                if (item.getItemId() == 1)
                    {
                        LinearLayout layout = new LinearLayout(getActivity());
                        layout.setOrientation(LinearLayout.VERTICAL);
                        final TextView txv = new TextView(getActivity());
                        txv.setText(R.string.yourmessage);
                        layout.addView(txv);
                        final EditText input = new EditText(getActivity());
                        layout.addView(input);
                        AlertDialog alertdialog3 = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.sendingmessage)
                                .setView(layout)
                                .setPositiveButton(R.string.send,
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int whichButton)
                                                {

                                                }
                                        })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                        {
                                        }
                                }).create();
                        alertdialog3.show();
                        Button theButton = alertdialog3.getButton(DialogInterface.BUTTON_POSITIVE);
                        theButton.setOnClickListener(new CustomListener(alertdialog3)
                        {
                            @Override
                            public void onClick(View v)
                                {
                                    if (globalActivity.mService.myIM.authed)
                                        {
                                            if (!(input.getText().toString().equals("")))
                                                {
                                                    JSONObject postjson = new JSONObject();
                                                    try
                                                        {
                                                            postjson.put("text", input.getText().toString());
                                                            globalActivity.mService.myIM.sendToServer("GCS:" + LocalService.channelList.get((int) acmi.id).u + '|' + postjson.toString(), true);
                                                        }
                                                    catch (JSONException e)
                                                        {
                                                            e.printStackTrace();
                                                        }
                                                }
                                            else
                                                {
                                                    Toast.makeText(
                                                            globalActivity,
                                                            R.string.noallenter, Toast.LENGTH_SHORT).show();
                                                }
                                        }
                                    else
                                        {
                                            Toast.makeText(globalActivity, R.string.CheckInternet, Toast.LENGTH_SHORT).show();
                                        }
                                }
                        });
                        return true;
                    }
                if (item.getItemId() == 2)
                    {
                        openChannelChat(LocalService.channelList.get((int) acmi.id).u);
                        return true;
                    }
                if (item.getItemId() == 3)
                    {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (LocalService.channelList.get((int) acmi.id).url != null)
                            {
                                clipboard.setText(LocalService.channelList.get((int) acmi.id).url);
                            }
                        return true;
                    }
                if (item.getItemId() == 4)
                    {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        //sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://z8zv6.app.goo.gl/?link="+LocalService.channelList.get((int) acmi.id).url+"&apn=com.OsMoDroid&utm_source=OsMoDroid_Share");
                        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, LocalService.channelList.get((int) acmi.id).url);
                        startActivity(Intent.createChooser(sendIntent, getActivity().getString(R.string.sharelink)));
                        return true;
                    }
                if (item.getItemId() == 5)
                    {
                        if (LocalService.channelList.get((int) acmi.id).url != null)
                            {
                                Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LocalService.channelList.get((int) acmi.id).browseurl));
                                startActivity(browseIntent);
                            }
                        else
                            {
                                Toast.makeText(globalActivity, R.string.CheckInternet, Toast.LENGTH_SHORT).show();
                            }
                        return true;
                    }
                if (item.getItemId() == 6)
                    {
                        //globalActivity.mService.myIM.sendToServer("GROUP_DISCONNECT:"+LocalService.channelList.get((int) acmi.id).group_id);
                        if(globalActivity.mService.myIM.authed)
                            {
                                globalActivity.mService.myIM.sendToServer("GL:" + LocalService.channelList.get((int) acmi.id).u, true);
                                return true;
                            }
                        else
                            {
                                Toast.makeText(
                                        globalActivity,
                                        R.string.noallenter, Toast.LENGTH_SHORT).show();
                            }
                    }


                return super.onContextItemSelected(item);
            }
        void openChannelChat(int i)
            {
                globalActivity.drawClickListener.chandev = new ChannelDevicesFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("channelpos", i);
                globalActivity.drawClickListener.chandev.setArguments(bundle);
                globalActivity.showFragment(globalActivity.drawClickListener.chandev, true);
            }
        /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
         */
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
            {
               //SubMenu m = menu.addSubMenu(0, 99, 5, R.string.sharelink);
                menu.add(0, 2, 1, R.string.chat).setIcon(android.R.drawable.ic_menu_delete);
               // menu.add(0, 1, 2, R.string.messagetochat).setIcon(android.R.drawable.ic_menu_share);
                menu.add(0, 5, 3, R.string.openinbrowser).setIcon(android.R.drawable.ic_menu_edit);
                menu.add(0, 4, 5, R.string.sharelink).setIcon(android.R.drawable.ic_menu_edit);
                menu.add(0, 3, 4, R.string.copylink).setIcon(android.R.drawable.ic_menu_edit);
                //menu.add(0, 7, 6, R.string.copychID).setIcon(android.R.drawable.ic_menu_edit);
                menu.add(0, 6, 7, R.string.exitfromchanal).setIcon(android.R.drawable.ic_menu_edit);
                //menu.add(0, 8, 8, R.string.shareID).setIcon(android.R.drawable.ic_menu_edit);
                super.onCreateContextMenu(menu, v, menuInfo);
            }
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setHasOptionsMenu(true);
                //setRetainInstance(true);
                super.onCreate(savedInstanceState);
            }
        @Override
        public void onAttach(Activity activity)
            {
                globalActivity = (GPSLocalServiceClient) activity;// TODO Auto-generated method stub
                super.onAttach(activity);
            }
        /* (non-Javadoc)
         * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
         */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
            {
                MenuItem refresh = menu.add(0, 3, 0, R.string.refresh);
                MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                refresh.setIcon(android.R.drawable.ic_menu_rotate);
                MenuItem createchannel = menu.add(0, 1, 0, R.string.createchanel);
                MenuItemCompat.setShowAsAction(createchannel, MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
                createchannel.setIcon(android.R.drawable.ic_menu_add);
                MenuItem enterchannel = menu.add(0, 2, 0, R.string.enterchanal);
                MenuItemCompat.setShowAsAction(enterchannel, MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
                enterchannel.setIcon(android.R.drawable.ic_menu_agenda);
                super.onCreateOptionsMenu(menu, inflater);
            }
        /* (non-Javadoc)
         * @see com.actionbarsherlock.app.SherlockFragment#onPrepareOptionsMenu(com.actionbarsherlock.view.Menu)
         */
        @Override
        public void onPrepareOptionsMenu(Menu menu)
            {
                // TODO Auto-generated method stub
                super.onPrepareOptionsMenu(menu);
            }
        /* (non-Javadoc)
         * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                if (item.getItemId() == 1)
                    {
                        creategroup();



                    }
                if (item.getItemId() == 2)
                    {
                        enterchanal(null);
                    }
                if (item.getItemId() == 3)
                    {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                try {
                                    File dir = new File(android.os.Environment.getExternalStorageDirectory()+"/OsMoDroid/channelsgpx/");
                                    dir.mkdirs();
                                    //if (dir.isDirectory())
                                    {
                                        String[] children = dir.list();
                                        for (int i = 0; i < children.length; i++)
                                        {
                                            new File(dir, children[i]).delete();
                                        }
                                    }

                                } catch (Exception e) {

                                    e.printStackTrace();
                                    StringWriter sw = new StringWriter();
                                    e.printStackTrace(new PrintWriter(sw));
                                    String exceptionAsString = sw.toString();
                                    LocalService.addlog(exceptionAsString);

                                }
                            }
                        };
                        runnable.run();
                        globalActivity.mService.myIM.sendToServer("GROUP", true);
                    }
                return super.onOptionsItemSelected(item);
            }
        private void creategroup()
            {
                ScrollView scrollView = new ScrollView(globalActivity);
                LinearLayout layout = new LinearLayout(globalActivity);
                layout.setOrientation(LinearLayout.VERTICAL);
                scrollView.addView(layout);
                final TextView txv3 = new TextView(globalActivity);
                txv3.setText(R.string.chanalname);
                layout.addView(txv3);
                final EditText input2 = new EditText(globalActivity);
                layout.addView(input2);
                final TextView nickTextView = new TextView(globalActivity);
                nickTextView.setText(R.string.nick);
                layout.addView(nickTextView);
                final EditText nickEditText = new EditText(globalActivity);
                if(OsMoDroid.settings.getString("u", "").equals(""))
                    {
                        nickEditText.setHint("Superman");
                    }
                else
                    {
                        nickEditText.setText(OsMoDroid.settings.getString("u", ""));
                    }
                layout.addView(nickEditText);
                final TextView emailTextView = new TextView(globalActivity);
                emailTextView.setText(R.string.Email);
                layout.addView(emailTextView);
                final EditText emailEditText = new EditText(globalActivity);
                emailEditText.setHint("joe@mail.com");
                emailEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                layout.addView(emailEditText);
                final TextView groupTypeTextView = new TextView(globalActivity);
                groupTypeTextView.setText(R.string.group_type_txt);
                layout.addView(groupTypeTextView);
                final Spinner groupTypeSpinner = new Spinner(globalActivity);
                layout.addView(groupTypeSpinner);
                List<String> typeList = new ArrayList<String>();
                typeList.add(getString(R.string.group_type_simple));
                typeList.add(getString(R.string.group_type_famaly));
                typeList.add(getString(R.string.group_type_poi));
                //typeList.add(getString(R.string.travel));
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(globalActivity, R.layout.spinneritem, typeList);
                groupTypeSpinner.setAdapter(dataAdapter);
                final TextView securityTextView = new TextView(globalActivity);
                securityTextView.setText(R.string.groupaccess);
                layout.addView(securityTextView);
                final Spinner groupSecuritySpinner = new Spinner(globalActivity);
                layout.addView(groupSecuritySpinner);
                List<String> securityList = new ArrayList<String>();
                securityList.add(getString(R.string.security_open));
                securityList.add(getString(R.string.security_restricted));
                securityList.add(getString(R.string.security_closed));
                ArrayAdapter<String> datasecureAdapter = new ArrayAdapter<String>(globalActivity, R.layout.spinneritem, securityList);
                groupSecuritySpinner.setAdapter(datasecureAdapter);
                if (!OsMoDroid.settings.getString("u", "").equals(""))
                    {
                        emailEditText.setVisibility(View.GONE);
                        emailTextView.setVisibility(View.GONE);


                    }
                final AlertDialog alertdialog4 = new AlertDialog.Builder(globalActivity)
                        .setTitle(R.string.createchanal)
                        .setView(scrollView)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                        {

                                        }
                                })
                        .setNegativeButton(R.string.No,
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                        {
                                        }
                                }).create();
                alertdialog4.show();
                Button theButton = alertdialog4.getButton(DialogInterface.BUTTON_POSITIVE);
                theButton.setOnClickListener(new CustomListener(alertdialog4)
                {
                    @Override
                    public void onClick(View v)
                        {
                            if (globalActivity.mService.myIM.authed)
                                {
                                    String canalname = input2.getText().toString();
                                    String email = emailEditText.getText().toString();


                                    if (!(nickEditText.getText().toString().equals(""))&&!(canalname.equals("")) && (!OsMoDroid.settings.getString("u", "").equals("")||!email.equals("")))
                                        {
                                            JSONObject j = new JSONObject();
                                            try
                                                {
                                                    j.put("name",canalname);
                                                    j.put("nick",nickEditText.getText().toString());
                                                    if(!email.equals(""))
                                                       {
                                                           j.put("email",email);
                                                       }
                                                    switch ((int)groupTypeSpinner.getSelectedItemId())
                                                        {
                                                            case 0:
                                                                j.put("type",1);
                                                                break;
                                                            case 1:
                                                                j.put("type",2);
                                                                break;
                                                            case 2:
                                                                j.put("type",5);
                                                                break;
                                                            case 3:
                                                                j.put("type",6);
                                                                break;

                                                        }


                                                    j.put("private",groupSecuritySpinner.getSelectedItemId());
                                                }
                                            catch (JSONException e)
                                                {
                                                    e.printStackTrace();
                                                }
                                            globalActivity.mService.myIM.sendToServer("GRPA|"+j.toString(), true);
                                            super.dialog.dismiss();;
                                        }
                                    else
                                        {
                                            Toast.makeText(
                                                    globalActivity,
                                                    R.string.noallenter, Toast.LENGTH_SHORT).show();
                                        }


                                }
                            else
                                {
                                    Toast.makeText(globalActivity, R.string.CheckInternet, Toast.LENGTH_SHORT).show();
                                }
                        }

                });
            }
        private void enterchanal(String url)
            {

                        LinearLayout layout = new LinearLayout(globalActivity);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        final TextView txv1 = new TextView(globalActivity);
                        txv1.setText(R.string.chanalcode);
                        layout.addView(txv1);
                        final EditText input = new EditText(globalActivity);
                        layout.addView(input);
                        final TextView txv2 = new TextView(globalActivity);
                        txv2.setText(R.string.iam);
                        layout.addView(txv2);
                        final EditText input2 = new EditText(globalActivity);
                        layout.addView(input2);
                        //input.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                        input2.setSingleLine(true);
                        input2.setText(OsMoDroid.settings.getString("u", ""));
                        if(url!=null)
                            {
                                input.setText(url);
                                input2.requestFocus();
                                groupurl=null;
                            }

                        AlertDialog alertdialog4 = new AlertDialog.Builder(
                                globalActivity)
                                .setTitle(R.string.bindtochanal)
                                .setView(layout)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton)
                                                {

                                                }
                                        })
                                .setNegativeButton(R.string.No,
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton)
                                                {
                                                }
                                        }).create();
                        alertdialog4.show();
                Button theButton = alertdialog4.getButton(DialogInterface.BUTTON_POSITIVE);
                theButton.setOnClickListener(new CustomListener(alertdialog4)
                {
                    @Override
                    public void onClick(View v)
                        {
                            if (globalActivity.mService.myIM.authed)
                                {
                                    canalid = Uri.encode(input.getText().toString());
                                    if (!(canalid.equals("")))
                                        {
                                            //{"id":"урл или введённый руками", "name":"указанное имя", "color":"цвет (если в группе разрешено, он будет принят, если нет - проигнорирован просто)"}
                                            JSONObject jo = new JSONObject();
                                            try
                                                {
                                                    jo.put("id", canalid);
                                                    jo.put("name", input2.getText().toString());
                                                    jo.put("color","");
                                                    globalActivity.mService.myIM.sendToServer("GE|" + jo.toString(), true);
                                                    super.dialog.dismiss();
                                                }
                                            catch (JSONException e)
                                                {
                                                    e.printStackTrace();
                                                }

                                        }
                                    else
                                        {
                                            Toast.makeText(
                                                    globalActivity,
                                                    R.string.noallenter, Toast.LENGTH_SHORT).show();
                                        }

                                }
                            else
                                {
                                    Toast.makeText(globalActivity, R.string.CheckInternet, Toast.LENGTH_SHORT).show();
                                }
                        }

                });


            }
        @Override
        public void onDestroy()
            {
                LocalService.channelsAdapter=null;

                super.onDestroy();
            }
        /* (non-Javadoc)
                 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
                 */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
            {
                globalActivity = (GPSLocalServiceClient) getActivity();
                LocalService.channelsAdapter = new ChannelsAdapter(globalActivity, R.layout.deviceitem, LocalService.channelList, globalActivity.mService);

                View view = inflater.inflate(R.layout.mychannels, container, false);
                ListView lv1 = (ListView) view.findViewById(R.id.mychannelslistView);
                lv1.setEmptyView(view.findViewById(android.R.id.empty));
                lv1.setAdapter(LocalService.channelsAdapter);
                registerForContextMenu(lv1);
                if (LocalService.channelsAdapter != null)
                    {
                        LocalService.channelsAdapter.notifyDataSetChanged();
                    }
                registerForContextMenu(lv1);
                lv1.setOnItemClickListener(new OnItemClickListener()
                {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                        {
                            arg0.showContextMenuForChild(arg1);
                        }
                });
                if (LocalService.channelList.size() == 0)
                    {
                        //Netutil.newapicommand((ResultsListener)LocalService.serContext,(Context)getSherlockActivity(), "om_device_channel_adaptive:"+OsMoDroid.settings.getString("device", ""));
                    }
                Button eg = (Button) view.findViewById(R.id.button2);
                Button cg = (Button) view.findViewById(R.id.button3);
                eg.setVisibility(View.GONE);
                cg.setVisibility(View.GONE);
                if(LocalService.channelList.isEmpty())
                    {
                        eg.setVisibility(View.VISIBLE);
                        cg.setVisibility(View.VISIBLE);

                        eg.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                    {
                                        enterchanal(null);
                                    }
                            });
                        cg.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                    {
                                        creategroup();
                                    }
                            });
                    }
                        return view;

            }

    }
