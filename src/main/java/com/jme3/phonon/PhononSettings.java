/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package com.jme3.phonon;

import com.jme3.phonon.scene.material.MaterialGenerator;
import com.jme3.phonon.scene.material.SingleMaterialGenerator;
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
    public ThreadMode threadMode=ThreadMode.NATIVE;

    public int outputSampleSize=-1; // -1=best

    public PhononSoundSystem system;
    public PhononSoundDevice device;

    public MaterialGenerator materialGenerator=new SingleMaterialGenerator();

       
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