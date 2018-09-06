package com.jme3.phonon.format;

import java.nio.ByteBuffer;

import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioStream;
import com.jme3.phonon.format.converter.AudioDataEncoder;
import com.jme3.phonon.format.converter.AudioDataEncoderFactory;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

/**
 * F32leAudioData
 * 
 * Audio data ready to be processed by phonon
 */
public class F32leAudioData {
    protected int sampleRate;
    protected int channels;
    protected ByteBuffer data;
    private long dataAddress;

    public F32leAudioData() { }


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
        return data.limit() / 4;
    }
 
    public F32leAudioData(AudioData ad) {
        if (ad instanceof AudioBuffer) {
            AudioBuffer ab = (AudioBuffer) ad;
            channels = ab.getChannels();
            sampleRate = ab.getSampleRate();
            int bufferBitsPerSample = ab.getBitsPerSample();

            // Little endian input buffer
            ByteBuffer inputData = ab.getData();
        
            data = BufferUtils.createByteBuffer((inputData.limit() / (bufferBitsPerSample / 8)) * 4);
            inputData.rewind();
            dataAddress = DirectBufferUtils.getAddr(data);

            AudioDataEncoder converter = AudioDataEncoderFactory.getEncoder(bufferBitsPerSample);
            converter.encodeData(inputData, data);

            inputData.rewind();
            data.rewind();
        } else if (ad instanceof AudioStream) { // Handle audio stream
            throw new UnsupportedOperationException("Can't handle audio stream right now");
        } else {
            throw new UnsupportedOperationException("Unknown audio data " + ad.getClass());
        }
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