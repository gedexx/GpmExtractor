package com.gedexx.gpmextractor.service;

import android.content.Intent;
import android.net.Uri;

import com.gedexx.gpmextractor.domain.Album;
import com.gedexx.gpmextractor.domain.Artist;
import com.gedexx.gpmextractor.domain.Track;
import com.gedexx.gpmextractor.helper.database.GpmDatabaseHelper;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;

import static com.gedexx.gpmextractor.GpmExtractorApplication.DATA_DIR_PATH;
import static com.gedexx.gpmextractor.GpmExtractorApplication.GPM_PACKAGE_NAME;

@EIntentService
public class GpmDbService extends AbstractIntentService {

    public static String DATABASES_DIR_NAME = "/databases/";
    public static String GPM_DB_NAME = "music.db";

    public static int SONG_ID_COLUMN_NUM = 0;
    public static int SONG_TITLE_COLUMN_NUM = 1;
    public static int SONG_DURATION_COLUMN_NUM = 2;
    public static int SONG_LOCAL_COPY_PATH_COLUMN_NUM = 3;
    public static int SONG_CP_DATA_COLUMN_NUM = 4;
    public static int ARTIST_ID_COLUMN_NUM = 5;
    public static int ARTIST_NAME_COLUMN_NUM = 6;
    public static int ALBUM_ID_COLUMN_NUM = 7;
    public static int ALBUM_NAME_COLUMN_NUM = 8;
    public static int ALBUM_GENRE_COLUMN_NUM = 9;
    public static int ALBUM_ART_LOCATION_COLUMN_NUM = 10;

    @OrmLiteDao(helper = GpmDatabaseHelper.class)
    Dao<Artist, Long> artistDao;

    @OrmLiteDao(helper = GpmDatabaseHelper.class)
    Dao<Album, Long> albumDao;

    @OrmLiteDao(helper = GpmDatabaseHelper.class)
    Dao<Track, Long> trackDao;

    public GpmDbService() {
        super(GpmDbService.class.getSimpleName());
    }

    @ServiceAction
    public void extractDb() {

        sendBroadcast(new Intent("com.gedexx.gpmextractor.extraction_start"));

        try {
            String pathSource = DATA_DIR_PATH + GPM_PACKAGE_NAME + DATABASES_DIR_NAME + GPM_DB_NAME;

            String commandSelect = " \"SELECT SongId, Title, Duration, LocalCopyPath, HEX(CpData), " +
                    "ArtistId, Artist, " +
                    "AlbumId, Album, Genre, AlbumArtLocation  " +
                    "FROM Music " +
                    "WHERE LocalCopyType = 200\";";
            String commandSqliteCsv = "sqlite3 -csv ";
            String command = commandSqliteCsv + pathSource + commandSelect;

            Process p = Runtime.getRuntime().exec("su");

            OutputStream os = p.getOutputStream();

            os.write((command).getBytes("ASCII"));
            os.flush();

            os.close();

            try {
                p.waitFor();
                if (p.exitValue() != 255) {
                    // be happy and work with the database

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    StringBuffer result = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Artist artist = createArtist(line);
                        Album album = createAlbum(line);
                        Track track = createTrack(line);

                        album.setArtist(artist);
                        track.setArtist(artist);
                        track.setAlbum(album);

                        if (!artistDao.idExists(artist.getId())) {
                            artistDao.create(artist);
                        }

                        if (!albumDao.idExists(album.getId())) {
                            AlbumArtCoverDownloadService_.intent(getApplication()).downloadAlbumArtCoverFromUrl(album.getCoverArtUrl(), album.getCoverArtLocalPath()).start();
                            albumDao.create(album);
                        }
                        trackDao.createIfNotExists(track);
                    }
                    sendBroadcast(new Intent("com.gedexx.gpmextractor.service.extraction_finished"));
                }
            } catch (SQLException | InterruptedException | NumberFormatException | IndexOutOfBoundsException e) {
                sendBroadcast(new Intent("com.gedexx.gpmextractor.service.extraction_error"));
            } finally {
                p.destroy();
            }
        } catch (IOException e) {
            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.extraction_error"));
        }

    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.download_error")
    void onErrorDownload() {
        sendBroadcast(new Intent("com.gedexx.gpmextractor.service.extraction_error"));
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.download_finished")
    void onFinishedDownload() {
    }

    private Artist createArtist(String line) throws NumberFormatException, IndexOutOfBoundsException {

        String[] splittedLine = line.split(",");

        Artist artist = new Artist();
        artist.setId(Long.valueOf(splittedLine[ARTIST_ID_COLUMN_NUM]));
        artist.setName(splittedLine[ARTIST_NAME_COLUMN_NUM]);

        return artist;
    }

    private Album createAlbum(String line) throws NumberFormatException, IndexOutOfBoundsException {

        String[] splittedLine = line.split(",");

        Album album = new Album();
        album.setId(Long.valueOf(splittedLine[ALBUM_ID_COLUMN_NUM]));
        album.setName(splittedLine[ALBUM_NAME_COLUMN_NUM]);
        album.setCoverArtUrl(splittedLine[ALBUM_ART_LOCATION_COLUMN_NUM]);
        album.setCoverArtLocalPath(Uri.parse(album.getCoverArtUrl()).getLastPathSegment());
        album.setGenre(splittedLine[ALBUM_GENRE_COLUMN_NUM]);

        return album;
    }

    private Track createTrack(String line) throws NumberFormatException, IndexOutOfBoundsException {

        String[] splittedLine = line.split(",");

        Track track = new Track();
        track.setId(Long.valueOf(splittedLine[SONG_ID_COLUMN_NUM]));
        track.setTitle(splittedLine[SONG_TITLE_COLUMN_NUM]);
        track.setDuration(Long.valueOf(splittedLine[SONG_DURATION_COLUMN_NUM]));
        track.setLocalCopyPath(splittedLine[SONG_LOCAL_COPY_PATH_COLUMN_NUM]);
        track.setCpData(splittedLine[SONG_CP_DATA_COLUMN_NUM]);

        return track;
    }

}
