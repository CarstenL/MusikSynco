package com.musiksynchronisation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

import jcifs.smb.SmbFile;

public class GUI extends Activity {

    public ProgressDialog progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menubar, menu);
        //set icon for the edit button
        Drawable icon = getResources().getDrawable(R.drawable.ic_action_edit);
        menu.getItem(0).setIcon(icon);
        return true;
    }

    /**
     * show the config gui
     */
    public void editConfig(MenuItem item) {
        if (item.getItemId() == R.id.action_settings)
            startActivity(new Intent(GUI.this, Settings.class));
    }

    /**
     * button event to start the synchronisation
     */
    public void startSync(View v) {
        try {
            //Dialog anzeigen
            progressbar = new ProgressDialog(this);
            progressbar.setCanceledOnTouchOutside(false);
            setProgressbarMessage("Starte Synchronisation");
            progressbar.show();

            //load settings
            Settings settings = new Settings(this);
            final String[] loadedPreferences = settings.LoadPreferences();

            final Helper helper = new Helper(GUI.this);
            setProgressbarMessage("Überprüfe WLAN");

            Samba samba = new Samba(this, loadedPreferences[0], loadedPreferences[1], loadedPreferences[3], loadedPreferences[4]);
            ArrayList<SmbFile> PCfiles = new ArrayList<SmbFile>();

            //connected with home-network?
            if (helper.checkWiFiConnection(loadedPreferences[2])) {
                //connect to shared folder and copy files to target path
                setProgressbarMessage("Ermittle Dateien vom PC");
                PCfiles = samba.getPCFiles();
                if (PCfiles == null) {
                    setProgressbarMessage("Keine Dateien vom Quellpfad ermittelt.\nSynchronisation wurde beendet");
                    enableTouchEvent();
                    return;
                }
                //get local files
                setProgressbarMessage("Ermittle lokale Dateien");
                File[] localFiles = samba.getLocalFiles();

                //get new files
                setProgressbarMessage("Ermittle neue Dateien");
                PCfiles = samba.checkExistingFiles(PCfiles, localFiles);
                if (PCfiles == null) {
                    setProgressbarMessage("Keine neuen Dateien ermittelt.\nSynchronisation wurde beendet");
                    enableTouchEvent();
                    return;
                }

                //check available space on target path
                setProgressbarMessage("Überprüfe freien Speicherplatz");
                PCfiles = helper.checkAvailableSpace(loadedPreferences[1], PCfiles);
            }

            new AsyncTask<Object, String, Context>() {
                @Override
                protected Context doInBackground(Object[] objects) {
                    for (SmbFile smbFile : (ArrayList<SmbFile>) objects[0]) {
                        onProgressUpdate("Übertrage Datei: " + smbFile.getName());
                        ((Samba) objects[1]).copyFiles(smbFile);
                    }
                    return (Context) objects[2];
                }

                protected void onProgressUpdate(String text) {
                    setProgressbarMessage(text);
                }

                @Override
                protected void onPostExecute(Context context) {
                    super.onPostExecute(context);
                    //start Mediascan
                    onProgressUpdate("Medienscanner wird im Hintergrund ausgeführt.\nSynchronisation wurde beendet.");
                    new MediaScanner(context, loadedPreferences[1]);

                    enableTouchEvent();
                }
            }.execute(PCfiles, samba, this);
        } catch (Exception ex) {
            progressbar.setMessage(ex.getMessage());
            enableTouchEvent();
        }
    }

    public void setProgressbarMessage(String text) {
        progressbar.setMessage(text);
    }

    private void enableTouchEvent() {
        progressbar.setCanceledOnTouchOutside(true);

        new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int eventaction = motionEvent.getAction();

                if (eventaction == MotionEvent.ACTION_DOWN)
                    progressbar.cancel();

                return true;
            }
        };
    }
}
