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
            setLastProcessedFrameId(bufferSize - 1);
            buffer.position(CHANNEL_LAYOUT.BODY);
            buffer.put(content);
            buffer.position(CHANNEL_LAYOUT.BODY);
        }
    }

    @Test
    public void testPlayerWriter() {
        final int sampleSizeBytes = 2;
        final int inputArraySize = 64, outputArraySize = (inputArraySize / 4) * sampleSizeBytes, lineSize = 4;        

        byte[] inputArray = {56, 98, 84, 45, 30, 88, 15, 64, 2, 1, 98, 62, 86, 96, 18, 21, 19, 16, 31, 15, 77, 33, 56, 10, 27, 62, 24, 41, 44, 41, 90, 84, 93, 78, 88, 90, 62, 62, 43, 43, 66, 87, 96, 96, 44, 98, 82, 52, 39, 5, 62, 18, 58, 67, 38, 49, 36, 53, 10, 92, 31, 22, 7, 25};
        byte[] outputArray = new byte[outputArraySize];

        /*for(int i = 0; i < inputArraySize; ++i) {
            inputArray[i] = (byte) FastMath.nextRandomInt(0, 100);
        }*/

        System.out.println("Input array: " + Arrays.toString(inputArray));

        PhononChannelWrapper channelWrapper = new PhononChannelWrapper(8 * sampleSizeBytes, inputArraySize, inputArray);
        PhononPlayerBuffer buffer = new PhononPlayerBuffer(8 * sampleSizeBytes, channelWrapper);

        byte[] convertedInputArray = new byte[outputArraySize];
        buffer.convertFloats(inputArray, convertedInputArray, 0);

        System.out.println("Converted input array: " + Arrays.toString(convertedInputArray));

        SourceDataLine dataLine = new SourceDataLine() {
            byte[] lineCache = new byte[lineSize];
            int outputOffset = 0, writtenLineBytes = 0, lastAvailableBytes = 0;

            @Override public int available() { 
                lastAvailableBytes = FastMath.nextRandomInt(0, lineSize - writtenLineBytes);
                return lastAvailableBytes;
            }

            @Override public int write(byte[] b, int off, int len) {
                assertTrue("Tried to write more bytes than available.", len <= lastAvailableBytes);

                System.out.println("Writing (" + off + ", " + len + "): " + Arrays.toString(b));

                for(int i = 0; i < len; ++i) {
                    lineCache[i] = b[off + i];
                }

                writtenLineBytes += len;
                
                System.out.println("Line cache is: " + Arrays.toString(lineCache) + "(" + writtenLineBytes + ")");
                return len;
            }

            @Override public void drain() {
                if(writtenLineBytes > 0) { 
                    for(int i = 0; i < writtenLineBytes; ++i) {
                        outputArray[outputOffset + i] = lineCache[i];
                    }

                    outputOffset += writtenLineBytes;
                    writtenLineBytes = 0;

                    System.out.println("Output array is: " + Arrays.toString(outputArray));
                }
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
            @Override public void open(AudioFormat format, int bufferSize) throws LineUnavailableException { }
            @Override public void open(AudioFormat format) throws LineUnavailableException { }
        };

        PhononPlayerWriter writer = new PhononPlayerWriter(dataLine, lineSize);

        int writtenBytes = 0;

        while(writtenBytes < outputArraySize) {
            int lastWrittenBytes = writer.writeFromBuffer(buffer);

            if(lastWrittenBytes > 0) {
                dataLine.drain();
                writtenBytes += lastWrittenBytes;

                System.out.println("Wrote " + writtenBytes + "/" + outputArraySize + " bytes");
            }
        }

        assertArrayEquals("Output differs from input:\n" + Arrays.toString(convertedInputArray) + "\n" + Arrays.toString(outputArray),
        convertedInputArray, outputArray);
    }
}