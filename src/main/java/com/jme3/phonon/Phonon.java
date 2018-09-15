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
import com.jme3.app.LegacyApplication;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Listener;

/**
 * Phonon main class. Contains helpers methods to initialize and configure Phonon.
 * 
 */
public final class Phonon {
    static enum PhononAudioParam {
        DipoleWeight("phonon.dipole_weight"),
        DipolePower("phonon.dipole_power");

        String key;

        PhononAudioParam(String key){
            this.key = key;
        }
    }
    
    /**
     * Initializes phonon context in the given application
     * 
     * @param settings Phonon initialization settings
     * @param app Application in which Phonon must be inizialized
     * @return Initialized PhononRenderer
     * @throws Exception Generic initialization exception, check messages for more informations.
     */
    public static PhononRenderer init(PhononSettings settings, Application app) throws Exception {
        return PhononInitializer.initInApplication(settings, app);
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
     * @param dipoleWeight Dipole power value
     */
    public static void setAudioNodeDipolePower(AudioNode node, float dipolePower) {
        node.setUserData(PhononAudioParam.DipolePower.key, dipolePower);
        communicateUpdateToRenderer(node, PhononAudioParam.DipolePower); 
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
     * Auxiliary methods used by setters to communicate Phonon parameters updates
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