package com.jme3.phonon.player;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.BitUtils;
import com.jme3.phonon.F32leAudioData;

import org.lwjgl.BufferUtils;

public class PhononPlayer {
    // public final static int FRAME_SIZE = 8, CACHE_SIZE = 128;

    public final ByteBuffer audioBuffer;
    // public final long totalFrames;

    private final SourceDataLine dataLine;
    private final AudioFormat audioFormat;

    public final int channels;

    /**
     * @param audioData Float encoded audio data
     * @param sampleSize Either 8 16 or 24
     * @param bufferedFrames How many frames are buffered before sending the data to the audio device.
     */
    public PhononPlayer(F32leAudioData audioData, int sampleSize, int bufferedFrames)
            throws LineUnavailableException {
        this.audioBuffer = audioData.getData();
        this.channels = audioData.getChannels();
        float sampleRate = (float) audioData.getSampleRate();

        audioFormat = new AudioFormat(sampleRate, sampleSize, audioData.getChannels(), true, false);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open(audioFormat, bufferedFrames * audioFormat.getFrameSize());

    }

    public void play(boolean loop) {
        
        int samplesBytes=(audioFormat.getSampleSizeInBits() / 8);
        byte floatBuffer[]=new byte[dataLine.getBufferSize()*4*channels];
        byte intBuffer[] = new byte[dataLine.getBufferSize()*samplesBytes * channels];

       
        while (loop) { // Rewind and loop
            audioBuffer.rewind();

            while (audioBuffer.hasRemaining()) { // Keep going until there is no more data available

                int writableBytes = dataLine.available();
                int writableSamples = writableBytes / samplesBytes;
                int remainingBytes = audioBuffer.remaining();
                int remainingSamples = remainingBytes / 4;//audioBuffer is always float

                // How many samples before the sound line buffer is filled or audioBuffer is over.
                int samplesToRead = remainingSamples < writableSamples ? remainingSamples : writableSamples;
                
                BitUtils.nextF32le(audioBuffer,floatBuffer,samplesToRead);

                // Convert to proper encoding
                convertFloats(floatBuffer, intBuffer, channels);
                int available = dataLine.available();
                if (samplesToRead >  available) {
                    System.err.println("FIX ME: " + samplesToRead
                            + " bytes ready to be written but the source buffer has only " +available
                            + " left. This will cause the thread to stop and wait until more bytes are available");
                }
                
                dataLine.write(intBuffer, 0,samplesToRead*samplesBytes);

                // Start the dataLine if it is not playing yet. 
                // We do this here to be sure there is some data already available to be played
                if (!dataLine.isRunning())
                    dataLine.start();

            }
        }
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
    
    private void convertFloats(byte[] inb, byte[] outb, int n) {
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