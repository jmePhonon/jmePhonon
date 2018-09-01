import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.phonon.F32leAudioData;
import com.jme3.phonon.PhononChannel;
import com.jme3.phonon.PhononRenderer;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.util.BufferUtils;

public class TestPhononRenderer extends SimpleApplication {
    public static void main(String[] args) {
        TestPhononRenderer app = new TestPhononRenderer();
        app.setShowSettings(false);
        app.start();
    }

    F32leAudioData songAudioData, ambientAudioData;

    @Override
    public void simpleInitApp() {
        songAudioData = new F32leAudioData(assetManager.loadAudio("399354__romariogrande__eastandw_mono.ogg"));
        ambientAudioData = new F32leAudioData(assetManager.loadAudio("48412__luftrum__oceanwavescrushing.wav"));

        PhononRenderer renderer = new PhononRenderer(1024, 1024);
        renderer.initialize();    
        renderer.connectSource(songAudioData, 0);

        try {
            PhononPlayer songPlayer = new PhononPlayer(renderer.getChannel(0), 1, 16);
            renderer.attachPlayer(songPlayer);
            songPlayer.startPlayback();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for(int c = 1; c < 5; c++) {
            try {
                Thread.sleep(500);
                renderer.connectSource(ambientAudioData, c);

                PhononPlayer ambientPlayer = new PhononPlayer(renderer.getChannel(c), 1, 16);
                renderer.attachPlayer(ambientPlayer);
                ambientPlayer.startPlayback();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    } 
}