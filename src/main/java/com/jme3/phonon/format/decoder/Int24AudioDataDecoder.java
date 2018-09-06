package com.jme3.phonon.format.decoder;

import com.jme3.phonon.utils.BitUtils;

class Int24AudioDataDecoder implements AudioDataDecoder {
    private static Int24AudioDataDecoder instance;

    public static Int24AudioDataDecoder instance() {
        if(instance == null)
            instance = new Int24AudioDataDecoder();
        
        return instance;
    }

    final byte[] partInputBuffer;
    final byte[] partOutputBuffer;

    private Int24AudioDataDecoder() {
        partInputBuffer = new byte[4];
        partOutputBuffer = new byte[3];
    }

    @Override
    public void decode(byte[] inputBuffer, byte[] outputBuffer) {
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