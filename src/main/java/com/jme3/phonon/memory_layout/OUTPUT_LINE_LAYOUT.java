package com.jme3.phonon.memory_layout;

/**
 * CHANNEL_LAYOUT
 * Layout of output channel,
 */
public class OUTPUT_LINE_LAYOUT {
    public static final boolean JNIEXPORT = true;

    /** Size in bytes of every field */
    public static final byte UNUSED_fieldsize=12;
    public static final byte LAST_PROCESSED_FRAME_fieldsize=4;
    public static final byte LAST_PLAYED_FRAME_fieldsize=4;
    public static final byte HEADER_size = UNUSED_fieldsize + LAST_PROCESSED_FRAME_fieldsize + LAST_PLAYED_FRAME_fieldsize;

    // Header
    // Position of the first byte of each field
    public static final byte UNUSED = 0; 
    public static final byte LAST_PROCESSED_FRAME = UNUSED_fieldsize; 
    public static final byte LAST_PLAYED_FRAME = UNUSED_fieldsize+LAST_PROCESSED_FRAME_fieldsize;

    // Body
    // Position of the first byte of body
    public static final byte BODY = HEADER_size; // ...
    
 
}