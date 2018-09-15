package com.jme3.phonon.format.encoder;

public class AudioDataEncoderFactory {

    /**
     * Factory method that returns a proper encoder for the given sample size.
     * 
     * @param bitsPerSample Bits per sample
     * @return A proper encoder from the given format to float32
     * 
     * @author aegroto
     */

    public static AudioDataEncoder getEncoder(int bitsPerSample) {
        switch(bitsPerSample) {
            case 8:
                return new Int8AudioDataEncoder();
            case 16:
                return new Int16AudioDataEncoder();
            case 24:
                return new Int24AudioDataEncoder();
         
            default:
                System.err.println("Unknown sample size: "+bitsPerSample);
                return null;
        }
    }
}