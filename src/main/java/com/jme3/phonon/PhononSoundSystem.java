package com.jme3.phonon;

import java.util.Collection;
import java.util.List;

import com.jme3.phonon.PhononSoundDevice;


public interface PhononSoundSystem {

    
    public List<Integer> getOutputFormats(PhononSoundDevice device, int nchannels);
 
    public List<PhononSoundDevice> getAudioDevices();

    public PhononSoundPlayer newPlayer();
}