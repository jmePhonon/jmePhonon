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
        settings.threadMode=ThreadMode.NONE;
        PhononNativeLoader.loadAll();
        PhononRenderer renderer=PhononInitializer.init(settings,null,true,true);
        renderer.initializePhonon();
        renderer.playSourceDataRaw(bbf.limit()/4, bbf);
        // renderer.preInit();        


        PhononOutputLine chan = renderer.getLine(0);

        ByteBuffer tmpout_cnv = ByteBuffer.allocateDirect(frameSize*4).order(ByteOrder.LITTLE_ENDIAN);
        byte tmpout[] = new byte[frameSize*4];

        for(int i=0;i<bufferSize;i++){
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