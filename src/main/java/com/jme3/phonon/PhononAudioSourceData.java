package com.jme3.phonon;

import static com.jme3.phonon.memory_layout.AUDIOSOURCE_LAYOUT.*;

import java.nio.ByteBuffer;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.Vector3f;
import com.jme3.phonon.mt.VFloat;
import com.jme3.phonon.mt.VVector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

public class PhononAudioSourceData {
    public volatile boolean needNativeUpdate = false;
    private final ByteBuffer MEMORY;
    private AudioSource source;
    
    private final VVector3f POS = new VVector3f();
    private final VVector3f AHEAD = new VVector3f();
    private final VVector3f UP = new VVector3f();
    private final VVector3f RIGHT = new VVector3f();
    private final VFloat DWEIGHT = new VFloat();
    private final VFloat DPOWER = new VFloat();
    private final VFloat VOL = new VFloat();

    // private final boolean UPDATE_EVERYTHING = true;

    public PhononAudioSourceData() {
        MEMORY = BufferUtils.createByteBuffer(SIZE);

  
        POS.updateFrom(Vector3f.ZERO);
        AHEAD.updateFrom(Vector3f.UNIT_Z);
        UP.updateFrom(Vector3f.UNIT_Y);
        RIGHT.updateFrom(Vector3f.UNIT_X);
        DWEIGHT.updateFrom(0f);
        DPOWER.updateFrom(0f);
        VOL.updateFrom(1f);

        source = null;

        finalizeUpdate();
    }

    public void setSource(AudioSource src) {
        source = src;
    }

    public void update() {
        if(source != null) {
            POS.updateFrom(source.getPosition());
            AHEAD.updateFrom(source.getDirection());

            if(source instanceof AudioNode) {
                UP.updateFrom(((AudioNode) source).getWorldRotation().getRotationColumn(1));
                RIGHT.updateFrom(((AudioNode) source).getWorldRotation().getRotationColumn(0).negate());
            }

            // DWEIGHT and DPOWER update?
            VOL.updateFrom(source.getVolume());
        }
    }

    public void finalizeUpdate() {
        POS.finalizeUpdate(MEMORY, POSX);
        AHEAD.finalizeUpdate(MEMORY, AHEADX);

        if(source instanceof AudioNode) {
            UP.finalizeUpdate(MEMORY, UPX);
            RIGHT.finalizeUpdate(MEMORY, RIGHTX);
        }

        DWEIGHT.finalizeUpdate(MEMORY, DIPOLEWEIGHT);
        DPOWER.finalizeUpdate(MEMORY, DIPOLEPOWER);
        VOL.finalizeUpdate(MEMORY, VOLUME);
    }

    public void setPosUpdateNeeded() {
        POS.setUpdateNeeded();
    }

    public void setDirUpdateNeeded() {
        AHEAD.setUpdateNeeded();
        
        if(source instanceof AudioNode) {
            UP.setUpdateNeeded();
            RIGHT.setUpdateNeeded();
        }
    }
        
    public void setVolUpdateNeeded() {
        VOL.setUpdateNeeded();
    }

    public long getAddress() {
        return DirectBufferUtils.getAddr(MEMORY);
    }
}