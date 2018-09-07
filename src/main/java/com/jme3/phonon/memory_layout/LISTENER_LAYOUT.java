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