package com.jme3.phonon.format.converter;

import java.nio.ByteBuffer;

import com.jme3.phonon.utils.BitUtils;

class Int16AudioDataConverter implements AudioDataConverter {
    byte float_le[] = new byte[4];
    byte sm_le[] = new byte[2];

    /**
     * Converts input 16-bit audio data to float32 audio data.
     * 
     * @param input Input audio data.
     * @param output Output buffer;
     * 
     * @author aegroto, riccardobl
     */

    public void convertData(ByteBuffer input, ByteBuffer output) {
        for (int i = 0; i < input.limit(); i += 2) {
            BitUtils.nextI16le(input, sm_le);
            BitUtils.cnvI16leToF32le(sm_le,float_le);
            output.put(float_le);
        }
    }
}