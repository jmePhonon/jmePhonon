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

/**
 * PhononMaterials
 */
public final class PhononMaterialPresets {

    // Values from: https://valvesoftware.github.io/steam-audio/doc/capi/struct_i_p_l_material.html#details
    public static final PhononMaterial generic=new PhononMaterial("generic",0.10f,0.20f,0.30f,0.05f,0.100f,0.050f,0.030f);
    public static final PhononMaterial brick=new PhononMaterial("brick",0.03f,0.04f,0.07f,0.05f,0.015f,0.015f,0.015f);
    public static final PhononMaterial concrete=new PhononMaterial("concrete",0.05f,0.07f,0.08f,0.05f,0.015f,0.002f,0.001f);
    public static final PhononMaterial ceramic=new PhononMaterial("ceramic",0.01f,0.02f,0.02f,0.05f,0.060f,0.044f,0.011f);
    public static final PhononMaterial gravel=new PhononMaterial("gravel",0.60f,0.70f,0.80f,0.05f,0.031f,0.012f,0.008f);
    public static final PhononMaterial carpet=new PhononMaterial("carpet",0.24f,0.69f,0.73f,0.05f,0.020f,0.005f,0.003f);
    public static final PhononMaterial glass=new PhononMaterial("glass",0.06f,0.03f,0.02f,0.05f,0.060f,0.044f,0.011f);
    public static final PhononMaterial plaster=new PhononMaterial("plaster",0.12f,0.06f,0.04f,0.05f,0.056f,0.056f,0.004f);
    public static final PhononMaterial wood=new PhononMaterial("wood",0.11f,0.07f,0.06f,0.05f,0.070f,0.014f,0.005f);
    public static final PhononMaterial metal=new PhononMaterial("metal",0.20f,0.07f,0.06f,0.05f,0.200f,0.025f,0.010f);
    public static final PhononMaterial rock=new PhononMaterial("rock",0.13f,0.20f,0.24f,0.05f,0.015f,0.002f,0.001f);
    

}