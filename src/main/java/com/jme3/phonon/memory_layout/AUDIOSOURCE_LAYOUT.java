package com.jme3.phonon.memory_layout;

public class AUDIOSOURCE_LAYOUT {
    public static final boolean JNIEXPORT = true;

    public final static int FLAG_POSITIONAL=(1<<0);
    public final static int FLAG_DIRECTIONAL=(1<<1);
    public final static int FLAG_PAUSED=(1<<2);
    public final static int FLAG_LOOP=(1<<3);

    public static final byte FLAGS_fieldsize = 1;
    public static final byte NUM_CHANNELS_fieldsize = 1;
    public static final byte UNUSED0_fieldsize = 1;
    public static final byte UNUSED1_fieldsize = 1;

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

    public static final byte FLAGS = 0;
    public static final byte NUM_CHANNELS = FLAGS + FLAGS_fieldsize;
    public static final byte UNUSED0 = NUM_CHANNELS + NUM_CHANNELS_fieldsize;
    public static final byte UNUSED1 = UNUSED0 + UNUSED0_fieldsize;

    public static final byte POSX = UNUSED1 + UNUSED1_fieldsize; 
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