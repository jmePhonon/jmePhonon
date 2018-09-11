package com.jme3.phonon;

import com.jme3.audio.AudioSource;

public class PhononAudioSourcesDataManager {
    private final PhononAudioSourceData[] DATA;

    public PhononAudioSourcesDataManager(int nOutputLines, int nSourcesPerLine) {
        int nTotalSource = nOutputLines * nSourcesPerLine;

        DATA = new PhononAudioSourceData[nTotalSource];

        for(int i = 0; i < nTotalSource; ++i) {
            DATA[i] = new PhononAudioSourceData(); 
        }
    }

    public void pairSourceAndData(AudioSource src, int dataIndex) {
        src.setChannel(dataIndex);
        DATA[dataIndex].setSource(src);
    }

    public void unpairSourceAndData(AudioSource src) {
        DATA[src.getChannel()].setSource(null);
        src.setChannel(-1);
    }

    public void setSrcFlagsUpdateNeeded(AudioSource src) {
        DATA[src.getChannel()].setFlagsUpdateNeeded();

    }

    public void setSrcPosUpdateNeeded(AudioSource src) {
        DATA[src.getChannel()].setPosUpdateNeeded();
    }
    
    public void setSrcDirUpdateNeeded(AudioSource src) {
        DATA[src.getChannel()].setDirUpdateNeeded();
    }

    public void setSrcVolUpdateNeeded(AudioSource src) {
        DATA[src.getChannel()].setVolUpdateNeeded();
    }
    
    public void finalizeDataUpdates() {
        for(PhononAudioSourceData sourceData : DATA) {
            sourceData.finalizeUpdate();
        }
    }

    public void updateData() {
        for(PhononAudioSourceData sourceData : DATA) {
            sourceData.update();
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