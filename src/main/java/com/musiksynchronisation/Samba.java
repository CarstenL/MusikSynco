package com.musiksynchronisation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class Samba {

    private SmbFile smbFile;
    private String target;
    private SmbFile[] files;
    private ProgressBar prgBar;
    private TextView LBL_actualFile;
    private TextView LBL_remainingFiles;
    private TextView LBL_remainingFilesSize;
    private int zaehler = 0;
    private double fileSize = 0;
    private GUI gui;

    public Samba(GUI _gui, String sourcePath, String targetPath, String username, String password){
        try {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", username, password);
            smbFile = new SmbFile("smb://" + sourcePath, auth);
            target = targetPath;
            gui = _gui;
            LBL_actualFile = (TextView)gui.findViewById(R.id.LBL_actualFile);
            LBL_remainingFiles = (TextView)gui.findViewById(R.id.LBL_remainingFiles);
            LBL_remainingFilesSize = (TextView)gui.findViewById(R.id.LBL_remainingFileSize);
            prgBar = (ProgressBar)gui.findViewById(R.id.progressBar);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get files in source path
     */
    public ArrayList<SmbFile> getFiles(){
        try {
            if (smbFile.exists()){
                files = smbFile.listFiles("*.mp3");
                ArrayList<SmbFile> arrFiles = new ArrayList<SmbFile>();
                Collections.addAll(arrFiles, files);
                return arrFiles;
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * delete .mp3 files in target path
     */
    public void deleteFiles(){
        try{
            File dir = new File(target);
            //check if target is a directoy
            if (dir.isDirectory()){
                //get all mp3-files in target directory
                File[] targetFiles = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.getName().contains(".mp3"))
                            return true;
                        else
                            return false;
                    }
                });

                //delete file
                for(File file : targetFiles)
                {
                    file.delete();
                    LBL_actualFile.setText("Lösche Datei " + file.getName());
                }
            }
            else{
                Toast.makeText(gui, "Zielpfad ist kein Ordner", Toast.LENGTH_LONG);
            }
        }
        catch (Exception ex){
            new AlertDialog.Builder(gui).setTitle("Fehler beim Löschen der Dateien").setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        }
    }

    public void copyFiles(ArrayList<SmbFile> arrFiles){
        try {
            for(SmbFile file: arrFiles)
                fileSize += file.length();
        } catch (SmbException e) {
                e.printStackTrace();
        }

        new AsyncTask() {
            @Override
            protected  void onPreExecute(){
                prgBar.setMax(files.length);
                super.onPreExecute();
            }
            @Override
            protected void onCancelled(){
                prgBar.setMax(0);
            }
            @Override
            protected Object doInBackground(Object[] objects) {
                zaehler = 0;
                try {
                    for (SmbFile file : files){
                        file.copyTo(new SmbFile(target));
                        onProgressUpdate(file);
                    }
                } catch (Exception e) {
                    new AlertDialog.Builder(gui).setTitle("Fehler beim Kopieren der Dateien").setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            cancel(true);
                        }
                    }).show();
                }
                return null;
            }

            protected void onProgressUpdate(final SmbFile file){
                prgBar.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            prgBar.setProgress(prgBar.getProgress() + 1);
                            LBL_actualFile.setText("Kopiere Datei: " + file.getName());
                            LBL_remainingFiles.setText("Verbleibene Dateien: " + (files.length - zaehler));
                            LBL_remainingFilesSize.setText("Verbleibende Dateimenge: " + (fileSize - file.length()));
                        }
                        catch (Exception ex){
                            Toast.makeText(gui, "Fehler beim Progress-Update", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }.execute();
    }
}
