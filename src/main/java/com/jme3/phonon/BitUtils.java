package com.jme3.phonon;

import java.nio.ByteBuffer;

/**
 * BinUtils
 */
public class BitUtils {

    /**
     * Convert little endian bytes to int32 representing a float
     */
    private static int leBytesToBEfloat_int(byte bytes[]) {
        return (int) ((bytes[3] & 0xFF) << 24
         | (bytes[2] & 0xFF) << 16
         | (bytes[1] & 0xFF) << 8 
         | (bytes[0] & 0xFF)); // bigendian int32 (contains encoded float)
    }
    
    private static void  bEfloat_intToleBytes(int fbe_int,byte out_le[]) {
        out_le[0] = (byte)(fbe_int & 0xFF);
        out_le[1] = (byte)((fbe_int >> 8) & 0xFF);
        out_le[2] = (byte)((fbe_int >> 16) & 0xFF);
        out_le[3] = (byte) ((fbe_int >> 24) & 0xFF);
    }
    

    // Converters F LE to I LE
    /**
    * Convert little endian float32 to little endian int24
    */
    public static void cnvF32leToI24le(byte in_le[], byte out_le[]) {
        int fbe_int = leBytesToBEfloat_int(in_le);
        float fbe = Float.intBitsToFloat(fbe_int); //bigendian float
        int ibe = (int) (fbe * 8388607f); // bigendian int24 stored inside bigendian int32

        out_le[0] = (byte) (ibe & 0xFF);
        out_le[1] = (byte) ((ibe >> 8) & 0xFF);
        out_le[2] = (byte) ((ibe >> 16) & 0xFF);
    }
    
    /**
    * Convert little endian float32 to little endian int16
    */
    public static void cnvF32leToI16le(byte in_le[], byte out_le[]) {
        int fbe_int = leBytesToBEfloat_int(in_le);
        float fbe = Float.intBitsToFloat(fbe_int); //bigendian float
        short sbe = (short) (fbe * Short.MAX_VALUE);
        out_le[0] = (byte) ((sbe) & 0xFF);
        out_le[1] = (byte) ((sbe  >> 8) & 0xFF);
        
    }

    /**
    * Convert little endian float32 to  int8
    */
    public static void cnvF32leToI8le(byte in_le[], byte out_le[]) {
        int fbe_int = leBytesToBEfloat_int(in_le);
        float fbe = Float.intBitsToFloat(fbe_int); //bigendian float

        byte sbe = (byte) (fbe * Byte.MAX_VALUE);
        out_le[0] = sbe;
    }

    
    // Converters I LE to F LE
    /**
     * Convert little endian int24 to little endian float32
     */
    public static void cnvI24leToF32le(byte in_le[],byte out_le[]) {
        int ibe = (int) ((in_le[2]&0xFF) << 16 | (in_le[1]&0xFF) << 8 |( in_le[0] & 0xFF)); // bigendian int24 stored inside int32 (only the first 3 bytes on the right are used)
        
        // Extend sign to int32
        if ((ibe & 0x00800000) > 0) ibe |= 0xFF000000;
        
        float fbe = (float) ibe / 8388607f;
        int fbe_int = Float.floatToRawIntBits(fbe);//bigendian int32 representing float32
 
        
        bEfloat_intToleBytes(fbe_int, out_le);

    }


    /**
     * Convert little endian int16 to little endian float32
     */
    public static void cnvI16leToF32le(byte in_le[],byte out_le[]) {
        short sbe =(short)((in_le[1]&0xFF) << 8 | (in_le[0]&0xFF)); // bigendian short
        float fbe = (float) sbe / Short.MAX_VALUE; // bigendian float
        int fbe_int=Float.floatToRawIntBits(fbe);//bigendian int32 representing float32
        bEfloat_intToleBytes(fbe_int, out_le);
    }
    
    

    /**
     * Convert  int8 to  little endian float32
     */
    public static void cnvI8leToF32le(byte in_le[],byte out_le[]) {
        byte n =   in_le[0];
        float fbe = (float) n / Byte.MAX_VALUE; // bigendian float
        int fbe_int = Float.floatToRawIntBits(fbe); //bigendian int32 representing float32        
        bEfloat_intToleBytes(fbe_int, out_le);
    }
   


    // Readers I/F LE    
    /**
     * Read little endian float32 from little endian buffer
     * @param le_bbf Little endian input buffer
     * @param le_out Little endian output array
     * @param n How many items to read.
     */
    public static void nextF32le(ByteBuffer le_bbf, byte[] le_out, int n) {
        le_bbf.get(le_out, 0, 4*n);
        
        // le_out[0] = le_bbf.get();
        // le_out[1] = le_bbf.get();
        // le_out[2] = le_bbf.get();
        // le_out[3]=le_bbf.get();
    }


    /**
     * Read little endian int24 from little endian buffer
     */
    public static void nextI24le(ByteBuffer le_bbf, byte[] le_out,int n) {
        le_bbf.get(le_out, 0, 3*n);

        // le_out[0] = le_bbf.get();
        // le_out[1] = le_bbf.get();
        // le_out[2]=le_bbf.get();
    }


    /**
     * Read little endian int16 from little endian buffer
     */
    public static void nextI16le(ByteBuffer le_bbf, byte[] le_out,int n) {
        le_bbf.get(le_out, 0, 2*n);

        // le_out[0] = le_bbf.get();
        // le_out[1]=le_bbf.get();
    }


    /**
     * Read  int8 from  buffer
     */
    public static void nextI8le(ByteBuffer le_bbf, byte[] le_out) {
        le_out[0] = le_bbf.get();
    }
    

   /**
     * Read little endian float32 from little endian buffer
     */
    public static void nextF32le(ByteBuffer le_bbf, byte[] le_out) {
        nextF32le(le_bbf, le_out, 1);
    }


    /**
     * Read little endian int24 from little endian buffer
     */
    public static void nextI24le(ByteBuffer le_bbf, byte[] le_out) {
        nextI24le(le_bbf, le_out, 1);

    }


    /**
     * Read little endian int16 from little endian buffer
     */
    public static void nextI16le(ByteBuffer le_bbf, byte[] le_out) {
        nextI16le(le_bbf, le_out, 1);
    }

    
}