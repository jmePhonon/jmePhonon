package com.jme3.phonon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * NtiveBufferUtils
 */
public class DirectBufferUtils {

    public static long getAddr(ByteBuffer directBuffer) {
        if (!directBuffer.isDirect()) {
            throw new UnsupportedOperationException("Can't get native address from a Non direct buffer");
        }
        try {
            Method m = directBuffer.getClass().getMethod("address");
            m.setAccessible(true);
            return (long) m.invoke(directBuffer);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}