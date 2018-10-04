package com.jme3.phonon.manager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioSource;
import com.jme3.phonon.PhononSettings;
import com.jme3.phonon.scene.emitters.SoundEmitterControl;
import com.jme3.phonon.thread.ThreadSafeQueue;

/**
 * AudioManager
 */
public class AudioManager extends BaseAppState{
    private final boolean enabledRemoteManager=true;


    private final Map<String,Map<String,Object>> SOUNDS_DEF=new HashMap<String,Map<String,Object>>();
    
    private volatile String soundDefJSON;
    private volatile String sysInfo;

    private final JmeSoundDefExporter EXPORTER=new JmeSoundDefExporter(SOUNDS_DEF);
    private final JmeSoundDefImporter IMPORTER;
    private final JSON JSON_PARSER;
    private final WeakHashMap<SoundEmitterControl,String> CONNECTED_SOURCES=new WeakHashMap<SoundEmitterControl,String>();
    private final List<SoundEmitterControl> NEED_UPDATE=new LinkedList<SoundEmitterControl>();
    private final ThreadSafeQueue QUEUE=new ThreadSafeQueue();
    private final PhononSettings SETTINGS;
    private final AssetManager AM;
    public AudioManager(PhononSettings sett,AssetManager am,JSON json){
        IMPORTER=new JmeSoundDefImporter(EXPORTER,am,SOUNDS_DEF);
        SETTINGS=sett;
        JSON_PARSER=json;
        AM=am;
        reset();

        try{
            new HttpServer(6601,this);
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void preload(InputStream is) throws IOException {
        byte chunk[]=new byte[1024*1024];
        int read;
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        while((read=is.read(chunk))!=-1){
            bos.write(chunk,0,read);
        }
        String s=bos.toString("UTF-8");
        System.out.println("Load "+s);
        if(!s.isEmpty()) SOUNDS_DEF.putAll(JSON_PARSER.parse(s));

    }

    public void preload(String am) throws IOException {
        AssetInfo info=AM.locateAsset(new AssetKey(am));
        if(info==null) throw new FileNotFoundException(am);
        InputStream is=info.openStream();
        try{
            preload(is);
        }finally{
            try{
                is.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void init(SoundEmitterControl as) {
        try{
            as.read(IMPORTER);
        }catch(IOException e){
            e.printStackTrace();
        }
        if(!enabledRemoteManager) return;
        CONNECTED_SOURCES.put(as,as.getName());
    }

    public String getDef() {
        return soundDefJSON==null?"{}":soundDefJSON;
    }
    
    public void updateDef(String def) {
        if(!enabledRemoteManager) return;

        QUEUE.enqueue(() -> {
            SOUNDS_DEF.putAll(JSON_PARSER.parse(def));
            for(Entry<SoundEmitterControl,String> e:CONNECTED_SOURCES.entrySet()){
                try{
                    e.getKey().read(IMPORTER);
                }catch(IOException e1){
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
    }
    
    public String getSysInfo() {
        return sysInfo;

    }

    public void sourceAction(String def) {
        if(!enabledRemoteManager) return;

        QUEUE.enqueue(() -> {
            Map<String,Object> action=JSON_PARSER.parse(def);
            String act=action.get("action").toString();
            String id=action.get("source").toString();
            
                    
            for(Entry<SoundEmitterControl,String> e:CONNECTED_SOURCES.entrySet()){
                if(e.getValue().equals(id)){
                    switch(act){
                        case "play":{
                            e.getKey().stop();
                            e.getKey().play();
                            break;
                        }

                        case "stop":{
                            e.getKey().stop();
                            break;
                        }

                        case "pause":{
                            e.getKey().play();
                            e.getKey().pause();

                            break;
                        }
                    }
                }
            }
        });
    }

    
    public void reset() {
        
        SOUNDS_DEF.clear();
        CONNECTED_SOURCES.clear();
        NEED_UPDATE.clear();

        if(!enabledRemoteManager) return;
        Map<String,Object> info=new HashMap<String,Object>();;
        info.put("lastReset",System.currentTimeMillis());
        info.put("sampleRate",SETTINGS.sampleRate);
        info.put("device",SETTINGS.device!=null?SETTINGS.device.getID():"null");
        info.put("nOutputChannels",SETTINGS.nOutputChannels);
        info.put("frameSize",SETTINGS.frameSize);
        sysInfo=JSON_PARSER.stringify(info);
    }

    public void updateSource(AudioSource src) {

        if(!enabledRemoteManager||!(src instanceof SoundEmitterControl)) return;

        if(!NEED_UPDATE.contains(src)){
            // System.out.println("Source "+src+" marked for update");
            NEED_UPDATE.add((SoundEmitterControl)src);
        }
    }

    @Override
    public void update(float tpf) {
        if(!enabledRemoteManager) return;
        super.update(tpf);
        QUEUE.run();
        boolean needJsonUpdate=false;
        Iterator<SoundEmitterControl> updateI=NEED_UPDATE.iterator();
        while(updateI.hasNext()){
            needJsonUpdate=true;
            SoundEmitterControl as=updateI.next();
            updateI.remove();
            try{
                as.write(EXPORTER);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        if(needJsonUpdate||soundDefJSON==null||soundDefJSON.isEmpty()){
            // System.out.println("Update json");
            soundDefJSON=JSON_PARSER.stringify(SOUNDS_DEF);
        }
    }

    @Override
    protected void initialize(Application app) {

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

	}

}