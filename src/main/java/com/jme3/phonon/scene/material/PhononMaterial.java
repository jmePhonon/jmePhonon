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
package com.jme3.phonon.scene.material;

import java.nio.ByteBuffer;

/**
 * PhononMaterial
 */
public class PhononMaterial{
    private final float DATA[]=new float[7];
    public static final int SERIALIZED_SIZE=4*7;

    // public final float lowFreqAbsorption;
    // public final float midFreqAbsorption;
    // public final float highFreqAbsorption;
    // public final float scattering;
    // public final float lowFreqTransmission;
    // public final float midFreqTransmission;
    // public final float highFreqTransmission;
    private final String NAME;
    
    public String getName() {
        return NAME;
    }

    public float getLowFreqAbsorption() {
        return DATA[0];
    }
    public float getMidFreqAbsorption() {
        return DATA[1];
    }
    public float getHighFreqAbsorption() {
        return DATA[2];
    }

    public float getScattering() {
        return DATA[3];
    }
    public float getLowFreqTransmission() {
        return DATA[4];
    }
    public float getMidFreqTransmission() {
        return DATA[5];
    }

    public float getHighFreqTransmission() {
        return DATA[6];
    }

    
    public PhononMaterial(String name,
            float lowFreqAbsorption,
            float midFreqAbsorption,
            float highFreqAbsorption,
            float scattering,
            float lowFreqTransmission,
            float midFreqTransmission,
            float highFreqTransmission){
        NAME=name;
        int i=0;
        DATA[i++]=lowFreqAbsorption;
        DATA[i++]=midFreqAbsorption;
        DATA[i++]=highFreqAbsorption;
        DATA[i++]=scattering;
        DATA[i++]=lowFreqTransmission;
        DATA[i++]=midFreqTransmission;
        DATA[i++]=highFreqTransmission;
    }
    
    public PhononMaterial(String name,float... values){
        NAME=name;
        for(int i=0;i<values.length;i++){
            DATA[i]=values[i];
        }      
    }
    
    public void serialize(ByteBuffer bbf) {
        for(float f:DATA){
            bbf.putFloat(f);
        }
    }
    
    public void deserialize(ByteBuffer bbf) {
        for(int i=0;i<DATA.length;i++){
            DATA[i]=bbf.getFloat();
        }
    }
    
}