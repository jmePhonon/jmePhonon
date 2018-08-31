package com.jme3.phonon.player;

import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.BitUtils;
import com.jme3.phonon.PhononOutputChannel;
import com.jme3.phonon.PhononOutputChannel.ChannelStatus;

class PhononPlayerBuffer {
    public final int sampleSize, bufferFrameSize;
    public final byte[] floatFrame, intBuffer;

    public final PhononOutputChannel phononChannel;

    private int remainingBytes, bufferLoadIndex, bufferWriteIndex;

    /**
     * An auxiliary class containing a buffer for PhononPlayer.
     * 
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

        System.out.println("Initialized buffer of size " + intBuffer.length);

        bufferLoadIndex = 0;
        bufferWriteIndex = 0;
    }

    /**
     * Fills the buffer when it is empty after creation. SHOULD NOT BE CALLED.
     * 
     * @author aegroto
     */

    private int preloadedFrames = 0, writtenFrames = 0;

    public void fillBuffer() {
        while(!isBufferFilled()) {
            if(loadNextFrame() == ChannelStatus.NODATA) {
                break;
            } /*else {
                preloadedFrames++;
            }*/
        }

        if(isBufferFilled()) {
            remainingBytes = bufferFrameSize;
        }
    }

    private boolean isBufferFilled() {
        return preloadedFrames >= 2;
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
    
    public ChannelStatus loadNextFrame() {
        ChannelStatus stat = phononChannel.readNextFrameForPlayer(floatFrame);

        switch (stat) {
            case NODATA:
                System.err.println("No data to read. Phonon is lagging behind");
                break;
            case OVER:
                System.out.println("Audio data is over");
                break;
            case READY:
                // Convert to proper encoding
                int offset = bufferLoadIndex * bufferFrameSize;

                System.err.printf("Loading a frame -- %d ... ", bufferLoadIndex);
                convertFloats(floatFrame, intBuffer, offset);
                
                boolean areBytesNull = true;
                for(int i = 0; i < bufferFrameSize && areBytesNull; ++i) {
                    if(intBuffer[offset + i] != 0) {
                        areBytesNull = false;
                    }
                }

                if(areBytesNull) {
                    System.err.printf("[FIXME] Loading an entire null frame (%d)\n", preloadedFrames);
                }

                System.err.printf("Loaded %d\n", bufferLoadIndex);
                preloadedFrames++;
                bufferLoadIndex = 1 - bufferLoadIndex;
        }

        return stat;
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
        if(!isBufferFilled()) {
            fillBuffer();
            return true;
        }

        if(remainingBytes > 0) {
            int availableBytes = dataLine.available();

            if(availableBytes > 0) {
                int maxWritableBytes;

                if(remainingBytes > availableBytes) {
                    maxWritableBytes = availableBytes < bufferFrameSize ? availableBytes : bufferFrameSize;
                } else {
                    maxWritableBytes = remainingBytes;
                }

                int offset = (bufferWriteIndex * bufferFrameSize) + bufferFrameSize - remainingBytes;
                // System.out.printf("Writing frame (%d) -- %d %d %d\n", maxWritableBytes, bufferIndex, bufferIndex * bufferFrameSize, offset);

                boolean areBytesNull = true;
                for(int i = 0; i < maxWritableBytes && areBytesNull; ++i) {
                    if(intBuffer[offset + i] != 0) {
                        areBytesNull = false;
                    }
                }

                /*System.err.printf("Writing frame (%d / %d) -- (%d, %d)\n", maxWritableBytes, bufferFrameSize, writtenFrames, preloadedFrames);

                if(areBytesNull) {
                    System.err.printf("[FIXME] Writing a null frame fragment (%d)\n", writtenFrames);
                }*/

                dataLine.write(intBuffer, offset, maxWritableBytes);
                remainingBytes -= maxWritableBytes;                
            }

            if(remainingBytes == 0) {
                bufferWriteIndex = 1 - bufferWriteIndex;
                remainingBytes = bufferFrameSize;
                writtenFrames++;
                // System.err.printf("Finished to write a frame (%d, %d)\n", writtenFrames, preloadedFrames);

                availableBytes = dataLine.available();
                if(availableBytes > 0) {
                    if(availableBytes > bufferFrameSize) {
                        availableBytes = bufferFrameSize;
                    }

                    int offset = bufferWriteIndex * bufferFrameSize;
                    // System.out.printf("Writing next frame start (%d) -- %d %d\n", availableBytes, bufferIndex, offset);

                    dataLine.write(intBuffer, offset, availableBytes);
                    remainingBytes -= availableBytes;
                }

                return loadNextFrame() != ChannelStatus.OVER;
            }
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