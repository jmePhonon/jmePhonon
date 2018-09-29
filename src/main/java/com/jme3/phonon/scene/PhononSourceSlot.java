/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
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
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMode;
import com.jme3.phonon.types.CommitableMemoryObject;
import com.jme3.phonon.types.VByte;
import com.jme3.phonon.types.VFloat;
import com.jme3.phonon.types.VInteger;
import com.jme3.phonon.types.VVector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;

public class PhononSourceSlot extends CommitableMemoryObject{
    private final ByteBuffer MEMORY;
    
    private final VVector3f POS = new VVector3f();
    private final VByte CHANNELS = new VByte();
    private final VVector3f AHEAD = new VVector3f();
    private final VVector3f UP = new VVector3f();
    private final VVector3f RIGHT = new VVector3f();
    private final VFloat DWEIGHT = new VFloat();
    private final VFloat DPOWER = new VFloat();
    private final VFloat VOL = new VFloat();
    private final VFloat PIT = new VFloat();
    private final VByte DIROM = new VByte();
    private final VByte FLS=new VByte();

    private volatile PhononOutputLine connectedLine;
    private volatile AudioSource source;
    private volatile boolean isOver;
    private volatile boolean instance;
    private final int ID;

   
    public PhononSourceSlot(int id){
        ID=id;
        MEMORY=BufferUtils.createByteBuffer(SIZE);
      


    }

    public boolean isConnected() {
        return source!=null;
    }

    /**
     * Check if source is playing
     * @return true if source is playing or its status isn't finalized.
     */
    public boolean isPlaying() {
        int status=FLS.x;
        return (status&FLAG_PLAYING)==status;
    }

    public void setLine(PhononOutputLine line) {
        connectedLine = line;
    }

    public AudioSource getSource() {
        return source;
    }

   

    public boolean isInstance() {
        return instance;
    }

    public int getId() {
            return ID;
    }

    public void setSource(AudioSource src, boolean instance) {
        isOver=false; // reset isOver status until next native update
        // System.out.println(ID+" recycled");
        if(source!=null){
            if(getSource().getChannel()==ID){
                getSource().setStatus(Status.Stopped);

                getSource().setChannel(-1);
                if(src instanceof AudioNode){
                    System.out.println("Set "+((AudioNode)src).getName()+" to stopped");
                }

            }
            // else{
                // assert this.instance;
            // }
        }
        source=src;

        this.instance=false;

        if(src!=null){

            this.instance=instance;

            if(src instanceof AudioNode){
                AudioNode node=(AudioNode)src;
            }

            CHANNELS.setUpdateNeeded();
            FLS.setUpdateNeeded();
            POS.setUpdateNeeded();
            AHEAD.setUpdateNeeded();
            UP.setUpdateNeeded();
            RIGHT.setUpdateNeeded();
            DWEIGHT.setUpdateNeeded();
            DPOWER.setUpdateNeeded();
            VOL.setUpdateNeeded();
            PIT.setUpdateNeeded();
            DIROM.setUpdateNeeded();
           
            if(!instance){
                AudioSource.Status cs=src.getStatus();
                src.setStatus(AudioSource.Status.Stopped);
                src.setChannel(ID);
                src.setStatus(cs);
            }
            update(0);        
        }else{
            FLS.setUpdateNeeded();
            FLS.update((byte)(((int)FLS.x)&~(int)FLAG_PLAYING));
        }

    
        // System.out.println(ID+" attached");

    }
    
    public boolean isOver() {
        // if(recycled)System.out.println("Recycled :"+recycled);
        return isOver;
    }

    @Override
    public void onUpdate(float tpf) {
        if (source != null) {
            // if (isOver) {
            //   source.setStatus(Status.Stopped);
            // }
            CHANNELS.update((byte)source.getAudioData().getChannels());

            POS.update(source.getPosition());
            if(FLS.needUpdate){
                int f = 0;
                if (source.isPositional())
                    f |= FLAG_POSITIONAL;
                if (source.isDirectional())
                    f |= FLAG_DIRECTIONAL;
                if (source.getStatus()==Status.Playing||instance)
                    f |= FLAG_PLAYING;
                if (source.isLooping())
                    f |= FLAG_LOOP;
                if (source.isReverbEnabled()) 
                    f|=FLAG_REVERB;
                
                if(source instanceof AudioNode) {
                    AudioNode node = (AudioNode) source;
                    if(Phonon.getAudioNodeApplyAirAbsorption(node))
                        f |= FLAG_AIRABSORPTION;
                }

                FLS.update((byte) f);
            }

            AHEAD.update(source.getDirection());

            if(source instanceof AudioNode) {
                AudioNode node = (AudioNode) source;
                UP.update(node.getWorldRotation().getRotationColumn(1));
                RIGHT.update(node.getWorldRotation().getRotationColumn(0).negate());
                DWEIGHT.update(Phonon.getAudioNodeDipoleWeight(node));
                DPOWER.update(Phonon.getAudioNodeDipolePower(node));
                DIROM.update(Phonon.getAudioNodeDirectOcclusionMode(node));
            }

            VOL.update(source.getVolume());
            PIT.update(source.getPitch());
        }
    }

    @Override
    public void onCommit(float tpf) {
       
        POS.commit(MEMORY, POSX);
        AHEAD.commit(MEMORY, AHEADX);
        CHANNELS.commit(MEMORY,NUM_CHANNELS);
        if(source instanceof AudioNode) {
            UP.commit(MEMORY, UPX);
            RIGHT.commit(MEMORY, RIGHTX);
        }

        DWEIGHT.commit(MEMORY, DIPOLEWEIGHT);
        DPOWER.commit(MEMORY, DIPOLEPOWER);
        VOL.commit(MEMORY, VOLUME);
        PIT.commit(MEMORY, PITCH);
        DIROM.commit(MEMORY, DIROCCMODE);

        FLS.commit(MEMORY, FLAGS);

        int stopAt=MEMORY.getInt(STOPAT);
        isOver=stopAt!=-1&&connectedLine.getLastPlayedFrameId()>=stopAt;
       
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

    public void setPitchUpdateNeeded() {
        PIT.setUpdateNeeded();
    }

    public void setDirectOcclusionModeNeeded() {
        DIROM.setUpdateNeeded();
    }

    public long getDataAddress() {
        return DirectBufferUtils.getAddr(MEMORY);
    }
}