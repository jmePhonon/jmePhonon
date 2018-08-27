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

    /**
     * @param audioData Float encoded audio data
     * @param sampleSize Either 8 16 or 24
     * @param bufferedFrames How many frames are buffered before sending the data to the audio device.
     */
    public PhononPlayer(F32leAudioData audioData, int sampleSize, int bufferedFrames)
            throws LineUnavailableException {
        this.audioBuffer = audioData.getData();
        float sampleRate = (float) audioData.getSampleRate();

        audioFormat = new AudioFormat(sampleRate, sampleSize, audioData.getChannels(), true, false);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open(audioFormat, bufferedFrames * audioFormat.getFrameSize());

    }

    public void play(boolean loop) {
        
        ByteBuffer tempBuffer = ByteBuffer.allocate(dataLine.getBufferSize());

        byte[] inputBuffer = new byte[4];
        byte[] outBuffer = new byte[audioFormat.getSampleSizeInBits() / 8];

        
        while (loop) { // Rewind and loop
            audioBuffer.rewind();

            while (audioBuffer.hasRemaining()) { // Keep going until there is no more data available

                // Fill a temp buffer to reduce the writes to the audio device
                // Ensure that you stop if the audioBuffer limit is reached while filling the temp buffer
                while (tempBuffer.hasRemaining() && audioBuffer.hasRemaining()) {
                    // Read a little endian float
                    BitUtils.nextF32le(audioBuffer, inputBuffer);

                    // Convert to the proper output format for this source line
                    if (audioFormat.getSampleSizeInBits() == 8) {
                        BitUtils.cnvF32leToI8le(inputBuffer, outBuffer);
                    } else if (audioFormat.getSampleSizeInBits() == 16) {
                        BitUtils.cnvF32leToI16le(inputBuffer, outBuffer);
                    } else {
                        BitUtils.cnvF32leToI24le(inputBuffer, outBuffer);
                    }

                    // Store in temp buffer
                    tempBuffer.put(outBuffer);
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
                tempBuffer.rewind();

                // Start the dataLine if it is not playing yet. 
                // We do this here to be sure there is some data already available to be played
                if (!dataLine.isRunning())
                    dataLine.start();

            }
        }
    }
}