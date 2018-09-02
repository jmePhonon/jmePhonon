package com.jme3.phonon.player;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import com.jme3.phonon.PhononChannel;
import com.jme3.phonon.PhononChannel.ChannelStatus;
import com.jme3.phonon.utils.BitUtils;

/**
 * PhononChanneInputStream
 */
public class PhononChannelIntInputStream extends InputStream {
    ChannelStatus lastStat;
    PhononChannel chan;
    byte floatBuffer[];
    byte tmpBuffer[];
    int tmpBufferI = 0;
    int sampleSize;
    public PhononChannelIntInputStream(PhononChannel chan,int sampleSize) {
        this.chan = chan;
        this.sampleSize = sampleSize;
        floatBuffer = new byte[chan.getFrameSize() * 4];
        tmpBuffer = new byte[chan.getFrameSize() * (sampleSize/8)];

    }

    @Override
    public int read() throws IOException {
        if (tmpBufferI == tmpBuffer.length) {
            if (lastStat == ChannelStatus.OVER)
                // return -1;
                throw new EOFException(lastStat.toString());
            lastStat = chan.readNextFrameForPlayer(floatBuffer);
            if (lastStat == ChannelStatus.NODATA) {
                // throw new EOFException(lastStat.toString());
                return -1;
            }
            convertFloats(floatBuffer, tmpBuffer,0);

            tmpBufferI = 0;
        }
        int b = tmpBuffer[tmpBufferI++];
        
        b = b & 0xff; // to unsigned byte
        return b;
     }

    

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
     * @param inputBuffer  Input buffer
     * @param outputBuffer Output buffer
     * @param offset       Output buffer's write offset
     * 
     * @author aegroto
     */

    public void convertFloats(byte[] inb, byte[] outb, int offset) {
        byte[] partInputBuffer = new byte[4];
        byte[] partOutputBuffer = new byte[sampleSize / 8];

        for (int i = 0; i < inb.length; i += 4) {
            partInputBuffer[0] = inb[i];
            partInputBuffer[1] = inb[i + 1];
            partInputBuffer[2] = inb[i + 2];
            partInputBuffer[3] = inb[i + 3];

            convertFloat(partInputBuffer, partOutputBuffer);

            for (int j = 0; j < partOutputBuffer.length; ++j)
                outb[offset + (i / 4) * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}