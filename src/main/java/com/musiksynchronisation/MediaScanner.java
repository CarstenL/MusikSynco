package com.musiksynchronisation;


import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.util.ArrayList;

import jcifs.smb.SmbFile;

public class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mediaScannerConnection;
    private ArrayList<SmbFile> files;

    public MediaScanner(Context context, ArrayList<SmbFile> copiedFiles) {
        files = copiedFiles;
        mediaScannerConnection = new MediaScannerConnection(context, this);
        mediaScannerConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        for (SmbFile file : files) {
            mediaScannerConnection.scanFile(file.getPath(), null);
        }
    }

    @Override
    public void onScanCompleted(String s, Uri uri) {
        mediaScannerConnection.disconnect();
    }
}
