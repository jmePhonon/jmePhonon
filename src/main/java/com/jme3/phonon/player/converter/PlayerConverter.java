package com.jme3.phonon.player.converter;

public interface PlayerConverter {
    /**
     * Auxiliary method to decode multiple floats to ints.
     * 
     * @param inputBuffer  Input buffer
     * @param outputBuffer Output buffer
     * 
     * @author aegroto
     */

    public abstract void convert(byte[] input, byte[] output);

}