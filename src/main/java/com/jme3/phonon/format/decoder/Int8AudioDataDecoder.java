package com.jme3.phonon.format.decoder;

import com.jme3.phonon.utils.BitUtils;

class Int8AudioDataDecoder implements AudioDataDecoder {
    private static Int8AudioDataDecoder instance;    

    public static Int8AudioDataDecoder instance() {
        if(instance == null) 
            instance = new Int8AudioDataDecoder();

        return instance;
    }

    final byte[] partInputBuffer;
    final byte[] partOutputBuffer;

    private Int8AudioDataDecoder() {
        partInputBuffer = new byte[4];
        partOutputBuffer = new byte[1];
    }

    @Override
    public void decode(byte[] inputBuffer, byte[] outputBuffer) {
        for (int i = 0; i < inputBuffer.length; i += 4) {
            for(int j = 0; j < partInputBuffer.length; ++j) {
                partInputBuffer[j] = inputBuffer[i + j];
            }

            BitUtils.cnvF32leToI8le(partInputBuffer, partOutputBuffer);

            for (int j = 0; j < partOutputBuffer.length; ++j)
                outputBuffer[(i / 4) * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}