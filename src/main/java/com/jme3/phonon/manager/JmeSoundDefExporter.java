package com.jme3.phonon.manager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.jme3.export.JmeExporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.phonon.scene.emitters.SoundEmitterControl;

/**
 * JmeToMapExporter
 */
public class JmeSoundDefExporter implements JmeExporter{

    private final Map<String,Map<String,Object>> SOUNDS_DEF;

    public JmeSoundDefExporter(Map<String,Map<String,Object>> def){
        SOUNDS_DEF=def;
    }

    @Override
    public void save(Savable object, OutputStream f) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void save(Savable object, File f) throws IOException {
        throw new UnsupportedOperationException();
    }

  
    @Override
    public OutputCapsule getCapsule(Savable object) {
        if(object instanceof SoundEmitterControl){
            SoundEmitterControl as=(SoundEmitterControl)object;
            Map<String,Object> def=SOUNDS_DEF.get(as.getName());
            if(def==null){
                def=new HashMap<String,Object>();
                SOUNDS_DEF.put(as.getName(),def);
            }
            return new SoundDefOutputCapsule(def);
        }else return null;
    }
}