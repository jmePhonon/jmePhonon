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
package com.jme3.phonon.utils;

import java.nio.ByteBuffer;

/**
 * BinUtils
 */
public class BitUtils{

    /**
    //  * Convert little endian bytes to int32 representing a float
    //  */
    // private static int leBytesToBEfloat_int(byte bytes[]) {
    //     return (int)((bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF)); // bigendian int32 (contains encoded float)
    // }

    private static void bEfloat_intToleBytes(int fbe_int, byte out_le[]) {
        out_le[0]=(byte)(fbe_int & 0xFF);
        out_le[1]=(byte)((fbe_int >> 8) & 0xFF);
        out_le[2]=(byte)((fbe_int >> 16) & 0xFF);
        out_le[3]=(byte)((fbe_int >> 24) & 0xFF);
    }

    // // Converters F LE to I LE
    // /**
    // * Convert little endian float32 to little endian int24
    // */
    // public static void cnvF32leToI24le(byte in_le[], byte out_le[], int offset) {
    //     int fbe_int=leBytesToBEfloat_int(in_le);
    //     float fbe=Float.intBitsToFloat(fbe_int); //bigendian float
    //     int ibe;
    //     if(fbe < 0){
    //         ibe=(int)(fbe * 8388608f); // bigendian int24 stored inside bigendian int32
    //     }else{
    //         ibe=(int)(fbe * 8388607f); // bigendian int24 stored inside bigendian int32
    //     }
    //     assert ibe >= -8388608f && ibe <= 8388607f:"sample exceeding [-8388608;8388607] range: " + ibe + " converted from " + fbe;
    //     out_le[offset + 0]=(byte)(ibe & 0xFF);
    //     out_le[offset + 1]=(byte)((ibe >> 8) & 0xFF);
    //     out_le[offset + 2]=(byte)((ibe >> 16) & 0xFF);
    // }

    // /**
    // * Convert little endian float32 to little endian int16
    // */
    // public static void cnvF32leToI16le(byte in_le[], byte out_le[], int offset) {
    //     int fbe_int=leBytesToBEfloat_int(in_le);
    //     float fbe=Float.intBitsToFloat(fbe_int); //bigendian float
    //     int sbe;
    //     if(fbe < 0){
    //         sbe=(short)(fbe * 32768f);
    //     }else{
    //         sbe=(short)(fbe * 32767f);
    //     }
    //     assert sbe >= -32768f && sbe <= 32767f:"sample exceeding [-32768;32767] range: " + sbe + " converted from " + fbe;
    //     out_le[offset + 0]=(byte)((sbe) & 0xFF);
    //     out_le[offset + 1]=(byte)((sbe >> 8) & 0xFF);

    // }

    // /**
    // * Convert little endian float32 to  int8
    // */
    // public static void cnvF32leToI8le(byte in_le[], byte out_le[], int offset) {
    //     int fbe_int=leBytesToBEfloat_int(in_le);
    //     float fbe=Float.intBitsToFloat(fbe_int); //bigendian float
    //     int sbe;
    //     if(fbe < 0){
    //         sbe=(byte)(fbe * 128);
    //     }else{
    //         sbe=(byte)(fbe * 127);
    //     }
    //     assert sbe >= -128 && sbe <= 127:"sample exceeding [-128;127] range: " + sbe + " converted from " + fbe;
    //     out_le[offset + 0]=(byte)sbe;
    // }

    // Converters I LE to F LE
    /**
     * Convert little endian int24 to little endian float32
     */
    public static void cnvI24leToF32le(byte in_le[], byte out_le[]) {
        int ibe=(int)((in_le[2] & 0xFF) << 16 | (in_le[1] & 0xFF) << 8 | (in_le[0] & 0xFF)); // bigendian int24 stored inside int32 (only the first 3 bytes on the right are used)

        // Extend sign to int32
        if((ibe & 0x00800000) > 0) ibe|=0xFF000000;

        float fbe;
        if(ibe < 0){
            fbe=(float)ibe / 8388608f;
        }else{
            fbe=(float)ibe / 8388607f;
        }
        assert fbe >= -1 && fbe <= 1:"sample exceeding [-1;1] range: " + fbe + " converted from " + ibe;
        int fbe_int=Float.floatToRawIntBits(fbe);//bigendian int32 representing float32
        bEfloat_intToleBytes(fbe_int,out_le);

    }

    /**
     * Convert little endian int16 to little endian float32
     */
    public static void cnvI16leToF32le(byte in_le[], byte out_le[]) {
        short sbe=(short)((in_le[1] & 0xFF) << 8 | (in_le[0] & 0xFF)); // bigendian short

        float fbe;
        if(sbe < 0){
            fbe=(float)sbe / 32768f; // bigendian float
        }else{
            fbe=(float)sbe / 32767f; // bigendian float
        }
        assert fbe >= -1 && fbe <= 1:"sample exceeding [-1;1] range: " + fbe + " converted from " + sbe;
        int fbe_int=Float.floatToRawIntBits(fbe);//bigendian int32 representing float32
        bEfloat_intToleBytes(fbe_int,out_le);
    }

    /**
     * Convert  int8 to  little endian float32
     */
    public static void cnvI8leToF32le(byte in_le[], byte out_le[]) {
        int n=in_le[0];
        float fbe;
        if(n < 0){
            fbe=(float)n / 128f; // bigendian float
        }else{
            fbe=(float)n / 127f; // bigendian float
        }
        assert fbe >= -1 && fbe <= 1:"sample exceeding [-1;1] range: " + fbe + " converted from " + n;
        int fbe_int=Float.floatToRawIntBits(fbe); //bigendian int32 representing float32        
        bEfloat_intToleBytes(fbe_int,out_le);
    }

    // Readers I/F LE    
    /**
     * Read little endian float32 from little endian buffer
     * @param le_bbf Little endian input buffer
     * @param le_out Little endian output array
     * @param n How many items to read.
     */
    public static void nextF32le(ByteBuffer le_bbf, byte[] le_out) {
        le_bbf.get(le_out,0,4);

    }

    /**
     * Read little endian int16 from little endian buffer
     */
    public static void nextI24le(ByteBuffer le_bbf, byte[] le_out) {
        le_bbf.get(le_out,0,3);

    }

  
    /**
     * Read little endian int16 from little endian buffer
     */
    public static void nextI16le(ByteBuffer le_bbf, byte[] le_out) {
        le_bbf.get(le_out, 0, 2);

    }


    /**
     * Read  int8 from  buffer
     */
    public static void nextI8le(ByteBuffer le_bbf, byte[] le_out) {
        le_out[0] = le_bbf.get();
    }
    
}