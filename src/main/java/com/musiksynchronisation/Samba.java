package com.musiksynchronisation;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class Samba {

    private SmbFile smbFile;
    private String target;
    private Context context;


    public Samba(Context _context, String sourcePath, String targetPath, String username, String password) {
        try {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", username, password);

            //stellt sicher, dass es mit // beginnt
            if (sourcePath.startsWith("//"))
                sourcePath = sourcePath.substring(2);

            //stellt sicher, dass es mit / endet
            if (!sourcePath.endsWith("/"))
                sourcePath += "/";

            smbFile = new SmbFile("smb://" + sourcePath, auth);

            target = targetPath;
            context = _context;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * get files in source path
     */
    public ArrayList<SmbFile> getPCFiles() {
        ArrayList<SmbFile> arrFiles = new ArrayList<SmbFile>();
        try {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
            if (smbFile.exists()) {
                SmbFile[] files = smbFile.listFiles("*.mp3");
                arrFiles = new ArrayList<SmbFile>();
                Collections.addAll(arrFiles, files);
            } else
                Toast.makeText(context, "Quellpfad kann nicht ermittelt werden", Toast.LENGTH_LONG).show();
        } catch (SmbException e) {
            Toast.makeText(context, "Fehler beim Ermitteln der Dateien", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return arrFiles;
    }

    public File[] getLocalFiles() {
        File dir = new File(target);
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().contains(".mp3");
            }
        });
    }

    public ArrayList<SmbFile> checkExistingFiles(ArrayList<SmbFile> PCfiles, File[] localFiles) {
        ArrayList<SmbFile> toCopyFiles = new ArrayList<SmbFile>();
        for (SmbFile PCfile : PCfiles) {
            boolean vorhanden = false;
            for (File lFile : localFiles) {
                if (PCfile.getName().contains(lFile.getName())) {
                    vorhanden = true;
                    break;
                }
            }
            if (!vorhanden)
                toCopyFiles.add(PCfile);

        }
        return toCopyFiles;
    }

    public void copyFiles(final SmbFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            File localFilePath = new File(target + "/" + file.getName());
            OutputStream out = new FileOutputStream(localFilePath);
            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.close();
            inputStream.close();
            MediaScanner mediaScanner = new MediaScanner(context, localFilePath);
            mediaScanner.insertToMediaStore();
        } catch (Exception e) {
            new AlertDialog.Builder(context).setTitle("Fehler beim Kopieren der Dateien").setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            }).show();
        }
    }
}
