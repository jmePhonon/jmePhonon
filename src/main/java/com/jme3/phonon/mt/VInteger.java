package com.jme3.phonon.mt;

import java.nio.ByteBuffer;

/**
 * VInteger
 */
public class VInteger extends AbstractVolatileObject<Integer,ByteBuffer> {
    public volatile int x;

    @Override
    public void onFinalizeUpdate(ByteBuffer out,int i) {
        out.putInt(i, x);
    }

    @Override
    public void onUpdateFrom(Integer v) {       
        x = v;       
    }


    
}