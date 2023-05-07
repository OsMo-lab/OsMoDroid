package com.OsMoDroid;

import static android.content.Context.STORAGE_SERVICE;
import static android.provider.DocumentsContract.EXTRA_INITIAL_URI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DebugFragment extends Fragment
    {
        final private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        private GPSLocalServiceClient globalActivity;
        private final String EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents";
        private final String ANDROID_DOCID = "primary:Android/data/com.OsmoDroid";
        Uri directoryUri;
       // Uri uri;
        //Uri treeUri;
        private Intent intent;
        private ActivityResultLauncher<Intent> handleIntentActivityResult;

        private Boolean checkIfGotAccess(Uri uri) {
            List<UriPermission> permissionList = getActivity().getContentResolver().getPersistedUriPermissions();
            for (int i = 0; i < permissionList.size(); i++) {
                UriPermission it = permissionList.get(i);
                if (it.getUri().equals(uri) && it.isReadPermission())
                    return true;
            }
            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        private void saveDebugLog() {

            if (checkIfGotAccess(directoryUri)) {
                saveFile(directoryUri);
                //return;
            }

            handleIntentActivityResult.launch(intent);

        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        private StorageVolume getPrimaryVolume() {
            StorageManager sm = (StorageManager) getActivity().getSystemService(STORAGE_SERVICE);
            return sm.getPrimaryStorageVolume();
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        private void saveFile(Uri treeUri) {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);
            String extension = "log";
            try {
                assert pickedDir != null;
                DocumentFile existing = pickedDir.findFile("debug.log");
                if(existing!=null)
                    existing.delete();
                DocumentFile newFile = pickedDir.createFile("*/" + extension, "debug.log");
                assert newFile != null;
                ParcelFileDescriptor out = getActivity().getContentResolver().openFileDescriptor(newFile.getUri(), "w");

                FileWriter writer = new FileWriter(out.getFileDescriptor());
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : LocalService.debuglist)
                {
                    stringBuilder.append(s);
                    stringBuilder.append("\n");
                }
                writer.write(stringBuilder.toString());
                Toast.makeText(getActivity(), "Log saved to "+out.toString(), Toast.LENGTH_SHORT).show();
                out.close();
                writer.close();
            } catch (Exception e) {
                LocalService.addlog(e.toString());
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.Q)
        void initContetResolver()
        {

            intent =
                    getPrimaryVolume().createOpenDocumentTreeIntent()
                            ;
            handleIntentActivityResult = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if (result.getData() == null || result.getData().getData() == null)
                                return;
                            directoryUri = result.getData().getData();
                            getActivity().getContentResolver().takePersistableUriPermission(
                                    directoryUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            if (checkIfGotAccess(directoryUri))
                                saveFile(directoryUri);
                            else
                                Log.d("AppLog", "you didn't grant permission to the correct folder");
                        }
                    });
        }


        @Override
        public void onDestroy()
            {
                LocalService.debugAdapter=null;
                super.onDestroy();
            }
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
            {
                View view = inflater.inflate(R.layout.simlinks, container, false);
                final ListView lv1 = (ListView) view.findViewById(R.id.listView1);
                lv1.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                LocalService.debugAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, LocalService.debuglist);
                lv1.setAdapter(LocalService.debugAdapter);
                lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText( (String)arg0.getItemAtPosition(pos));
                        Toast.makeText(getActivity(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();

                        return true;
                    }
                });
                lv1.setOnItemClickListener(new OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View v, int position,
                                            long arg3)
                    {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText( (String)adapter.getItemAtPosition(position));

                    }
                });
                return view;
            }
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
            {
                globalActivity = (GPSLocalServiceClient) getActivity();
                super.onActivityCreated(savedInstanceState);
            }
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setHasOptionsMenu(true);
                //setRetainInstance(true);
            }
        @Override
        public void onResume()
            {
                globalActivity.actionBar.setTitle("debug");
                super.onResume();
            }
        @Override
        public void onDetach()
            {
                globalActivity = null;
                super.onDetach();
            }
        @Override
        public void onAttach(Activity activity)
            {
                globalActivity = (GPSLocalServiceClient) activity;// TODO Auto-generated method stub
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    initContetResolver();
                }
                super.onAttach(activity);
            }
        /* (non-Javadoc)
         * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
         */
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
            {
                MenuItem clear = menu.add(0, 1, 0, "Очистить");
                MenuItem share = menu.add(0, 2, 0, "Отправить журнал");
                MenuItem save = menu.add(0, 3, 0, "Сохранить журнал на sdcard");
                MenuItem copy = menu.add(0,4,0,"Копировать журнал");
                super.onCreateOptionsMenu(menu, inflater);
            }
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
            {
                switch (item.getItemId())
                    {
                        case 1:
                            LocalService.debuglist.clear();
                            if(LocalService.debugAdapter!=null) {
                                LocalService.debugAdapter.notifyDataSetChanged();
                            }
                            break;
                        case 2:
                            String sendtext = getDebugAsString();
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.setType("text/plain");
                            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"osmo.mobi@gmail.com"});
                            sendIntent.putExtra(Intent.EXTRA_TEXT, sendtext);
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Debug log");
                            startActivity(Intent.createChooser(sendIntent, "Email"));
                            break;
                        case 3:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                saveDebugLog();
                                break;
                            }
                            final Date dumpDate = new Date(System.currentTimeMillis());
                            final String state = Environment.getExternalStorageState();
                            final DateFormat fileFormatter = new SimpleDateFormat("dd-MM-yy");
                            StringBuilder stringBuilder = new StringBuilder();
                            if (Environment.MEDIA_MOUNTED.equals(state))
                                {
                                    String stacktraceDir = String.format("/OsMoDroid/debuglog/");
                                    File sd = Environment.getExternalStorageDirectory();
                                    File stacktrace = new File(
                                            sd.getPath() + stacktraceDir,
                                            String.format(
                                                    "debug-%s.txt",
                                                    fileFormatter.format(dumpDate)));
                                    File dumpdir = stacktrace.getParentFile();
                                    boolean dirReady = dumpdir.isDirectory() || dumpdir.mkdirs();
                                    if (dirReady)
                                        {
                                            FileWriter writer = null;
                                            try
                                                {
                                                    writer = new FileWriter(stacktrace, true);
                                                    for (String s : LocalService.debuglist)
                                                        {
                                                            stringBuilder.append(s);
                                                            stringBuilder.append("\n");
                                                        }
                                                    writer.write(stringBuilder.toString());
                                                    Toast.makeText(getActivity(), "Log saved to "+stacktrace, Toast.LENGTH_SHORT).show();
                                                }
                                            catch (IOException e)
                                                {
                                                    // ignore
                                                }
                                            finally
                                                {
                                                    try
                                                        {
                                                            if (writer != null)
                                                                {
                                                                    writer.close();
                                                                }
                                                        }
                                                    catch (IOException e)
                                                        {
                                                            // ignore
                                                        }
                                                }
                                        }
                                }
                            break;
                        case 4:
                            ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);

                                    clipboard.setText(getDebugAsString());
                                    Toast.makeText(getActivity(),"Скопировал", Toast.LENGTH_SHORT).show();
                                break;
                        default:
                            break;
                    }
                return super.onOptionsItemSelected(item);
            }
        @NonNull
        private String getDebugAsString()
            {
                StringBuilder sb = new StringBuilder();
                if (LocalService.debuglist.size() > 1000)
                    {
                        for (String s : LocalService.debuglist.subList(LocalService.debuglist.size() - 1000, LocalService.debuglist.size()))
                            {
                                sb.append(s);
                                sb.append("\n");
                            }
                    }
                else
                    {
                        for (String s : LocalService.debuglist)
                            {
                                sb.append(s);
                                sb.append("\n");
                            }
                    }
                return sb.toString();
            }
    }
