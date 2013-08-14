package com.musiksynchronisation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class Settings extends Activity{

    SharedPreferences sharedPreferences;
    /**
     * Constructor
     */
    Settings(GUI gui) {
        sharedPreferences = gui.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    /**
     * load the config file
     */
    public java.lang.String[] LoadPreferences(){
        try{
        String source = sharedPreferences.getString("SOURCE", "");
        String target = sharedPreferences.getString("TARGET", "");
        String ssid = sharedPreferences.getString("SSID", "");
        String username = sharedPreferences.getString("USERNAME", "");
        String password = sharedPreferences.getString("PASSWORD", "");
        String strArray[] = new String[5];
        strArray[0] = source;
        strArray[1] = target;
        strArray[2] = ssid;
        strArray[3] = username;
        strArray[4] = password;

        return strArray;
        }
        catch (Exception ex){
            Toast.makeText(this, "Fehler beim Auslesen der Konfiguration", Toast.LENGTH_LONG).show();
            return  null;
        }
    }

    /**
     * writes the gui parameter into the config file
     * @param source the file source path
     * @param target the file target path
     * @param ssid the network ssid
     */
    public void writePreferences(String source, String target, String ssid, String username, String password){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SOURCE", source);
        editor.putString("TARGET", target);
        editor.putString("SSID", ssid);
        editor.putString("USERNAME", username);
        editor.putString("PASSWORD", password);
        editor.commit();
    }
}
