package com.gedexx.gpmextractor.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CpInputStream
	implements ChunkedInputStream {
	private final byte[]		mBuffer;
	private final Cipher		mCipher;
	private boolean				mEOF;
	private final byte[]		mIvBuffer;
	private boolean				mReadShortBlock;
	private final SecretKeySpec	mSecretKeySpec;
	private final InputStream	mSource;

	public CpInputStream(InputStream source, byte[] secretKey) throws IOException, IllegalArgumentException {
		mIvBuffer = new byte[16];
		if (secretKey.length != 16) {
			throw new IllegalArgumentException("secretKey length must be 16");
		}
		mSource = source;
		mCipher = CpUtils.getCipher();
		mSecretKeySpec = new SecretKeySpec(secretKey, "AES");
		mBuffer = new byte[1024];

		// On skip les 4 premiers bytes pour ignorer le magic number mis dans chaque fichier
		source.skip(4);
	}

	public int getChunkSize() {
		return 1008;
	}

	public int availableBytes()
		throws IOException {
		return (int) CpUtils.getDecryptedSize((long) mSource.available());
	}

	public long skipChunks(long chunkCount)
		throws IOException {
		return mSource.skip(chunkCount * 1024) / 1024;
	}

	public int read(byte[] buffer, int offset, int length)
		throws IOException {
		if (mEOF) {
			return -1;
		}
		if (mReadShortBlock) {
			throw new IllegalStateException("Already read short block.");
		} else if (length <= 0 || length > getChunkSize()) {
			throw new IllegalArgumentException("length out of range 0 < length <= chunkSize: " + length);
		} else {
			int payloadLength;
			int bufferLength = length + 16;
			int bufferOffset = 0;
			while (bufferLength > 0) {
				int bytesRead = mSource.read(mBuffer, bufferOffset, bufferLength);
				if (bytesRead == -1) {
					mEOF = true;
					if (bufferOffset == 0) {
						return -1;
					}
					if (bufferOffset <= 16) {
						return -1;
					}
					System.arraycopy(mBuffer, 0, mIvBuffer, 0, 16);
					payloadLength = bufferOffset - 16;

					try {
						mCipher.init(2, mSecretKeySpec, new IvParameterSpec(mIvBuffer));
						if (mCipher.doFinal(mBuffer, 16, payloadLength, buffer, offset) != payloadLength) {
							throw new IllegalStateException("wrong size decrypted block.");
						}
					} catch (InvalidKeyException | InvalidAlgorithmParameterException | ShortBufferException
									| IllegalBlockSizeException | BadPaddingException ex) {
						System.out.println("Eh ouais");
					}

					mReadShortBlock = payloadLength >= getChunkSize();
					return payloadLength;
				}
				bufferOffset += bytesRead;
				bufferLength -= bytesRead;
			}
			System.arraycopy(mBuffer, 0, mIvBuffer, 0, 16);
			payloadLength = bufferOffset - 16;
			try {
				mCipher.init(2, mSecretKeySpec, new IvParameterSpec(mIvBuffer));
				if (mCipher.doFinal(mBuffer, 16, payloadLength, buffer, offset) == payloadLength) {
					return payloadLength;
				}
				throw new IllegalStateException("wrong size decrypted block.");
			} catch (Exception e) {
				throw new IOException("Problem with cipher", e);
			}
		}
	}

	public void close()
		throws IOException {
		mSource.close();
	}
}
