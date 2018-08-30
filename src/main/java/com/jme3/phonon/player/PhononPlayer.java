package com.jme3.phonon.player;

import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.utils.BitUtils;
import com.jme3.phonon.F32leAudioData;
import com.jme3.phonon.PhononChannel;
import com.jme3.phonon.PhononChannel.ChannelStatus;

import org.lwjgl.BufferUtils;

public class PhononPlayer {
    // public final static int FRAME_SIZE = 8, CACHE_SIZE = 128;

    public final PhononChannel phononChannel;
    // public final long totalFrames;

    private final SourceDataLine dataLine;
    private final AudioFormat audioFormat;

    public final int channels;

    private boolean inPlayback = false;
    private int samplesBytes, remainingBytes;
    private byte[] floatFrame, intBuffer;

    /**
     * @param audioData Float encoded audio data
     * @param sampleSize Either 8 16 or 24
     * @param bufferedFrames How many frames are buffered before sending the data to the audio device.
     */


    public PhononPlayer(PhononChannel chan,int channels, int sampleSize)
            throws LineUnavailableException {
        phononChannel = chan;
        this.channels = channels;
        float sampleRate = 44100;

        audioFormat = new AudioFormat(sampleRate, sampleSize,  this.channels, true, false);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open(audioFormat, chan.getBufferSize() * audioFormat.getFrameSize());

    }

    public void startPlayback() {
        samplesBytes = (audioFormat.getSampleSizeInBits() / 8);
        floatFrame = new byte[phononChannel.getFrameSize() * 4];
        intBuffer = new byte[phononChannel.getFrameSize() * samplesBytes];

        inPlayback = true;

        remainingBytes = 0;         
    }

    public void continuePlayback() {
        if(remainingBytes > 0) {
            int available = dataLine.available();
            int writable = remainingBytes > available ? available : remainingBytes;

            if (writable >  available) {
                System.err.println("FIX ME: " + writable 
                        + " bytes ready to be written but the source buffer has only " +available
                        + " left. This will cause the thread to stop and wait until more bytes are available");
            }

            dataLine.write(intBuffer, intBuffer.length - remainingBytes, writable);
            remainingBytes -= writable;

            // Start the dataLine if it is not playing yet. 
            // We do this here to be sure there is some data already available to be played
            if (!dataLine.isRunning())
                dataLine.start();
        }

        if(remainingBytes == 0) {        
            ChannelStatus stat = phononChannel.readNextFrameForPlayer(floatFrame);
            switch (stat) {
                case NODATA:
                    System.err.println("No data to read. Phonon is lagging behind");
                    break;
                case OVER:
                    inPlayback = false;
                    System.out.println("Audio data is over");
                    break;
                case READY:
                    // System.out.println("Playing");
                    // Convert to proper encoding
                    convertFloats(floatFrame, intBuffer);
                    remainingBytes = intBuffer.length;
            }
        }    
    }

    public boolean isInPlayback() {
        return inPlayback;
    }

    private void convertFloat(byte[] inputBuffer, byte[] outputBuffer) {
        // Convert to the proper output format for this source line
        if (audioFormat.getSampleSizeInBits() == 8) {
            BitUtils.cnvF32leToI8le(inputBuffer, outputBuffer);
        } else if (audioFormat.getSampleSizeInBits() == 16) {
            BitUtils.cnvF32leToI16le(inputBuffer, outputBuffer);
        } else {
            BitUtils.cnvF32leToI24le(inputBuffer, outputBuffer);
        }
    }
    
    private void convertFloats(byte[] inb, byte[] outb) {
        byte[] partInputBuffer = new byte[4];
        byte[] partOutputBuffer = new byte[audioFormat.getSampleSizeInBits() / 8];

        for (int i = 0; i < inb.length; i+=4) {
            partInputBuffer[0] = inb[i];
            partInputBuffer[1] = inb[i+1];
            partInputBuffer[2] = inb[i + 2];
            partInputBuffer[3] = inb[i+3];
           
            convertFloat(partInputBuffer, partOutputBuffer);

            for(int j = 0; j < partOutputBuffer.length; ++j)
                outb[(i/4) * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}