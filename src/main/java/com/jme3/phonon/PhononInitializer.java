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

class PhononInitializer {

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