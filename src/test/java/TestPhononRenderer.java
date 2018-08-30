import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.phonon.F32leAudioData;
import com.jme3.phonon.PhononOutputChannel;
import com.jme3.phonon.PhononRenderer;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.util.BufferUtils;

public class TestPhononRenderer extends SimpleApplication {
    public static void main(String[] args) {
        TestPhononRenderer app = new TestPhononRenderer();
        app.setShowSettings(false);
        app.start();
    }

    F32leAudioData f32le;
    @Override
    public void simpleInitApp() {
         System.out.println("Input file 399354__romariogrande__eastandw_mono.ogg");
        AudioData ad = assetManager.loadAudio("399354__romariogrande__eastandw_mono.ogg");
        f32le = new F32leAudioData(ad);


        PhononRenderer renderer = new PhononRenderer(2048, 1024);
        renderer.initialize();
        renderer.wire(f32le,  0);        
        PhononOutputChannel chan = renderer.getChannel(0);     
        int bufferSize = 2048; 
        try {
            PhononPlayer player = new PhononPlayer(chan, 1, 16);
            player.play();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 

        /*
        Thread playerThread = new Thread(new Runnable() {        
            @Override
            public void run() {
                
            }
        });

        playerThread.start();*/
    } 

 
}