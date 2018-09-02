package com.jme3.phonon.player;

import java.util.Arrays;

import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.PhononChannel;
import com.jme3.phonon.PhononChannel.ChannelStatus;
import com.jme3.phonon.utils.BitUtils;
import com.jme3.phonon.utils.FrameCache;

public class PhononPlayerBuffer {
    public final int CACHE_SIZE=2;

    public final int sampleSize;
    public final byte[] floatFrame, intFrame;
    public final FrameCache frameCache;

    public final PhononChannel phononChannel;
    byte needFrames;

    // private int remainingBytes, bufferLoadIndex, bufferWriteIndex;

    /**
     * An auxiliary class containing a buffer for PhononPlayer.
     * 
     * @param sampleSize Samples size
     * @param channel Phonon channel to stream on
     * 
     * @author aegroto
     */

    public PhononPlayerBuffer(int sampleSize, PhononChannel channel) {
        needFrames =(byte) CACHE_SIZE;
        this.phononChannel = channel;
        this.sampleSize = sampleSize;
        int bytesPerSample = sampleSize / 8;
        
        this.floatFrame = new byte[channel.getFrameSize() * 4];
        this.intFrame = new byte[channel.getFrameSize() * bytesPerSample];


        this.frameCache = new FrameCache(CACHE_SIZE, intFrame.length);
    }

    /**
     * Fills the buffer when it is empty after creation. SHOULD NOT BE CALLED.
     * 
     * @author aegroto
     */

    // private boolean bufferFilled = false;

 

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
                // System.out.println("[Buffer] Read frame: " + Arrays.toString(floatFrame));
                convertFloats(floatFrame, intFrame, 0);
            // System.out.println("[Buffer] Converted frame: " + Arrays.toString(intFrame));
                frameCache.loadFrame(intFrame);
                needFrames--;

              
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

    // private boolean dataInBuffer = false;

    public int write(SourceDataLine outLine) {
        if (needFrames > 0) {
            if (loadNextFrame() == ChannelStatus.OVER)
                return -1;
        }
        
        if (needFrames == 0) {
            int writableBytes = outLine.available();
            if (writableBytes > 0) {
                if (frameCache.readNextFrame(outLine, writableBytes)) {
                    // System.out.println("[Buffer] Frame cache says we need more frames");
                    needFrames++;
                }
            }
            return writableBytes;
        }

        return 0;

       
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
     * Auxiliary method to decode multiple floats to ints.
     * 
     * @param inputBuffer Input buffer
     * @param outputBuffer Output buffer
     * @param offset Output buffer's write offset
     * 
     * @author aegroto
     */

    public void convertFloats(byte[] inb, byte[] outb, int offset) {
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