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

public class AUDIOSOURCE_LAYOUT {
    public static final boolean JNIEXPORT = true;

    public final static int FLAG_POSITIONAL=(1<<0);
    public final static int FLAG_DIRECTIONAL=(1<<1);
    public final static int FLAG_PLAYING=(1<<2);
    public final static int FLAG_LOOP=(1<<3);
    public final static int FLAG_REVERB=(1<<4);
    public final static int FLAG_AIRABSORPTION=(1<<5);
    public final static int FLAG_MARKED_FOR_DISCONNECTION=(1<<6);
    public final static int FLAG_HRTF=(1<<7);
    public final static int FLAG_USE_DIRECTPATH_FUNCTION=(1<<8);

    public static final byte FLAGS_fieldsize = 1;
    public static final byte NUM_CHANNELS_fieldsize = 1;
    public static final byte DIROCCMODE_fieldsize = 1;
    public static final byte DIROCCMETHOD_fieldsize = 1;
    
    public static final byte SOURCERADIUS_fieldsize = 4;

    public static final byte POSX_fieldsize = 4;
    public static final byte POSY_fieldsize = 4;
    public static final byte POSZ_fieldsize = 4;
 
    public static final byte AHEADX_fieldsize = 4;
    public static final byte AHEADY_fieldsize = 4;
    public static final byte AHEADZ_fieldsize = 4;

    public static final byte UPX_fieldsize = 4;
    public static final byte UPY_fieldsize = 4;
    public static final byte UPZ_fieldsize = 4;
    
    public static final byte RIGHTX_fieldsize = 4;
    public static final byte RIGHTY_fieldsize = 4;
    public static final byte RIGHTZ_fieldsize = 4;

    public static final byte DIPOLEWEIGHT_fieldsize = 4;
    public static final byte DIPOLEPOWER_fieldsize = 4;
    
    public static final byte VOLUME_fieldsize = 4;
    public static final byte PITCH_fieldsize = 4;


    // Direct path mem
    public static final byte DIRPATH_DIRECTIONX_fieldsize = 4;
    public static final byte DIRPATH_DIRECTIONY_fieldsize = 4;
    public static final byte DIRPATH_DIRECTIONZ_fieldsize = 4;

    public static final byte DIRPATH_DISTATT_fieldsize = 4;

    public static final byte DIRPATH_AIRABSORP0_fieldsize = 4;
    public static final byte DIRPATH_AIRABSORP1_fieldsize = 4;
    public static final byte DIRPATH_AIRABSORP2_fieldsize = 4;

    public static final byte DIRPATH_PROPDELAY_fieldsize = 4;

    public static final byte DIRPATH_OCCFACT_fieldsize = 4;

    public static final byte DIRPATH_TRANSFACT0_fieldsize = 4;
    public static final byte DIRPATH_TRANSFACT1_fieldsize = 4;
    public static final byte DIRPATH_TRANSFACT2_fieldsize = 4;

    public static final byte DIRPATH_DIRFACT_fieldsize = 4;
    //// 
        
    public static final byte FLAGS = 0;
    public static final byte NUM_CHANNELS = FLAGS + FLAGS_fieldsize;
    public static final byte DIROCCMODE = NUM_CHANNELS + NUM_CHANNELS_fieldsize;
    public static final byte DIROCCMETHOD = DIROCCMODE + DIROCCMODE_fieldsize;

    public static final byte SOURCERADIUS = DIROCCMETHOD + DIROCCMETHOD_fieldsize; 
    
    public static final byte POSX = SOURCERADIUS + SOURCERADIUS_fieldsize; 
    public static final byte POSY = POSX+POSX_fieldsize;
    public static final byte POSZ = POSY+POSY_fieldsize;
    
    public static final byte AHEADX = POSZ+POSZ_fieldsize; 
    public static final byte AHEADY = AHEADX+AHEADX_fieldsize; 
    public static final byte AHEADZ = AHEADY + AHEADY_fieldsize;
    
    public static final byte UPX = AHEADZ+AHEADZ_fieldsize; 
    public static final byte UPY = UPX+UPX_fieldsize; 
    public static final byte UPZ = UPY+UPY_fieldsize; 

    public static final byte RIGHTX = UPZ+UPZ_fieldsize;
    public static final byte RIGHTY = RIGHTX+RIGHTX_fieldsize;
    public static final byte RIGHTZ = RIGHTY+RIGHTY_fieldsize;

    public static final byte DIPOLEWEIGHT = RIGHTZ+RIGHTZ_fieldsize; 
    public static final byte DIPOLEPOWER = DIPOLEWEIGHT+DIPOLEWEIGHT_fieldsize;

    public static final byte VOLUME = DIPOLEPOWER + DIPOLEPOWER_fieldsize;
    
    public static final byte PITCH = VOLUME+VOLUME_fieldsize;


    // Dir path
    public static final byte DIRPATH_DIRECTIONX = PITCH+PITCH_fieldsize;
    public static final byte DIRPATH_DIRECTIONY = DIRPATH_DIRECTIONX+DIRPATH_DIRECTIONX_fieldsize;
    public static final byte DIRPATH_DIRECTIONZ = DIRPATH_DIRECTIONY+DIRPATH_DIRECTIONY_fieldsize;

    public static final byte DIRPATH_DISTATT = DIRPATH_DIRECTIONZ+DIRPATH_DIRECTIONZ_fieldsize;

    public static final byte DIRPATH_AIRABSORP0 = DIRPATH_DISTATT+DIRPATH_DISTATT_fieldsize;
    public static final byte DIRPATH_AIRABSORP1 = DIRPATH_AIRABSORP0+DIRPATH_AIRABSORP0_fieldsize;
    public static final byte DIRPATH_AIRABSORP2 = DIRPATH_AIRABSORP1+DIRPATH_AIRABSORP1_fieldsize;

    public static final byte DIRPATH_PROPDELAY = DIRPATH_AIRABSORP2+DIRPATH_AIRABSORP2_fieldsize;

    public static final byte DIRPATH_OCCFACT = DIRPATH_PROPDELAY+DIRPATH_PROPDELAY_fieldsize;

    public static final byte DIRPATH_TRANSFACT0 = DIRPATH_OCCFACT+DIRPATH_OCCFACT_fieldsize;
    public static final byte DIRPATH_TRANSFACT1 = DIRPATH_TRANSFACT0+DIRPATH_TRANSFACT0_fieldsize;
    public static final byte DIRPATH_TRANSFACT2 = DIRPATH_TRANSFACT1+DIRPATH_TRANSFACT1_fieldsize;

    public static final byte DIRPATH_DIRFACT = DIRPATH_TRANSFACT2+DIRPATH_TRANSFACT2_fieldsize;
    //// 

    public static final byte SIZE = DIRPATH_DIRFACT+DIRPATH_DIRFACT_fieldsize;
} 