package com.jme3.phonon.scene.emitters;

import java.io.IOException;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Filter;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.phonon.PhononRenderer;
import com.jme3.phonon.Phonon.PhononAudioParam;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMethod;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMode;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.phonon.manager.AudioManager;
import com.jme3.phonon.manager.JmeSoundDefExporter;
import com.jme3.phonon.manager.JmeSoundDefImporter;

import com.jme3.phonon.utils.F32leCachedConverter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.PlaceholderAssets;
import com.jme3.util.clone.Cloner;

/**
 * SoundEmitterControl
 */
public class SoundEmitterControl  extends AbstractControl implements AudioSource {
    private boolean loop = false;
    private float volume = 1;
    private float pitch = 1;
    private float timeOffset = 0;
    private AudioKey audioKey;
    private transient F32leAudioData data = null;
    private transient volatile AudioSource.Status status = AudioSource.Status.Stopped;
    private transient volatile int channel = -1;
    private String name;


    public SoundEmitterControl() { }

    public SoundEmitterControl(String name){
        init(name,null,null);
     }

   
    public SoundEmitterControl(AssetManager am,String path){
        audioKey=new AudioKey(path);
        init(null,F32leCachedConverter.toF32le(am.loadAudio(audioKey)),audioKey);
    }

    public SoundEmitterControl(AssetManager am,AudioKey audioKey){
        init(null,F32leCachedConverter.toF32le(am.loadAudio(audioKey)),audioKey);
    }

    public SoundEmitterControl(String name,F32leAudioData audioData,AudioKey audioKey){
        init(name, audioData, audioKey);   
    }
    
    protected void init(String name, F32leAudioData audioData, AudioKey audioKey) {
        if(name==null) this.name=getDefaultName(audioKey);
        else this.name=name;
        if(audioData!=null&&audioKey!=null){
            setF32leAudioData(audioData,audioKey);
        }
        AudioManager mng=getRenderer().getMng();
        if(mng!=null)mng.init(this);
    }
    
  

    protected String getDefaultName(AudioKey audioKey) {
        return audioKey.getName()+" (env)";
    }
    


    public void setF32leAudioData(F32leAudioData audioData, AudioKey audioKey) {
        this.audioKey=audioKey;
        this.data=audioData;
    }

  
    public AssetKey getAssetKey() {
        return audioKey;
    }
    public F32leAudioData getF32leAudioData() {
        return this.data;
    }


    protected PhononRenderer getRenderer() {
        AudioRenderer result = AudioContext.getAudioRenderer();
        if( result == null )
            throw new IllegalStateException( "No audio renderer available, make sure call is being performed on render thread." );
        return (PhononRenderer)result;
    }

    /**
     * Start playing the audio.
     */
    public void play(){
        getRenderer().playSource(this);
    }

    /**
     * Start playing an instance of this audio. This method can be used
     * to play the same <code>AudioEmitterControl</code> multiple times. Note
     * that changes to the parameters of this AudioEmitterControl will not affect the
     * instances already playing.
     */
    public void playInstance(){
       getRenderer().playSourceInstance(this);
    }

    /**
     * Stop playing the audio that was started with {@link PhononAudioEmitterControl#play() }.
     */
    public void stop(){
        getRenderer().stopSource(this);
    }

    /**
     * Pause the audio that was started with {@link PhononAudioEmitterControl#play() }.
     */
    public void pause(){
        getRenderer().pauseSource(this);
    }

    /** DO NOT USE THESE METHODS **/
    /** They are defined for compatibility with AudioSource **/
    public float getRefDistance() { return 0f; }
    public float getOuterAngle() { return 0f; }
    public float getInnerAngle() { return 0f; }
    public float getMaxDistance() { return 0f; }
    public Vector3f getVelocity () { return Vector3f.NAN; }
    public Filter getDryFilter() { return null; }
    public Filter getReverbFilter() { return null; }
    public boolean isReverbEnabled() { return false; } // FIXME: Returns true when native reverb is enabled

    /**
     * Do not use.
     */
    public final void setChannel(int channel) {
        if (status != AudioSource.Status.Stopped) {
            throw new IllegalStateException("Can only set source id when stopped");
        }

        this.channel = channel;
    }

    /**
     * Do not use.
     */
    public int getChannel() {
        return channel;
    }


    @Deprecated
    public void setAudioData(AudioData audioData, AudioKey audioKey) {
        throw new UnsupportedOperationException("Not supported, use setF32leAudioData instead");
    }

    @Deprecated
    public AudioData getAudioData() {
        throw new UnsupportedOperationException("Not supported, use getF32leAudioData instead");
    }

    /**
     * @return The {@link Status} of the audio node.
     * The status will be changed when either the {@link PhononAudioEmitterControl#play() }
     * or {@link PhononAudioEmitterControl#stop() } methods are called.
     */
    public AudioSource.Status getStatus() {
        return status;
    }

    /**
     * Do not use.
     */
    public final void setStatus(AudioSource.Status status) {
        this.status=status;
        if (channel >= 0)
        getRenderer().updateSourcePhononParam(this, PhononAudioParam.Status);
    }


    /**
     * @return True if the audio will keep looping after it is done playing,
     * otherwise, false.
     * @see PhononAudioEmitterControl#setLooping(boolean)
     */
    public boolean isLooping() {
        return loop;
    }

    /**
     * Set the looping mode for the audio node. The default is false.
     *
     * @param loop True if the audio should keep looping after it is done playing.
     */
    public void setLooping(boolean loop) {
        this.loop = loop;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.Looping);
    }

    /**
     * @return The pitch of the audio, also the speed of playback.
     *
     * @see PhononAudioEmitterControl#setPitch(float)
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Set the pitch of the audio, also the speed of playback.
     * The value must be between 0.5 and 2.0.
     *
     * @param pitch The pitch to set.
     * @throws IllegalArgumentException If pitch is not between 0.5 and 2.0.
     */
    public void setPitch(float pitch) {
        if (pitch < 0.5f || pitch > 2.0f) {
            throw new IllegalArgumentException("Pitch must be between 0.5 and 2.0");
        }

        this.pitch = pitch;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.Pitch);
    }

    /**
     * @return The volume of this audio node.
     *
     * @see PhononAudioEmitterControl#setVolume(float)
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Set the volume of this audio node.
     *
     * The volume is specified as gain. 1.0 is the default.
     *
     * @param volume The volume to set.
     * @throws IllegalArgumentException If volume is negative
     */
    public void setVolume(float volume) {
        if (volume < 0f) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }

        this.volume = volume;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.Volume);
    }

    /**
     * @return the time offset in the sound sample when to start playing.
     */
    public float getTimeOffset() {
        return timeOffset;
    }

    /**
     * Set the time offset in the sound sample when to start playing.
     *
     * @param timeOffset The time offset
     * @throws IllegalArgumentException If timeOffset is negative
     */
    public void setTimeOffset(float timeOffset) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public float getPlaybackTime() {
        throw new UnsupportedOperationException("Not implemented");
    }

    
    @Override
    protected void controlUpdate(float tpf) { }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
      
    }

    @Override
    public SoundEmitterControl clone() {
        try {
            return (SoundEmitterControl) super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone AudioEmitterControl");
            return null;
        }
    }

    /**
     *  Called internally by com.jme3.util.clone.Cloner.  Do not call directly.
     */
    @Override
    public void cloneFields( Cloner cloner, Object original ) {
        super.cloneFields(cloner,original);
        this.channel=-1;
        // if(this.getStatus()==Status.Playing){
        //     this.play();
        
        // }
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc=ex.getCapsule(this);
        if(!(ex instanceof JmeSoundDefExporter)){

            oc.write(audioKey,"audioKey",null);
        }
        oc.write(loop, "loop", false);
        oc.write(volume, "volume", 1);
        oc.write(pitch, "pitch", 1);
        oc.write(timeOffset, "timeOffset", 0);
        oc.write("environmental","type",null);
        oc.write(name, "name", "unnamed");
        switch(status){
            case Playing:
                oc.write("playing","status",null);
                break;
            case Paused:
                oc.write("paused","status",null);
                break;
            case Stopped:
                oc.write("stopped","status",null);
                break;
        }

       
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);      
        setLooping(ic.readBoolean("loop", false));
        setVolume(volume = ic.readFloat("volume", 1));
        setPitch(pitch = ic.readFloat("pitch", 1));
        // setTimeOffset(timeOffset=ic.readFloat("timeOffset",0)); TODO: Implement this.
        setName(name=ic.readString("name","unnamed"));
        
        if(!(im instanceof JmeSoundDefImporter)){
            audioKey=(AudioKey)ic.readSavable("audioKey",null);
            if(audioKey!=null){
                try{
                    AudioData adata=im.getAssetManager().loadAsset(audioKey);
                    data=F32leCachedConverter.toF32le(adata);
                }catch(AssetNotFoundException ex){
                    // TODO: Restore exception logging
                    // Logger.getLogger(AudioEmitterControl.class.getName()).log(Level.FINE, "Cannot locate {0} for audio node {1}", new Object[]{audioKey, key});
                    data=F32leCachedConverter.toF32le(PlaceholderAssets.getPlaceholderAudio());
                }
            }
        }
    }

    Vector3f p=Vector3f.ZERO.clone();
    Vector3f d=Vector3f.UNIT_Z.clone();

    @Override
    public Vector3f getPosition() {
        return p;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public Vector3f getDirection() {
        return d;
    }

    @Override
    public boolean isPositional() {
        return false;
    }

	public String getName() {
		return name;
	}


    public void setName(String name) {
		this.name=name;
	}

    @Override
    public String toString() {
        return getName();
    }
}