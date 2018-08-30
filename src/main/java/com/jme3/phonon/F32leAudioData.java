package com.jme3.phonon;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioStream;
import com.jme3.util.BufferUtils;

/**
 * F32leAudioData
 * 
 * Audio data ready to be processed by phonon
 */
public class F32leAudioData {
    private int channels,sampleRate;
    private ByteBuffer data;
    private long dataAddress;
    public F32leAudioData() {
    }


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
        return data.limit()/4;
    }
 
    public F32leAudioData(AudioData ad) {
        if (ad instanceof AudioBuffer) {
            AudioBuffer ab = (AudioBuffer) ad;
            channels = ab.getChannels();
            sampleRate = ab.getSampleRate();
            int bitsPerSample = ab.getBitsPerSample();

            // Little endian input buffer
            ByteBuffer inputData = ab.getData();
        
            data = BufferUtils.createByteBuffer((inputData.limit() / (bitsPerSample / 8)) * 4);
            inputData.rewind();
            dataAddress = DirectBufferUtils.getAddr(data);

            byte float_le[] = new byte[4];
            switch (bitsPerSample) {
            case 8: {
                byte sm_le[] = new byte[1];
                for (int i = 0; i < inputData.limit(); i++) {
                    BitUtils.nextI8le(inputData, sm_le);
                    BitUtils.cnvI8leToF32le(sm_le,float_le);
                    data.put(float_le);
                }
                break;
            }
            case 16: {
                byte sm_le[] = new byte[2];
                for (int i = 0; i < inputData.limit(); i += 2) {
                    BitUtils.nextI16le(inputData, sm_le,1);
                    BitUtils.cnvI16leToF32le(sm_le,float_le);
                    data.put(float_le);
                }
                break;
            }
            case 24: {
                byte sm_le[] = new byte[3];
                for (int i = 0; i < inputData.limit(); i += 3) {
                    BitUtils.nextI24le(inputData, sm_le,1);
                    BitUtils.cnvI24leToF32le(sm_le,float_le);
                    data.put(float_le);                    
                }
                break;
            }
            }
            inputData.rewind();
            data.rewind();
        } else if (ad instanceof AudioStream) { // Handle audio stream
            throw new UnsupportedOperationException("Can't handle audio stream right now");
        } else {
            throw new UnsupportedOperationException("Unknown audio data " + ad.getClass());
        }
    }

    public ByteBuffer getData() {
        return data;
    }

    public F32leAudioData rewind() {
        data.rewind();
        return this;
    }
    
    public int getChannels() {
        return channels;
    }

    public int getBitsPerSample() {
        return 32;
    }


    public int getSampleRate() {
        return sampleRate;
    }

    public F32leAudioData writeRaw(OutputStream os) throws IOException {
        data.rewind();
        byte array[] = new byte[data.limit()];
        data.get(array);
        os.write(array);
        data.rewind();
        return this;
    }

    public F32leAudioData readRaw(int channels, int sampleRate, InputStream is) throws IOException {
        this.channels = channels;
        this.sampleRate = sampleRate;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int read;
        byte part[]=new byte[1024];
        while ((read = is.read(part)) != -1) {
            bos.write(part, 0, read);
        }
        data = BufferUtils.createByteBuffer(bos.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);        
        return this;
    }
}