package com.jme3.phonon;

import java.nio.ByteBuffer;
import com.jme3.audio.Listener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.mt.VFloat;
import com.jme3.phonon.mt.VVector3f;
import com.jme3.util.BufferUtils;
import static com.jme3.phonon.memory_layout.LISTENER_LAYOUT.*;

/**
 * PhononListener
 */
public class PhononListener {
    public volatile boolean needNativeUpdate = false;
    private final ByteBuffer MEMORY;

    private final VVector3f POS = new VVector3f();
    private final VVector3f DIR = new VVector3f();
    private final VVector3f UP = new VVector3f();
    private final VVector3f VEL = new VVector3f();
    private final VFloat VOL = new VFloat();

    /**debug */
    final boolean LIVE_ON_THE_EDGE = false, UPDATE_EVERYTHING = true;
    
    public PhononListener() {
        MEMORY = BufferUtils.createByteBuffer(LISTENER_size);
        // initialization
        
        POS.updateFrom(Vector3f.ZERO);
        DIR.updateFrom(Vector3f.UNIT_Z);
        UP.updateFrom(Vector3f.UNIT_Y);
        VEL.updateFrom(Vector3f.ZERO);
        VOL.updateFrom(1f);
        finalizeUpdate();
    }

    

    public void finalizeUpdate() {
        POS.finalizeUpdate(MEMORY, POSX);
        DIR.finalizeUpdate(MEMORY,DIRX);
        VEL.finalizeUpdate(MEMORY,VELX);
        VOL.finalizeUpdate(MEMORY, VOLUME);
        UP.finalizeUpdate(MEMORY,UPX);

    }

    public void update(Listener listener) {
        if(UPDATE_EVERYTHING){
            POS.setUpdateNeeded();
            DIR.setUpdateNeeded();
            VEL.setUpdateNeeded();
            VOL.setUpdateNeeded();
            UP.setUpdateNeeded();            
        }
        POS.updateFrom(listener.getLocation());
        DIR.updateFrom(listener.getDirection());
        VEL.updateFrom(listener.getVelocity());
        VOL.updateFrom(listener.getVolume());
        UP.updateFrom(listener.getUp());
    }

    public void setPosUpdateNeeded() {
        POS.setUpdateNeeded();
       
    }


    public void setRotUpdateNeeded() {
        DIR.setUpdateNeeded();
        UP.setUpdateNeeded();

    }

    public void setVelUpdateNeeded() {
        VEL.setUpdateNeeded();
    }

    public void setVolumeUpdateNeeded() {
        VOL.setUpdateNeeded();
    }

    public long getAddress() {
        return DirectBufferUtils.getAddr(MEMORY);
    }


}