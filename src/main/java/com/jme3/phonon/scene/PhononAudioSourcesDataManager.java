/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package com.jme3.phonon.scene;

import com.jme3.audio.AudioSource;
import com.jme3.phonon.PhononOutputLine;

public class PhononAudioSourcesDataManager {
    private final PhononAudioSourceData[] DATA;

    public PhononAudioSourcesDataManager(int nOutputLines, int nSourcesPerLine) {
        int nTotalSource = nOutputLines * nSourcesPerLine;

        DATA = new PhononAudioSourceData[nTotalSource];
        for(int i = 0; i < nTotalSource; ++i) {
            DATA[i] = new PhononAudioSourceData(); 
        }
    }

    public PhononAudioSourceData[] getSourceDatas() {
        return DATA;
    }

    public void pairSourceAndData(PhononOutputLine line,AudioSource src, int dataIndex) {
        src.setChannel(dataIndex);
        DATA[dataIndex].setLine(line);
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

    public void setSrcDipPowerUpdateNeeded(AudioSource src) {
        DATA[src.getChannel()].setDipolePowerUpdateNeeded();
    }
    
    public void setSrcDipWeightUpdateNeeded(AudioSource src) {
        DATA[src.getChannel()].setDipoleWeightUpdateNeeded();
    }
    
    public void finalizeDataUpdates() {
        for(PhononAudioSourceData sourceData : DATA) {
            sourceData.finalizeUpdate();
        }
    }

    public void updateData() {
        for (PhononAudioSourceData sourceData : DATA) {
            sourceData.update();
            if (sourceData.getSource() != null && sourceData.getSource().getStatus() == AudioSource.Status.Stopped) {                
		        unpairSourceAndData(sourceData.getSource());
            }            
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