package unit;

import java.nio.ByteBuffer;

import com.jme3.phonon.DirectBufferUtils;

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