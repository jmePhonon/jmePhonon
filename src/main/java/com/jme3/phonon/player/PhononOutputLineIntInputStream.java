package com.jme3.phonon.player;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.PhononOutputLine.ChannelStatus;
import com.jme3.phonon.format.decoder.AudioDataDecoder;
import com.jme3.phonon.format.decoder.AudioDataDecoderFactory;

/**
 * PhononChanneInputStream
 */

public class PhononOutputLineIntInputStream extends InputStream {
    ChannelStatus lastStat;
    PhononOutputLine line;
    byte floatBuffer[];
    byte tmpBuffer[];
    int tmpBufferI = 0;
    int sampleSize;
 
    private AudioDataDecoder decoder;

    public PhononOutputLineIntInputStream(PhononOutputLine line,int sampleSize) {
        this.line = line;
        this.sampleSize = sampleSize;
        floatBuffer = new byte[line.getFrameSize() *line.getChannels()* 4];
        tmpBuffer = new byte[line.getFrameSize() *line.getChannels()* (sampleSize/8)];

        decoder = AudioDataDecoderFactory.getAudioDataDecoder(sampleSize);
    }

    @Override
    public int read() throws IOException {
        if (tmpBufferI == tmpBuffer.length) {
            if (lastStat == ChannelStatus.OVER)
                throw new EOFException(lastStat.toString());
            lastStat = line.readNextFrameForPlayer(floatBuffer);
            if (lastStat == ChannelStatus.NODATA) {
                return -1;
            }

            decoder.decode(floatBuffer, tmpBuffer);

            tmpBufferI = 0;
        }
        int b = tmpBuffer[tmpBufferI++];
        
        b = b & 0xff; // to unsigned byte
        return b;
    }
}