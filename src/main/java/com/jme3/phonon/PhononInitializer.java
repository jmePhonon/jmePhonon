package com.jme3.phonon;

import java.lang.reflect.Field;
import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.LegacyApplication;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;

/**
 * This class is used INTERNALLY to initialize phonon components.
 * 
 * @author aegroto
 * 
 */

public class PhononInitializer {

    /**
     * Auxiliary method for Phonon.init(...)
     * 
     * @param settings Phonon settings
     * @param app Application in which Phonon must be initialized
     * 
     * @return Initialized PhononRenderer 
     * 
     * @throws Exception Generic initialization exception, check messages for more informations.
     * 
     * @author riccardobl, aegroto
     */

    public static PhononRenderer initInApplication(PhononSettings settings, Application app) throws Exception {
        initWithBestSettings(settings);

        PhononRenderer phononRenderer = createPhononRenderer(settings);
        Listener listener = createListener(phononRenderer);

        if(app != null && app instanceof LegacyApplication) {
            if(app.getAudioRenderer() != null)
                app.getAudioRenderer().cleanup();

            forceFieldsReplace(app, phononRenderer, listener);          
        }

        phononRenderer.initialize();

        return phononRenderer;
    }

    private static void initWithBestSettings(PhononSettings settings) throws Exception {
        if(settings.system == null) {
            throw new Exception("No system found in settings");
        }

        if(settings.device == null){
            settings.device = settings.system.getAudioDevices().get(0);
        }

        if(settings.outputSampleSize == -1){
            List<Integer> formats = settings.system.getOutputFormats(settings.device,settings.nOutputChannels);
            settings.outputSampleSize = formats.get(0);
        }
    }
   
    private static PhononRenderer createPhononRenderer(PhononSettings settings) throws Exception {
        PhononRenderer phononRenderer = new PhononRenderer(settings);
        AudioContext.setAudioRenderer(phononRenderer);

        return phononRenderer;
    }

    private static Listener createListener(PhononRenderer phononRenderer) {
        Listener listener = new Listener();
        listener.setRenderer(phononRenderer);
        listener.setVolume(1f);
        phononRenderer.setListener(listener);

        return listener;
    }

    private static void forceFieldsReplace(Application app, PhononRenderer phononRenderer, Listener listener) throws Exception {
        Field fields[] = app.getClass().getDeclaredFields();

        for(Field f : fields) {
            if(f.getType().isAssignableFrom(AudioRenderer.class)) {
                f.setAccessible(true);
                f.set(app, phononRenderer);
            } else if(f.getType().isAssignableFrom(Listener.class)) {
                f.setAccessible(true);
                f.set(app, listener);
            }
        }  
    }
}