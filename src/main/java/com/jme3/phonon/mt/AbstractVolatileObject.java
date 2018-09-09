package com.jme3.phonon.mt;

import com.sun.corba.se.impl.ior.ByteBuffer;

/**
 * AbstractVolatileObject
 */
public abstract class AbstractVolatileObject<A,B> implements VolatileObject<A,B>{

    private volatile boolean needUpdate=true;
    private volatile boolean needUpdateFinalization;

    @Override
    public void setUpdateNeeded() {
        needUpdate = true;
    }


    @Override
    public void finalizeUpdate(B out,int i) {
        if (!needUpdateFinalization)
            return;
        onFinalizeUpdate(out, i);          
        needUpdateFinalization = false;
    }
    public abstract void onFinalizeUpdate(B out, int i);

    @Override
    public void updateFrom(A v) {
        if (!needUpdate || needUpdateFinalization) 
            return;
        

        onUpdateFrom(v);
        needUpdate = false;
        needUpdateFinalization = true;
    }
    
    public abstract void onUpdateFrom(A out);

}