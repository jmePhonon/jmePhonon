package com.jme3.phonon.format.decoder;

public class AudioDataDecoderFactory {
    /**
     * Factory method to initialize a proper PlayerConverter
     * Warning: Converters are not thread safe
     * 
     * @param sampleSize Sample's size
     * 
     * @return Proper format.decoder for given sampleSize 
     */

    public static AudioDataDecoder getAudioDataDecoder(int sampleSize) {
        switch(sampleSize) {
            case 8:
                return Int8AudioDataDecoder.instance();
            case 16:
                return Int16AudioDataDecoder.instance();
            case 24:
                return Int24AudioDataDecoder.instance();
            default:
                return null;
        }
    }
}