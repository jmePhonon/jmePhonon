package com.jme3.phonon;

import java.util.Arrays;

import org.junit.Test;
import junit.framework.TestCase;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.Line.Info;

import com.jme3.phonon.player.PhononPlayerWriter;

import com.jme3.math.FastMath;

public class PhononPlayerWriterUnitTest extends TestCase {

    @Test
    public void testPlayerWriter() {
        final int arraySize = 8, lineSize = 3;

        byte[] inputArray = new byte[arraySize];
        byte[] outputArray = new byte[arraySize];

        for(int i = 0; i < arraySize; ++i) {
            inputArray[i] = (byte) FastMath.nextRandomInt(0, 100);
        }

        System.out.println ("Input array: " + Arrays.toString(inputArray));

        PhononPlayerWriter writer = new PhononPlayerWriter(new SourceDataLine() {
            @Override public int available() { 
                return FastMath.nextRandomInt(0, lineSize);
            }

            @Override public int write(byte[] b, int off, int len) { 
                return 0;
            }
            
            @Override public void removeLineListener(LineListener listener) { }        
            @Override public void open() throws LineUnavailableException { }        
            @Override public boolean isOpen() { return false; }        
            @Override public boolean isControlSupported(Type control) { return false; }
            @Override public Info getLineInfo() { return null; }        
            @Override public Control[] getControls() { return null; }        
            @Override public Control getControl(Type control) { return null;}
            @Override public void close() { }
            @Override public void addLineListener(LineListener listener) { }
            @Override public void stop() { }
            @Override public void start() { }
            @Override public boolean isRunning() { return false; }
            @Override public boolean isActive() { return false; }
            @Override public long getMicrosecondPosition() { return 0; }
            @Override public long getLongFramePosition() { return 0; }
            @Override public float getLevel() { return 0; }
            @Override public int getFramePosition() { return 0; }
            @Override public AudioFormat getFormat() { return null; }
            @Override public int getBufferSize() { return 0; }
            @Override public void flush() { }
            @Override public void drain() { }
            @Override public void open(AudioFormat format, int bufferSize) throws LineUnavailableException { }
            @Override public void open(AudioFormat format) throws LineUnavailableException { }
        }, lineSize);
    }
}