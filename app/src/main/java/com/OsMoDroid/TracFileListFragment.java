package com.OsMoDroid;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static com.OsMoDroid.IM.writeException;

public class TracFileListFragment extends Fragment implements ResultsListener
    {
        int count = 0;
        int progress = 0;
        private File path;
        private ProgressDialog progressDialog;
        private ReadTrackList readTask;
        private File sdDir = OsMoDroid.osmodirFile;
        private GPSLocalServiceClient globalActivity;
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setHasOptionsMenu(true);
                //setRetainInstance(true);
            }
        @Override
        public void onDetach()
            {
                globalActivity = null;
                super.onDetach();
            }
        @Override
        public void onResume()
            {
              /*  if (!OsMoDroid.settings.getString("sdpath", "").equals(""))
                    {
                        sdDir = new File(OsMoDroid.settings.getString("sdpath", ""));
                    }*/
                getFileList();
                globalActivity.actionBar.setTitle(getString(R.string.tracks));
                super.onResume();
            }
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
            {
                MenuItem refresh = menu.add(0, 1, 0, R.string.refresh);
                MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                refresh.setIcon(android.R.drawable.ic_menu_rotate);
                super.onCreateOptionsMenu(menu, inflater);
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                switch (item.getItemId())
                    {
                        case 1:
                            getFileList();
                            break;
                        default:
                            break;
                    }
                return super.onOptionsItemSelected(item);
            }
        @Override
        public void onAttach(Activity activity)
            {
                globalActivity = (GPSLocalServiceClient) activity;// TODO Auto-generated method stub
                super.onAttach(activity);
            }
        @Override
        public boolean onContextItemSelected(android.view.MenuItem item)
            {
                final AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
                if (item.getItemId() == 1)
                    {
                        File file = new File(sdDir, "OsMoDroid/" + LocalService.trackFileList.get((int) acmi.id).fileName);
                        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(), PendingIntent.FLAG_MUTABLE);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                                LocalService.serContext.getApplicationContext(),"default")
                                .setWhen(System.currentTimeMillis())
                                .setContentText(file.getName())
                                .setContentTitle(getActivity().getString(
                                        R.string.osmodroiduploadfile))
                                .setSmallIcon(android.R.drawable.arrow_up_float)
                                .setAutoCancel(true)
                                .setContentIntent(contentIntent)
                                .setProgress(100, 0, false).setChannelId("silent");
                        ;
                        Notification notification = notificationBuilder.build();
                        int uploadid = OsMoDroid.uploadnotifyid();
                        LocalService.mNotificationManager.notify(uploadid, notification);
                        Netutil.newapicommand((ResultsListener) TracFileListFragment.this, "tr_track_upload:1", file, notificationBuilder, uploadid);
                    }
                if (item.getItemId() == 2)
                    {
                        File fileName = new File(sdDir, "OsMoDroid/" + LocalService.trackFileList.get((int) acmi.id).fileName);
                        ColoredGPX load = new ColoredGPX(0, fileName, "#0000FF", null);
                        Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                        while (it.hasNext())
                            {
                                ColoredGPX cg = it.next();
                                if (cg.gpxfile.equals(load.gpxfile))
                                    {
                                        it.remove();
                                    }
                            }
                        LocalService.trackFileList.get((int) acmi.id).showedonmap = false;
                        LocalService.trackFileAdapter.notifyDataSetChanged();
                        fileName.delete();
                        getFileList();
                    }
                if (item.getItemId() == 3)
                    {
                        if(LocalService.trackFileList.get((int) acmi.id).fromServer)
                        {
                            for(ColoredGPX cg:LocalService.showedgpxList)
                            {
                                if(cg.u==LocalService.trackFileList.get((int) acmi.id).u)
                                {
                                    if (cg.centerGeoPoint != null) {
                                        Log.d(this.getClass().getName(), "TrackFileListFragment centerGeoPoint=" + cg.centerGeoPoint);
                                        OsMoDroid.editor.putInt("centerlat", cg.centerGeoPoint.getLatitudeE6());
                                        OsMoDroid.editor.putInt("centerlon", cg.centerGeoPoint.getLongitudeE6());
                                        OsMoDroid.editor.putInt("zoom", 10);
                                        OsMoDroid.editor.putBoolean("isfollow", false);
                                        OsMoDroid.editor.commit();
                                        globalActivity.drawClickListener.selectItem(OsMoDroid.context.getString(R.string.map), null);
                                        LocalService.currentItemName = OsMoDroid.context.getString(R.string.map);

                                    }
                                }
                            }


                        }
                        else {
                            int indexoftrack = 0;


                            File fileName = new File(sdDir, "OsMoDroid/" + LocalService.trackFileList.get((int) acmi.id).fileName);
                            Log.d(getClass().getSimpleName(), "filename=" + fileName);
                            ColoredGPX load = new ColoredGPX(0, fileName, "#0000FF", null);
                            Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                            boolean exist = false;

                            while (it.hasNext()) {
                                ColoredGPX cg = it.next();
                                if (cg.gpxfile.equals(load.gpxfile)) {
                                    exist = true;
                                    break;
                                }
                                indexoftrack++;
                            }
                            if (!exist) {
                                LocalService.showedgpxList.add(load);
                                load.initPathOverlay();
                                LocalService.trackFileList.get((int) acmi.id).showedonmap = true;
                            }


                            LocalService.trackFileAdapter.notifyDataSetChanged();
                            if (LocalService.showedgpxList.get(indexoftrack).centerGeoPoint != null) {
                                Log.d(this.getClass().getName(), "TrackFileListFragment centerGeoPoint=" + LocalService.showedgpxList.get(indexoftrack).centerGeoPoint);
                                OsMoDroid.editor.putInt("centerlat", LocalService.showedgpxList.get(indexoftrack).centerGeoPoint.getLatitudeE6());
                                OsMoDroid.editor.putInt("centerlon", LocalService.showedgpxList.get(indexoftrack).centerGeoPoint.getLongitudeE6());
                                OsMoDroid.editor.putInt("zoom", 10);
                                OsMoDroid.editor.putBoolean("isfollow", false);
                                OsMoDroid.editor.commit();
                                globalActivity.drawClickListener.selectItem(OsMoDroid.context.getString(R.string.map), null);
                                LocalService.currentItemName = OsMoDroid.context.getString(R.string.map);

                            } else {

                            }

                        }

                    }

                if (item.getItemId()==5)
                    {
                        globalActivity.drawClickListener.trackStatFragment = new TrackStatFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("file", LocalService.trackFileList.get((int) acmi.id).fileName );
                        bundle.putBoolean("fromServer", LocalService.trackFileList.get((int) acmi.id).fromServer );
                        globalActivity.drawClickListener.trackStatFragment.setArguments(bundle);
                        globalActivity.showFragment(globalActivity.drawClickListener.trackStatFragment, true);
                    }
                if(item.getItemId()==6)
                {
                    createFile(LocalService.trackFileList.get((int) acmi.id).fileName);


                }


                return super.onContextItemSelected(item);
            }
        private static final int CREATE_FILE = 1;
        File currentExportFile;

        private void createFile(String fileName) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/gpx");
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            currentExportFile = new File(sdDir, "OsMoDroid/" + fileName);

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when your app creates the document.


            startActivityForResult(intent, CREATE_FILE);
        }
        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent resultData) {
            if (requestCode == CREATE_FILE
                    && resultCode == Activity.RESULT_OK) {
                InputStream is = null;
                OutputStream os = null;

                // Note: you may use try-with resources if your API is 19+
                try {
                    // InputStream constructor takes File, String (path), or FileDescriptor
                    is = new FileInputStream(currentExportFile);
                    // data.getData() holds the URI of the path selected by the picker
                    os = getActivity().getContentResolver().openOutputStream(resultData.getData());

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    writeException(e);
                    Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                } finally {
                    try {
                        is.close();
                        os.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        writeException(e);
                        Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                    }
                }
            }
            currentExportFile= null;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenuInfo menuInfo)
            {
                final AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
                //menu.add(0, 1, 1, R.string.uploadtotrera).setIcon(android.R.drawable.arrow_up_float);
                if(!LocalService.trackFileList.get((int) acmi.id).fromServer) {
                    menu.add(0, 2, 3, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
                }
                if(LocalService.trackFileList.get((int) acmi.id).showedonmap)
                    {
                        menu.add(0, 3, 1, R.string.showonmap).setIcon(android.R.drawable.ic_menu_directions);
                    }
                //menu.add(0, 4, 3, R.string.hidefromnmap).setIcon(android.R.drawable.ic_menu_manage);
                if(!LocalService.trackFileList.get((int) acmi.id).fromServer) {
                    menu.add(0, 5, 2, R.string.stat);
                }
                menu.add(0,6,4,"Save as file");

                super.onCreateContextMenu(menu, v, menuInfo);
            }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
            {
                View view = inflater.inflate(R.layout.trackfile, container, false);
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage(getActivity().getString(R.string.loading));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                final ListView lv1 = (ListView) view.findViewById(R.id.trackfilelistView);
                LocalService.trackFileAdapter = new TrackFileAdapter(getActivity(), R.layout.trackfileitem, LocalService.trackFileList);
                lv1.setAdapter(LocalService.trackFileAdapter);
                registerForContextMenu(lv1);
                lv1.setOnItemClickListener(new OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                        {
                            arg0.showContextMenuForChild(arg1);
                        }
                });
                return view;
            }
        void getFileList()
            {
                if (readTask != null)
                    {
                        readTask.cancel(true);
                    }
                readTask = new ReadTrackList();
                readTask.execute();
            }
        @Override
        public void onResultsSucceeded(APIComResult result)
            {
                LocalService.mNotificationManager.cancel(result.notificationid);
            }
        private class ReadTrackList extends AsyncTask<Void, TrackFile, Void>
            {
                private ArrayList<TrackFile> tempTrackFileList = new ArrayList<TrackFile>();
                @Override
                protected void onProgressUpdate(TrackFile... values)
                    {
                        progressDialog.setProgress(progress);
                        LocalService.trackFileList.add(values[0]);
                        Collections.sort(LocalService.trackFileList);
                        LocalService.trackFileAdapter.notifyDataSetChanged();
                        super.onProgressUpdate(values);
                    }
                @Override
                protected Void doInBackground(Void... params)
                    {
                        count = 0;
                        progress = 0;


                                path = new File(sdDir, "OsMoDroid/");
                                File[] fileArray = path.listFiles(new FilenameFilter()
                                {
                                    @Override
                                    public boolean accept(File dir, String filename)
                                        {
                                            return filename.toLowerCase().endsWith(".gpx");
                                        }
                                });
                                if(fileArray!=null)
                                    {
                                        for (File file : fileArray)
                                            {
                                                //tempTrackFileList.add(new TrackFile(file.getName(),file.lastModified(),file.length()));
                                                count++;
                                                progress += ((float) count / (float) fileArray.length) * 100;
                                                TrackFile tr = new TrackFile(file.getName(), file.lastModified(), file.length());
                                                ColoredGPX load = new ColoredGPX(0, file, "#0000FF", null);
                                                Iterator<ColoredGPX> it = LocalService.showedgpxList.iterator();
                                                while (it.hasNext())
                                                    {
                                                        ColoredGPX cg = it.next();
                                                        if (cg.gpxfile.equals(load.gpxfile))
                                                            {
                                                                tr.showedonmap = true;
                                                            }
                                                    }
                                                publishProgress(tr);
                                            }
                                    }

                        return null;
                    }
                /* (non-Javadoc)
                 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
                 */
                @Override
                protected void onPostExecute(Void result)
                    {
                        //  progressDialog.dismiss();
                        Collections.sort(LocalService.trackFileList);
                        if (true)
                        {
                            LocalService.myIM.sendToServer("HISTORY", true);
                        }
                        LocalService.trackFileAdapter.notifyDataSetChanged();
                        super.onPostExecute(result);
                    }
                /* (non-Javadoc)
                 * @see android.os.AsyncTask#onPreExecute()
                 */
                @Override
                protected void onPreExecute()
                    {
                        LocalService.trackFileList.clear();
                        // progressDialog.show();
                        Collections.sort(LocalService.trackFileList);
                        LocalService.trackFileAdapter.notifyDataSetChanged();
                        super.onPreExecute();
                    }

              /* (non-Javadoc)
               * @see android.os.AsyncTask#onProgressUpdate(Progress[])
               */
            }

    }
