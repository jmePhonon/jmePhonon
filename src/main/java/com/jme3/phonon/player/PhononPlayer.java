package com.jme3.phonon.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.PhononChannel;

public class PhononPlayer {
    // public final static int FRAMES_IN_BUFFER = 2; 

    public final PhononChannel phononChannel;

    private final SourceDataLine dataLine;
    private final AudioFormat audioFormat;

    public final int channels;

    private boolean inPlayback = false;
    
    private final PhononPlayerBuffer buffer;

    /**
     * Player object for jME's Phonon interface.
     * 
     * @param chan Phonon channel to stream on
     * @param channels Number of audio channels
     * @param sampleSize Either 8 16 or 24
     * 
     * @author aegroto, riccardobl
     */

    public PhononPlayer(PhononChannel chan,int channels, int sampleSize)
            throws LineUnavailableException {
        phononChannel = chan;
        this.channels = channels;
        float sampleRate = 44100;
        
        int bytesPerSample=(sampleSize/8);
        audioFormat = new AudioFormat(sampleRate, sampleSize,  this.channels, true, false);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open(audioFormat, chan.getFrameSize() *bytesPerSample);

        buffer = new PhononPlayerBuffer(audioFormat.getSampleSizeInBits(), phononChannel);
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
        if(!isInPlayback())
            return;
        
        int writtenBytes = buffer.write(dataLine);

        if(!dataLine.isRunning()&&writtenBytes > 0) {
            // Start the dataLine if it is not playing yet. 
            // We do this here to be sure there is some data already available to be played
            // if ()
            dataLine.start();
        } else if (writtenBytes == -1) {
            inPlayback = false;
        }
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