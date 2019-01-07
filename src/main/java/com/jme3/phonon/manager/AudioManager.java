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
public abstract class AudioManager extends BaseAppState{
    public abstract void preload(String am) throws IOException;
    public abstract void init(SoundEmitterControl as);   
    public abstract void reset();
    public abstract void updateSource(AudioSource src) ;
}