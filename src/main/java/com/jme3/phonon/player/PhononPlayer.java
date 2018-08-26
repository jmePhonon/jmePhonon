package com.jme3.phonon.player;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.F32leAudioData;

public class PhononPlayer {
    // public final static int FRAME_SIZE = 8, CACHE_SIZE = 128;

    public final ByteBuffer audioBuffer;
    public final long size;
    public final int frameSize;

    private SourceDataLine dataLine;

    public PhononPlayer(F32leAudioData audioData, int sampleSize, int frameRate) {
        this.frameSize = ((16 + 7) / 8) * audioData.getChannels();
        this.audioBuffer = audioData.getData();
        this.size = audioBuffer.capacity() / frameSize;

        final AudioFormat audioFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED, // encoding
            (float) audioData.getSampleRate(), // sample rate
            sampleSize, // sample size
            audioData.getChannels(), // channels
            frameSize, // frame size
            frameRate, // frame rate
            false // big endian
        );

        try {
            dataLine = AudioSystem.getSourceDataLine(audioFormat);
            dataLine.open(audioFormat, frameSize * sampleSize);
        } catch(LineUnavailableException ex) {
            dataLine = null;
            ex.printStackTrace();
        }
    }

    public void play() {
        dataLine.start();

        long playedFrames = 0;
        byte[] tempBuffer = new byte[frameSize];

        while(playedFrames < size) {
            audioBuffer.get(tempBuffer, 0, frameSize);

            dataLine.write(tempBuffer, 0, frameSize);

            ++playedFrames;
        }
    }
}