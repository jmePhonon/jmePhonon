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
package com.jme3.phonon.format;

import java.nio.ByteBuffer;

import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioStream;
import com.jme3.phonon.format.encoder.AudioDataEncoder;
import com.jme3.phonon.format.encoder.AudioDataEncoderFactory;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

/**
 * F32leAudioData
 * 
 * Audio data ready to be processed by phonon
 */

public class F32leAudioData {
    private final int sampleRate;
    private final int channels;
    private final ByteBuffer data;
    private final long dataAddress;
    private final int samples;

  
    /**
     * Get native address
     */
    public long getAddress() {
        return dataAddress;
    }

    /**
     * Get size in samples
     */
    public int getSizeInSamples() {
        return samples;
    }
 
    public F32leAudioData(AudioData ad){
        if(ad instanceof AudioBuffer){
            AudioBuffer ab=(AudioBuffer)ad;
            channels=ab.getChannels();
            sampleRate=ab.getSampleRate();
            int bufferBitsPerSample=ab.getBitsPerSample();

            // Little endian input buffer
            ByteBuffer inputData=ab.getData();

            samples=(inputData.limit()/(bufferBitsPerSample/8));
            data=BufferUtils.createByteBuffer(samples*4);
            inputData.rewind();
            dataAddress=DirectBufferUtils.getAddr(data);

            AudioDataEncoder converter=AudioDataEncoderFactory.getEncoder(bufferBitsPerSample);
            converter.encodeData(inputData,data);

            inputData.rewind();
            data.rewind();
        }else if(ad instanceof AudioStream){ // Handle audio stream
            throw new UnsupportedOperationException("Can't handle audio stream right now");
        }else{
            throw new UnsupportedOperationException("Unknown audio data "+ad.getClass());
        }
    }
    
    public F32leAudioData(int channels,int sampleRate,ByteBuffer data) {
        this.channels = channels;
        this.sampleRate=sampleRate;           
        this.samples=data.limit()/4;
        this.data=data;
        this.dataAddress = DirectBufferUtils.getAddr(data);
    }
    
    public F32leAudioData rewind() {
        data.rewind();
        return this;
    }
    
    public int getChannels() {
        return channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBitsPerSample() {
        return 32;
    }
    
    public ByteBuffer getData() {
        return data;
    }
 

    
}