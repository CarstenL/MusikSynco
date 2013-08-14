package com.musiksynchronisation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jcifs.smb.SmbFile;

public class GUI extends Activity {

    TextView LBL_actualFile;
    TextView LBL_remainingFile;
    TextView LBL_remainingFileSize;

    TextView TXT_source;
    TextView TXT_target;
    TextView TXT_ssid;
    TextView TXT_username;
    TextView TXT_password;

    private String choosenTargetDir = "";
    private boolean m_newFolderEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Labels leeren
        clearLabels();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //set icon for the edit button
        Drawable icon = getResources().getDrawable(R.drawable.ic_action_edit);
        menu.getItem(0).setIcon(icon);
        return true;
    }

    /**
     * show the config gui
     */
    public boolean editConfig(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            setContentView(R.layout.activity_config);
            Settings settings = new Settings(this);
            String loadedSettings[] = settings.LoadPreferences();
            TXT_source = (TextView) findViewById(R.id.TXT_SourcePath);
            TXT_target = (TextView) findViewById(R.id.TXT_TargetPath);
            TXT_ssid = (TextView) findViewById(R.id.TXT_SSID);
            TXT_username = (TextView) findViewById(R.id.TXT_Username);
            TXT_password = (TextView) findViewById(R.id.TXT_Passwort);
            TXT_source.setText(loadedSettings[0]);
            TXT_target.setText(loadedSettings[1]);
            TXT_ssid.setText(loadedSettings[2]);
            TXT_username.setText(loadedSettings[3]);
            TXT_password.setText(loadedSettings[4]);
        }
        return true;
    }

    /**
     * button event to save the config
     */
    public void saveConfig(View v) {
        Settings settings = new Settings(this);
        settings.writePreferences(TXT_source.getText().toString(), TXT_target.getText().toString(), TXT_ssid.getText().toString(), TXT_username.getText().toString(), TXT_password.getText().toString());

        Toast.makeText(this, "Konfiguration wurde gespeichert", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_main);
        clearLabels();
    }

    /**
     * creates a popup with a warning message
     */
    public void cancelConfig(View v){
        AlertDialog alertBuilder = new AlertDialog.Builder(this).setTitle("Beim Fortfahren gehen Änderungen verloren. Fortfahren?").setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setContentView(R.layout.activity_main);
            }
        }).setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    /**
     * button event to start the synchronisation
     */
    public void startSync(View v) {
        //load settings
        Settings settings = new Settings(this);
        String[] loadedPreferences = settings.LoadPreferences();

        Helper helper = new Helper();
        //connected with home-network?
        if (helper.checkWiFiConnection(loadedPreferences[2])){
            //connect to shared folder and copy files to target path
            Samba samba = new Samba(this, loadedPreferences[0], loadedPreferences[1], loadedPreferences[3], loadedPreferences[4]);
            ArrayList<SmbFile> files = samba.getFiles();
            samba.deleteFiles();

            files = helper.checkAvailableSpace(loadedPreferences[1], files);

            samba.copyFiles(files);

            LBL_actualFile.setText(R.string.LBL_actualFile);
            LBL_remainingFile.setText(R.string.LBL_remainingFiles);
            LBL_remainingFileSize.setText(R.string.LBL_remainingFileSize);
        }
    }

    /**
     * clear all Labels on the Main GUI
     */
    private void clearLabels() {
        LBL_actualFile = (TextView) findViewById(R.id.LBL_actualFile);
        LBL_actualFile.setText("");

        LBL_remainingFile = (TextView) findViewById(R.id.LBL_remainingFiles);
        LBL_remainingFile.setText("");

        LBL_remainingFileSize = (TextView) findViewById(R.id.LBL_remainingFileSize);
        LBL_remainingFileSize.setText("");
    }

    /**
     * button event to choose the target directory
     */
    public void chooseTargetDir(View v) {
        ChooseTargetDirectoryDialog chooseTargetDirectoryDialog = new ChooseTargetDirectoryDialog(GUI.this, TXT_target.getText().toString(),  new ChooseTargetDirectoryDialog.ChosenDirectoryListener() {
            @Override
            public void onChosenDir(String chosenDir) {
                choosenTargetDir = chosenDir;
                Toast.makeText(GUI.this, "gewählter Ordner: " + chosenDir, Toast.LENGTH_LONG).show();
                TXT_target.setText(chosenDir);
            }
        });
        // Toggle new folder button enabling
        chooseTargetDirectoryDialog.setNewFolderEnabled(true);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        chooseTargetDirectoryDialog.chooseDirectory(choosenTargetDir);
        m_newFolderEnabled = ! m_newFolderEnabled;
    }

}
