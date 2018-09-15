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