package com.jme3.phonon;

import com.jme3.audio.AudioNode;

/**
 * Phonon main class. Contains helpers methods to initialize and edit Phonon's settings.
 * 
 * @author aegroto
 */

public class Phonon {
    private static final String DIPOLE_WEIGHT_UDID = "phonon.dipole_weight";
    private static final String DIPOLE_POWER_UDID = "phonon.dipole_power";

    /**
     * Set audio node dipole weight.
     * 
     * @param node Audio node
     * @param dipoleWeight Dipole weight value
     *
     * @author aegroto
     */
    public static void setAudioNodeDipoleWeight(AudioNode node, float dipoleWeight) {
        node.setUserData(DIPOLE_WEIGHT_UDID, dipoleWeight);
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
        node.setUserData(DIPOLE_POWER_UDID, dipolePower);
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
        Object data = node.getUserData(DIPOLE_WEIGHT_UDID);
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
        Object data = node.getUserData(DIPOLE_POWER_UDID);
        return data == null ? 0f : (float) data;
    }
}