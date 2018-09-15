package com.jme3.phonon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

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
    static enum PhononAudioParam{
        DipoleWeight("phonon.dipole_weight"),DipolePower("phonon.dipole_power");

        String key;

        PhononAudioParam(String key){
            this.key=key;
        }
    }


     
    public static PhononRenderer init(PhononSettings settings, Application app
     ) throws Exception {
 
        if(settings.system==null){
            //exception
        }
        if(settings.device==null){
            settings.device=settings.system.getAudioDevices().get(0);
        }
        if(settings.outputSampleSize==-1){
            List<Integer> formats=settings.system.getOutputFormats(settings.device,settings.nOutputChannels);
            settings.outputSampleSize=formats.get(0);
                   
        }

        PhononRenderer phononRenderer = new PhononRenderer(settings);
        AudioContext.setAudioRenderer(phononRenderer);


        Listener listener = new Listener();
        listener.setRenderer(phononRenderer);
        listener.setVolume(1);
        phononRenderer.setListener(listener);

        if(app!=null&&app instanceof LegacyApplication){
            System.out.println("Found LegacyApplication, force replace audioRenderer...");
            if( app.getAudioRenderer()!=null)app.getAudioRenderer().cleanup();
            Field fs[]=LegacyApplication.class.getDeclaredFields();
            for(Field f:fs){
                if(f.getType().isAssignableFrom(AudioRenderer.class)){
                    System.out.println("Found field "+f.getName()+", replace.");
                    f.setAccessible(true);
                    f.set(app,phononRenderer);
                }else if(f.getType().isAssignableFrom(Listener.class)){
                    System.out.println("Found field "+f.getName()+", replace.");
                    f.setAccessible(true);
                    f.set(app,listener);
                }
            }            
        }
        phononRenderer.initialize();

        System.out.println("Phonon initialized with settings "+settings);
        return phononRenderer;
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