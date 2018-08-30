package com.jme3.phonon.utils;

import java.nio.ByteBuffer;


import org.junit.Test;

import junit.framework.TestCase;

/**
 * DirectBufferUtilsUnitTest
 */
public class DirectBufferUtilsUnitTest extends TestCase {


    @Test
    public void testGetAddr() {
        ByteBuffer bbf=ByteBuffer.allocateDirect(10);
        long addr = DirectBufferUtils.getAddr(bbf);
        System.out.println("Addr is " + addr);
        assertTrue("Addr is undefined",addr!= -1l);
    }
}