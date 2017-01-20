package com.gedexx.gpmextractor.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.gedexx.gpmextractor.domain.Album;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;

@EIntentService
public class AlbumArtCoverDownloadService extends AbstractIntentService {

    public AlbumArtCoverDownloadService() {
        super(AlbumArtCoverDownloadService.class.getSimpleName());
    }

    @ServiceAction
    public void downloadAlbumArtCoverFromUrl(Collection<Album> albums) {
        try {

            for (Album album : albums) {

                final File albumCoverFile = new File(getFilesDir(), album.getCoverArtLocalPath());

                if (!albumCoverFile.exists() || albumCoverFile.length() == 0) {
                    final FileOutputStream fos = new FileOutputStream(albumCoverFile);

                    final Bitmap albumCover = getBitmapFromURL(album.getCoverArtUrl());
                    albumCover.compress(Bitmap.CompressFormat.PNG, 100, fos);

                    fos.close();
                }
            }

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
