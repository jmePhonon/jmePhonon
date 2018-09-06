package com.jme3.phonon.format.converter;

import java.nio.ByteBuffer;

import com.jme3.phonon.utils.BitUtils;

class Int8AudioDataEncoder implements AudioDataEncoder {
    byte float_le[] = new byte[4];
    byte sm_le[] = new byte[1];

    /**
     * Encode 8-bit input audio data to float32 audio data.
     * 
     * @param input Input audio data.
     * @param output Output buffer;
     * 
     * @author aegroto, riccardobl
     */

    public void encodeData(ByteBuffer input, ByteBuffer output) {
        for (int i = 0; i < input.limit(); i++) {
            BitUtils.nextI8le(input, sm_le);
            BitUtils.cnvI8leToF32le(sm_le, float_le);
            output.put(float_le);
        }
    }
}