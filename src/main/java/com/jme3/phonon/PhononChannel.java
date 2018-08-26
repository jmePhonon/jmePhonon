package com.jme3.phonon;

import java.nio.ByteBuffer;

import com.jme3.util.BufferUtils;


/**
 * PhononChannel
 */
public class PhononChannel {
    public final ByteBuffer outputBuffer;

    public PhononChannel(int outputBufferSize) {
        outputBuffer=BufferUtils.createByteBuffer(outputBufferSize);
    }        
    
}