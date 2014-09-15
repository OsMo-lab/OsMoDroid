package com.OsMoDroid;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class TracFileListFragment extends Fragment  implements ResultsListener {
	  private TrackFileAdapter trackFileAdapter;
      private ArrayList<TrackFile> trackFileList = new ArrayList<TrackFile>();
      private File path;
      int count=0;
      int progress=0;
      private ProgressDialog progressDialog;
      private ReadTrackList readTask;
      
      
      @Override
      public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setHasOptionsMenu(true);
          //setRetainInstance(true);
          super.onCreate(savedInstanceState);
      }
      @Override
	public void onDetach() {
		globalActivity=null;
		super.onDetach();
	}
	private class ReadTrackList extends AsyncTask<Void, TrackFile, Void> {
              @Override
              protected void onProgressUpdate(TrackFile... values) {
                      
                              progressDialog.setProgress(progress);
                              trackFileList.add(values[0]);
                              Collections.sort(trackFileList);
                              trackFileAdapter.notifyDataSetChanged();
                      
                      super.onProgressUpdate(values);
              }

              private ArrayList<TrackFile> tempTrackFileList = new ArrayList<TrackFile>();
              @Override
              protected Void doInBackground(Void... params) {
                       count=0;
                       progress=0;
                      
                      String sdState = android.os.Environment.getExternalStorageState();
                      if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {

                               File sdDir = android.os.Environment.getExternalStorageDirectory();

                              

                               path = new File (sdDir, "OsMoDroid/");

                               File[] fileArray = path.listFiles(new FilenameFilter() {
                                      
                                      @Override
                                      public boolean accept(File dir, String filename) {
                                              
                                              return filename.toLowerCase().endsWith(".gpx");
                                      }
                              });
                               for (File file :fileArray){
                                       //tempTrackFileList.add(new TrackFile(file.getName(),file.lastModified(),file.length()));
                                       count++;
               progress += ( (float)count / (float)fileArray.length ) * 100;
               publishProgress(new TrackFile(file.getName(),file.lastModified(),file.length()));
              
                               }
                              
                      
                      }
                      
                      return null;
              }

              /* (non-Javadoc)
               * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
               */
              @Override
              protected void onPostExecute(Void result) {
                      progressDialog.dismiss();
                      Collections.sort(trackFileList);
                      trackFileAdapter.notifyDataSetChanged();
                      super.onPostExecute(result);
              }

              /* (non-Javadoc)
               * @see android.os.AsyncTask#onPreExecute()
               */
              @Override
              protected void onPreExecute() {
                      trackFileList.clear();
                      progressDialog.show();
                      Collections.sort(trackFileList);
                      trackFileAdapter.notifyDataSetChanged();
                      super.onPreExecute();
              }

              /* (non-Javadoc)
               * @see android.os.AsyncTask#onProgressUpdate(Progress[])
               */
              
              
      }
      private GPSLocalServiceClient globalActivity;
      @Override
	public void onResume() {
               getFileList();
               globalActivity.actionBar.setTitle(getString(R.string.tracks));
              super.onResume();
      }
      
      @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	MenuItem refresh = menu.add(0, 1, 0, R.string.refresh);
    	 MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
  		refresh.setIcon(android.R.drawable.ic_menu_rotate);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			getFileList();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAttach(Activity activity) {
		globalActivity = (GPSLocalServiceClient)activity;// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		 final AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();

         if (item.getItemId() == 1) {
                 File sdDir = android.os.Environment.getExternalStorageDirectory();
                 File file = new File (sdDir,"OsMoDroid/"+trackFileList.get((int) acmi.id).fileName);
                 PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(), 0);
                        
                        NotificationCompat.Builder notificationBuilder =new NotificationCompat.Builder(

                                        LocalService.serContext.getApplicationContext())

                                 .setWhen(System.currentTimeMillis())

                                 .setContentText(file.getName())

                                 .setContentTitle(getActivity().getString(
										R.string.osmodroiduploadfile))

                                 .setSmallIcon(android.R.drawable.arrow_up_float)

                                 .setAutoCancel(true)
                                 .setContentIntent(contentIntent)
                                 .setProgress(100, 0, false);
                                 ;


                                Notification notification = notificationBuilder.build();
                                int uploadid = OsMoDroid.uploadnotifyid();

                                LocalService.mNotificationManager.notify(uploadid, notification);
                        
                        
                        Netutil.newapicommand((ResultsListener)TracFileListFragment.this, "tr_track_upload:1", file,notificationBuilder,uploadid);
                
         }
        
         if (item.getItemId() == 2){
                 File sdDir = android.os.Environment.getExternalStorageDirectory();
                 File file = new File (sdDir,"OsMoDroid/"+trackFileList.get((int) acmi.id).fileName);
                 file.delete();
                 getFileList();
         }
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		 //menu.add(0, 1, 1, R.string.uploadtotrera).setIcon(android.R.drawable.arrow_up_float);
         menu.add(0, 2, 2, R.string.delete).setIcon(android.R.drawable.ic_menu_delete);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.trackfile, container, false);
	     progressDialog = new ProgressDialog(getActivity());
         progressDialog.setMessage(getActivity().getString(R.string.loading));
         progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
       
 final ListView lv1 = (ListView) view.findViewById(R.id.trackfilelistView);
 
 trackFileAdapter = new TrackFileAdapter(getActivity(),R.layout.trackfileitem, trackFileList);
 lv1.setAdapter(trackFileAdapter);
 registerForContextMenu(lv1);
 lv1.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        arg0.showContextMenuForChild(arg1);
                        
                }
        });
 
		return view;
	}

	void getFileList(){
          if (readTask!=null){
                  readTask.cancel(true);
          }
          readTask=new ReadTrackList();
          readTask.execute();
  }
      @Override
      public void onResultsSucceeded(APIComResult result) {
              LocalService.mNotificationManager.cancel(result.notificationid);
      }

}
