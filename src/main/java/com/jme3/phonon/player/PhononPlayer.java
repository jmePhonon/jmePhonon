package com.jme3.phonon.player;

import java.io.EOFException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import com.jme3.phonon.PhononChannel;
import com.jme3.phonon.PhononChannel.ChannelStatus;
import com.jme3.phonon.utils.BitUtils;

public class PhononPlayer {


    PhononChannel channel;
    PhononChannelIntInputStream input;
    SourceDataLine output;
    AudioFormat audioFormat;
    int dataLineSampleSize;    
    int preloadBytes = 0;



    public PhononPlayer(PhononChannel chan, int sampleRate,
            int outputChannels,int outputSampleSize,int preloadedSamplesNum) throws LineUnavailableException {
        channel = chan;
        dataLineSampleSize = outputSampleSize;

        int bytesPerSample = (outputSampleSize / 8);

        input = new PhononChannelIntInputStream(channel,outputSampleSize);
        audioFormat = new AudioFormat(sampleRate, outputSampleSize, outputChannels, true, false);
        output = AudioSystem.getSourceDataLine(audioFormat);
        output.open(audioFormat, chan.getBufferSize()*chan.getFrameSize()  * bytesPerSample);


        preloadBytes = preloadedSamplesNum * bytesPerSample;
        long nsPerSample= 1000000000l / sampleRate;
  

        System.out.println("Delay playback for " +( (preloadedSamplesNum *nsPerSample )/1000000l )+ " ms / " + preloadedSamplesNum
                + " samples / " + preloadBytes + " bytes");
    }



    public boolean playLoop() {
        int writableBytes = 0;
        int read = 0;
        try {
            
            writableBytes= output.available();
            
            byte tmp[] = new byte[writableBytes];
            read = input.read(tmp);
            
            if (read > 0) {
                // System.out.println("Write "+read);

                output.write(tmp, 0, read);
                if (preloadBytes > 0) {
                    System.out.println("Loaded "+read+" bytes");
                    preloadBytes -= read;
                }
            } else {
                // System.out.println("No data");

                // no data available
            }
        } catch (EOFException e) {
            // Channel over;
            System.err.println("TO BE IMPLEMENTED: End of channel");

        } catch (Exception e) {
            System.out.println("Writable " + writableBytes+" read "+read);

            e.printStackTrace();
        }

        if (preloadBytes <= 0) {
            if (!output.isRunning()) {
                output.start();
                System.out.println("Start");
            }
        } else {
            System.out.println("Preloading " + preloadBytes + " bytes");
        }
      

      

       
        return true;

    }

}
