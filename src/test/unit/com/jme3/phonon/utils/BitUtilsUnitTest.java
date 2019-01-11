/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package com.jme3.phonon.utils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BiConsumer;


import org.junit.Test;


import junit.framework.TestCase;

/**
 * BinUtilsUnitTest
 */
public class BitUtilsUnitTest extends TestCase {
   
    private static int leBytesToBEfloat_int(byte bytes[]) {
        return (int) ((bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF)); // bigendian int32 (contains encoded float)
    }

    public static float bytesLEToFloatBE(byte bytes[]) {
        return Float.intBitsToFloat(leBytesToBEfloat_int(bytes));
    }

    private void compareData(String name ,int r,byte data[], byte data2[]) {
        for (int i = 0; i < data2.length; i++) {
            // System.out.println(data2[i] + " readed, " + data[i] + " expected");
            assertEquals("Error in test "+name+" n"+r+","+data2[i]+" readed, but "+data[i]+" was expected",data[i], data2[i]);
        }
    }

    private void runTestFor(String name,Object inputs[][], long maxValuePos,long maxValueNeg, int dataSize, BiConsumer<ByteBuffer, byte[]> reader,
            BiConsumer<byte[], byte[]> converter1, BiConsumer<byte[], byte[]> converter2

    ) {
        int r = 0;
        for (Object[] inputo : inputs) {
            long inputValue = (long) inputo[0];
            // System.out.println("Run TestN" + (r++) + " " + name + " with inputValue=" + inputValue
            //         + " maxValue=" + maxValue + " dataSize=" + dataSize);
            ByteBuffer inputBuffer = (ByteBuffer) inputo[1];

            byte data[] = new byte[dataSize];
            byte data2[] = new byte[dataSize];
            byte float_data[] = new byte[4];

            reader.accept(inputBuffer, data);
            converter1.accept(data, float_data);

            float expectedFloat =inputValue >0? (float) inputValue / maxValuePos:(float) inputValue /maxValueNeg;
            float convertedFloat = bytesLEToFloatBE(float_data);
            // System.out.println("Expected float: " + expectedFloat + " Converted float: " + convertedFloat);
            assertEquals("Error in test "+name+" n"+r+", Expected float: " + expectedFloat + " but converted float is: " + convertedFloat,expectedFloat, convertedFloat);

            converter2.accept(float_data, data2);
            compareData(name,r,data, data2);
        }
    }

    private void runRandomTestFor(String name,int ntests, int maxValuePos,int maxValueNeg, int dataSize, BiConsumer<ByteBuffer, byte[]> reader,
            BiConsumer<byte[], byte[]> converter1, BiConsumer<byte[], byte[]> converter2

    ) {
        Object inputs[][] = new Object[ntests][2];
        for (int i = 0; i < ntests; i++) {
            long value = (long) (Math.random() * (maxValuePos +maxValueNeg + 1) - maxValueNeg);

            ByteBuffer byte_valuebbf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(value);
            byte_valuebbf.rewind().position(8 - dataSize);
            byte byte_value[] = new byte[dataSize];
            byte_valuebbf.get(byte_value);

            byte byte_value_le[] = new byte[dataSize];
            int k = 0;
            for (int j = dataSize - 1; j >= 0; j--) {
                byte_value_le[k++] = byte_value[j];
            }
            ByteBuffer bval = ByteBuffer.wrap(byte_value_le).order(ByteOrder.LITTLE_ENDIAN);

            inputs[i] = new Object[] { value, bval };
        }

        runTestFor(name,inputs, maxValuePos,maxValueNeg, dataSize, reader, converter1, converter2);

    }

    @Test
    public void testI8Conversion() {
        runRandomTestFor("i8 Conversion Test",1000, Byte.MAX_VALUE,-Byte.MIN_VALUE, 1, BitUtils::nextI8le, BitUtils::cnvI8leToF32le, BitUtils::cnvF32leToI8le);
    }

    @Test
    public void testI16Conversion() {
        runRandomTestFor("i16 Conversion Test",1000,  Short.MAX_VALUE,- Short.MIN_VALUE, 2, BitUtils::nextI16le, BitUtils::cnvI16leToF32le, BitUtils::cnvF32leToI16le);
    }

    @Test
    public void testI24Conversion() {
        runRandomTestFor("i24 Conversion Test",1000, 8388607,8388608, 3, BitUtils::nextI24le, BitUtils::cnvI24leToF32le, BitUtils::cnvF32leToI24le);
    }

}