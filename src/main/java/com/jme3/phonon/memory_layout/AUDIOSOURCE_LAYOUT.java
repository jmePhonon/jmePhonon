package com.jme3.phonon.memory_layout;

public class AUDIOSOURCE_LAYOUT {
    public static final boolean JNIEXPORT = true;

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

    public static final byte POSX = 0; 
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
    
    public static final byte SIZE = VOLUME+VOLUME_fieldsize;
} 