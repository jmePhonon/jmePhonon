package com.jme3.phonon.format.decoder;

public interface AudioDataDecoder {
    /**
     * Auxiliary method to decode multiple floats to ints.
     * 
     * @param inputBuffer  Input buffer
     * @param outputBuffer Output buffer
     * 
     * @author aegroto
     */

    public abstract void decode(byte[] input, byte[] output);

}