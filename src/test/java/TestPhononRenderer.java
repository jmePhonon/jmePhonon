import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.ListenerParam;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.PhononRenderer;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;

public class TestPhononRenderer extends SimpleApplication {
    public static void main(String[] args) {
        TestPhononRenderer app = new TestPhononRenderer();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        super.simpleRender(rm);
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
        renderer.update(0);
        renderer.updateListenerParam(listener, ListenerParam.Position);
        renderer.updateListenerParam(listener, ListenerParam.Rotation);
      
      
    }
    @Override
    public void simpleUpdate(float tpf) {
      
    
    }

    PhononRenderer renderer;
    ArrayList<F32leAudioData> loadedSound = new ArrayList<F32leAudioData>();
    @Override
    public void simpleInitApp() {
        this.setPauseOnLostFocus(false);

        Geometry audioSourceGeom = new Geometry("AudioSource", new Box(2, 2, 2));
        Material audioSourceGeomMat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        audioSourceGeom.setMaterial(audioSourceGeomMat);
        audioSourceGeomMat.setColor("Color",ColorRGBA.Red);
        rootNode.attachChild(audioSourceGeom);
        flyCam.setMoveSpeed(10f);



        int outputLines = 16;
        int frameSize = 1024;
        int frameBuffer = 64;
        int maxPreBuffering = 50; //ms

        renderer = new PhononRenderer(44100, outputLines, 32, 2, frameSize, frameBuffer);
        renderer.setListener(listener);
        // renderer.effects.passThrough = true;

        renderer.initialize();

        try {

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            for (int k = 0; k < outputLines; k++) {
                PhononPlayer songPlayer = new PhononPlayer(renderer.getLine(k), 44100, 2, 16,maxPreBuffering);
                renderer.attachPlayer(songPlayer);
            }


            F32leAudioData audio;
            audio = new F32leAudioData(
                    assetManager.loadAudio("mono/399354__romariogrande__eastandw.ogg"));
            renderer.connectSource(audio);
            loadedSound.add(audio); // nb. protect sound from garbage collector...

            // audio = new F32leAudioData(assetManager.loadAudio("mono/433016__derjuli__ocean.wav"));
            // renderer.connectSource(audio, i);
            // songPlayer = new PhononPlayer(renderer.getLine(i++), 44100, 2, 16);
            // renderer.attachPlayer(songPlayer);
            // loadedSound.add(audio);

            // audio = new F32leAudioData(assetManager.loadAudio("mono/awesomeness.wav"));
            // renderer.connectSource(audio);
            // loadedSound.add(audio);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
