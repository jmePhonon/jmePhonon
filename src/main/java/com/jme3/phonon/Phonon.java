package com.jme3.phonon;

import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;

/**
 * Phonon main class. Contains helpers methods to initialize and edit Phonon's settings.
 * 
 * @author aegroto
 */

public class Phonon {
    public static enum PhononAudioParam {
        DipoleWeight("phonon.dipole_weight"),
        DipolePower("phonon.dipole_power");

        String key;

        PhononAudioParam(String key) {
            this.key = key;
        }
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

    private static void communicateUpdateToRenderer(AudioSource source, PhononAudioParam param) {
        if(AudioContext.getAudioRenderer() instanceof PhononRenderer) {
            PhononRenderer renderer = (PhononRenderer) AudioContext.getAudioRenderer();
            renderer.updateSourcePhononParam(source, param);
        }
    }
}