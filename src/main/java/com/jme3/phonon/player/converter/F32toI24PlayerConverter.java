package com.jme3.phonon.player.converter;

import com.jme3.phonon.utils.BitUtils;

class F32toI24PlayerConverter implements PlayerConverter {
    private static F32toI24PlayerConverter instance;

    public static F32toI24PlayerConverter instance() {
        if(instance == null)
            instance = new F32toI24PlayerConverter();
        
        return instance;
    }

    final byte[] partInputBuffer;
    final byte[] partOutputBuffer;

    private F32toI24PlayerConverter() {
        partInputBuffer = new byte[4];
        partOutputBuffer = new byte[3];
    }

    public void convert(byte[] inputBuffer, byte[] outputBuffer) {
        for (int i = 0; i < inputBuffer.length; i += 4) {
            for(int j = 0; j < partInputBuffer.length; ++j) {
                partInputBuffer[j] = inputBuffer[i + j];
            }

            BitUtils.cnvF32leToI24le(partInputBuffer, partOutputBuffer);

            for (int j = 0; j < partOutputBuffer.length; ++j)
                outputBuffer[(i / 4) * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}