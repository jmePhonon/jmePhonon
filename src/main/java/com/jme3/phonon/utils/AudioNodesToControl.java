package com.jme3.phonon.utils;

import java.lang.reflect.Field;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioNode;
import com.jme3.phonon.scene.emitters.DirectionalSoundEmitterControl;
import com.jme3.phonon.scene.emitters.PositionalSoundEmitterControl;
import com.jme3.phonon.scene.emitters.SoundEmitterControl;
import com.jme3.scene.Node;
/**
 * AudioNodesToControl
 */
public class AudioNodesToControl {

    public static Node convert(AssetManager am,AudioNode an) {
        Node parent=an.getParent();
        Node newnode=new Node(an.getName()==null?"Speaker":an.getName());
        newnode.setLocalTransform(an.getLocalTransform());
        parent.attachChild(newnode);

        SoundEmitterControl emitter;
        AudioKey audioKey=null;

        Field[] fields=AudioNode.class.getDeclaredFields();
        for(Field f:fields){
            if(f.getType()==(AudioKey.class)){
                f.setAccessible(true);
                try{
                    audioKey=(AudioKey)f.get(an);
                }catch(IllegalArgumentException e){
                    e.printStackTrace();
                }catch(IllegalAccessException e){
                    e.printStackTrace();
                }
                break;
            }
        }
    
        if(an.isDirectional()){
            DirectionalSoundEmitterControl dem
                    =new DirectionalSoundEmitterControl(am,audioKey);
            dem.setDirection(an.getDirection());
            dem.setReverbEnabled(an.isReverbEnabled());
            emitter=dem;
        }else if(an.isPositional()){
            PositionalSoundEmitterControl dem
                    =new PositionalSoundEmitterControl(am,audioKey);
            dem.setReverbEnabled(an.isReverbEnabled());
            emitter=dem;
        }else{
            emitter=new SoundEmitterControl(am,audioKey);
            
        }
        emitter.setLooping(an.isLooping());
        emitter.setVolume(an.getVolume());
        emitter.setPitch(an.getPitch());
        emitter.setChannel(an.getChannel());
        emitter.setStatus(an.getStatus());
        newnode.addControl(emitter);
        
        an.removeFromParent();
        return newnode;
    }
}