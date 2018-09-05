package com.jme3.phonon.player;

import java.io.EOFException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import com.jme3.phonon.PhononChannel;
import com.jme3.phonon.PhononChannel.ChannelStatus;
import com.jme3.phonon.utils.BitUtils;

public class PhononPlayer {



    PhononChannel channel;
    InputStream input;
    SourceDataLine output;
    AudioFormat audioFormat;
    int dataLineSampleSize;    
    int preloadBytes = 0;

    boolean isRunning;

    public PhononPlayer(PhononChannel chan, int sampleRate,
            int outputChannels,int outputSampleSize) throws LineUnavailableException {
        channel = chan;
        dataLineSampleSize = outputSampleSize;


        int bytesPerSample = (outputSampleSize / 8);

        input = new PhononChannelIntInputStream(channel,outputSampleSize);
        audioFormat = new AudioFormat(sampleRate, outputSampleSize, outputChannels, true, false);
        output = AudioSystem.getSourceDataLine(audioFormat);
        output.open(audioFormat);//, chan.getBufferSize()*chan.getFrameSize()  * bytesPerSample);

   
      //preloadedSamplesNum * bytesPerSample;


        long nsPerSample= 1000000000l / sampleRate;
       
       preloadBytes = (int)(((1000000l * 100l) / nsPerSample)*bytesPerSample);
       if (output.getBufferSize() < preloadBytes)
           preloadBytes = output.getBufferSize();
           int preloadedSamplesNum = preloadBytes / bytesPerSample;

        System.out.println("Delay playback for " +( (preloadedSamplesNum *nsPerSample )/1000000l )+ " ms / " + preloadedSamplesNum
                + " samples / " + preloadBytes + " bytes");
    }




    public byte playLoop() {
        byte out = 0;
        int writableBytes = 0;
        int read = 0;
        try {
            
            writableBytes= output.available();
            
            byte tmp[] = new byte[writableBytes];
            read = input.read(tmp);
            
            if (read > 0) {
                // System.out.println("Write "+read);

                int written=output.write(tmp, 0, read);
                if (preloadBytes > 0) {
                    System.out.println("Loaded "+written+" bytes");
                    preloadBytes -= written;
                }
            } else {
                // System.out.println("No data");
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

        if (preloadBytes <= 0) {
            if (!isRunning) {
                isRunning = true;
                output.start();
                
                System.out.println("Start");
            }
        } else {
            System.out.println("Preloading " + preloadBytes + " bytes");
        }
      

      

       
        return out;

    }

}
