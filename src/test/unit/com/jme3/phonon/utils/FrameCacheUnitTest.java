package com.jme3.phonon.utils;

import java.nio.ByteBuffer;

import com.jme3.phonon.utils.FrameCache;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * FrameCacheUnitTest
 */
public class FrameCacheUnitTest extends TestCase{

    @Test
    public void testFrameCache() {
        int tests = 10;
        int frames=800;
        int framesize=1024;
        int cached = 8;
        byte frame1[] = new byte[framesize];


        for (int t = 0; t < tests; t++) {
            int frameLoaded = 0;

            // Initialize random input
            ByteBuffer bbf = ByteBuffer.allocate(framesize * frames);
            for (int i = 0; i < bbf.limit(); i++)
                bbf.put(i, (byte) (Math.random() * Byte.MAX_VALUE));

            FrameCache cache = new FrameCache(cached, framesize);

            // Preload frames
            for (int i = 0; i < framesize; i++) {
                bbf.get(frame1);
                // System.out.println("Load frame " + i);
                frameLoaded++;
                boolean full = cache.loadFrame(frame1);
                if (full)
                    break;
            }

            int readedBytes = 0;

            while (readedBytes < bbf.limit()) {
                // System.out.println("Readed bytes " + readedBytes);
                // System.out.println("Limit " + bbf.limit());

                byte frameToRead[] = new byte[(int) (Math.random() * (framesize)) + 1];
                // System.out.println("Read " + frameToRead.length + " bytes");
                // load new frame

   if (cache.readNextFrame(sourceLineWrapper, sourceLineWrapper.available())) {
                FrameCacheUnitTestSourceDataLineWrapper sourceLineWrapper = new FrameCacheUnitTestSourceDataLineWrapper(frameToRead);
                if (cache.readNextFrame(sourceLineWrapper)) {

                    int remaining = bbf.limit() - bbf.position();
                    if (remaining > frame1.length)
                        remaining = frame1.length;

                    bbf.get(frame1, 0, remaining);
                    cache.loadFrame(frame1);
                    // System.out.println("Load next frame " + (frameLoaded++));
                }
                for (int j = 0; j < frameToRead.length; j++) {
                    byte readByte = frameToRead[j];
                    byte expectedByte = bbf.get(readedBytes);

                    // System.out.println(readByte + " read and " + expectedByte + " was expected");
                    assertEquals(readByte + " read but " + expectedByte + " was expected", readByte, expectedByte);
                    readedBytes++;
                    if (readedBytes == bbf.limit()) {
                        System.out.println("Source is over");
                        break;
                    }
                }
            }
            assertEquals("There are some unread bytes", readedBytes, bbf.limit());

        }
    }

}