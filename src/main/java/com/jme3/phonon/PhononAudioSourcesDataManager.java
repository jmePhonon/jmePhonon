package com.jme3.phonon;

import java.nio.ByteBuffer;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.Vector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

public class PhononAudioSourcesDataManager {
    private final PhononAudioSourceData[] DATA;

    public PhononAudioSourcesDataManager(int nOutputLines, int nSourcesPerLine) {
        int nTotalSource = nOutputLines * nSourcesPerLine;

        DATA = new PhononAudioSourceData[nTotalSource];

        for(int i = 0; i < nTotalSource; ++i) {
            DATA[i] = new PhononAudioSourceData(); 
        }
    }

    public void updateSourcePosition(AudioSource src) {
        DATA[src.getChannel()].positionUpdate(src);
    }
    
    public void updateSourceDirection(AudioSource src) {
        DATA[src.getChannel()].directionUpdate(src);
    }

    public void updateSourceVolume(AudioSource src) {
        DATA[src.getChannel()].volumeUpdate(src);
    }
    
    public void finalizeUpdates() {
        for(PhononAudioSourceData sourceData : DATA) {
            sourceData.finalizeUpdate();
        }
    }

    public long[] memoryAddresses() {
        long audioSourceDataAddresses[] = new long[DATA.length];

		for(int i = 0; i < audioSourceDataAddresses.length; i++) {
			audioSourceDataAddresses[i] = DATA[i].getAddress(); 
		}

        return audioSourceDataAddresses;
    }
}