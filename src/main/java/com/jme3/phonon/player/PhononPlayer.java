package com.jme3.phonon.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.PhononOutputChannel;

public class PhononPlayer {
    // public final static int FRAMES_IN_BUFFER = 2; 

    public final PhononOutputChannel phononChannel;

    private final SourceDataLine dataLine;
    private final AudioFormat audioFormat;

    public final int channels;

    private boolean inPlayback = false;
    
    private final PhononPlayerBuffer buffer;
    private final PhononPlayerWriter writer;

    /**
     * Player object for jME's Phonon interface.
     * 
     * @param chan Phonon channel to stream on
     * @param channels Number of audio channels
     * @param sampleSize Either 8 16 or 24
     * 
     * @author aegroto, riccardobl
     */

    public PhononPlayer(PhononOutputChannel chan,int channels, int sampleSize)
            throws LineUnavailableException {
        phononChannel = chan;
        this.channels = channels;
        float sampleRate = 44100;

        audioFormat = new AudioFormat(sampleRate, sampleSize,  this.channels, true, false);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open(audioFormat, chan.getBufferSize() * audioFormat.getFrameSize());

        buffer = new PhononPlayerBuffer(audioFormat.getSampleSizeInBits(), phononChannel);
        writer = new PhononPlayerWriter(dataLine, phononChannel.getFrameSize() * (audioFormat.getSampleSizeInBits() / 8));
    }

    /**
     * Starts playback.
     * 
     * @author aegroto
     */

    public void startPlayback() {
        inPlayback = true;
    }

    /**
     * Proceed with next playback step.
     * 
     * @author aegroto, riccardobl
     */

    public void continuePlayback() {
        int writableBytes = writer.getWritableBytes();
        int writtenBytes = buffer.write(writer.getCache(), writableBytes); 

        if(writtenBytes > 0)
            writer.writeToLine(writableBytes);
    }
    
    /**
     * @return true if player is in playback, false otherwise.
     * 
     * @author aegroto
     */
    public boolean isInPlayback() {
        return inPlayback;
    }
}