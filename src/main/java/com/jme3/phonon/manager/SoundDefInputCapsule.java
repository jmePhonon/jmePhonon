package com.jme3.phonon.manager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

import com.jme3.export.InputCapsule;
import com.jme3.export.Savable;
import com.jme3.phonon.scene.emitters.SoundEmitterControl;
import com.jme3.util.IntMap;

/**
 * SoundDefInputCapsule
 */
public class SoundDefInputCapsule implements InputCapsule{
    private final Map<String,Object> INPUT;
    private final SoundEmitterControl EMITTER;
    public SoundDefInputCapsule(SoundEmitterControl emitter,Map<String,Object> map){
        INPUT=map;
        EMITTER=emitter;
    }


    @Override
    public Savable readSavable(String name, Savable defVal) throws IOException {
        if(name.equals("audioKey")) return EMITTER.getAssetKey();
        else if(name.equals("spatial")) return EMITTER.getSpatial();
        else throw new UnsupportedOperationException("Can't read "+name);
    }

     @Override
    public int getSavableVersion(Class<? extends Savable> clazz) {
        return 0;
    }

    @Override
    public byte readByte(String name, byte defVal) throws IOException {
        return ((Number)INPUT.getOrDefault(name, defVal)).byteValue();
    }


    @Override
    public int readInt(String name, int defVal) throws IOException {
        return ((Number)INPUT.getOrDefault(name, defVal)).intValue();
    }
    @Override
    public float readFloat(String name, float defVal) throws IOException {
        return ((Number)INPUT.getOrDefault(name, defVal)).floatValue();
    }

    @Override
    public double readDouble(String name, double defVal) throws IOException {
        return ((Number)INPUT.getOrDefault(name, defVal)).doubleValue();
    }

    @Override
    public long readLong(String name, long defVal) throws IOException {
        return ((Number)INPUT.getOrDefault(name, defVal)).longValue();
    }

    @Override
    public short readShort(String name, short defVal) throws IOException {
        return ((Number)INPUT.getOrDefault(name, defVal)).shortValue();
    }
    @Override
    public boolean readBoolean(String name, boolean defVal) throws IOException {
        return ((boolean)INPUT.getOrDefault(name, defVal));
    }

    @Override
    public String readString(String name, String defVal) throws IOException {
        return ((String)INPUT.getOrDefault(name, defVal));
    }

   


    // Unsupported


    @Override
    public byte[] readByteArray(String name, byte[] defVal) throws IOException {
        throw new UnsupportedOperationException();

    }

    @Override
    public byte[][] readByteArray2D(String name, byte[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] readIntArray(String name, int[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[][] readIntArray2D(String name, int[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

   
    @Override
    public float[] readFloatArray(String name, float[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[][] readFloatArray2D(String name, float[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] readDoubleArray(String name, double[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[][] readDoubleArray2D(String name, double[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] readLongArray(String name, long[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[][] readLongArray2D(String name, long[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public short[] readShortArray(String name, short[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public short[][] readShortArray2D(String name, short[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

  
    @Override
    public boolean[] readBooleanArray(String name, boolean[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean[][] readBooleanArray2D(String name, boolean[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

  

    @Override
    public String[] readStringArray(String name, String[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[][] readStringArray2D(String name, String[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BitSet readBitSet(String name, BitSet defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

 

    @Override
    public Savable[] readSavableArray(String name, Savable[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Savable[][] readSavableArray2D(String name, Savable[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList readSavableArrayList(String name, ArrayList defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList[] readSavableArrayListArray(String name, ArrayList[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList[][] readSavableArrayListArray2D(String name, ArrayList[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList<FloatBuffer> readFloatBufferArrayList(String name, ArrayList<FloatBuffer> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList<ByteBuffer> readByteBufferArrayList(String name, ArrayList<ByteBuffer> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<? extends Savable,? extends Savable> readSavableMap(String name, Map<? extends Savable,? extends Savable> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String,? extends Savable> readStringSavableMap(String name, Map<String,? extends Savable> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntMap<? extends Savable> readIntSavableMap(String name, IntMap<? extends Savable> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatBuffer readFloatBuffer(String name, FloatBuffer defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntBuffer readIntBuffer(String name, IntBuffer defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuffer readByteBuffer(String name, ByteBuffer defVal) throws IOException {
        throw new UnsupportedOperationException();
	}

	@Override
	public ShortBuffer readShortBuffer(String name, ShortBuffer defVal) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Enum<T>> T readEnum(String name, Class<T> enumType, T defVal) throws IOException {
		throw new UnsupportedOperationException();
	}

    
}