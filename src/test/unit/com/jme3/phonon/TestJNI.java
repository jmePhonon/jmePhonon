package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.LineUnavailableException;

import com.jme3.phonon.desktop_javasound.JavaSoundPhononSettings;

import org.junit.Test;

import junit.framework.TestCase;

public class TestJNI extends TestCase {
    


    @Test
    public void testJNIPassthrough () throws Exception {

        int bufferSize = 800;
        int frameSize = 1024;
        ByteBuffer bbf = ByteBuffer.allocateDirect(frameSize*bufferSize * 4);
        bbf.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < frameSize*bufferSize; i++) {
            bbf.putFloat((float) Math.random());
        }
        JavaSoundPhononSettings settings = new JavaSoundPhononSettings();
        settings.passThrough = true;
        settings.initPlayers=false;
        settings.outputSampleSize=16;
        settings.bufferSize=bufferSize;
        settings.frameSize=frameSize;
        settings.nOutputLines=1;
        settings.nSourcesPerLine=1;
        settings.nOutputChannels=1;
        
        PhononRenderer renderer=Phonon.init(settings,null);
   
        
        // renderer.preInit();        
        renderer.connectSourceRaw(bbf.limit()/4, bbf);


        PhononOutputLine chan = renderer.getLine(0);

        ByteBuffer tmpout_cnv = ByteBuffer.allocateDirect(frameSize*4).order(ByteOrder.LITTLE_ENDIAN);
        byte tmpout[] = new byte[frameSize*4];

        for (int i = 0; i < bufferSize; i++) {
            renderer.updateNative();
            chan.readNextFrameForPlayer(tmpout);
            tmpout_cnv.rewind();
            tmpout_cnv.put(tmpout);
            tmpout_cnv.rewind();
            for (int j = 0; j < frameSize; j++) {

                float result = tmpout_cnv.getFloat();
                bbf.position((i * frameSize + j) * 4);

                float expected_result = bbf.getFloat();
                // System.out
                //         .println(expected_result + " was expected but got " + result + " instead.");
                assertEquals(expected_result + " was expected but got " + result + " instead.", expected_result,
                result);
            }
            
          
        }
    }

}