package com.jme3.phonon.player;

import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.BitUtils;
import com.jme3.phonon.PhononOutputChannel;
import com.jme3.phonon.PhononOutputChannel.ChannelStatus;

class PhononPlayerBuffer {
    public final int sampleSize, bufferFrameSize;
    public final byte[] floatFrame, intBuffer;

    public final PhononOutputChannel phononChannel;

    private int remainingBytes, bufferIndex;

    /**
     * An auxiliary class containing a buffer for PhononPlayer.
     * 
     * @param sizeInFrames Size of buffer in frames
     * @param sampleSize Samples size
     * @param channel Phonon channel to stream on
     * 
     * @author aegroto
     */

    PhononPlayerBuffer(int sampleSize, PhononOutputChannel channel) {
        this.phononChannel = channel;
        this.sampleSize = sampleSize;

        this.bufferFrameSize = channel.getFrameSize() * sampleSize / 8;
        this.floatFrame = new byte[channel.getFrameSize() * 4];
        this.intBuffer = new byte[bufferFrameSize * 2];
    }

    /**
     * Fills the buffer when it is empty after creation. SHOULD NOT BE CALLED.
     * 
     * @author aegroto
     */

    public void fillBuffer() {
        loadNextFrame();
        loadNextFrame();

        remainingBytes = bufferFrameSize;
    }
    /**
     * @return Remaining bytes in the current loaded frame
     * 
     * @author aegroto
     */

    public int getRemainingFrameBytes() {
        return remainingBytes;
    }

    /**
     * Loads next frame in the buffer.
     * 
     * @return true if playback should be able to continue, false otherwise.
     * 
     * @author aegroto
     */
    
    public boolean loadNextFrame() {
        ChannelStatus stat = phononChannel.readNextFrameForPlayer(floatFrame);
        switch (stat) {
            case NODATA:
                System.err.println("No data to read. Phonon is lagging behind");
                break;
            case OVER:
                System.out.println("Audio data is over");
                return false; 
            case READY:
                // Convert to proper encoding
                int loadBufferIndex = 1 - bufferIndex;
                convertFloats(floatFrame, intBuffer, loadBufferIndex * bufferFrameSize);
        }

        return true;
    }

    /**
     * Called by PhononPlayer to write frame data on a SourceDataLine.
     * 
     * @param dataLine SourceDataLine to be written
     * @param bytesToWrites Number of bytes to write on the line
     * 
     * @author aegroto
     */

    public boolean writeToLine(SourceDataLine dataLine) {
        if(remainingBytes > 0) {
            int available = dataLine.available();
            int writable = remainingBytes > available ? available : remainingBytes;

            dataLine.write(intBuffer, (bufferIndex * bufferFrameSize) + bufferFrameSize - remainingBytes, writable);
            remainingBytes -= writable;
        }

        if(remainingBytes == 0) {
            bufferIndex = 1 - bufferIndex;
            remainingBytes = bufferFrameSize;

            return loadNextFrame();
        }

        return true;
    }
   
    /**
     * Auxiliary method to decode a float to an int.
     * 
     * @param inputBuffer Input buffer
     * @param outputBuffer Output buffer
     * 
     * @author aegroto
     */

    private void convertFloat(byte[] inputBuffer, byte[] outputBuffer) {
        // Convert to the proper output format for this source line
        if (sampleSize == 8) {
            BitUtils.cnvF32leToI8le(inputBuffer, outputBuffer);
        } else if (sampleSize == 16) {
            BitUtils.cnvF32leToI16le(inputBuffer, outputBuffer);
        } else {
            BitUtils.cnvF32leToI24le(inputBuffer, outputBuffer);
        }
    }

    /**
     * Auxiliary method to decode multiple floats to an ints.
     * 
     * @param inputBuffer Input buffer
     * @param outputBuffer Output buffer
     * @param offset Output buffer's write offset
     * 
     * @author aegroto
     */

    private void convertFloats(byte[] inb, byte[] outb, int offset) {
        byte[] partInputBuffer = new byte[4];
        byte[] partOutputBuffer = new byte[sampleSize / 8];

        for (int i = 0; i < inb.length; i+=4) {
            partInputBuffer[0] = inb[i];
            partInputBuffer[1] = inb[i+1];
            partInputBuffer[2] = inb[i + 2];
            partInputBuffer[3] = inb[i+3];
           
            convertFloat(partInputBuffer, partOutputBuffer);

            for(int j = 0; j < partOutputBuffer.length; ++j)
                outb[offset + (i/4) * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}