package com.jme3.phonon;


import java.nio.ByteBuffer;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;

import static com.jme3.phonon.memory_layout.AUDIOSOURCE_LAYOUT.*;

public class PhononAudioSourcesData {
    private final ByteBuffer[] MEMORIES;

    public PhononAudioSourcesData(int nOutputLines, int nSourcesPerLine) {
        int nTotalSource = nOutputLines * nSourcesPerLine;

        MEMORIES = new ByteBuffer[nTotalSource];
        for(int i = 0; i < nTotalSource; ++i) {
            MEMORIES[i] = BufferUtils.createByteBuffer(SIZE);
            initializeMemory(MEMORIES[i]);
        }
    }

    public void initializeMemory(ByteBuffer memory) {
        setMemoryPosition(memory, Vector3f.ZERO);
        setMemoryAhead(memory, Vector3f.ZERO);
        setMemoryUp(memory, Vector3f.UNIT_Y);
        setMemoryRight(memory, Vector3f.UNIT_X);
    }

    public void updateSourcePosition(AudioSource src) {
        int index = src.getChannel();
        Vector3f position = src.getPosition();
        setMemoryPosition(MEMORIES[index], position);
    }    

    public void updateSourceDirection(AudioSource src) {
        int index = src.getChannel();
        Vector3f ahead = src.getDirection();

        setMemoryAhead(MEMORIES[index], ahead);

        if(src instanceof AudioNode) {
            updateSourceUp((AudioNode) src);
            updateSourceRight((AudioNode) src);
        }
    }

    public void updateSourceUp(AudioNode node) {
        int index = node.getChannel();
        Vector3f up = node.getWorldRotation().getRotationColumn(1);
        setMemoryUp(MEMORIES[index], up);        
    }

    public void updateSourceRight(AudioNode node) {
        int index = node.getChannel();
        Vector3f right = node.getWorldRotation().getRotationColumn(0).negate();
        setMemoryRight(MEMORIES[index], right);
    }

    private void setMemoryPosition(ByteBuffer memory, Vector3f position) {
        memory.putFloat(POSX, position.x);
        memory.putFloat(POSY, position.y);
        memory.putFloat(POSZ, position.z);
    }

    private void setMemoryAhead(ByteBuffer memory, Vector3f ahead) {
        memory.putFloat(AHEADX, ahead.x);
        memory.putFloat(AHEADY, ahead.y);
        memory.putFloat(AHEADZ, ahead.z);
    }

    private void setMemoryUp(ByteBuffer memory, Vector3f up) {
        memory.putFloat(UPX, up.x);
        memory.putFloat(UPY, up.y);
        memory.putFloat(UPZ, up.z);
    }

    private void setMemoryRight(ByteBuffer memory, Vector3f right) {
        memory.putFloat(RIGHTX, right.x);
        memory.putFloat(RIGHTY, right.y);
        memory.putFloat(RIGHTZ, right.z);
    }

    public ByteBuffer[] getMemoryBuffers() {
        return MEMORIES;
    }
}