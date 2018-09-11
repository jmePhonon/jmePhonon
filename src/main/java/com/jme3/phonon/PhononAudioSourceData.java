package com.jme3.phonon;

import static com.jme3.phonon.memory_layout.AUDIOSOURCE_LAYOUT.*;

import java.nio.ByteBuffer;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.audio.AudioSource.Status;
import com.jme3.math.Vector3f;
import com.jme3.phonon.mt.VByte;
import com.jme3.phonon.mt.VFloat;
import com.jme3.phonon.mt.VInteger;
import com.jme3.phonon.mt.VVector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

public class PhononAudioSourceData {
    private final ByteBuffer MEMORY;
    private AudioSource source;
    
    private final VVector3f POS = new VVector3f();
    private final VByte CHANNELS = new VByte();
    private final VVector3f AHEAD = new VVector3f();
    private final VVector3f UP = new VVector3f();
    private final VVector3f RIGHT = new VVector3f();
    private final VFloat DWEIGHT = new VFloat();
    private final VFloat DPOWER = new VFloat();
    private final VFloat VOL = new VFloat();

    private final VInteger FLS = new VInteger();


    
    
    // private final boolean UPDATE_EVERYTHING = true;

    public PhononAudioSourceData() {
        MEMORY = BufferUtils.createByteBuffer(SIZE);

        FLS.updateFrom(0);
        CHANNELS.updateFrom((byte)1);
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
        CHANNELS.setUpdateNeeded();
        CHANNELS.updateFrom((byte)src.getAudioData().getChannels());
        FLS.setUpdateNeeded();
        POS.setUpdateNeeded();
        AHEAD.setUpdateNeeded();
        UP.setUpdateNeeded();
        RIGHT.setUpdateNeeded();
        DWEIGHT.setUpdateNeeded();
        DPOWER.setUpdateNeeded();
        VOL.setUpdateNeeded();
        
    }

    public void update() {
        if(source != null) {
            POS.updateFrom(source.getPosition());
            if (FLS.needUpdate) {
                int f = 0;
                if (source.isPositional())
                    f |= FLAG_POSITIONAL;
                    if (source.isDirectional())
                    f |= FLAG_DIRECTIONAL;
                if (source.getStatus()==Status.Paused)
                    f |= FLAG_PAUSED;
                if (source.isLooping())
                    f |= FLAG_LOOP;
                FLS.updateFrom(f);
            }

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
        CHANNELS.finalizeUpdate(MEMORY,NUM_CHANNELS);
        if(source instanceof AudioNode) {
            UP.finalizeUpdate(MEMORY, UPX);
            RIGHT.finalizeUpdate(MEMORY, RIGHTX);
        }

        DWEIGHT.finalizeUpdate(MEMORY, DIPOLEWEIGHT);
        DPOWER.finalizeUpdate(MEMORY, DIPOLEPOWER);
        VOL.finalizeUpdate(MEMORY, VOLUME);

        FLS.finalizeUpdate(MEMORY, FLAGS);
    }

    public void setPosUpdateNeeded() {
        POS.setUpdateNeeded();
    }


    public void setFlagsUpdateNeeded() {
        FLS.setUpdateNeeded();
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