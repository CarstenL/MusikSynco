package com.musiksynchronisation;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;

public class MediaScanner {

    private File file;
    private Context mContext;

    public MediaScanner(Context context, File copiedfile) {
        file = copiedfile;
        mContext = context;
    }

    public void insertToMediaStore() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, file.getName().split(" - ")[1].split(".mp3")[0]);
        values.put(MediaStore.MediaColumns.MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3"));
        values.put(MediaStore.Audio.Media.ARTIST, file.getName().split(" - ")[0]);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);

        mContext.getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath()), values);

        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), "file://" + file.getAbsolutePath());
    }
}
