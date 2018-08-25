package com.jme3.audio.sa;

import java.io.ByteArrayOutputStream;
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
 * Audio data ready to be processed by steam audio
 */
public class F32leAudioData {
    public int channels,sampleRate;
    private ByteBuffer data;

    public F32leAudioData() {
    }

    public F32leAudioData(AudioData ad) {
        if (ad instanceof AudioBuffer) {
            AudioBuffer ab = (AudioBuffer) ad;
            channels = ab.getChannels();
            sampleRate = ab.getSampleRate();
            int bitsPerSample = ab.getBitsPerSample();
            ByteBuffer inputData = ab.getData().order(ByteOrder.LITTLE_ENDIAN);
        
            data = BufferUtils.createByteBuffer((inputData.limit() / (bitsPerSample / 8)) * 4)
                    .order(ByteOrder.LITTLE_ENDIAN);
            switch (bitsPerSample) {
            case 8: {
                for (int i = 0; i < inputData.limit(); i++) {
                    float v = (float) inputData.get(i) / Byte.MAX_VALUE;
                    data.putFloat(v);
                }
                break;
            }
            case 16: {
                ShortBuffer inputSb = inputData.asShortBuffer();
                for (int i = 0; i < inputSb.limit(); i++) {
                    float v = (float) inputSb.get(i) / Short.MAX_VALUE;
                    data.putFloat(v);
                }
                break;
            }
            // case 24?
            }
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