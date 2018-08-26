package com.jme3.phonon.player;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.BinUtils;
import com.jme3.phonon.F32leAudioData;

import org.lwjgl.BufferUtils;

public class PhononPlayer {
    // public final static int FRAME_SIZE = 8, CACHE_SIZE = 128;

    public final ByteBuffer audioBuffer;
    public final long size;
    public final int frameSize, sampleSize;

    private SourceDataLine dataLine;

    public PhononPlayer(F32leAudioData audioData, int sampleSize, int frameRate, int frameSizeMultiplier) {
        this.frameSize = ((16 + 7) / 8) * audioData.getChannels() * frameSizeMultiplier;
        this.sampleSize = sampleSize;
        this.audioBuffer = audioData.getData();
        this.size = audioBuffer.limit() / 4;

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
        ByteBuffer tempBuffer = BufferUtils.createByteBuffer(frameSize);

        byte[] inputBuffer = new byte[4];
        byte[] outBuffer = new byte[sampleSize/8];
        byte[] frameBuffer = new byte[frameSize];

        while(playedFrames < size) {
            int reads = 0;            

            while(reads < frameSize/(sampleSize/8)) {
                BinUtils.nextF32le(audioBuffer, inputBuffer);

                if(sampleSize == 8) {
                    BinUtils.cnvF32leToI8le(inputBuffer, outBuffer);
                } else if (sampleSize == 16) {
                    BinUtils.cnvF32leToI16le(inputBuffer, outBuffer);
                } else {
                    BinUtils.cnvF32leToI24le(inputBuffer, outBuffer);
                }

                tempBuffer.put(outBuffer);

                ++reads;
            }

            tempBuffer.position(0);
            tempBuffer.get(frameBuffer);
            dataLine.write(frameBuffer, 0, frameSize);
            tempBuffer.rewind();

            ++playedFrames;
        }
    }
}