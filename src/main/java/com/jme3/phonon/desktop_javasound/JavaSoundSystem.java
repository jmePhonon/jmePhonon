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
    public  List<Integer> getOutputFormats(PhononSoundDevice device,int nchannels) {
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
                 if(f.getEncoding()!=Encoding.PCM_SIGNED) continue;
                 if(f.getChannels()!=nchannels) continue;
                 if(f.isBigEndian()) continue;

                 int sampleSize=f.getSampleSizeInBits();
                 if(sampleSize>24) continue;   

                 // if(f.getSampleSizeInBits()!=sampleSize) continue;
                 System.out.println("? "+f);
                 if(f.getFrameSize()!=(sampleSize/8)*nchannels) continue;
                 formats.add(sampleSize);
             }
 
         }catch(Exception e){
 
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
        Mixer.Info mixInfos[]=AudioSystem.getMixerInfo();

        for(Mixer.Info mixInfo:mixInfos){
            PhononSoundDevice device=new JavaSoundDevice(mixInfo.getName(),
                    mixInfo.getName()+": "+mixInfo.getDescription()+", "+mixInfo.getVendor()+", "+mixInfo.getVersion(),
                AudioSystem.getMixer(mixInfo));
            devices.add(device);
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