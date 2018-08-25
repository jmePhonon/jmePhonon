package com.jme3.steamaudio.player;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SteamAudioPlayer {
    // public final static int FRAME_SIZE = 8, CACHE_SIZE = 128;

    public final ByteBuffer audioBuffer;
    public final long size;
    public final int frameSize;

    private SourceDataLine dataLine;

    public SteamAudioPlayer(ByteBuffer audioBuffer, long size, 
            int sampleRate, int sampleSize, int frameSize, int channels, int frameRate) {
        this.audioBuffer = audioBuffer;
        this.size = size;
        this.frameSize = frameSize;

        final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSize, channels, frameSize, frameRate, false);

        try {
            dataLine = AudioSystem.getSourceDataLine(audioFormat);
            dataLine.open(audioFormat, sampleRate);
            dataLine.start();
        } catch(LineUnavailableException ex) {
            dataLine = null;
            ex.printStackTrace();
        }
    }

    public void play() {
        long playedFrames = 0;
        byte[] tempBuffer = new byte[frameSize];

        while(playedFrames < size) {
            audioBuffer.get(tempBuffer, 0, frameSize);
            dataLine.write(tempBuffer, 0, frameSize);
            ++playedFrames;
        }
    }
}