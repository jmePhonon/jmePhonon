package com.jme3.phonon.types;

import java.nio.ByteBuffer;
import com.jme3.math.Vector3f;

/**
 * VVector3f
 */
public class VVector3f extends AbstractVolatileObject<Vector3f,ByteBuffer> {
    public volatile float x, y, z;
  
    @Override
    public void onFinalizeUpdate(ByteBuffer out,int i) {
        out.putFloat(i,x);
        out.putFloat(i+4,y);
        out.putFloat(i+4+4,z);        
    }

    @Override
    public void onUpdateFrom(Vector3f v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

}