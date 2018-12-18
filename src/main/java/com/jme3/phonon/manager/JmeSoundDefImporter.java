package com.jme3.phonon.manager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.phonon.scene.emitters.SoundEmitterControl;

/**
 * JmeToMapExporter
 */
public class JmeSoundDefImporter implements JmeImporter{

    private final Map<String,Map<String,Object>> SOUNDS_DEF;
    private final AssetManager AM;
    private final JmeSoundDefExporter EXPORTER;
    public JmeSoundDefImporter(JmeSoundDefExporter exp,AssetManager am,Map<String,Map<String,Object>> def){
        SOUNDS_DEF=def;
        AM=am;
        EXPORTER=exp;
    }

 
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        return null;
    }

    @Override
    public InputCapsule getCapsule(Savable object) {
        if(object instanceof SoundEmitterControl){
            SoundEmitterControl as=(SoundEmitterControl)object;
            Map<String,Object> def=SOUNDS_DEF.get(as.getName());
            if(def==null){
                try{
                    as.write(EXPORTER);
                    def=SOUNDS_DEF.get(as.getName());
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            return new SoundDefInputCapsule(as,def);
        }else return null;
    }

    @Override
    public AssetManager getAssetManager() {
        return AM;
    }

    @Override
	public int getFormatVersion() {
		return 0;
	}
}