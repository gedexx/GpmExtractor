package com.gedexx.gpmextractor.service.utils;

import java.io.IOException;
import java.io.InputStream;

public class ChunkedInputStreamAdapter extends InputStream {
    final byte[] mBuffer;
    int mBufferEnd;
    final int mChunkSize;
    final ChunkedInputStream mInput;
    int mPosition;

    public ChunkedInputStreamAdapter(ChunkedInputStream chunkedInputStream) {
        mInput = chunkedInputStream;
        mChunkSize = mInput.getChunkSize();
        mBuffer = new byte[mChunkSize];
        mPosition = 0;
        mBufferEnd = 0;
    }

    public int read() throws IOException {
        if (mPosition == mBufferEnd) {
            fillBuffer();
        }
        if (mPosition == mBufferEnd) {
            return -1;
        }
        byte[] bArr = mBuffer;
        int i = mPosition;
        mPosition = i + 1;
        return bArr[i] & 255;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (len < 1) {
            return 0;
        }
        int bytesToCopy = Math.min(len, mBufferEnd - mPosition);
        System.arraycopy(mBuffer, mPosition, b, off, bytesToCopy);
        mPosition += bytesToCopy;
        off += bytesToCopy;
        int totalBytesRead = 0 + bytesToCopy;
        len -= bytesToCopy;
        while (len >= mChunkSize) {
            int bytesReadThisChunk = mInput.read(b, off, mChunkSize);
            if (bytesReadThisChunk != -1) {
                off += bytesReadThisChunk;
                totalBytesRead += bytesReadThisChunk;
                len -= bytesReadThisChunk;
            } else if (totalBytesRead == 0) {
                return -1;
            } else {
                return totalBytesRead;
            }
        }
        if (len <= 0) {
            return totalBytesRead;
        }
        fillBuffer();
        if (mPosition != mBufferEnd) {
            bytesToCopy = Math.min(len, mBufferEnd - mPosition);
            System.arraycopy(mBuffer, mPosition, b, off, bytesToCopy);
            mPosition += bytesToCopy;
            off += bytesToCopy;
            len -= bytesToCopy;
            return totalBytesRead + bytesToCopy;
        } else if (totalBytesRead == 0) {
            return -1;
        } else {
            return totalBytesRead;
        }
    }

    public long skip(long n) throws IOException {
        long totalSkipped = 0;
        if (n > 0) {
            long bytesToSkip = Math.min(n, (long) (mBufferEnd - mPosition));
            totalSkipped = 0 + bytesToSkip;
            mPosition = (int) (((long) mPosition) + bytesToSkip);
            n -= bytesToSkip;
            long chunksToSkip = n / ((long) mChunkSize);
            if (chunksToSkip > 0) {
                long bytesSkipped = mInput.skipChunks(chunksToSkip) * ((long) mChunkSize);
                totalSkipped += bytesSkipped;
                n -= bytesSkipped;
                if (bytesSkipped != chunksToSkip * ((long) mChunkSize)) {
                    return totalSkipped;
                }
            }
            if (n > 0) {
                fillBuffer();
                bytesToSkip = Math.min(n, (long) (mBufferEnd - mPosition));
                totalSkipped += bytesToSkip;
                mPosition = (int) (((long) mPosition) + bytesToSkip);
                n -= bytesToSkip;
            }
        }
        return totalSkipped;
    }

    public int available() throws IOException {
        return (mBufferEnd - mPosition) + mInput.availableBytes();
    }

    public void close() throws IOException {
        mInput.close();
    }

    private void fillBuffer() throws IOException {
        mPosition = 0;
        int bytesRead = mInput.read(mBuffer, 0, mChunkSize);
        if (bytesRead == -1) {
            mBufferEnd = 0;
        } else {
            mBufferEnd = bytesRead;
        }
    }
}
