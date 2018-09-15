package com.jme3.phonon;

import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Listener;
/**
 * Phonon main class. Contains helpers methods to initialize and edit Phonon's settings.
 * 
 * @author aegroto
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
     * 
     * @return Initialized PhononRenderer
     * 
     * @throws Exception Generic initialization exception, check messages for more informations.
     * 
     * @author riccardobl, aegroto
     */

    public static PhononRenderer init(PhononSettings settings, Application app) throws Exception {
        return PhononInitializer.initInApplication(settings, app);
    }

    /**
     * Set audio node dipole weight.
     * 
     * @param node Audio node
     * @param dipoleWeight Dipole weight value
     *
     * @author aegroto
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
     *
     * @author aegroto
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
     *
     * @author aegroto
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
     *
     * @author aegroto
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
     * 
     * @author aegroto
     */

    private static void communicateUpdateToRenderer(AudioSource source, PhononAudioParam param) {
        if(AudioContext.getAudioRenderer() instanceof PhononRenderer) {
            PhononRenderer renderer = (PhononRenderer) AudioContext.getAudioRenderer();
            renderer.updateSourcePhononParam(source, param);
        }
    }
}