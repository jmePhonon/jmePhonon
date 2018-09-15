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
package tests;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.phonon.utils.BitUtils;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.system.AppSettings;

public class F32leAudioDataTest extends SimpleApplication {
    /**
     * This example will read an ogg file, convert it into a 32 bit float point raw audio file and save it in /tmp/f32.raw.
     * Then it will take the converted output and re transform it back into a Signed PCM format with a number of bytes per sample 
     * specified by the constant below. The result will be saved in /tmp/f32.pcm_" + (8 * bytes.length) + "bit.raw
     */
    final int OUTPUT2_BYTES_PER_SAMPLE = 1;
    /////////////////////////////////////////////

    public static void main(String[] args) {
        F32leAudioDataTest app = new F32leAudioDataTest();
        app.setShowSettings(false);
        app.start();
    }

    public void writeRaw(ByteBuffer data,OutputStream os) throws IOException {
        data.rewind();
        byte array[] = new byte[data.limit()];
        data.get(array);
        os.write(array);
        data.rewind();
    }
    @Override
    public void simpleInitApp() {
        try {
            System.out.println("Input file 399354__romariogrande__eastandw_mono.ogg");
            AudioData ad = assetManager.loadAudio("399354__romariogrande__eastandw_mono.ogg");
            F32leAudioData f32le = new F32leAudioData(ad);
            OutputStream fo=new FileOutputStream("/tmp/f32.raw");
            writeRaw(f32le.getData(), fo);
            fo.close();


            byte bytes[]=new byte[OUTPUT2_BYTES_PER_SAMPLE];

            String n2 = "/tmp/f32.pcm_" + (8 * bytes.length) + "bit.raw";
            fo=new BufferedOutputStream(new FileOutputStream(n2));
            ByteBuffer bbf = f32le.rewind().getData();
            
            byte f[] = new byte[4];

            while (bbf.hasRemaining()) {
                bbf.get(f);
                if (bytes.length == 2) {
                    BitUtils.cnvF32leToI16le(f, bytes);
                } else if (bytes.length == 3) {
                    BitUtils.cnvF32leToI24le(f, bytes);
                } else {
                    BitUtils.cnvF32leToI8le(f, bytes);
                }
                fo.write(bytes);            
            }
            fo.close();

            System.out.println("Output file /tmp/f32.raw");
            System.out.println("Output file "+n2);

            stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}