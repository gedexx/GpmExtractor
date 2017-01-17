package com.gedexx.gpmextractor.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

@EIntentService
public class AlbumArtCoverDownloadService extends AbstractIntentService {

    public AlbumArtCoverDownloadService() {
        super(AlbumArtCoverDownloadService.class.getSimpleName());
    }

    @ServiceAction
    public void downloadAlbumArtCoverFromUrl(String urlSrc, String fileNameDest) {
        try {
            final FileOutputStream fos = openFileOutput(fileNameDest, Context.MODE_PRIVATE);

            final Bitmap albumCover = getBitmapFromURL(urlSrc);
            albumCover.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.close();

            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.download_finished"));
        } catch (IOException e) {
            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.download_error"));
        }
    }

    private Bitmap getBitmapFromURL(String src) throws IOException {

        java.net.URL url = new java.net.URL(src);

        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();
        connection.setDoInput(true);
        connection.connect();

        InputStream input = connection.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        input.close();

        return myBitmap;
    }
}
