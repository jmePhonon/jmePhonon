package com.jme3.phonon;

import java.nio.ByteBuffer;

import com.jme3.audio.AudioSource;
import com.jme3.phonon.memory_layout.AUDIOSOURCE_LAYOUT;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

public class PhononAudioSourcesData {
    private final ByteBuffer[] MEMORIES;
    private final long[] ADDRESSES;

    public PhononAudioSourcesData(int nOutputLines, int nSourcesPerLine) {
        int nTotalSource = nOutputLines * nSourcesPerLine;

        MEMORIES = new ByteBuffer[nTotalSource];
        for(int i = 0; i < nTotalSource; ++i) {
            MEMORIES[i] = BufferUtils.createByteBuffer(AUDIOSOURCE_LAYOUT.SIZE);
        }

        ADDRESSES = new long[nTotalSource];
        for(int i = 0; i < nTotalSource; ++i) {
            ADDRESSES[i] = DirectBufferUtils.getAddr(MEMORIES[i]);
        }
    }

    public void updateSourcePosition(AudioSource src) {

    }


    public long[] getAddresses() {
        return ADDRESSES;
    }
}