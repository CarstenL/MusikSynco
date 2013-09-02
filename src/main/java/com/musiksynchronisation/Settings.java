package com.musiksynchronisation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {

    SharedPreferences sharedPreferences;
    String source;
    String target;
    String ssid;
    String username;
    String password;
    Context context;

    private String choosenTargetDir = "";
    private boolean m_newFolderEnabled = true;

    public Settings() {

    }

    public Settings(Context _context) {
        context = _context;
        if (source == null)
            getValuesFromConfig(1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (source == null)
            getValuesFromConfig(0);

        TextView TXT_source = (TextView) findViewById(R.id.TXT_SourcePath);
        TextView TXT_target = (TextView) findViewById(R.id.TXT_TargetPath);
        TextView TXT_ssid = (TextView) findViewById(R.id.TXT_SSID);
        TextView TXT_username = (TextView) findViewById(R.id.TXT_Username);
        TextView TXT_password = (TextView) findViewById(R.id.TXT_Passwort);
        TXT_source.setText(source);
        TXT_target.setText(target);
        TXT_ssid.setText(ssid);
        TXT_username.setText(username);
        TXT_password.setText(password);
    }

    /**
     * @param view 0: GUI; 1: Settings
     */
    private void getValuesFromConfig(int view) {
        if (view == 0)
            sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        else
            sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        source = sharedPreferences.getString("SOURCE", "");
        target = sharedPreferences.getString("TARGET", "");
        ssid = sharedPreferences.getString("SSID", "");
        username = sharedPreferences.getString("USERNAME", "");
        password = sharedPreferences.getString("PASSWORD", "");
    }

    /**
     * load the config file
     */
    public java.lang.String[] LoadPreferences() {
        try {
            String strArray[] = new String[5];
            strArray[0] = source;
            strArray[1] = target;
            strArray[2] = ssid;
            strArray[3] = username;
            strArray[4] = password;

            return strArray;
        } catch (Exception ex) {
            Toast.makeText(this, "Fehler beim Auslesen der Konfiguration", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * writes the gui parameter into the config file
     */
    public void writePreferences(String source, String target, String ssid, String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SOURCE", source);
        editor.putString("TARGET", target);
        editor.putString("SSID", ssid);
        editor.putString("USERNAME", username);
        editor.putString("PASSWORD", password);
        editor.commit();
    }

    /**
     * button event to save the config
     */
    public void saveConfig(View view) {
        writePreferences(((TextView) findViewById(R.id.TXT_SourcePath)).getText().toString(), ((TextView) findViewById(R.id.TXT_TargetPath)).getText().toString(), ((TextView) findViewById(R.id.TXT_SSID)).getText().toString(), ((TextView) findViewById(R.id.TXT_Username)).getText().toString(), ((TextView) findViewById(R.id.TXT_Passwort)).getText().toString());

        Toast.makeText(getApplicationContext(), "Konfiguration wurde gespeichert", Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * creates a popup with a warning message
     */
    public void cancelConfig(View view) {
        new AlertDialog.Builder(this).setTitle("Beim Fortfahren gehen Änderungen verloren. Fortfahren?").setPositiveButton("Ja", new DialogInterface.OnClickListener() {
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
     * button event to choose the target directory
     */
    public void chooseTargetDir(View v) {
        ChooseTargetDirectoryDialog chooseTargetDirectoryDialog = new ChooseTargetDirectoryDialog(this, ((TextView) findViewById(R.id.TXT_TargetPath)).getText().toString(), new ChooseTargetDirectoryDialog.ChosenDirectoryListener() {
            @Override
            public void onChosenDir(String chosenDir) {
                choosenTargetDir = chosenDir;
                Toast.makeText(getApplicationContext(), "gewählter Ordner: " + chosenDir, Toast.LENGTH_LONG).show();
                ((TextView) findViewById(R.id.TXT_TargetPath)).setText(chosenDir);
            }
        });
        // Toggle new folder button enabling
        chooseTargetDirectoryDialog.setNewFolderEnabled(true);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        chooseTargetDirectoryDialog.chooseDirectory(choosenTargetDir);
        m_newFolderEnabled = !m_newFolderEnabled;
    }
}