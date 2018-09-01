package com.jme3.phonon;

import static org.junit.Assert.assertArrayEquals;

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

import com.jme3.phonon.player.PhononPlayerBuffer;
import com.jme3.phonon.player.PhononPlayerWriter;

import com.jme3.math.FastMath;

public class PhononPlayerWriterUnitTest extends TestCase {

    class PhononChannelWrapper extends PhononChannel {
        PhononChannelWrapper(int frameSize, int bufferSize, byte[] content) {
            super(frameSize, bufferSize);
            buffer.put(content);
            buffer.putInt(CHANNEL_LAYOUT.LAST_PROCESSED_FRAME, bufferSize - 1);
        }
    }

    @Test
    public void testPlayerWriter() {
        final int arraySize = 8, lineSize = 3;

        byte[] inputArray = new byte[arraySize];
        byte[] outputArray = new byte[arraySize];

        for(int i = 0; i < arraySize; ++i) {
            inputArray[i] = (byte) FastMath.nextRandomInt(0, 100);
        }

        PhononChannelWrapper channelWrapper = new PhononChannelWrapper(1024, 2048, inputArray);
        PhononPlayerBuffer buffer = new PhononPlayerBuffer(24, channelWrapper);
        PhononPlayerWriter writer = new PhononPlayerWriter(new SourceDataLine() {
            int lastAvailableBytes = 0;

            @Override public int available() { 
                lastAvailableBytes = FastMath.nextRandomInt(0, lineSize);
                return lastAvailableBytes;
            }

            @Override public int write(byte[] b, int off, int len) {
                assertTrue("Tried to write more bytes than available.", len <= lastAvailableBytes);

                for(int i = 0; i < len; ++i) {
                    outputArray[off + i] = b[i];
                }

                return len;
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

        int writtenBytes = 0;

        while(writtenBytes < arraySize) {
            writtenBytes += writer.writeFromBuffer(buffer);
        }

        assertArrayEquals("Output differs from input: " + Arrays.toString(inputArray) + " -- " + Arrays.toString(outputArray),
        inputArray, outputArray);
    }
}