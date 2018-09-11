package com.jme3.phonon.mt;

import java.nio.ByteBuffer;

/**
 * VByte
 */
public class VByte extends AbstractVolatileObject<Byte,ByteBuffer> {
    public volatile byte x;

    @Override
    public void onFinalizeUpdate(ByteBuffer out,int i) {
        out.put(i, x);
    }

    @Override
    public void onUpdateFrom(Byte v) {       
        x = v;       
    }


    
}