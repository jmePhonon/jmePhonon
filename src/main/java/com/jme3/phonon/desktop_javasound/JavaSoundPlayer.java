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
package com.jme3.phonon.desktop_javasound;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.PhononSoundPlayer;
import com.sun.media.sound.AudioFloatConverter;

class JavaSoundPlayer implements PhononSoundPlayer<JavaSoundPhononSettings,JavaSoundSystem,JavaSoundDevice>{

    private AudioFloatConverter converter;

    private SourceDataLine output;
    private AudioFormat audioFormat;

    private byte decodedFrame[];
    private byte emptyDecodedFrame[];
    private volatile boolean decodedFrameReady=false;

    private float encodedFrame[];

    private boolean started=false;

    private FloatBuffer floatFrame;
    private ByteBuffer frame;

    private JavaSoundPhononSettings settings;

    @Override
    public void init(JavaSoundPhononSettings settings, JavaSoundSystem system, JavaSoundDevice device,

            PhononOutputLine chan, int sampleRate, int channels, int frameSize, int sampleSize) throws Exception {
        this.settings=settings;

        int bytesPerSample=(sampleSize / 8);
        int frameSizeInBytes=frameSize * bytesPerSample * channels;

        encodedFrame=new float[frameSize * channels];
        decodedFrame=new byte[frameSizeInBytes];
        emptyDecodedFrame=new byte[frameSizeInBytes];
        audioFormat=new AudioFormat(sampleRate,sampleSize,channels,true,false);
        converter=com.sun.media.sound.AudioFloatConverter.getConverter(audioFormat);

        /*
        * Q/A
        * Why is there a dedicated thread with the only purpose of writing into the javamixer couldn't this be done in the play() method? 
        *      Long story short, it seems that when a delay in you app causes the buffer to empty, some systems
        *      internally might decide to shut off the line and this triggers a chain reaction of weirdness 
        *      that causes screeching and broken sound that last for some seconds. A thread that always write, even when the data is not
        *      available, seems to solve the issue. (if the data is not available we write empty frames).
        */
    
        Thread t=new Thread(() -> {      
            try{
                DataLine.Info info=new DataLine.Info(SourceDataLine.class,audioFormat);
                output=(SourceDataLine)device.getMixer().getLine(info);
                output.open(audioFormat,frameSizeInBytes*settings.playerBuffer );
        
              
                while(true){
                    jmixPlay();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        });
        t.setName("JavaMixer Writer");
        t.setPriority(Thread.MAX_PRIORITY);
        t.setDaemon(true);
        t.start();
    }

    public void close() {
        output.flush();
        output.close();
    }

 
    private void jmixPlay() {        
        if(decodedFrameReady){
            // if we have a frame, we write it and we let the other thread continue
            synchronized(decodedFrame){
                // This will automatically pause the thread if the buffer is full
                output.write(decodedFrame,0,decodedFrame.length); 
                decodedFrameReady=false;
                decodedFrame.notify();
            }
        }else if(output.available()>=emptyDecodedFrame.length*settings.playerBuffer){
            // If the buffer is empty and we don't have a frame, we resort to write empty bytes
            output.write(emptyDecodedFrame,0,emptyDecodedFrame.length);
        }

        if(!started && (!settings.playerStartWhenBufferIsFull || output.available() < emptyDecodedFrame.length)){
            output.start();
            started=true;
        }
    }

    @Override
    public void play(ByteBuffer frame, int framesize, int channels) {

        frame.rewind();

        if(floatFrame == null || this.frame != frame){
            this.frame=frame;
            floatFrame=frame.asFloatBuffer();
        }

        floatFrame.rewind();

        floatFrame.get(encodedFrame);

        frame.rewind();
        floatFrame.rewind();

        // We convert the new frame and wait for it to be played (or buffered)
        synchronized(decodedFrame){
            converter.toByteArray(encodedFrame,decodedFrame);
            decodedFrameReady=true;
            do{
                try{
                    decodedFrame.wait();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }while(decodedFrameReady);
        }        

    }

}
