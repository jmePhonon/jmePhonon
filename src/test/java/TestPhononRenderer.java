import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.Listener;
import com.jme3.audio.ListenerParam;
import com.jme3.audio.AudioData.DataType;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.phonon.PhononEffects;
import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.PhononRenderer;
import com.jme3.phonon.ThreadMode;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;

public class TestPhononRenderer extends SimpleApplication {

    static  int outputLines = 16;
    static int frameSize = 1024;
    static int frameBuffer = 2;
    static int maxPreBuffering = 10; //ms
    static int channels = 2;
    
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);     
        settings.setAudioRenderer(null);
        TestPhononRenderer app = new TestPhononRenderer();
        app.setSettings(settings);

        app.setShowSettings(false);
        app.start();
    }
    
    public TestPhononRenderer() {
  
       
    }

    @Override
    public void simpleRender(RenderManager rm) {
        super.simpleRender(rm);
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }

    @Override
    public void simpleUpdate(float tpf) {
      
    
    }

    ArrayList<F32leAudioData> loadedSound = new ArrayList<F32leAudioData>();
    @Override
    public void simpleInitApp() {
        this.setPauseOnLostFocus(false);
      
        double latency=((double)1000/44100)*frameSize*frameBuffer+maxPreBuffering;
        System.out.println("Expected Latency "+latency);
        PhononEffects effects=new PhononEffects();
        try {
            audioRenderer = new PhononRenderer(44100, outputLines, 16, channels, frameSize,
                    frameBuffer, 24, maxPreBuffering, ThreadMode.JAVA,effects);
        } catch (Exception e1) {
            e1.printStackTrace();
        }


        audioRenderer.initialize();
    
        AudioContext.setAudioRenderer(audioRenderer);
        listener = new Listener();
        audioRenderer.setListener(listener);
         

        Geometry audioSourceGeom = new Geometry("AudioSource", new Box(2, 2, 2));
        Material audioSourceGeomMat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        audioSourceGeom.setMaterial(audioSourceGeomMat);
        audioSourceGeomMat.setColor("Color",ColorRGBA.Red);
        rootNode.attachChild(audioSourceGeom);
        flyCam.setMoveSpeed(10f);

        AudioNode an = new AudioNode(assetManager, "mono/399354__romariogrande__eastandw.ogg",DataType.Buffer);
        rootNode.attachChild(an);
        an.play();


        

        
        




    }
}
