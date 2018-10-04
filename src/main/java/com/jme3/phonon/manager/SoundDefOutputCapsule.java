package com.jme3.phonon.manager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.util.IntMap;

/**
 * SoundDefOutputCapsule
 */
public class SoundDefOutputCapsule implements OutputCapsule{
    
    private final Map<String,Object> OUTPUT;

    public SoundDefOutputCapsule(Map<String,Object> map){
        OUTPUT=map;
    }
    
    @Override
    public void write(String value, String name, String defVal) throws IOException {
        OUTPUT.put(name,value);
    }
    @Override
    public void write(Savable object, String name, Savable defVal) throws IOException {
        // OUTPUT.put(name,object);
        // throw new UnsupportedOperationException();

    }
    @Override
    public void write(byte value, String name, byte defVal) throws IOException {
        OUTPUT.put(name,value);
    }

    @Override
    public void write(float value, String name, float defVal) throws IOException {
        OUTPUT.put(name,value);
    }

    @Override
    public void write(long value, String name, long defVal) throws IOException {
        OUTPUT.put(name,value);
    }

    @Override
    public void write(short value, String name, short defVal) throws IOException {
        OUTPUT.put(name,value);
    }

    @Override
    public void write(boolean value, String name, boolean defVal) throws IOException {
        OUTPUT.put(name,value);
    }

    @Override
    public void write(int value, String name, int defVal) throws IOException {
        OUTPUT.put(name,value);

    }


    // Unsupported


    @Override
    public void write(byte[] value, String name, byte[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[][] value, String name, byte[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

  
    @Override
    public void write(int[] value, String name, int[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int[][] value, String name, int[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

  

    @Override
    public void write(float[] value, String name, float[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(float[][] value, String name, float[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(double value, String name, double defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(double[] value, String name, double[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(double[][] value, String name, double[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

   
    @Override
    public void write(long[] value, String name, long[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(long[][] value, String name, long[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

 
    @Override
    public void write(short[] value, String name, short[] defVal) throws IOException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void write(short[][] value, String name, short[][] defVal) throws IOException {
        throw new UnsupportedOperationException();

    }

    

    @Override
    public void write(boolean[] value, String name, boolean[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(boolean[][] value, String name, boolean[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

   
    @Override
    public void write(String[] value, String name, String[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(String[][] value, String name, String[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(BitSet value, String name, BitSet defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

   

    @Override
    public void write(Savable[] objects, String name, Savable[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(Savable[][] objects, String name, Savable[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeSavableArrayList(ArrayList array, String name, ArrayList defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeSavableArrayListArray(ArrayList[] array, String name, ArrayList[] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeSavableArrayListArray2D(ArrayList[][] array, String name, ArrayList[][] defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeFloatBufferArrayList(ArrayList<FloatBuffer> array, String name, ArrayList<FloatBuffer> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeByteBufferArrayList(ArrayList<ByteBuffer> array, String name, ArrayList<ByteBuffer> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeSavableMap(Map<? extends Savable,? extends Savable> map, String name, Map<? extends Savable,? extends Savable> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeStringSavableMap(Map<String,? extends Savable> map, String name, Map<String,? extends Savable> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeIntSavableMap(IntMap<? extends Savable> map, String name, IntMap<? extends Savable> defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(FloatBuffer value, String name, FloatBuffer defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(IntBuffer value, String name, IntBuffer defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer value, String name, ByteBuffer defVal) throws IOException {
        throw new UnsupportedOperationException();
	}

	@Override
	public void write(ShortBuffer value, String name, ShortBuffer defVal) throws IOException {
        throw new UnsupportedOperationException();
    }

	@Override
	public void write(Enum value, String name, Enum defVal) throws IOException {
        throw new UnsupportedOperationException();
	}

    
}