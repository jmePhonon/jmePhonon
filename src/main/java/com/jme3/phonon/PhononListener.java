package com.jme3.phonon;

import java.nio.ByteBuffer;
import com.jme3.audio.Listener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.util.BufferUtils;
import static com.jme3.phonon.memory_layout.LISTENER_LAYOUT.*;

/**
 * PhononListener
 */
public class PhononListener {
    public volatile boolean needNativeUpdate = false;
    private final ByteBuffer MEMORY;

    public volatile boolean posUpdate = false;
    public volatile float posX=0, posY=0, posZ=0;
    public volatile boolean rotUpdate = false;
    public volatile float dirX=0, dirY=0, dirZ=0, upX=0,upY=0,upZ=0;
    public volatile boolean velUpdate = false;
    public volatile float velX=0, velY=0, velZ=0;
    public volatile boolean volumeUpdate = false;
    public volatile float volume=0;

    boolean LIVE_ON_THE_EDGE = false,UPDATE_EVERYTHING=true;
    public PhononListener() {
        MEMORY = BufferUtils.createByteBuffer(LISTENER_size);
        memorySetPos(0,0,0);
        memorySetDirUp(0,0,0,0,0,0);
        memorySetVel(0, 0, 0);
        memorySetVolume(1);
    }

    private void memorySetPos(float x, float y, float z) {
        MEMORY.putFloat(POSX, x);
        MEMORY.putFloat(POSY, y);
        MEMORY.putFloat(POSZ, z);
    }
    
    private void memorySetDirUp(float dx, float dy, float dz, float upx, float upy, float upz) {
        MEMORY.putFloat(DIRX, dx);
        MEMORY.putFloat(DIRY, dy);
        MEMORY.putFloat(DIRZ, dz);
        MEMORY.putFloat(UPX, upx);
        MEMORY.putFloat(UPY, upy);
        MEMORY.putFloat(UPZ, upz);
    }
    
    private void memorySetVel(float velx, float vely, float velz) {
        MEMORY.putFloat(VELX, velx);
        MEMORY.putFloat(VELY, vely);
        MEMORY.putFloat(VELZ, velz);
    }
    
    private void memorySetVolume(float v) {
        MEMORY.putFloat(VOLUME, v);

    }
    
    

    public void updateNative() {
        if (LIVE_ON_THE_EDGE||needNativeUpdate) {
            // System.out.println("JListener position "+posX+" "+posY+" "+posZ);
            if (UPDATE_EVERYTHING||posUpdate) {
                memorySetPos(posX, posY, posZ);
                posUpdate = false;
            }
            if (UPDATE_EVERYTHING||rotUpdate) {
                memorySetDirUp(dirX, dirY, dirZ, upX, upY, upZ);
                rotUpdate = false;
            }
            if (UPDATE_EVERYTHING||velUpdate) {
                memorySetVel(velX,  velY, velZ);
                velUpdate = false;
            }

            if (UPDATE_EVERYTHING || volumeUpdate) {
                memorySetVolume(volume);
                volumeUpdate = false;
            }
            needNativeUpdate = false;
        }
    }

    public void update(Listener listener) {
        if (LIVE_ON_THE_EDGE || !needNativeUpdate) {
            if (UPDATE_EVERYTHING||posUpdate) {
            Vector3f pos = listener.getLocation();
            posX = pos.x;
            posY = pos.y;
            posZ = pos.z;
            }

            if (UPDATE_EVERYTHING||rotUpdate) {
            Vector3f dir = listener.getDirection();
            dirX = dir.x;
            dirY = dir.y;
            dirZ = dir.z;

            Vector3f up = listener.getUp();
            upX = up.x;
            upY = up.y;
            upZ = up.z;

            }

            if (UPDATE_EVERYTHING||velUpdate) {
            Vector3f vel = listener.getVelocity();
            velX = vel.x;
            velY = vel.y;
            velZ = vel.z;
            }

            if (UPDATE_EVERYTHING||volumeUpdate) {
            volume = listener.getVolume();
            }
            needNativeUpdate = UPDATE_EVERYTHING||volumeUpdate||velUpdate||rotUpdate||posUpdate;
        }
    }

    public void setPosUpdateNeeded() {
        posUpdate = true;
    }


    public void setRotUpdateNeeded() {
        rotUpdate = true;
    }

    public void setVelUpdateNeeded() {
        velUpdate = true;
    }

    public void setVolumeUpdateNeeded() {
        volumeUpdate = true;
    }

    public long getAddress() {
        return DirectBufferUtils.getAddr(MEMORY);
    }


}