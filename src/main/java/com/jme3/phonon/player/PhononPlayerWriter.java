package com.jme3.phonon.player;

import javax.sound.sampled.SourceDataLine;

class PhononPlayerWriter {
    public final SourceDataLine dataLine;
    public final int size;

    private final byte[] lineWriteCache;

    public PhononPlayerWriter(SourceDataLine line, int size) {
        this.dataLine = line;
        this.size = size;

        this.lineWriteCache = new byte[size];
    }

    public void writeToLine(int length) {
        dataLine.write(lineWriteCache, 0, length);
    }

    public int getWritableBytes() {
        int available = dataLine.available();
        return available < size ? available : size;
    }

    public byte[] getCache() {
        return lineWriteCache;
    }
}