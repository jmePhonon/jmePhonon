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
package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jme3.phonon.utils.BitUtils;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

/**
 * This class is used to store processed data
 * Note: Unless otherwise specified, methods in here must be call only withing the audio renderer thread.
 */
public class PhononOutputLine{
    private static final int SAMPLE_SIZE = 4;//Always 4 byte (float32) sample
    private final ByteBuffer BUFFER;
    private final long BUFFER_ADDRESS;
    private final int CHANNELS;
    private final int FRAMESIZE; // Frame size in samples, total framesize is CHANNELS*FRAMESIZE
    private Thread rendererThread;

    /**
     * Memory buffer that has two long indices and a sequence of frames.
     * 
     * Endianess is little endian for everything
     * 
     * Phonon processor fills this buffer from left to right with processed frames, the write index is
     * increased every time a new frame is written, when the end of the buffer is reached, the processor
     * restarts from the beginning, when the audio source is over, if loop is not enabled, the write index is
     * flipped to negative.
     * 
     * 
     * @param frameSize Samples per frame
     * @param channels Output Channels (1=mono, 2=stereo)
     * @param bufferSize Frames per buffer 
     */
    public PhononOutputLine(int frameSize, int channels) {
        CHANNELS = channels;
        FRAMESIZE = frameSize;
        frameSize*=channels;
        BUFFER = BufferUtils.createByteBuffer(frameSize * SAMPLE_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        BUFFER_ADDRESS=DirectBufferUtils.getAddr(BUFFER);
        rendererThread=null; // We don't care which thread initialize this.
    }

    /**
     * Thread-safe
     * @return
     */
    public int getChannels() {
        return CHANNELS;
    }
   
    /**
     * How many samples per frame
     * Thread-safe
     */
    public int getFrameSize() {
        return FRAMESIZE;
    }

 
    
    /**
     * Get native address for this buffer
     * Thread-safe
     */
    public long getAddress() {
        return BUFFER_ADDRESS;
    }
    

  
   
    public ByteBuffer getFrame() {
        assert (rendererThread==null&&(rendererThread=Thread.currentThread())!=null)||Thread.currentThread()==rendererThread;
        BUFFER.position(0);
        return BUFFER;
    }
    
    

}