package com.jme3.phonon.desktop_javasound;

import java.io.EOFException;
import java.io.InputStream;
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
import com.jme3.phonon.PhononOutputLine.ChannelStatus;
import com.jme3.phonon.utils.BitUtils;
import com.jme3.phonon.utils.PhononOutputLineIntInputStream;

class JavaSoundPlayer implements PhononSoundPlayer<JavaSoundSystem,JavaSoundDevice>{



    PhononOutputLine channel;
    InputStream input;
    SourceDataLine output;
    AudioFormat audioFormat;
    int preloadBytes = 0;

    boolean isRunning;
    byte tmp[];


    @Override
    public void init(
        JavaSoundSystem system,
        JavaSoundDevice device,
    
    PhononOutputLine chan, int sampleRate,
            int outputChannels,int outputSampleSize,int maxPreBufferingSamples) throws Exception {
        channel = chan;

        int bytesPerSample = (outputSampleSize / 8);

        input = new PhononOutputLineIntInputStream(channel, outputSampleSize);
        preloadBytes = maxPreBufferingSamples * bytesPerSample;

        audioFormat=new AudioFormat(sampleRate,outputSampleSize,outputChannels,true,false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        output=(SourceDataLine)device.getMixer().getLine(info);
        output.open(audioFormat, preloadBytes);//, chan.getBufferSize()*chan.getFrameSize()  * bytesPerSample);
     

        tmp= new byte[preloadBytes];
    }


    public void close() {
        output.close();
    }

    @Override
    public byte loop() {

     
       
        if (preloadBytes <= 0) {
            if (!isRunning) {
                isRunning = true;
                output.start();
                
                // System.out.println("Start");
            }
        } else {
            // System.out.println("Preloading " + preloadBytes + " bytes");
        }

        byte out = 0;
        int writableBytes = 0;
        int read = 0;
        try {
            
            writableBytes= output.available();
            if (writableBytes > tmp.length)
            writableBytes = tmp.length;
            

            read = input.read(tmp, 0, writableBytes);
            
            if (read > 0) {
                // System.out.println("Write "+read);

                int written=output.write(tmp, 0, read);
                if (preloadBytes > 0) {
                    // System.out.println("Loaded "+written+" bytes");
                    preloadBytes -= written;
                }
            } else {
                // System.out.println("FIXME: Phonon is lagging behind");
                out=1;
                // no data available
            }
        } catch (EOFException e) {
            // Channel over;
            System.err.println("TO BE IMPLEMENTED: End of channel");
            return -1;
        } catch (Exception e) {
            System.out.println("Writable " + writableBytes+" read "+read);

            e.printStackTrace();
        }

      
      

       
        return out;

    }

}
