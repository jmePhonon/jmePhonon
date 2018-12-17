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
import com.jme3.phonon.thread.PhononExecutor;
import com.jme3.phonon.thread.PhononJavaExecutor;
/**
 * PhononEffects
 */
public class PhononSettings {

    /////// Inherited from Phonon's source ///////////

    public enum PhononSceneType {
        IPL_SCENETYPE_PHONON,
        IPL_SCENETYPE_EMBREE,
        IPL_SCENETYPE_RADEONRAYS,
        IPL_SCENETYPE_CUSTOM
    };

    public enum PhononDirectOcclusionMode {
        IPL_DIRECTOCCLUSION_NONE,
        IPL_DIRECTOCCLUSION_NOTRANSMISSION,
        IPL_DIRECTOCCLUSION_TRANSMISSIONBYVOLUME,
        IPL_DIRECTOCCLUSION_TRANSMISSIONBYFREQUENCY
    };

    public enum PhononDirectOcclusionMethod {
        IPL_DIRECTOCCLUSION_RAYCAST,
        IPL_DIRECTOCCLUSION_VOLUMETRIC
    } ;

    //////////////////////////////////////////////////

    public int sampleRate=44100;
    public int nOutputLines=1;
    public int nSourcesPerLine=255;
    public int nOutputChannels=2;
    public int frameSize=1024;
    public int bufferSize=2; 
    public int maxPreBuffering=1024*2*2; 

    public int outputSampleSize=-1; // -1=best

    public PhononSoundSystem system;
    public PhononSoundDevice device;

    public MaterialGenerator materialGenerator=new SingleMaterialGenerator();
    public PhononExecutor executor = new PhononJavaExecutor();

    public int sceneType = PhononSceneType.IPL_SCENETYPE_PHONON.ordinal(); 
    public int numRays = 1024;// typical values are in the range of 1024 to 131072
    public int numDiffuseSamples = 32;//typical values are in the range of 32 to 4096. 
    public int numBounces = 1;//typical values are in the range of 1 to 32. 
    public int numThreads = 4;//The performance improves linearly with the number of threads upto the number of physical cores available on the CPU.
    public float irDuration = 0.5f; // 0.5 to 4.0.
    public int ambisonicsOrder = 1;//Supported values are between 0 and 3.
    public int maxConvolutionSources = 100; // TODO
    public int bakingBatchSize=1;//IPL_SCENETYPE_RADEONRAYS

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
                +maxPreBuffering+" executor "+executor.getClass()+ " outputSampleSize "+outputSampleSize+" system "+system+" device "+device;

    }
}