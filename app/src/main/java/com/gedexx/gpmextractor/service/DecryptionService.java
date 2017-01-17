package com.gedexx.gpmextractor.service;

import android.content.Intent;
import android.os.Environment;

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

import static com.gedexx.gpmextractor.GpmExtractorApplication.DATA_DIR_PATH;
import static com.gedexx.gpmextractor.GpmExtractorApplication.GPM_PACKAGE_NAME;

@EIntentService
public class DecryptionService extends AbstractIntentService {

    final protected static String GPM_MUSIC_FILE_PATH = "/files/music/";
    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public DecryptionService() {
        super(DecryptionService.class.getSimpleName());
    }

    @ServiceAction
    public void decrypt(String fileName, String cpData) {

        try {
            byte[] secretkey = hexStringToByteArray(cpData);
            FileInputStream fis = new FileInputStream(new File(DATA_DIR_PATH + GPM_PACKAGE_NAME + GPM_MUSIC_FILE_PATH + fileName));

            ChunkedInputStream cpInput = new CpInputStream(fis, secretkey);
            ChunkedInputStreamAdapter in = new ChunkedInputStreamAdapter(cpInput);

            FileOutputStream out = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + fileName);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();

            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_success"));
        } catch (IOException e) {
            sendBroadcast(new Intent("com.gedexx.gpmextractor.service.file_decryption_error"));
        }

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
