package com.jme3.phonon.utils;

import javax.sound.sampled.SourceDataLine;

/**
 * FrameCache
 */

public class FrameCache {
    private final byte cache[];
    private int lastLoadedFrameIndex;
    private final int frameSize,nFrames;
    private int readIndex = 0;
    private int lastReadedFrameIndex = 0;

    /**
     * @param nFrames num of frames to cache at the same time
     * @param frameSize Frame size in byte
     */
    public FrameCache(int nFrames, int frameSize) {
        this.frameSize = frameSize;
        this.nFrames = nFrames;
        cache = new byte[nFrames * frameSize];
        lastLoadedFrameIndex = 0;
    }
    
    /**
     * Load one frame in cache, when it reach the end of the cache, restart from the beginning
     * @return true if cache is full
     */
    public boolean loadFrame(byte frame[]) {
        System.arraycopy(frame, 0, cache, lastLoadedFrameIndex * frameSize, frame.length);
        if (++lastLoadedFrameIndex >= nFrames) {
            lastLoadedFrameIndex = 0;
            return true;
        }
        return false;
    }
    
    /**
     * Read the next chunk of bytes of length up to frameSize. 
     */
    public boolean readNextFrame(SourceDataLine out, int length) {
        boolean needNewFrame = false;
        
        int readable = length;
        int remaining = cache.length - readIndex;
        // if we need more bytes than the ones availables until the end of the cache
        // we split the read in two
        if (readable > remaining) {
            readable = remaining;
        }
        // Read first batch 
        out.write(cache, readIndex, readable);
        // System.arraycopy(cache, readIndex, out, 0, readable);
        if (readable != length) {
            // bytes that we still need to read
            int leftToRead = length - readable;

            // Read the second batch from the beginning of the cache
            // System.arraycopy(cache, 0, out, readable,leftToRead);
            out.write(cache, 0, leftToRead);

            // readIndex restarts from 0 
            readIndex = leftToRead;

        } else {
            readIndex += length;
        }
        // get the frame index to which we are at.
        int readedFrameIndex = (int) ((readIndex-1) / frameSize);

        // if the frame we read is different than the one we read before...
        needNewFrame = (readedFrameIndex != lastReadedFrameIndex);
        lastReadedFrameIndex = readedFrameIndex;
        
        return needNewFrame;
    }
}