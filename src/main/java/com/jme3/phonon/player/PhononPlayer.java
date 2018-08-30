package com.jme3.phonon.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.PhononOutputChannel;

public class PhononPlayer {
    public final static int FRAMES_IN_BUFFER = 16;

    public final PhononOutputChannel phononChannel;

    private final SourceDataLine dataLine;
    private final AudioFormat audioFormat;

    public final int channels;

    private boolean inPlayback = false;
    
    private PhononPlayerBuffer buffer;

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

        buffer = new PhononPlayerBuffer(FRAMES_IN_BUFFER, audioFormat.getSampleSizeInBits(), phononChannel);
    }

    /**
     * Starts playback.
     * 
     * @author aegroto
     */

    public void startPlayback() {
        inPlayback = true; 
        
        buffer.fillBuffer();
    }

    /**
     * Proceed with next playback step.
     * 
     * @author aegroto, riccardobl
     */

    public void continuePlayback() {
        if(buffer.getRemainingFrameBytes() > 0) {
            int available = dataLine.available();
            int writable = buffer.getRemainingFrameBytes() > available ? available : buffer.getRemainingFrameBytes();

            if (writable >  available) {
                System.err.println("FIX ME: " + writable 
                        + " bytes ready to be written but the source buffer has only " +available
                        + " left. This will cause the thread to stop and wait until more bytes are available");
            }

            buffer.writeToLine(dataLine, writable); 

            // Start the dataLine if it is not playing yet. 
            // We do this here to be sure there is some data already available to be played
            if (!dataLine.isRunning())
                dataLine.start();
        }

        if(buffer.getRemainingFrameBytes() == 0) {        
            inPlayback = buffer.loadNextFrame();
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