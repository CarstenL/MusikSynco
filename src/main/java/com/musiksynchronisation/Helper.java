package com.musiksynchronisation;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class Helper {

    private static Context context;
    public Helper(Context _context){
        context = _context;
    }

    public boolean checkWiFiConnection(String configSSID){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSSID().contains(configSSID))
            return true;
        else
            return false;
    }

    /**
     * check the available space at the target path
     */
    public ArrayList<SmbFile> checkAvailableSpace(String targetPath, ArrayList<SmbFile> files){
        int filespace = 0;
        for(SmbFile file: files)
            //Speichermenge in Byte
            try {
                filespace += file.length();
            } catch (SmbException e) {
                e.printStackTrace();
            }

        //in MB umrechnen
        filespace = filespace / 1024 / 1024;

        double availableSpace = new File(targetPath).getFreeSpace();

        //in MB umrechnen
        //availableSpace = availableSpace / 1024 / 1024;

        //filespace lower than the available space --> return all files
        if(filespace < availableSpace)
            return files;
        else{
        //remove one random file and check again
            files.remove(getRandomNumber(files.size()));
            checkAvailableSpace(targetPath, files);
        }
        return  null;
    }

    /**
     * get a random number between 0 and the counted files
     */
    private int getRandomNumber(int countFiles){
        Random random = new Random();
        return random.nextInt(countFiles);
    }
}
