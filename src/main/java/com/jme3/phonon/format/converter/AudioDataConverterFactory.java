package com.jme3.phonon.format.converter;

public class AudioDataConverterFactory {
    /**
     * Factory method that returns a proper converter for the given sample size.
     * 
     * @param bitsPerSample Bits per sample
     * @return A proper converter from the given format to float32
     * 
     * @author aegroto
     */

    public static AudioDataConverter getConverter(int bitsPerSample) {
        switch(bitsPerSample) {
            case 8:
                return new Int8AudioDataConverter();
            case 16:
                return new Int16AudioDataConverter();
            case 24:
                return new Int24AudioDataConverter();
            default:
                return null;
        }
    }
}