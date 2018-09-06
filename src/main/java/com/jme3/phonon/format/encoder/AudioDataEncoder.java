package com.jme3.phonon.format.encoder;

import java.nio.ByteBuffer;

public interface AudioDataEncoder {
    /**
     * Encode input audio data and stores them in a given output buffer.
     * 
     * @param input Input audio data
     * @param output Output buffer
     * 
     * @author aegroto
     */
    public abstract void encodeData(ByteBuffer input, ByteBuffer output);
}