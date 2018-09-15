package com.jme3.phonon.desktop_javasound;

import com.jme3.phonon.PhononSettings;


public class JavaSoundPhononSettings extends PhononSettings{

    public JavaSoundPhononSettings(){
        super(new JavaSoundSystem());
    }
   
}