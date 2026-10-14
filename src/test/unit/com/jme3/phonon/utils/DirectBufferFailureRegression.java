package com.jme3.phonon.utils;

import java.nio.ByteBuffer;

/** Standalone regression: run on Java 8 and a strongly encapsulated JDK. */
public class DirectBufferFailureRegression {
    public static void main(String[] args) {
        boolean modern = !System.getProperty("java.specification.version").startsWith("1.");
        try {
            DirectBufferUtils.getAddr(ByteBuffer.allocate(16));
            throw new AssertionError("Heap buffer accepted");
        } catch (UnsupportedOperationException expected) {
            // Native addresses are supported only for direct buffers.
        }
        try {
            long address = DirectBufferUtils.getAddr(ByteBuffer.allocateDirect(16));
            if (address == 0) {
                throw new AssertionError("Null direct-buffer address");
            }
            System.out.println("DIRECT_ADDRESS_OK");
        } catch (IllegalStateException expected) {
            Throwable cause = expected.getCause();
            if (!modern || cause == null || !cause.getClass().getName()
                    .equals("java.lang.reflect.InaccessibleObjectException")) {
                throw expected;
            }
            System.out.println("RECOVERABLE_ACCESS_ERROR cause=" + cause.getClass().getName());
        }
        System.out.println("CALLER_SURVIVED");
    }
}
