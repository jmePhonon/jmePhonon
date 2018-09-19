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
import static com.jme3.phonon.memory_layout.OUTPUT_LINE_LAYOUT.*;

/**
 * This class is used to store processed data
 * Note: Unless otherwise specified, methods in here must be call only withing the audio renderer thread.
 */
public class PhononOutputLine{
    private static final int SAMPLE_SIZE = 4;//Always 4 byte (float32) sample
    private final ByteBuffer BUFFER;
    private final int BUFFER_SIZE;    // Buffersize in frames
    private final long BUFFER_ADDRESS;
    private final int CHANNELS;
    private final int FRAMESIZE; // Frame size in samples, total framesize is CHANNELS*FRAMESIZE
    
    

    private Thread rendererThread;

    public static enum LineStatus {
        OVER, NODATA, READY
    }
    


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
    public PhononOutputLine(int frameSize, int channels,int bufferSize) {
        this.BUFFER_SIZE = bufferSize;
        CHANNELS = channels;
        FRAMESIZE = frameSize;
        frameSize*=channels;
        BUFFER = BufferUtils.createByteBuffer(HEADER_size + frameSize * SAMPLE_SIZE * bufferSize).order(ByteOrder.LITTLE_ENDIAN);
        BUFFER.position(HEADER_size);
        reset();
        BUFFER_ADDRESS = DirectBufferUtils.getAddr(BUFFER);
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
     * How many frames for this buffer. 
     * Thread-safe
     */
    public int getBufferSize() {
        return BUFFER_SIZE;
    }
    
    /**
     * Get native address for this buffer
     * Thread-safe
     */
    public long getAddress() {
        return BUFFER_ADDRESS;
    }
    

    /**
     * Reset buffer
     */
    private void reset() {
        setLastProcessedFrameId(0);
        setLastPlayedFrameId(0);
    }

  
    private void setLastProcessedFrameId(int v) {
        assert (rendererThread==null&&(rendererThread=Thread.currentThread())!=null)|| Thread.currentThread()==rendererThread;
        BUFFER.putInt(LAST_PROCESSED_FRAME,v);
    }

    /**
     * Get id of the latest frame processed by phonon
     */
    public int getLastProcessedFrameId() {
        assert (rendererThread==null&&(rendererThread=Thread.currentThread())!=null)|| Thread.currentThread()==rendererThread;
        int n = BUFFER.getInt(LAST_PROCESSED_FRAME);
        if (n < 0)
            n=-n;
        return n;
    }
    
    /**
     * Returns true if the entire audio has been processed, will always return false for loops.
     */
    public boolean isProcessingCompleted() {
        assert (rendererThread==null&&(rendererThread=Thread.currentThread())!=null)|| Thread.currentThread()==rendererThread;
        return BUFFER.getInt(LAST_PROCESSED_FRAME) < 0;
    }


    /**
     * Set id of last played frame, internal use only
     */
    private void setLastPlayedFrameId(int v) {
        assert (rendererThread==null&&(rendererThread=Thread.currentThread())!=null)|| Thread.currentThread()==rendererThread;
        BUFFER.putInt(LAST_PLAYED_FRAME, v);
    }

    /**
     * Get id of latest played frame
     */
    public int getLastPlayedFrameId() {
        assert (rendererThread==null&&(rendererThread=Thread.currentThread())!=null)|| Thread.currentThread()==rendererThread;
        int n = BUFFER.getInt(LAST_PLAYED_FRAME);
        return n;
    }




   
    public LineStatus readNextFrameForPlayer(byte le_out[]) {
        assert (rendererThread==null&&(rendererThread=Thread.currentThread())!=null)|| Thread.currentThread()==rendererThread;

        int rawIndex = getLastPlayedFrameId();
        int phononIndex = getLastProcessedFrameId();
        if(rawIndex>=phononIndex){
            // System.err.println("Error... no data is ready to be read");
            return LineStatus.NODATA;
        }
              
        // If we reached the end of the buffer, restart from the begin
        int readindex = rawIndex % BUFFER_SIZE;  

        int frameSize = getFrameSize()*getChannels();

        // Move buffer cursor to the correct position
        BUFFER.position(BODY + readindex * frameSize * SAMPLE_SIZE);
        // System.out.println("Read from position " + buffer.position()+" buffer length "+buffer.limit()+" frame size "+frameSize);

        // Read next frame at once
        BitUtils.nextF32le(BUFFER, le_out, frameSize);

        // CHeck if buffer is fully readed
        if (rawIndex == phononIndex && isProcessingCompleted()) {
            return LineStatus.OVER;
        }

        // Jump to next frame
        rawIndex++;
        setLastPlayedFrameId(rawIndex);

        return LineStatus.READY;


    }
    
    

}