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
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.phonon.utils.F32leCachedConverter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.util.clone.Cloner;

/**
 * DirectionalSoundEmitterControl
 */
public class DirectionalSoundEmitterControl extends PositionalSoundEmitterControl{
    private Vector3f previousDirection=Vector3f.NAN.clone();
    private float dipoleWeight=0f;
    private float dipolePower=1f;
    public DirectionalSoundEmitterControl() { }

    public DirectionalSoundEmitterControl(String name){
        init(name,null,null);
     }

   
    public DirectionalSoundEmitterControl(AssetManager am,String path){
        AudioKey audioKey=new AudioKey(path);
        init(null,F32leCachedConverter.toF32le(am.loadAudio(audioKey)),audioKey);
    }

    public DirectionalSoundEmitterControl(AssetManager am,AudioKey audioKey){
        init(null,F32leCachedConverter.toF32le(am.loadAudio(audioKey)),audioKey);
    }

    public DirectionalSoundEmitterControl(String name,F32leAudioData audioData,AudioKey audioKey){
        init(name, audioData, audioKey);   
    }
    

    @Override
    protected String getDefaultName(AudioKey audioKey) {
        return audioKey.getName()+" (dir)";
    }

    @Override
    public boolean isDirectional() {
        return true;
    }

    public void setDirection(Vector3f dir) {

        spatial.getWorldRotation().lookAt(dir,getUp());
    }
    
    @Override
    public Vector3f getDirection() {
        if(spatial==null) return p;

        return spatial.getWorldRotation().getRotationColumn(2);
    }

    
    public Vector3f getUp() {
        if(spatial==null) return p;

        return spatial.getWorldRotation().getRotationColumn(1);
    }

    public Vector3f getRight() {
        if(spatial==null) return p;

        return spatial.getWorldRotation().getRotationColumn(0).negate();
    }

    public void setDipoleWeight(float dipoleWeight) {
        this.dipoleWeight=dipoleWeight;
        if(getChannel()>=0)
        getRenderer().updateSourcePhononParam(this, PhononAudioParam.DipoleWeight);

    }

    public float getDipoleWeight() {
        return dipoleWeight;
    }

    public void setDipolePower(float dipolePower) {
        this.dipolePower=dipolePower;
        if(getChannel()>=0)
        getRenderer().updateSourcePhononParam(this, PhononAudioParam.DipolePower);

    }

    public float getDipolePower() {
        return dipolePower;
    }


    @Override
    public DirectionalSoundEmitterControl clone() {
            return (DirectionalSoundEmitterControl) super.clone();
      
    }

    /**
     *  Called internally by com.jme3.util.clone.Cloner.  Do not call directly.
     */
    @Override
    public void cloneFields( Cloner cloner, Object original ) {
        super.cloneFields(cloner, original); 
        this.previousDirection=Vector3f.NAN.clone();
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);       
        oc.write(getDipoleWeight(), "dipoleWeight", 1f);
        oc.write(getDipolePower(),"dipolePower",0f);
        oc.write( "directional", "type", null);

    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic=im.getCapsule(this);
        setDipoleWeight(ic.readFloat("dipoleWeight",1f));
        setDipolePower(ic.readFloat("dipolePower",0f));
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        if(getChannel()<0||spatial.getParent()==null) return;
        Vector3f currentDirection=getDirection();

        if(!previousDirection.equals(currentDirection)){
            getRenderer().updateSourceParam(this,AudioParam.Direction);

            previousDirection.set(currentDirection);
        }
    }

}