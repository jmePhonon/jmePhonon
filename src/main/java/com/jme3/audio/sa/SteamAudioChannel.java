package com.jme3.audio.sa;

import java.nio.ByteBuffer;

import com.jme3.util.BufferUtils;


/**
 * SteamAudioChannel
 */
public class SteamAudioChannel {
    public final ByteBuffer outputBuffer;

    public SteamAudioChannel(int outputBufferSize) {
        outputBuffer=BufferUtils.createByteBuffer(outputBufferSize);
    }        
    
}