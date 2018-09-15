package com.jme3.phonon.types;



/**
 * VolatileObject
 */
public interface VolatileObject<A,B> {  
    public void setUpdateNeeded();
    public void finalizeUpdate(B out,int i);
    public void updateFrom(A v);
}