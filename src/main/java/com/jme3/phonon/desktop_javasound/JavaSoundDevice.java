package com.jme3.phonon.desktop_javasound;

import javax.sound.sampled.Mixer;

import com.jme3.phonon.PhononSoundDevice;

/**
 * JSDevice
 */
class JavaSoundDevice implements PhononSoundDevice{
    private final Object ID;
    private final String NAME;
    private final Mixer MIXER;

    JavaSoundDevice(Object id,String name,Mixer mixer){
        ID=id;
        NAME=name;
        MIXER=mixer;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getID() {
        return ID;
    }

    Mixer getMixer(){
        return MIXER;
    }

    @Override
    public String toString() {
        return NAME;
    }
    


}