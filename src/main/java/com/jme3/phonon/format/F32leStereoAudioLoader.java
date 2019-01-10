package com.jme3.phonon.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

/**
 * F32leAudioLoader
 */
public class F32leStereoAudioLoader extends F32leAudioLoader{

    public F32leStereoAudioLoader(){
        NCHANNELS=2;
    }

    
}