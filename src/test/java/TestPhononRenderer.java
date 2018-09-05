import java.nio.ByteBuffer;
import java.util.ArrayList;
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

    ArrayList<F32leAudioData> loadedSound = new ArrayList<F32leAudioData>();
    @Override
    public void simpleInitApp() {
        this.setPauseOnLostFocus(false);

        int outputLines = 16;

        PhononRenderer renderer = new PhononRenderer(44100, outputLines, 2, 1024, 64);
        // renderer.effects.passThrough = true;

        renderer.initialize();

        try {



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            F32leAudioData audio;
            int i = 0;

            audio = new F32leAudioData(
                    assetManager.loadAudio("mono/399354__romariogrande__eastandw.ogg"));
            renderer.connectSource(audio, i);
            PhononPlayer songPlayer = new PhononPlayer(renderer.getLine(i++), 44100, 2, 16);
            renderer.attachPlayer(songPlayer);
            loadedSound.add(audio); // nb. protect sound from garbage collector...

            audio = new F32leAudioData(assetManager.loadAudio("mono/433016__derjuli__ocean.wav"));
            renderer.connectSource(audio, i);
            songPlayer = new PhononPlayer(renderer.getLine(i++), 44100, 2, 16);
            renderer.attachPlayer(songPlayer);
            loadedSound.add(audio);

            audio = new F32leAudioData(assetManager.loadAudio("mono/awesomeness.wav"));

            renderer.connectSource(audio, i);
            songPlayer = new PhononPlayer(renderer.getLine(i++), 44100, 2, 16);
            renderer.attachPlayer(songPlayer);
            loadedSound.add(audio);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
