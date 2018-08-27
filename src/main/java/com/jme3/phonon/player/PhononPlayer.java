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
        
        ByteBuffer tempBuffer = ByteBuffer.allocate(dataLine.getBufferSize());

        byte[] inputBuffer = new byte[4 * channels];
        byte[] outputBuffer = new byte[(audioFormat.getSampleSizeInBits() / 8) * channels];

       
        dataLine.start();
        while (loop) { // Rewind and loop
            audioBuffer.rewind();

            while (audioBuffer.hasRemaining()) { // Keep going until there is no more data available
                // Fill a temp buffer to reduce the writes to the audio device
                // Ensure that you stop if the audioBuffer limit is reached while filling the temp buffer
                /*while (tempBuffer.hasRemaining() && audioBuffer.hasRemaining()) {
                    // Read a little endian float
                    BitUtils.nextF32le(audioBuffer, inputBuffer, channels);

                    convertFloats(inputBuffer, outputBuffer, channels);
                    tempBuffer.put(outputBuffer);
                }

                // Temp buffer is ready to be written, reset the position to 0
                tempBuffer.rewind();

                // The source line may not have enough bytes left to store the entire tempBuffer, this should be avoided.
                int writable= dataLine.available();
                if (tempBuffer.limit() > writable) {
                    System.err.println("FIX ME: " + tempBuffer.limit()
                            + " bytes ready to be written but the source buffer has only " + writable
                            + " left. This will cause the thread to stop and wait until more bytes are available");
                }
                // Extract a plain byte array from the temp buffer
                byte[] tempBufferB = tempBuffer.array(); 
                dataLine.write(tempBufferB, 0, tempBufferB.length);

                // The temp buffer is fully writen, so we reset his position to 0
                tempBuffer.rewind();*/

                // Read a little endian float
                BitUtils.nextF32le(audioBuffer, inputBuffer, channels);

                convertFloats(inputBuffer, outputBuffer, channels);
                dataLine.write(outputBuffer, 0, outputBuffer.length);

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
    
    private void convertFloats(byte[] inputBuffer, byte[] outputBuffer, int n) {
        byte[] partInputBuffer = new byte[4];
        byte[] partOutputBuffer = new byte[audioFormat.getSampleSizeInBits() / 8];

        for(int i = 0; i < n; ++i) {
            for(int j = 0; j < partInputBuffer.length; ++j)
                partInputBuffer[j] = inputBuffer[i * partInputBuffer.length + j]; 

            if (audioFormat.getSampleSizeInBits() == 8) {
                BitUtils.cnvF32leToI8le(partInputBuffer, partOutputBuffer);
            } else if (audioFormat.getSampleSizeInBits() == 16) {
                BitUtils.cnvF32leToI16le(partInputBuffer, partOutputBuffer);
            } else {
                BitUtils.cnvF32leToI24le(partInputBuffer, partOutputBuffer);
            }

            for(int j = 0; j < partOutputBuffer.length; ++j)
                outputBuffer[i * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}