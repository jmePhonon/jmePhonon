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
    final static int TEST_COUNT = 1;

    final int sampleSizeBytes = 1;
    final int inputArraySize = 128, outputArraySize = (inputArraySize / 4) * sampleSizeBytes, lineSize = 4;

    class PhononChannelWrapper extends PhononChannel {
        PhononChannelWrapper(int frameSize, int bufferSize, byte[] content) {
            super(frameSize, bufferSize);            
            setLastProcessedFrameId(bufferSize - 1);
            buffer.position(CHANNEL_LAYOUT.BODY);
            buffer.put(content);
            buffer.position(CHANNEL_LAYOUT.BODY);

            /*byte[] testBuffer = new byte[inputArraySize];
            buffer.get(testBuffer);
            System.out.println("Channel wrapper initialized (" + testBuffer.length + "): " + Arrays.toString(testBuffer));*/
        }
    }

    @Test
    public void testPlayerWriter() {
        for(int t = 0; t < TEST_COUNT; ++t) {
            byte[] inputArray =  {72, 90, 8, 91, 80, 22, 51, 48, 69, 83, 56, 60, 48, 9, 19, 58, 31, 0, 63, 58, 16, 32, 31, 92, 50, 80, 17, 25, 94, 45, 32, 78, 91, 0, 95, 55, 69, 18, 56, 35, 43, 67, 79, 22, 2, 82, 13, 59, 59, 45, 86, 85, 64, 53, 13, 22, 19, 89, 20, 45, 49, 82, 87, 97, 6, 97, 4, 44, 45, 94, 15, 80, 50, 75, 20, 94, 11, 51, 75, 41, 77, 30, 25, 50, 92, 25, 29, 18, 53, 55, 79, 34, 58, 51, 39, 65, 42, 91, 81, 38, 100, 39, 63, 21, 89, 80, 82, 31, 70, 13, 79, 18, 41, 93, 98, 19, 14, 95, 66, 98, 78, 92, 48, 76, 37, 73, 51, 44};
            // byte[] inputArray = new byte[inputArraySize];
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
                int outputOffset = 0, writtenLineBytes = 0;

                @Override public int available() { 
                    return lineSize - writtenLineBytes;
                }

                @Override public int write(byte[] b, int off, int len) {
                    assertTrue("Tried to write more bytes than available.", len <= available());

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
}