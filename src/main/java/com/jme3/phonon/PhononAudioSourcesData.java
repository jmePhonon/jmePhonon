package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.jme3.audio.AudioSource;
import com.jme3.math.Vector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;
import static com.jme3.phonon.memory_layout.AUDIOSOURCE_LAYOUT.*;

public class PhononAudioSourcesData {
    private final ByteBuffer[] MEMORIES;
    private final long[] ADDRESSES;

    public PhononAudioSourcesData(int nOutputLines, int nSourcesPerLine) {
        int nTotalSource = nOutputLines * nSourcesPerLine;

        MEMORIES = new ByteBuffer[nTotalSource];
        for(int i = 0; i < nTotalSource; ++i) {
            MEMORIES[i] = BufferUtils.createByteBuffer(SIZE);
        }

        ADDRESSES = new long[nTotalSource];
        for(int i = 0; i < nTotalSource; ++i) {
            ADDRESSES[i] = DirectBufferUtils.getAddr(MEMORIES[i]);
        }
    }

    public void updateSourcePosition(AudioSource src) {
        int index = src.getChannel();
        Vector3f position = src.getPosition();

        MEMORIES[index].putFloat(POSX, position.x);
        MEMORIES[index].putFloat(POSY, position.y);
        MEMORIES[index].putFloat(POSZ, position.z);
    }


    public long[] getAddresses() {
        return ADDRESSES;
    }
}