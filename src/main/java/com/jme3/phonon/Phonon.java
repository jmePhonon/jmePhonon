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
import com.jme3.phonon.scene.PhononAudioEmitterControl;
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
    static enum PhononAudioParam {
        DipoleWeight("phonon.dipole_weight"),
        DipolePower("phonon.dipole_power"),
        ApplyDistanceAttenuation("phonon.apply_distance_attenuation"),
        ApplyAirAbsorption("phonon.apply_air_absorption"),
        DirectOcclusionMode("phonon.direct_occlusion_mode"),
        DirectOcclusionMethod("phonon.direct_occlusion_method"),
        SourceRadius("phonon.source_radius");

        String key;

        PhononAudioParam(String key){
            this.key = key;
        }
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
  
    public static void loadScene(PhononSettings settings,Application app,Node root, SpatialFilter filter) {
        PhononMesh scene=PhononMeshBuilder.build(root,filter,settings.materialGenerator);
        PhononRenderer renderer=(PhononRenderer)app.getAudioRenderer();
        renderer.setMesh(scene);
    }
    
    public static void unloadScene(PhononSettings settings,Application app) {
        PhononRenderer renderer=(PhononRenderer)app.getAudioRenderer();
        renderer.setMesh(null);
    }

    /**
     * Set audio node dipole weight.
     * 
     * @param node Audio node
     * @param dipoleWeight Dipole weight value
     */
    public static void setAudioNodeDipoleWeight(AudioNode node, float dipoleWeight) {
        node.setUserData(PhononAudioParam.DipoleWeight.key, dipoleWeight);
        communicateUpdateToRenderer(node, PhononAudioParam.DipoleWeight); 
    }

    /**
     * Set audio node dipole power.
     * 
     * @param node Audio node
     * @param dipolePower Dipole power value
     */
    public static void setAudioNodeDipolePower(AudioNode node, float dipolePower) {
        node.setUserData(PhononAudioParam.DipolePower.key, dipolePower);
        communicateUpdateToRenderer(node, PhononAudioParam.DipolePower); 
    }

    /**
     * Set audio node air absorption property.
     * 
     * @param node Audio node
     * @param state True to apply air absorption, false otherwise
     */
    public static void setAudioNodeApplyAirAbsorption(AudioNode node, boolean state) {
        node.setUserData(PhononAudioParam.ApplyAirAbsorption.key, state);
        communicateUpdateToRenderer(node, PhononAudioParam.ApplyAirAbsorption);
    }

    /**
     * Set audio node direct occlusion mode.
     * 
     * @param node Audio node
     * @param mode Direct occlusion mode
     */
    public static void setAudioNodeDirectOcclusionMode(AudioNode node, PhononDirectOcclusionMode mode) {
        node.setUserData(PhononAudioParam.DirectOcclusionMode.key, mode.ordinal());
        communicateUpdateToRenderer(node, PhononAudioParam.DirectOcclusionMode);
    }

    /**
     * Set audio node direct occlusion method.
     * 
     * @param node Audio node
     * @param method Direct occlusion method
     */
    public static void setAudioNodeDirectOcclusionMethod(AudioNode node, PhononDirectOcclusionMethod method) {
        node.setUserData(PhononAudioParam.DirectOcclusionMethod.key, method.ordinal());
        communicateUpdateToRenderer(node, PhononAudioParam.DirectOcclusionMethod);
    }

    /**
     * Set audio node source radius.
     * 
     * @param node Audio node
     * @param sourceRadius Source radius value
     */
    public static void setAudioNodeSourceRadius(AudioNode node, float sourceRadius) {
        node.setUserData(PhononAudioParam.SourceRadius.key, sourceRadius);
        communicateUpdateToRenderer(node, PhononAudioParam.SourceRadius); 
    }

    /**
     * Return the given audio node's dipole weight.
     * 
     * @param node Audio node
     * @return node's dipole weight (0 if still not set)
     */
    public static float getAudioNodeDipoleWeight(AudioNode node) {
        Object data = node.getUserData(PhononAudioParam.DipoleWeight.key);
        return data == null ? 0f : (float) data;
    }
    
    /**
     * Return the given audio node's dipole power.
     * 
     * @param node Audio node
     * @return node's dipole power (0 if still not set)
     */
    public static float getAudioNodeDipolePower(AudioNode node) {
        Object data = node.getUserData(PhononAudioParam.DipolePower.key);
        return data == null ? 0f : (float) data;
    }

    /**
     * Return the given audio node's air absorption property
     * 
     * @param node Audio node
     * @return True if air absorption is applied to node, false otherwise
     */

    public static boolean getAudioNodeApplyAirAbsorption(AudioNode node) {
        Object data = node.getUserData(PhononAudioParam.ApplyAirAbsorption.key);
        return data == null ? false : (boolean) data;
    }

    /**
     * Return the given audio node's direct occlusion mode
     * 
     * @param node Audio node
     * @return node's direct occlusion mode ordinal
     */
    public static byte getAudioNodeDirectOcclusionMode(AudioNode node) {
        Object data = node.getUserData(PhononAudioParam.DirectOcclusionMode.key);
        return data == null ? 0 : (byte)(int)data;
    }

    /**
     * Return the given audio node's direct occlusion method
     * 
     * @param node Audio node
     * @return node's direct occlusion method ordinal
     */
    public static byte getAudioNodeDirectOcclusionMethod(AudioNode node) {
        Object data = node.getUserData(PhononAudioParam.DirectOcclusionMethod.key);
        return data == null ? 0 : (byte)(int)data;
    }

    /**
     * Return the given audio node's source radius.
     * 
     * @param node Audio node
     * @return node's source radius (0 if still not set)
     */
    public static float getAudioNodeSourceRadius(AudioNode node) {
        Object data = node.getUserData(PhononAudioParam.SourceRadius.key);
        return data == null ? 0f : (float) data;
    }

    /**
     * Auxiliary method used by setters to communicate Phonon parameters updates
     * 
     * @param source Source in which the param has been updated
     * @param param Param that has been updated
     */
    private static void communicateUpdateToRenderer(AudioSource source, PhononAudioParam param) {
        if(AudioContext.getAudioRenderer() instanceof PhononRenderer) {
            PhononRenderer renderer = (PhononRenderer) AudioContext.getAudioRenderer();
            renderer.updateSourcePhononParam(source, param);
        }
    }
}