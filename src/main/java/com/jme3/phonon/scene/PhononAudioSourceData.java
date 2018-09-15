package com.jme3.phonon.scene;

import static com.jme3.phonon.memory_layout.AUDIOSOURCE_LAYOUT.*;
import static com.jme3.phonon.Phonon.*;

import java.nio.ByteBuffer;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.audio.AudioSource.Status;
import com.jme3.math.Vector3f;
import com.jme3.phonon.Phonon;
import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.types.VByte;
import com.jme3.phonon.types.VFloat;
import com.jme3.phonon.types.VVector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

public class PhononAudioSourceData {
    private final ByteBuffer MEMORY;
    private  PhononOutputLine connectedLine;
    private AudioSource source;
    
    private final VVector3f POS = new VVector3f();
    private final VByte CHANNELS = new VByte();
    private final VVector3f AHEAD = new VVector3f();
    private final VVector3f UP = new VVector3f();
    private final VVector3f RIGHT = new VVector3f();
    private final VFloat DWEIGHT = new VFloat();
    private final VFloat DPOWER = new VFloat();
    private final VFloat VOL = new VFloat();

    private final VByte FLS = new VByte();


    private volatile int stopAt;
    
    // private final boolean UPDATE_EVERYTHING = true;

    public PhononAudioSourceData() {
        MEMORY = BufferUtils.createByteBuffer(SIZE);
        FLS.updateFrom((byte) 0);
        CHANNELS.updateFrom((byte) 1);
        POS.updateFrom(Vector3f.ZERO);
        AHEAD.updateFrom(Vector3f.UNIT_Z);
        UP.updateFrom(Vector3f.UNIT_Y);
        RIGHT.updateFrom(Vector3f.UNIT_X);
        DWEIGHT.updateFrom(0f);
        DPOWER.updateFrom(0f);
        VOL.updateFrom(1f);

        MEMORY.putInt(STOPAT, -1);

        source = null;

        finalizeUpdate();
    }

    public void setLine(PhononOutputLine line) {
        connectedLine = line;
    }

    public AudioSource getSource() {
        return source;
    }

    private boolean isOver() {
        return stopAt!=-1&&connectedLine.getLastPlayedFrameId() >= stopAt;
    }

    public void setSource(AudioSource src) {
        source = src;
        if (src == null)
            return;

        if(src instanceof AudioNode) {
            AudioNode node = (AudioNode) src;
        }

        CHANNELS.setUpdateNeeded();
        CHANNELS.updateFrom((byte) src.getAudioData().getChannels());
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
        if (source != null) {
            if (isOver()) {
              source.setStatus(Status.Stopped);
            }

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
                if (source.isReverbEnabled()) 
                    f|=FLAG_REVERB;
                
                FLS.updateFrom((byte) f);
            }

            AHEAD.updateFrom(source.getDirection());

            if(source instanceof AudioNode) {
                AudioNode node = (AudioNode) source;
                UP.updateFrom(node.getWorldRotation().getRotationColumn(1));
                RIGHT.updateFrom(node.getWorldRotation().getRotationColumn(0).negate());
                DWEIGHT.updateFrom(Phonon.getAudioNodeDipoleWeight(node));
                DPOWER.updateFrom(Phonon.getAudioNodeDipolePower(node));
            }

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

        stopAt = MEMORY.getInt(STOPAT);
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

    public void setDipolePowerUpdateNeeded() {
        DPOWER.setUpdateNeeded();
    }
    
    public void setDipoleWeightUpdateNeeded() {
        DWEIGHT.setUpdateNeeded();
    }
        
    public void setVolUpdateNeeded() {
        VOL.setUpdateNeeded();
    }

    public long getAddress() {
        return DirectBufferUtils.getAddr(MEMORY);
    }
}