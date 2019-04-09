package com.jme3.phonon.scene.emitters;

import java.io.IOException;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioParam;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.phonon.Phonon.PhononAudioParam;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMethod;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMode;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.phonon.scene.DirectSoundPathFun;
import com.jme3.phonon.utils.F32leCachedConverter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.util.clone.Cloner;

/**
 * PositionalSoundEmitterControl
 */
public class PositionalSoundEmitterControl extends SoundEmitterControl{
    private PhononDirectOcclusionMode directOcclusionMode = PhononDirectOcclusionMode.IPL_DIRECTOCCLUSION_NOTRANSMISSION; 
    private PhononDirectOcclusionMethod directOcclusionMethod = PhononDirectOcclusionMethod.IPL_DIRECTOCCLUSION_RAYCAST;
    private float sourceRadius = 1f;
    private boolean applyAirAbsorption = false;
    private Vector3f previousWorldTranslation=Vector3f.NAN.clone();
    private boolean reverb=false;
    private Vector3f offset=new Vector3f(),pos=new Vector3f();
    private boolean BINAURAL=true;
    private volatile DirectSoundPathFun directSoundPathFun=null;

    public PositionalSoundEmitterControl() { }

    public PositionalSoundEmitterControl(String name){
        init(name,null,null);
     }

   
    public PositionalSoundEmitterControl(AssetManager am,String path){
        AudioKey audioKey=new AudioKey(path);
        init(null,F32leCachedConverter.toF32le(am.loadAsset(audioKey)),audioKey);
    }

    public PositionalSoundEmitterControl(AssetManager am,AudioKey audioKey){
        init(null,F32leCachedConverter.toF32le(am.loadAsset(audioKey)),audioKey);
    }

    public PositionalSoundEmitterControl(String name,F32leAudioData audioData,AudioKey audioKey){
        init(name, audioData, audioKey);   
    }
    

    public void setBinaural(boolean v){
        BINAURAL=v;
        if(getChannel()>=0)
            getRenderer().updateSourcePhononParam(this, PhononAudioParam.BinauralStatus);
    }

    public boolean isBinaural(){
        return BINAURAL;
    }



    @Override
    protected String getDefaultName(AudioKey audioKey){
        return audioKey.getName()+" (pos)";
     }
 
      
    public void setReverbEnabled(boolean v) {
        reverb=v;
        if(getChannel()>=0)
            getRenderer().updateSourcePhononParam(this, PhononAudioParam.ReverbStatus);
    }

    public boolean isReverbEnabled() {
        return reverb;
    }

    @Override
    public void play() {
        if(getF32leAudioData().getChannels()>1){ throw new IllegalStateException("Only mono audio is supported for positional audio nodes"); }
        super.play();
    }
    
    @Override
    public void playInstance() {
        if(getF32leAudioData().getChannels()>1){ throw new IllegalStateException("Only mono audio is supported for positional audio nodes"); }
        super.playInstance();
    }
    
    public void setOffset(Vector3f o) {
        offset.set(o);
    }

    @Override
    public Vector3f getPosition() {
        if(spatial==null) return super.getPosition();
        return pos.set(spatial.getWorldTranslation()).addLocal(offset);
    }

    @Override
    public boolean isPositional() {
        return true;
    }


    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if(spatial==null) stop();
    }

    public void setApplyAirAbsorption(boolean applyAirAbsorption) {
        this.applyAirAbsorption=applyAirAbsorption;
        if(getChannel()>=0)
            getRenderer().updateSourcePhononParam(this, PhononAudioParam.ApplyAirAbsorption);
    }

    public boolean isApplyAirAbsorption() {
        return applyAirAbsorption;
    }

    public boolean isAirAbsorptionApplied() {
        return applyAirAbsorption;
    }

    public void setDirectOcclusionMode(PhononDirectOcclusionMode directOcclusionMode) {
        this.directOcclusionMode=directOcclusionMode;
        if(getChannel()>=0)
        getRenderer().updateSourcePhononParam(this, PhononAudioParam.DirectOcclusionMode);

    }

    public PhononDirectOcclusionMode getDirectOcclusionMode() {
        return directOcclusionMode;
    }

    public void setDirectOcclusionMethod(PhononDirectOcclusionMethod directOcclusionMethod) {

        this.directOcclusionMethod=directOcclusionMethod;

        if(getChannel()>=0)
        getRenderer().updateSourcePhononParam(this, PhononAudioParam.DirectOcclusionMethod);

    }

    public PhononDirectOcclusionMethod getDirectOcclusionMethod() {
        return directOcclusionMethod;
    }

    public void setSourceRadius(float sourceRadius) {
        this.sourceRadius=sourceRadius;
        if(getChannel()>=0)
        getRenderer().updateSourcePhononParam(this, PhononAudioParam.SourceRadius);

    }

    public float getSourceRadius() {
        return sourceRadius;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        if(getChannel()<0||spatial.getParent()==null) return;
        Vector3f currentWorldTranslation=getPosition();
        if(!previousWorldTranslation.equals(currentWorldTranslation)){
            getRenderer().updateSourceParam(this,AudioParam.Position);
            previousWorldTranslation.set(currentWorldTranslation);
        }
    }
    


    /**
     *  Called internally by com.jme3.util.clone.Cloner.  Do not call directly.
     */
    @Override
    public void cloneFields( Cloner cloner, Object original ) {
        super.cloneFields(cloner,original);
        this.previousWorldTranslation=Vector3f.NAN.clone();
    }

    @Override
    public void write(JmeExporter ex) throws IOException {

        super.write(ex);
        OutputCapsule oc=ex.getCapsule(this);
        int domo=getDirectOcclusionMode().ordinal();
        int dome=getDirectOcclusionMethod().ordinal();
        //Phonon specific settings
        oc.write(isApplyAirAbsorption(), "applyAirAbsorption", false);
        oc.write(domo, "occlusionMode", 1);
        oc.write(dome, "occlusionMethod", 0);
        oc.write(getSourceRadius(),"sourceRadius",1f);
        oc.write("positional","type",null);
        oc.write(isReverbEnabled(),"convolutionEffect",false);

    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);

        InputCapsule ic = im.getCapsule(this);     
        setApplyAirAbsorption(ic.readBoolean("applyAirAbsorption", false));
        setDirectOcclusionMode(PhononDirectOcclusionMode.values()
            [ic.readInt("occlusionMode", 1)]);
        setDirectOcclusionMethod(PhononDirectOcclusionMethod.values()
            [ic.readInt("occlusionMethod", 0)]);
        setSourceRadius(ic.readFloat("sourceRadius",1f));
        setReverbEnabled(ic.readBoolean("convolutionEffect",false));

    }

	public DirectSoundPathFun getCustomDirectSoundPathFunction() {
		return directSoundPathFun;
    }
    
    public void setCustomDirectSoundPathFunction(DirectSoundPathFun fun){
        directSoundPathFun=fun;
        if(getChannel()>=0)
            getRenderer().updateSourcePhononParam(this, PhononAudioParam.DirectSoundPathFun);
    }
}