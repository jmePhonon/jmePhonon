package com.jme3.phonon.format.converter;

import java.nio.ByteBuffer;

public interface AudioDataConverter {
    /**
     * Converts input audio data and stores them in a given output buffer.
     * 
     * @param input Input audio data
     * @param output Output buffer
     * 
     * @author aegroto
     */
    public abstract void convertData(ByteBuffer input, ByteBuffer output);
}