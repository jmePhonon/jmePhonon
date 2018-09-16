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
package com.jme3.phonon.desktop_javasound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

import com.jme3.phonon.PhononSoundDevice;
import com.jme3.phonon.PhononSoundPlayer;
import com.jme3.phonon.PhononSoundSystem;



public class JavaSoundSystem implements PhononSoundSystem {

    @Override
    public List<Integer> getOutputFormats(PhononSoundDevice device, int nchannels) {
        return getOutputFormats(device,nchannels,false);

    }

    public  List<Integer> getOutputFormats(PhononSoundDevice device,int nchannels,boolean debugPrint) {
        Line.Info lineInfo = new Line.Info(SourceDataLine.class);
         Mixer mixer=null;
         for(Mixer.Info mixInfo:AudioSystem.getMixerInfo()){
             if(mixInfo.getName().equals(device.getID())){
                mixer=AudioSystem.getMixer(mixInfo);
                 
                 break;
             }
         }
        if(mixer==null){
            //exception
        }
         ArrayList<Integer> formats=new ArrayList<Integer>();
 
         try {
             SourceDataLine ln=(SourceDataLine)mixer.getLine(lineInfo);
             SourceDataLine.Info lnf=(SourceDataLine.Info)ln.getLineInfo();
            for(AudioFormat f:lnf.getFormats()){
                if(debugPrint) System.out.println(f);
                if(f.getEncoding()!=Encoding.PCM_SIGNED){
                    if(debugPrint) System.out.println("Skip, wrong encoding");
                    continue;
                }
                if(f.getChannels()!=nchannels){
                    if(debugPrint) System.out.println("Skip, wrong channels "+nchannels+" =/= "+f.getChannels());
                    continue;
                }
                if(f.isBigEndian()){
                    if(debugPrint) System.out.println("Skip, wrong endianess");
                    continue;
                }

                 int sampleSize=f.getSampleSizeInBits();
                if(sampleSize>24){
                    if(debugPrint) System.out.println("Skip, wrong sample size");
                    continue;
                }

                 // if(f.getSampleSizeInBits()!=sampleSize) continue;
                //  System.out.println("? "+f);
                if(f.getFrameSize()!=(sampleSize/8)*nchannels){
                    if(debugPrint) System.out.println("Skip, wrong framesize "+f.getFrameSize()+"=/="+((sampleSize/8)*nchannels));

                    continue;
                }
                 formats.add(sampleSize);
             }
 
         }catch(Exception e){
            e.printStackTrace();
         }
 
         if(formats.size()==0){
             // exception
         }
 
         formats.sort(Collections.reverseOrder());
         return formats;
     }
 
     @Override
    public List<PhononSoundDevice> getAudioDevices() {
        List<PhononSoundDevice> devices=new ArrayList<PhononSoundDevice>();

        Mixer.Info  defaultMixer=AudioSystem.getMixer(null).getMixerInfo();

        Mixer.Info mixInfos[]=AudioSystem.getMixerInfo();
        for(Mixer.Info mixInfo:mixInfos){
            PhononSoundDevice device=new JavaSoundDevice(mixInfo.getName(),
                    mixInfo.getName()+": "+mixInfo.getDescription()+", "+mixInfo.getVendor()+", "+mixInfo.getVersion(),
                    AudioSystem.getMixer(mixInfo));
            if(mixInfo==defaultMixer){
                System.out.println("Found default device "+device);
                devices.add(0,device);
            }else  devices.add(device);
        }
        return devices;
    }
     
    @Override
    public PhononSoundPlayer newPlayer() {
        return new JavaSoundPlayer();
    }

    @Override
    public String toString() {
        return "Java Sound";
    }
    


    
}