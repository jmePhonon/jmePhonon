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

import com.jme3.math.FastMath;

public class PhononPlayerInternalsUnitTest extends TestCase {
    final static int TEST_COUNT = 20000;

    final int sampleSizeBytes = 3;
    final int inputArraySize = 256, outputArraySize = (inputArraySize / 4) * sampleSizeBytes, lineSize = 32;

    class PhononChannelWrapper extends PhononChannel {
        PhononChannelWrapper(int frameSize, int bufferSize, byte[] content) {
            super(frameSize, bufferSize);            
            setLastProcessedFrameId(bufferSize - 1);
            buffer.position(CHANNEL_LAYOUT.BODY);
            buffer.put(content);
            // buffer.position(CHANNEL_LAYOUT.BODY);

            /*byte[] testBuffer = new byte[inputArraySize];
            buffer.get(testBuffer);
            System.out.println("Channel wrapper initialized (" + testBuffer.length + "): " + Arrays.toString(testBuffer));*/
        }
    }

    @Test
    public void testPlayerWriter() {
        for(int t = 0; t < TEST_COUNT; ++t) {
            // byte[] inputArray = {75, 16, 43, 94, 66, 94, 96, 82, 75, 61, 70, 6, 62, 91, 78, 55, 67, 27, 79, 0, 64, 80, 53, 3, 63, 8, 71, 46, 7, 87, 50, 33, 24, 19, 52, 67, 14, 46, 39, 92, 43, 78, 3, 84, 33, 62, 9, 72, 50, 72, 39, 45, 76, 5, 21, 26, 47, 48, 23, 62, 75, 95, 99, 24};
            byte[] inputArray = new byte[inputArraySize];
            byte[] outputArray = new byte[outputArraySize];

            for(int i = 0; i < inputArraySize; ++i) {
                inputArray[i] = (byte) FastMath.nextRandomInt(0, 100);
            }

            // System.out.println("Input array: " + Arrays.toString(inputArray));

            PhononChannelWrapper channelWrapper = new PhononChannelWrapper(8 * sampleSizeBytes, inputArraySize, inputArray);
            PhononPlayerBuffer buffer = new PhononPlayerBuffer(8 * sampleSizeBytes, channelWrapper);

            byte[] convertedInputArray = new byte[outputArraySize];
            buffer.convertFloats(inputArray, convertedInputArray, 0);

            // System.out.println("Converted input array: " + Arrays.toString(convertedInputArray));

            SourceDataLine dataLine = new SourceDataLine() {
                byte[] lineCache = new byte[lineSize];
                int outputOffset = 0, writtenLineBytes = 0;

                @Override public int available() { 
                    return lineSize - writtenLineBytes;
                }

                @Override public int write(byte[] b, int off, int len) {
                    assertTrue("Tried to write more bytes than available.", len <= available());

                    // System.out.println("Writing (" + off + ", " + len + "): " + Arrays.toString(b));

                    for(int i = 0; i < len; ++i) {
                        lineCache[writtenLineBytes + i] = b[off + i];
                    }

                    writtenLineBytes += len;
                    
                    // System.out.println("Line cache is: " + Arrays.toString(lineCache) + "(" + writtenLineBytes + ")");
                    return len;
                }

                @Override public void drain() {
                    if(writtenLineBytes > 0) { 
                        for(int i = 0; i < writtenLineBytes; ++i) {
                            outputArray[outputOffset + i] = lineCache[i];
                        }

                        outputOffset += writtenLineBytes;
                        writtenLineBytes = 0;

                        // System.out.println("Output array is: " + Arrays.toString(outputArray));
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

            // PhononPlayerWriter writer = new PhononPlayerWriter(dataLine, lineSize);

            int writtenBytes = 0;

            while(writtenBytes < outputArraySize) {
                int lastWrittenBytes = buffer.write(dataLine);

                if(lastWrittenBytes > 0) {
                    dataLine.drain();
                    writtenBytes += lastWrittenBytes;

                    // System.out.println("Wrote " + writtenBytes + "/" + outputArraySize + " bytes");
                }
            }

            assertArrayEquals("Output differs from input:\n" + Arrays.toString(convertedInputArray) + "\n" + Arrays.toString(outputArray) + "\n" +
            "Input array was: " + Arrays.toString(inputArray),
            convertedInputArray, outputArray);
        }
    }
}