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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.EnumControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.ReverbType;
import javax.sound.sampled.SourceDataLine;
import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.PhononSoundDevice;
import com.jme3.phonon.PhononSoundPlayer;
import com.jme3.phonon.PhononSoundSystem;
import com.jme3.phonon.format.decoder.AudioDataDecoder;
import com.jme3.phonon.format.decoder.AudioDataDecoderFactory;
import com.jme3.phonon.utils.BitUtils;

class JavaSoundPlayer implements PhononSoundPlayer<JavaSoundPhononSettings,JavaSoundSystem,JavaSoundDevice>{


 
    PhononOutputLine channel;
    InputStream input;
    SourceDataLine output;
    AudioFormat audioFormat;

    boolean isRunning;
    byte decodedFrame[];
    byte encodedFrame[];

    AudioDataDecoder decoder;
    JavaSoundPhononSettings settings;
    @Override
    public void init(
        JavaSoundPhononSettings settings,
        JavaSoundSystem system,
        JavaSoundDevice device,
    
    PhononOutputLine chan, int sampleRate,
            int channels, int frameSize, int sampleSize) throws Exception {
        this.settings=settings;
        channel = chan;


        decoder = AudioDataDecoderFactory.getAudioDataDecoder(sampleSize);

        int bytesPerSample=(sampleSize/8);
        int frameSizeInBytes=frameSize*bytesPerSample*channels;
        
        encodedFrame=new byte[frameSize*channels*4];
        decodedFrame=new byte[frameSizeInBytes];
        audioFormat=new AudioFormat(sampleRate,sampleSize,channels,true,false);
      
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        output=(SourceDataLine)device.getMixer().getLine(info);
        output.open(audioFormat,frameSizeInBytes*settings.playerBuffer);
    }

    boolean started=false;
    public void close() {
        output.flush();
        output.close();
    }

    @Override
    public void play(ByteBuffer frame, int framesize, int channels) {
        frame.rewind();
        frame.get(encodedFrame);
        frame.rewind();
        decoder.decode(encodedFrame,decodedFrame);
        output.write(decodedFrame,0,decodedFrame.length);
        if(!started&&(!settings.playerStartWhenBufferIsFull||output.available()<decodedFrame.length)){
            output.start();
            started=true;
        }
     
    }

}
