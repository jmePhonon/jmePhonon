package com.jme3.phonon.scene;

import java.nio.ByteBuffer;

import com.jme3.math.Vector3f;

import static com.jme3.phonon.memory_layout.AUDIOSOURCE_LAYOUT.*;

public class DirectSoundPath {
    
    public final Vector3f direction=new Vector3f();
    public float distanceAttenuation;
    public final float airAbsorption[]=new float[3];
    public float propagationDelay;
    public float  occlusionFactor;
    public final float transmissionFactor[]=new float[3];
    public float directivityFactor;  

    protected DirectSoundPath(){

    }

    protected void readFrom(ByteBuffer MEMORY){
        direction.x=MEMORY.getFloat(DIRPATH_DIRECTIONX);
        direction.y=MEMORY.getFloat(DIRPATH_DIRECTIONY);
        direction.z=MEMORY.getFloat(DIRPATH_DIRECTIONZ);
    
        distanceAttenuation=MEMORY.getFloat(DIRPATH_DISTATT);
    
        airAbsorption[0]=MEMORY.getFloat(DIRPATH_AIRABSORP0);
        airAbsorption[1]=MEMORY.getFloat(DIRPATH_AIRABSORP1);
        airAbsorption[2]=MEMORY.getFloat(DIRPATH_AIRABSORP2);
    
        propagationDelay=MEMORY.getFloat(DIRPATH_PROPDELAY);
    
        occlusionFactor=MEMORY.getFloat(DIRPATH_OCCFACT);
    
        transmissionFactor[0]=MEMORY.getFloat(DIRPATH_TRANSFACT0);
        transmissionFactor[1]=MEMORY.getFloat(DIRPATH_TRANSFACT1);
        transmissionFactor[2]=MEMORY.getFloat(DIRPATH_TRANSFACT2);
    
        directivityFactor=MEMORY.getFloat(DIRPATH_DIRFACT);           
    }

    protected void writeTo(ByteBuffer MEMORY){
        MEMORY.putFloat(DIRPATH_DIRECTIONX,direction.x);
        MEMORY.putFloat(DIRPATH_DIRECTIONY,direction.y);
        MEMORY.putFloat(DIRPATH_DIRECTIONZ,direction.z);
     
        MEMORY.putFloat(DIRPATH_DISTATT,distanceAttenuation);

        MEMORY.putFloat(DIRPATH_AIRABSORP0,airAbsorption[0]);
        MEMORY.putFloat(DIRPATH_AIRABSORP1,airAbsorption[1]);
        MEMORY.putFloat(DIRPATH_AIRABSORP2,airAbsorption[2]);
    
        MEMORY.putFloat(DIRPATH_PROPDELAY,propagationDelay);
    
        MEMORY.putFloat(DIRPATH_OCCFACT,occlusionFactor);
    
        MEMORY.putFloat(DIRPATH_TRANSFACT0,transmissionFactor[0]);
        MEMORY.putFloat(DIRPATH_TRANSFACT1,transmissionFactor[1]);
        MEMORY.putFloat(DIRPATH_TRANSFACT2,transmissionFactor[2]);
      
        MEMORY.putFloat(DIRPATH_DIRFACT,directivityFactor);           
    }
}