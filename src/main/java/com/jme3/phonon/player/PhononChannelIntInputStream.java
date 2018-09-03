package com.jme3.phonon.player;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.jme3.phonon.PhononChannel;
import com.jme3.phonon.PhononChannel.ChannelStatus;
import com.jme3.phonon.player.converter.PlayerConverter;
import com.jme3.phonon.player.converter.PlayerConverterManager;

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

    private PlayerConverter converter;

    public PhononChannelIntInputStream(PhononChannel chan,int sampleSize) {
        this.chan = chan;
        this.sampleSize = sampleSize;
        floatBuffer = new byte[chan.getFrameSize() * 4];
        tmpBuffer = new byte[chan.getFrameSize() * (sampleSize/8)];

        converter = PlayerConverterManager.getPlayerConverter(sampleSize);
    }

    @Override
    public int read() throws IOException {
        if (tmpBufferI == tmpBuffer.length) {
            if (lastStat == ChannelStatus.OVER)
                throw new EOFException(lastStat.toString());
            lastStat = chan.readNextFrameForPlayer(floatBuffer);
            if (lastStat == ChannelStatus.NODATA) {
                return -1;
            }

            converter.convert(floatBuffer, tmpBuffer);

            tmpBufferI = 0;
        }
        int b = tmpBuffer[tmpBufferI++];
        
        b = b & 0xff; // to unsigned byte
        return b;
    }
}