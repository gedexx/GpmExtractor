package com.gedexx.gpmextractor.service;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.gedexx.gpmextractor.domain.Track;
import com.gedexx.gpmextractor.service.utils.ChunkedInputStream;
import com.gedexx.gpmextractor.service.utils.ChunkedInputStreamAdapter;
import com.gedexx.gpmextractor.service.utils.CpInputStream;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import static com.gedexx.gpmextractor.GpmExtractorApplication.DATA_DIR_PATH;
import static com.gedexx.gpmextractor.GpmExtractorApplication.GPM_PACKAGE_NAME;

@EIntentService
public class DecryptionService extends AbstractIntentService {

    final protected static String GPM_MUSIC_FILE_PATH = "/files/music/";
    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final String MP3_EXTENSION = ".mp3";

    public DecryptionService() {
        super(DecryptionService.class.getSimpleName());
    }

    @ServiceAction
    public void decrypt(Collection<Track> tracks) {

        sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_started"));

        for (Track track : tracks) {
            try {

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

                        // Decryption du fichier copié

                        final byte[] secretkey = hexStringToByteArray(track.getCpData());

                        final File srcFile = new File(pathTarget);
                        final FileInputStream fis = new FileInputStream(srcFile);

                        final ChunkedInputStream cpInput = new CpInputStream(fis, secretkey);
                        final ChunkedInputStreamAdapter in = new ChunkedInputStreamAdapter(cpInput);

                        final String strippedArtistName = track.getArtist().getName().replace("\"", "");
                        final String strippedAlbumName = track.getAlbum().getName().replace("\"", "");
                        final String strippedTrackTitle = track.getTitle().replace("\"", "").replace(" / ", "_").replace(" ", "_");
                        final String outFileName = strippedTrackTitle + MP3_EXTENSION;

                        final File targetPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), strippedArtistName + File.separator + strippedAlbumName);
                        if (!targetPath.mkdirs()) {
                            Log.i(DecryptionService.class.getName(), "Dossier déjà existant !");
                        }

                        final File targetFile = new File(targetPath, outFileName);
                        final FileOutputStream out = new FileOutputStream(targetFile);

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        in.close();
                        out.close();

                        if (!srcFile.delete()) {
                            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_error"));
                        }
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
            }
        }
        sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_success"));
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
