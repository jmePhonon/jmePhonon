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
     */

    public static PhononRenderer init(PhononSettings settings, Application app,boolean skipSoundDetection,boolean skipReflection) throws Exception {
        if(!skipSoundDetection){
            if(settings.system==null){ throw new Exception("No system found in settings"); }

            List<PhononSoundDevice> devices=settings.system.getAudioDevices();
            int lastTriedDevice=0;

            boolean ready=false;
            do{
                PhononSoundDevice device=settings.device;
                if(device==null){
                    device=devices.get(lastTriedDevice++);
                }

                List<Integer> formats=settings.system.getOutputFormats(device,settings.nOutputChannels);
                if(formats.size()>0){
                    // If format unset or not available, pick the best one available
                    if(settings.outputSampleSize==-1||(!formats.contains(settings.outputSampleSize))){
                        settings.outputSampleSize=formats.get(0);
                    }
                    settings.device=device;
                    ready=true;
                }

            }while((!ready)&&settings.device==null&&lastTriedDevice<devices.size());
            if(!ready){ throw new Exception("Error initializing output device."); }
        }
        
        if(app!=null&&!skipReflection){
            if(app.getAudioRenderer()!=null) app.getAudioRenderer().cleanup();
        }

        PhononRenderer phononRenderer = new PhononRenderer(settings);
        AudioContext.setAudioRenderer(phononRenderer);

        Listener listener = new Listener();
        listener.setRenderer(phononRenderer);
        listener.setVolume(1f);
        phononRenderer.setListener(listener);


        if(app != null && app instanceof LegacyApplication&&!skipReflection) {           
            forceFieldsReplace((LegacyApplication)app, phononRenderer, listener);          
        }


        return phononRenderer;
    }
   
  
   
    private static void forceFieldsReplace(LegacyApplication app, PhononRenderer phononRenderer, Listener listener) throws Exception {
        Field fields[] = LegacyApplication.class.getDeclaredFields();

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