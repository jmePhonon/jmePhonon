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
package com.jme3.phonon.memory_layout;

/**
 * LISTENER_LAYOUT
 */
public class LISTENER_LAYOUT {
    public static final boolean JNIEXPORT = true;

    public static final byte POSX_fieldsize=4;
    public static final byte POSY_fieldsize=4;
    public static final byte POSZ_fieldsize=4;
 
    public static final byte DIRX_fieldsize=4;
    public static final byte DIRY_fieldsize=4;
    public static final byte DIRZ_fieldsize = 4;
    

    public static final byte UPX_fieldsize=4;
    public static final byte UPY_fieldsize=4;
    public static final byte UPZ_fieldsize=4;

    public static final byte VELX_fieldsize=4;
    public static final byte VELY_fieldsize=4;
    public static final byte VELZ_fieldsize = 4;
    
    public static final byte VOLUME_fieldsize=4;

    public static final byte POSX = 0; 
    public static final byte POSY = POSX+POSX_fieldsize;
    public static final byte POSZ = POSY+POSY_fieldsize;
    
    public static final byte DIRX = POSZ+POSZ_fieldsize; 
    public static final byte DIRY= DIRX+DIRX_fieldsize; 
    public static final byte DIRZ = DIRY + DIRY_fieldsize;
    
    public static final byte UPX = DIRZ+DIRZ_fieldsize; 
    public static final byte UPY = UPX+UPX_fieldsize; 
    public static final byte UPZ = UPY+UPY_fieldsize; 

    public static final byte VELX = UPZ+UPZ_fieldsize; 
    public static final byte VELY = VELX+VELX_fieldsize; 
    public static final byte VELZ = VELY+VELY_fieldsize;

    public static final byte VOLUME = VELZ+VELZ_fieldsize;

    public static final byte LISTENER_size=VOLUME+VOLUME_fieldsize;

}