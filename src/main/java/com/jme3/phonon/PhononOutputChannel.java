package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jme3.util.BufferUtils;

/**
 * PhononFrameBuffer
 * 
 * This class stores processed data
 */
public class PhononOutputChannel {
    /**
     * Little endian memory buffer
     */
    private final ByteBuffer buffer;
    // Buffersize in frames
    private final int bufferSize;
    // Frame size in samples
    private final int frameSize;
    // private final int channels;
    private final long bufferAddress;
    
    public static enum ChannelStatus {
        OVER, NODATA, READY
    }
    
    private static final int _SAMPLE_SIZE = 4;//Always 4 byte (float32) sample
    private static final int _HEADER_SIZE = 
            8 /*source address*/
            + 4 /*source length in bytes*/
            + 4 /* last processed frame */
            + 4/*last played frame*/;
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
     * @param bufferedFrames How many frames should this queue contain
     * @param samplesPerFrame 
     */
    public PhononOutputChannel(int frameSize, int bufferSize) {
        this.bufferSize = bufferSize;
        // this.channels = channels;
        // Allocate direct buffer, the first 8+ 4 + 4 bytes contain the source id and two int indices
        this.frameSize = frameSize;
        buffer = BufferUtils.createByteBuffer(_HEADER_SIZE + frameSize*_SAMPLE_SIZE * bufferSize).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(_HEADER_SIZE);

        disconnectSourceBuffer();

        bufferAddress = DirectBufferUtils.getAddr(buffer);
    }


   
    /**
     * How many samples per frame
     */
    public int getFrameSize() {
        return frameSize;
    }
    
    /**
     * How many frames for this buffer. 
     */
    public int getBufferSize() {
        return bufferSize;
    }
    
    /**
     * Get native address for this buffer
     */
    public long getAddress() {
        return bufferAddress;
    }
    

    /**
     * Connect an audio source to this channel
     * @param address Native addr
     * @param length Length in bytes
     */
    public void connectSourceBuffer(long address,int length) {
        setLastProcessedFrameId(0);
        setLastPlayedFrameId(0);
        buffer.putLong(0, address);
        buffer.putInt(8, length);
    }

    /**
     * Disconnect connected audio source
     */
    public void disconnectSourceBuffer() {
        setLastProcessedFrameId(0);
        setLastPlayedFrameId(0);
        buffer.putLong(0, -1);
    }
    
    
    
    public long getConnectedSourceBuffer() {
        return buffer.getLong(0);
    }
   
    public boolean hasConnectedSource() {
        return getConnectedSourceBuffer() != -1;
    }

    private void setLastProcessedFrameId(int v) {
        buffer.putInt(8+4, v);
    }

    /**
     * Get id of the latest frame processed by phonon
     */
    public int getLastProcessedFrameId() {
        int n = buffer.getInt(8 + 4);
        if (n < 0)
            n = -n;
        return n;
    }
    
    /**
     * Returns true if the entire audio has been processed, will always return false for loops.
     */
    public boolean isProcessingCompleted() {
        return buffer.getInt(8+4) < 0;
    }


    /**
     * Set id of last played frame, internal use only
     */
    private void setLastPlayedFrameId(int v) {
        buffer.putInt(8+4+4, v);
    }

    /**
     * Get id of latest played frame
     */
    public int getLastPlayedFrameId() {
        int n = buffer.getInt(8+4+4);
        return n;
    }


    public ChannelStatus readNextFrameForPlayer(byte le_out[]) {
        int rawIndex = getLastPlayedFrameId();
        int phononIndex = getLastProcessedFrameId();
        
        if(rawIndex>phononIndex){
            System.err.println("Error... no data is ready to be read");
            return ChannelStatus.NODATA;
        }
              
        // If we reached the end of the buffer, restart from the begin
        int readindex = rawIndex % bufferSize;  

        int frameSize = getFrameSize();

        // Move buffer cursor to the correct position
        buffer.position(_HEADER_SIZE + readindex * frameSize *_SAMPLE_SIZE);

        // Read next frame at once
        BitUtils.nextF32le(buffer, le_out, frameSize);

        // CHeck if buffer is fully readed
        if (rawIndex == phononIndex && isProcessingCompleted()) {
            return ChannelStatus.OVER;
        }

        // Jump to next frame
        rawIndex++;
        setLastPlayedFrameId(rawIndex);

        return ChannelStatus.READY;


    }
    
    

}