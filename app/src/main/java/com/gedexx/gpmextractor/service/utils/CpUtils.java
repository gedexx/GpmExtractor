package com.gedexx.gpmextractor.service.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class CpUtils {
    static final byte[] MAGIC_NUMBER;

    static {
        MAGIC_NUMBER = new byte[]{(byte) 18, (byte) -45, (byte) 21, (byte) 39};
    }

    public static long getDecryptedSize(long encryptedSize) {
        if (encryptedSize < 4) {
            return 0;
        }
        long contentSize = encryptedSize - 4;
        long wholeBlocks = contentSize / 1024;
        return (1008 * wholeBlocks) + Math.max(0, (contentSize - (1024 * wholeBlocks)) - 16);
    }

    static Cipher getCipher() {
        try {
            return Cipher.getInstance("AES/CTR/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Required content protection algorithm is not present: AES/CTR/NoPadding", e);
        } catch (NoSuchPaddingException e2) {
            throw new IllegalStateException("Required padding is not supported: AES/CTR/NoPadding", e2);
        }
    }
}
