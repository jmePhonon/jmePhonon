package com.jme3.phonon;

import java.util.Map;
import java.util.WeakHashMap;

import com.jme3.audio.AudioData;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.ListenerParam;

/**
 * PhononRenderer
 */
public class PhononRenderer implements AudioRenderer{
    private final Map<AudioData, F32leAudioData> conversionCache = new WeakHashMap<AudioData, F32leAudioData>();


    private F32leAudioData toF32leData(AudioData d) {
        F32leAudioData o=conversionCache.get(d);
        if (o == null) {
            o = new F32leAudioData(d);
            conversionCache.put(d,o);
        }
        return o;
    }
        

	@Override
	public void setListener(Listener listener) {
		
	}

	@Override
	public void setEnvironment(Environment env) {
		
	}

	@Override
    public void playSourceInstance(AudioSource src) {
        F32leAudioData data=toF32leData(src.getAudioData());

		
	}

	@Override
    public void playSource(AudioSource src) {
        F32leAudioData data=toF32leData(src.getAudioData());

		
	}

	@Override
	public void pauseSource(AudioSource src) {
		F32leAudioData data=toF32leData(src.getAudioData());

	}

	@Override
	public void stopSource(AudioSource src) {
		
	}

	@Override
	public void updateSourceParam(AudioSource src, AudioParam param) {
		
	}

	@Override
	public void updateListenerParam(Listener listener, ListenerParam param) {
		
	}

	@Override
	public float getSourcePlaybackTime(AudioSource src) {
		return 0;
	}

	@Override
	public void deleteFilter(Filter filter) {
		
	}

	@Override
	public void deleteAudioData(AudioData ad) {
		
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void update(float tpf) {
		
	}

	@Override
	public void pauseAll() {
		
	}

	@Override
	public void resumeAll() {
		
	}

	@Override
	public void cleanup() {
		
	}

    
}