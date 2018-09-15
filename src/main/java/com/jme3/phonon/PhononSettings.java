package com.jme3.phonon;


/**
 * PhononEffects
 */
public class PhononSettings{
 

    public int sampleRate=44100;
    public int nOutputLines=1;
    public int nSourcesPerLine=255;
    public int nOutputChannels=2;
    public int frameSize=1024;
    public int bufferSize=3; 
    public int maxPreBuffering=1024*2*4; 
    public ThreadMode threadMode=ThreadMode.JAVA;

    public int outputSampleSize=-1; // -1=best

    public PhononSoundSystem system;
    public PhononSoundDevice device;



       
    public PhononSettings(PhononSoundSystem ss){
        system=ss;
    }

    /**
     * Debug only: Disable everything
     * 
     */
    public boolean passThrough = false;
    public boolean initPlayers=true;
    
    @Override
    public String toString(){
        return "SampleRate "+sampleRate+" OutputLines "+nOutputLines+" SourcesPerLine "+nSourcesPerLine+
        " nOutputChannels "+nOutputChannels+" frameSize "+frameSize+" bufferSize "+bufferSize+" maxPreBuffering "
                +maxPreBuffering+" threadMode "+threadMode+ " outputSampleSize "+outputSampleSize+" system "+system+" device "+device;

    }
}