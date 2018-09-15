/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package com.jme3.phonon.utils;

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
 
    private AudioDataDecoder decoder;

    public PhononOutputLineIntInputStream(PhononOutputLine line,int sampleSize) {
        this.line = line;
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