package com.jme3.phonon.player;

import java.io.EOFException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.PhononOutputLine.ChannelStatus;
import com.jme3.phonon.utils.BitUtils;

public class PhononPlayer {



    PhononOutputLine channel;
    InputStream input;
    SourceDataLine output;
    AudioFormat audioFormat;
    int dataLineSampleSize;    
    int preloadBytes = 0;

    boolean isRunning;
    byte tmp[];
    public PhononPlayer(PhononOutputLine chan, int sampleRate,
            int outputChannels,int outputSampleSize,int maxPreBuffering) throws LineUnavailableException {
        channel = chan;
        dataLineSampleSize = outputSampleSize;


        int bytesPerSample = (outputSampleSize / 8);

        input = new PhononOutputLineIntInputStream(channel,outputSampleSize);
        audioFormat = new AudioFormat(sampleRate, outputSampleSize, outputChannels, true, false);
        output = AudioSystem.getSourceDataLine(audioFormat);
        output.open(audioFormat);//, chan.getBufferSize()*chan.getFrameSize()  * bytesPerSample);

   
      //preloadedSamplesNum * bytesPerSample;


        long nsPerSample= 1000000000l / sampleRate;
       
       preloadBytes = (int)(((1000000l * maxPreBuffering) / nsPerSample)*bytesPerSample);
       if (output.getBufferSize() < preloadBytes)
            preloadBytes = output.getBufferSize();
           
           int preloadedSamplesNum = preloadBytes / bytesPerSample;

           tmp= new byte[output.getBufferSize()];
        System.out.println("Delay playback for " +( (preloadedSamplesNum *nsPerSample )/1000000l )+ " ms / " + preloadedSamplesNum
                + " samples / " + preloadBytes + " bytes");
    }



    public byte playLoop() {

        if (preloadBytes <= 0) {
            if (!isRunning) {
                isRunning = true;
                output.start();
                
                System.out.println("Start");
            }
        } else {
            System.out.println("Preloading " + preloadBytes + " bytes");
        }
      

        byte out = 0;
        int writableBytes = 0;
        int read = 0;
        try {
            
            writableBytes= output.available();
            
            
            read = input.read(tmp);
            
            if (read > 0) {
                // System.out.println("Write "+read);

                int written=output.write(tmp, 0, read);
                if (preloadBytes > 0) {
                    System.out.println("Loaded "+written+" bytes");
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
