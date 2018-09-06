package com.jme3.phonon.format.encoder;

import java.nio.ByteBuffer;

import com.jme3.phonon.utils.BitUtils;

class Int24AudioDataEncoder implements AudioDataEncoder {
    byte float_le[] = new byte[4];
    byte sm_le[] = new byte[3];

    /**
     * Encode 24-bit input audio data to float32 audio data.
     * 
     * @param input Input audio data.
     * @param output Output buffer;
     * 
     * @author aegroto, riccardobl
     */

    public void encodeData(ByteBuffer input, ByteBuffer output) {
        for (int i = 0; i < input.limit(); i += 3) {
            BitUtils.nextI24le(input, sm_le);
            BitUtils.cnvI24leToF32le(sm_le,float_le);
            output.put(float_le);
        }
    }
}