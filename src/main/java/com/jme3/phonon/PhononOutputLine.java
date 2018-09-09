package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jme3.phonon.utils.BitUtils;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;
import static com.jme3.phonon.memory_layout.OUTPUT_LINE_LAYOUT.*;

/**
 * PhononFrameBuffer
 * 
 * This class stores processed data
 */
public class PhononOutputLine {
    private static final int _SAMPLE_SIZE = 4;//Always 4 byte (float32) sample

    protected final ByteBuffer buffer;
    private final int bufferSize;    // Buffersize in frames
    private final long bufferAddress;

    private final int CHANNELS;
    private final int FRAMESIZE;    // Frame size in samples, total framesize is CHANNELS*FRAMESIZE

    public static enum ChannelStatus {
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
     * @param bufferedFrames How many frames should this queue contain
     * @param samplesPerFrame 
     */
    public PhononOutputLine(int frameSize, int channels,int bufferSize) {
        this.bufferSize = bufferSize;
        CHANNELS = channels;
        FRAMESIZE = frameSize;
        // Allocate direct buffer, the first 8+ 4 + 4 bytes contain the source id and two int indices
        frameSize*=channels;
        buffer = BufferUtils.createByteBuffer(HEADER_size + frameSize * _SAMPLE_SIZE * bufferSize).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(HEADER_size);

        reset();

        bufferAddress = DirectBufferUtils.getAddr(buffer);
    }


    public int getChannels() {
        return CHANNELS;
    }

   
    /**
     * How many samples per frame
     */
    public int getFrameSize() {
        return FRAMESIZE;
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
     * Reset buffer
     */
    public void reset() {
        setLastProcessedFrameId(0);
        setLastPlayedFrameId(0);
    }

  
    protected void setLastProcessedFrameId(int v) {
        buffer.putInt(LAST_PROCESSED_FRAME, v);
    }

    /**
     * Get id of the latest frame processed by phonon
     */
    public int getLastProcessedFrameId() {
        int n = buffer.getInt(LAST_PROCESSED_FRAME);
        if (n < 0)
            n = -n;
        return n;
    }
    
    /**
     * Returns true if the entire audio has been processed, will always return false for loops.
     */
    public boolean isProcessingCompleted() {
        return buffer.getInt(LAST_PROCESSED_FRAME) < 0;
    }


    /**
     * Set id of last played frame, internal use only
     */
    private void setLastPlayedFrameId(int v) {
        buffer.putInt(LAST_PLAYED_FRAME, v);
    }

    /**
     * Get id of latest played frame
     */
    public int getLastPlayedFrameId() {
        int n = buffer.getInt(LAST_PLAYED_FRAME);
        return n;
    }


    public ChannelStatus readNextFrameForPlayer(byte le_out[]) {
        int rawIndex = getLastPlayedFrameId();
        int phononIndex = getLastProcessedFrameId();
        if(rawIndex>=phononIndex){
            // System.err.println("Error... no data is ready to be read");
            return ChannelStatus.NODATA;
        }
              
        // If we reached the end of the buffer, restart from the begin
        int readindex = rawIndex % bufferSize;  

        int frameSize = getFrameSize()*getChannels();

        // Move buffer cursor to the correct position
        buffer.position(BODY + readindex * frameSize * _SAMPLE_SIZE);
        // System.out.println("Read from position " + buffer.position()+" buffer length "+buffer.limit()+" frame size "+frameSize);

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