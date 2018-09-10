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
    }

    public void finalizeUpdate() {
        POS.finalizeUpdate(MEMORY, POSX);
        AHEAD.finalizeUpdate(MEMORY, AHEADX);
        UP.finalizeUpdate(MEMORY, UPX);
        RIGHT.finalizeUpdate(MEMORY, RIGHTX);
        DWEIGHT.finalizeUpdate(MEMORY, DIPOLEWEIGHT);
        DPOWER.finalizeUpdate(MEMORY, DIPOLEPOWER);
        VOL.finalizeUpdate(MEMORY, VOLUME);
    }

    public void positionUpdate(AudioSource src) {
        POS.setUpdateNeeded();
        POS.updateFrom(src.getPosition());
    }

    public void directionUpdate(AudioSource src) {
        AHEAD.setUpdateNeeded();
        AHEAD.updateFrom(src.getDirection());
        
        if(src instanceof AudioNode) {
            AudioNode node = (AudioNode) src;
            UP.updateFrom(node.getWorldRotation().getRotationColumn(1));
            UP.setUpdateNeeded();
            RIGHT.updateFrom(node.getWorldRotation().getRotationColumn(0).negate());
            RIGHT.setUpdateNeeded();
        }
        
        // DWEIGHT and DPOWER update?
    }
        
    public void volumeUpdate(AudioSource src) {
        VOL.setUpdateNeeded();
        VOL.updateFrom(src.getVolume());
    }

    public long getAddress() {
        return DirectBufferUtils.getAddr(MEMORY);
    }
}