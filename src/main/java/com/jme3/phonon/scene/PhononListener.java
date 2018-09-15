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

import java.nio.ByteBuffer;
import com.jme3.audio.Listener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.types.VFloat;
import com.jme3.phonon.types.VVector3f;
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