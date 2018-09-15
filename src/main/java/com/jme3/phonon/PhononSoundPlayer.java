package com.jme3.phonon;

import com.jme3.phonon.PhononOutputLine;

public interface PhononSoundPlayer<SYSTEM extends PhononSoundSystem,DEVICE extends PhononSoundDevice> {
    public void init(
        SYSTEM system,
        DEVICE device,
    PhononOutputLine chan, int sampleRate,int outputChannels,int outputSampleSize,int maxPreBufferingSamples) throws Exception;
    public void close();
    public byte loop();
}
