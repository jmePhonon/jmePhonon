package com.jme3.phonon.player;

import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.BitUtils;
import com.jme3.phonon.F32leAudioData;
import com.jme3.phonon.PhononOutputChannel;
import com.jme3.phonon.PhononOutputChannel.ChannelStatus;

import org.lwjgl.BufferUtils;

public class PhononPlayer {
    // public final static int FRAME_SIZE = 8, CACHE_SIZE = 128;

    public final PhononOutputChannel phononChannel;
    // public final long totalFrames;

    private final SourceDataLine dataLine;
    private final AudioFormat audioFormat;

    public final int channels;

    /**
     * @param audioData Float encoded audio data
     * @param sampleSize Either 8 16 or 24
     * @param bufferedFrames How many frames are buffered before sending the data to the audio device.
     */


    public PhononPlayer(PhononOutputChannel chan,int channels, int sampleSize)
            throws LineUnavailableException {
        phononChannel = chan;
        this.channels = channels;
        float sampleRate = 44100;

        audioFormat = new AudioFormat(sampleRate, sampleSize,  this.channels, true, false);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open(audioFormat, chan.getBufferSize() * audioFormat.getFrameSize());

    }
    public void play() {
        int samplesBytes = (audioFormat.getSampleSizeInBits() / 8);
        byte floatFrame[]=new byte[phononChannel.getFrameSize()*4];
        byte intBuffer[] = new byte[phononChannel.getFrameSize()* samplesBytes];

        // while (loop) { // Rewind and loop
            
            while (true) { // Keep going until there is no more data available              
                ChannelStatus stat = phononChannel.readNextFrameForPlayer(floatFrame);
                switch (stat) {
                case NODATA:
                    System.err.println("No data to read. Phonon is lagging behind");
                    break;
                case OVER:
                    System.out.println("Audio data is over");
                    break;
                case READY:
                // System.out.println("Playing");
                }
                // Convert to proper encoding
                convertFloats(floatFrame, intBuffer);
                int available = dataLine.available();
                if (floatFrame.length >  available) {
                    System.err.println("FIX ME: " + floatFrame.length
                            + " bytes ready to be written but the source buffer has only " +available
                            + " left. This will cause the thread to stop and wait until more bytes are available");
                }                
                dataLine.write(intBuffer, 0,intBuffer.length);

                // Start the dataLine if it is not playing yet. 
                // We do this here to be sure there is some data already available to be played
                if (!dataLine.isRunning())
                    dataLine.start();

            }
        // }
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
            if (audioFormat.getSampleSizeInBits() == 8) {
                BitUtils.cnvF32leToI8le(partInputBuffer, partOutputBuffer);
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                BitUtils.cnvF32leToI16le(partInputBuffer, partOutputBuffer);
            } else {
                BitUtils.cnvF32leToI24le(partInputBuffer, partOutputBuffer);
            }
            for(int j = 0; j < partOutputBuffer.length; ++j)
                outb[(i/4) * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}