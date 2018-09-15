package com.jme3.phonon.types;

import java.nio.ByteBuffer;
import com.jme3.math.Vector3f;

/**
 * VVector3f
 */
public class VFloat extends AbstractVolatileObject<Float,ByteBuffer> {
    public volatile float x;

    @Override
    public void onFinalizeUpdate(ByteBuffer out,int i) {
        out.putFloat(i, x);
    }

    @Override
    public void onUpdateFrom(Float v) {       
        x = v;       
    }

}