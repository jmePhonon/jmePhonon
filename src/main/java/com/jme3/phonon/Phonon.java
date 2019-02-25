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

import com.jme3.app.Application;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMethod;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMode;
import com.jme3.phonon.manager.AudioManager;
import com.jme3.phonon.scene.PhononMesh;
import com.jme3.phonon.scene.PhononMeshBuilder;
import com.jme3.phonon.scene.SpatialFilter;
import com.jme3.scene.Node;

/**
 * Phonon main class. Contains helpers methods to initialize and configure Phonon.
 * 
 */
public final class Phonon{
    static{
        PhononNativeLoader.loadAll();
    }
    public static enum PhononAudioParam {
        DipoleWeight,
        DipolePower,
        // ApplyDistanceAttenuation("phonon.apply_distance_attenuation"),
        ApplyAirAbsorption,
        DirectOcclusionMode,
        DirectOcclusionMethod,
        SourceRadius,
        Status;

        
    }
    
    /**
     * Initializes phonon context for the given application
     * 
     * @param settings Phonon initialization settings
     * @param app Application in which Phonon must be inizialized
     * @return Initialized PhononRenderer
     * @throws Exception Generic initialization exception, check messages for more informations.
     */
    public static PhononRenderer init(PhononSettings settings,Application app) throws Exception {
        PhononRenderer phononRenderer=PhononInitializer.init(settings,app,false,false);
        phononRenderer.initialize();
        return phononRenderer;
    }
  
    public static void setManager(PhononSettings settings, Application app, AudioManager mng) {

        PhononRenderer renderer=(PhononRenderer)app.getAudioRenderer();
        renderer.setAudioManager(mng);

        AudioManager oldmng=app.getStateManager().getState(mng.getClass());
        if(oldmng!=null)app.getStateManager().detach(oldmng);
        app.getStateManager().attach(mng);
    }


    public static void loadScene(PhononSettings settings,Application app,Node root, SpatialFilter filter) {
        PhononMesh scene=PhononMeshBuilder.build(root,filter,settings.materialGenerator);
        PhononRenderer renderer=(PhononRenderer)app.getAudioRenderer();
        renderer.setMesh(scene);
    }
    
    public static void unloadScene(PhononSettings settings,Application app) {
        PhononRenderer renderer=(PhononRenderer)app.getAudioRenderer();
        renderer.setMesh(null);
    }

    
}