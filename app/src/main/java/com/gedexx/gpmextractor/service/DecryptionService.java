package com.gedexx.gpmextractor.service;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.gedexx.gpmextractor.domain.Track;
import com.gedexx.gpmextractor.helper.database.GpmDatabaseHelper;
import com.gedexx.gpmextractor.service.utils.ChunkedInputStream;
import com.gedexx.gpmextractor.service.utils.ChunkedInputStreamAdapter;
import com.gedexx.gpmextractor.service.utils.CpInputStream;
import com.j256.ormlite.dao.Dao;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;

import static com.gedexx.gpmextractor.GpmExtractorApplication.DATA_DIR_PATH;
import static com.gedexx.gpmextractor.GpmExtractorApplication.GPM_PACKAGE_NAME;

@EIntentService
public class DecryptionService extends AbstractIntentService {

    final protected static String GPM_MUSIC_FILE_PATH = "/files/music/";
    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final String MP3_EXTENSION = ".mp3";

    @OrmLiteDao(helper = GpmDatabaseHelper.class)
    Dao<Track, Long> trackDao;

    public DecryptionService() {
        super(DecryptionService.class.getSimpleName());
    }

    @ServiceAction
    public void decrypt(Collection<Long> trackIds) {

        sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_started"));

        for (Long trackId : trackIds) {
            try {
                final Track track = trackDao.queryForId(trackId);

                String pathSource = DATA_DIR_PATH + GPM_PACKAGE_NAME + GPM_MUSIC_FILE_PATH + track.getLocalCopyPath();
                //String pathTarget = getFilesDir().getAbsolutePath() + File.separator + track.getLocalCopyPath();
                String pathTarget = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + track.getLocalCopyPath();

                String commandCp = "cp " + pathSource + " " + pathTarget + "\n";
                /*String commandCHOwn = "chown " + getApplicationInfo().uid + "." + getApplicationInfo().uid + " " + pathTarget + "\n";
                String commandCHMod = "chmod 660 " + pathTarget + "\n";*/

                Process p = Runtime.getRuntime().exec("su");

                OutputStream os = p.getOutputStream();

                os.write((commandCp).getBytes("ASCII"));
                os.flush();
                /*os.write((commandCHOwn).getBytes("ASCII"));
                os.flush();
                os.write((commandCHMod).getBytes("ASCII"));*/

                os.close();

                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {

                        final String strippedArtistName = track.getArtist().getName().replace("\"", "");
                        final String strippedAlbumName = track.getAlbum().getName().replace("\"", "");
                        final String strippedTrackTitle = track.getTitle().replace("\"", "").replace(" / ", "_");
                        final String trackNumber = track.getNumber() < 10 ? "0" + String.valueOf(track.getNumber()) : String.valueOf(track.getNumber());
                        final String outFileName = trackNumber + " - " + strippedTrackTitle + MP3_EXTENSION;

                        final File targetPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), strippedArtistName + File.separator + strippedAlbumName);
                        if (!targetPath.mkdirs()) {
                            Log.d(DecryptionService.class.getName(), "Dossier déjà existant !");
                        }

                        final File targetFile = new File(targetPath, outFileName);
                        final File targetFileTemp = new File(targetPath, "temp_" + outFileName);
                        final FileOutputStream out = new FileOutputStream(targetFileTemp);

                        // Decryption du fichier copié

                        final byte[] secretkey = hexStringToByteArray(track.getCpData());

                        final File srcFile = new File(pathTarget);
                        final FileInputStream fis = new FileInputStream(srcFile);

                        InputStream in;
                        try {
                            final ChunkedInputStream cpInput = new CpInputStream(fis, secretkey);
                            in = new ChunkedInputStreamAdapter(cpInput);
                        } catch (IllegalArgumentException e) {
                            Log.d(DecryptionService.class.getName(), "Musique provenant d'un upload ultérieur");
                            in = fis;
                        }

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        in.close();
                        out.close();

                        mp3TaggingTrack(targetFileTemp, targetFile, track);

                        if (!targetFileTemp.delete()) {
                            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_error"));
                        }

                        if (!srcFile.delete()) {
                            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_error"));
                        }

                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(targetFile)));
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_error"));
                } finally {
                    p.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
                sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_error"));
            } catch (SQLException e) {
                e.printStackTrace();
                sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_error"));
            }
        }
        sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_success"));
    }

    private void mp3TaggingTrack(File srcFile, File targetFile, Track track) {
        try {
            Mp3File trackFile = new Mp3File(srcFile);

            ID3v2 id3v2Tag;
            if (trackFile.hasId3v2Tag()) {
                id3v2Tag = trackFile.getId3v2Tag();
            } else {
                id3v2Tag = new ID3v24Tag();
                trackFile.setId3v2Tag(id3v2Tag);
            }

            final String trackNumber = String.valueOf(track.getNumber());
            final String trackTitle = track.getTitle().replace("\"", "");
            final String artistName = track.getArtist().getName().replace("\"", "");
            final String albumName = track.getAlbum().getName().replace("\"", "");
            final String trackYear = String.valueOf(track.getAlbum().getYear());
            final String albumGenre = track.getAlbum().getGenre().replace("\"", "");

            id3v2Tag.setTrack(trackNumber);
            id3v2Tag.setTitle(trackTitle);
            id3v2Tag.setArtist(artistName);
            id3v2Tag.setAlbum(albumName);
            id3v2Tag.setYear(trackYear);
            id3v2Tag.setGenreDescription(albumGenre);
            id3v2Tag.setAlbumImage(fileInputStreamToByteArray(openFileInput(track.getAlbum().getCoverArtLocalPath())), "image/png");

            trackFile.save(targetFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (InvalidDataException e) {
            e.printStackTrace();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        }
    }

    private static byte[] fileInputStreamToByteArray(FileInputStream fis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int bytesRead;

        while ((bytesRead = fis.read(b)) != -1) {
            bos.write(b, 0, bytesRead);
        }

        fis.close();
        bos.close();

        return bos.toByteArray();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;

        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }
}
