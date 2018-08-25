package com.jme3.steamaudio.player;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SteamAudioPlayer {
    public final static int FRAME_SIZE = 8, CACHE_SIZE = 128;

    public final ByteBuffer audioBuffer;
    public final long size;

    private SourceDataLine dataLine;

    public SteamAudioPlayer(ByteBuffer audioBuffer, long size,
                            int sampleRate, int sampleSize) {
        this.audioBuffer = audioBuffer;
        this.size = size;

        final AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSize, 2, false, false);

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
        byte[] tempBuffer = new byte[FRAME_SIZE];

        while(playedFrames < size) {
            audioBuffer.get(tempBuffer, 0, FRAME_SIZE);
            dataLine.write(tempBuffer, 0, FRAME_SIZE);
            ++playedFrames;
        }
    }
}