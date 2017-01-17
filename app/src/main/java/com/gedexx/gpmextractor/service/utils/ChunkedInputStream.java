package com.gedexx.gpmextractor.service.utils;

import java.io.IOException;

public interface ChunkedInputStream {
    int availableBytes() throws IOException;

    void close() throws IOException;

    int getChunkSize();

    int read(byte[] bArr, int i, int i2) throws IOException;

    long skipChunks(long j) throws IOException;
}
