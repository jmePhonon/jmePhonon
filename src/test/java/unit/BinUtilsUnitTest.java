package unit;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BiConsumer;

import com.jme3.steamaudio.BinUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;


import junit.framework.TestCase;

/**
 * BinUtilsUnitTest
 */
public class BinUtilsUnitTest extends TestCase {
   
    private static int leBytesToBEfloat_int(byte bytes[]) {
        return (int) ((bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF)); // bigendian int32 (contains encoded float)
    }

    public static float bytesLEToFloatBE(byte bytes[]) {
        return Float.intBitsToFloat(leBytesToBEfloat_int(bytes));
    }

    private void compareData(byte data[], byte data2[]) {
        for (int i = 0; i < data2.length; i++) {
            System.out.println(data2[i] + " readed, " + data[i] + " expected");
            assertEquals("Error: "+data2[i]+" readed, but "+data[i]+" was expected",data[i], data2[i]);
        }
    }

    private void runTestFor(String name,Object inputs[][], long maxValue, int dataSize, BiConsumer<ByteBuffer, byte[]> reader,
            BiConsumer<byte[], byte[]> converter1, BiConsumer<byte[], byte[]> converter2

    ) {
        int r = 0;
        for (Object[] inputo : inputs) {
            long inputValue = (long) inputo[0];
            System.out.println("Run TestN" + (r++) + " " + name + " with inputValue=" + inputValue
                    + " maxValue=" + maxValue + " dataSize=" + dataSize);
            ByteBuffer inputBuffer = (ByteBuffer) inputo[1];

            byte data[] = new byte[dataSize];
            byte data2[] = new byte[dataSize];
            byte float_data[] = new byte[4];

            reader.accept(inputBuffer, data);
            converter1.accept(data, float_data);

            float expectedFloat = (float) inputValue / maxValue;
            float convertedFloat = bytesLEToFloatBE(float_data);
            System.out.println("Expected float: " + expectedFloat + " Converted float: " + convertedFloat);
            assertEquals(expectedFloat, convertedFloat);

            converter2.accept(float_data, data2);
            compareData(data, data2);
        }
    }

    private void runRandomTestFor(String name,int ntests, int maxValue, int dataSize, BiConsumer<ByteBuffer, byte[]> reader,
            BiConsumer<byte[], byte[]> converter1, BiConsumer<byte[], byte[]> converter2

    ) {
        Object inputs[][] = new Object[ntests][2];
        for (int i = 0; i < ntests; i++) {
            long value = (long) (Math.random() * (maxValue * 2 + 1) - maxValue);

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

        runTestFor(name,inputs, maxValue, dataSize, reader, converter1, converter2);

    }

    @Test
    public void i8ConversionTest() {
        int maxValue = Byte.MAX_VALUE;
        runRandomTestFor("i8 Conversion Test",1000, maxValue, 1, BinUtils::nextI8le, BinUtils::cnvI8leToF32le, BinUtils::cnvF32leToI8le);
    }

    @Test
    public void i16ConversionTest() {
        int maxValue = Short.MAX_VALUE;
        runRandomTestFor("i16 Conversion Test",1000, maxValue, 2, BinUtils::nextI16le, BinUtils::cnvI16leToF32le, BinUtils::cnvF32leToI16le);
    }

    @Test
    public void i24ConversionTest() {
        int maxValue = 8388607;
        runRandomTestFor("i24 Conversion Test",1000, maxValue, 3, BinUtils::nextI24le, BinUtils::cnvI24leToF32le, BinUtils::cnvF32leToI24le);
    }

}