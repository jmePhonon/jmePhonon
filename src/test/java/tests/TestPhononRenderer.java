package tests;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode.Status;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Environment;
import com.jme3.audio.Listener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.phonon.PhononAudioSourceData;
import com.jme3.phonon.PhononRenderer;
import com.jme3.phonon.PhononSettings;
import com.jme3.phonon.Phonon;
import com.jme3.phonon.ThreadMode;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

public class TestPhononRenderer extends SimpleApplication implements ActionListener{

    static  int outputLines = 1;
    static int frameSize = 1024;// samples
    static int frameBuffer = 3;
    static int maxPreBuffering = 1024*2*4; //2 frame preload
    static int channels = 2;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        
        // phonon  
        int dialogResult =JOptionPane.showConfirmDialog(null, "Do you want to run the test with phonon? (Press No to run with jme renderer)");
        if (dialogResult == JOptionPane.YES_OPTION) {
            settings.setAudioRenderer(null);
        }
        settings.setFrameRate(200);
        TestPhononRenderer app = new TestPhononRenderer();
        app.setSettings(settings);

        app.setShowSettings(false);
        app.start();
    }
    
    public TestPhononRenderer() {
  
    }



    Node audioSourceNode;
    ArrayList<F32leAudioData> loadedSound = new ArrayList<F32leAudioData>();
    AudioNode engine;
    @Override
    public void simpleInitApp() {
        this.setPauseOnLostFocus(false);
        this.inputManager.addMapping("PAUSE", new KeyTrigger(KeyInput.KEY_P));
        this.inputManager.addMapping("DIRECTIONAL", new KeyTrigger(KeyInput.KEY_G));
        this.inputManager.addListener(this, "PAUSE", "DIRECTIONAL");
      
        if (audioRenderer == null) {
            double latency = ((double) 1000 / 44100) * frameSize * frameBuffer + maxPreBuffering;
            System.out.println("Expected Latency " + latency);
            PhononSettings effects = new PhononSettings();
            effects.passThrough = false;
            try {
                audioRenderer = new PhononRenderer(44100, outputLines, 16, channels, frameSize,
                        frameBuffer, 24, maxPreBuffering, ThreadMode.JAVA, effects);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            audioRenderer.initialize();

            AudioContext.setAudioRenderer(audioRenderer);
            listener = new Listener();
            listener.setRenderer(audioRenderer);
            listener.setVolume(1);
            audioRenderer.setListener(listener);
        }

        // Generic env
        audioRenderer.setEnvironment(Environment.Dungeon);

        audioSourceNode = new Node();

        Geometry audioSourceGeom = new Geometry("AudioSource", new Box(.5f, .5f, .5f));

        Material audioSourceGeomMat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        audioSourceGeom.setMaterial(audioSourceGeomMat);
        audioSourceGeomMat.setColor("Color",ColorRGBA.Red);
        audioSourceNode.attachChild(audioSourceGeom);
        flyCam.setMoveSpeed(10f);

        engine = new AudioNode(assetManager, "mono/264864__augustsandberg__marine-diesel-engine.wav", DataType.Buffer);
        audioSourceNode.attachChild(engine);
        engine.setName("Engine Audio Node");
        engine.setDirectional(true);
        engine.setPositional(true);
        engine.setRefDistance(1);
        engine.setVolume(1f);
        engine.setLooping(false);
        engine.setReverbEnabled(true);
        Phonon.setAudioNodeDipoleWeight(engine, 1f);
        engine.play();       

        AudioNode bg = new AudioNode(assetManager, "stereo/Juhani Junkala - Epic Boss Battle [Seamlessly Looping].wav", DataType.Buffer);
        audioSourceNode.attachChild(bg);
        bg.setName("Background Audio Node");
        bg.setPositional(false);
        bg.setVolume(.1f);
        bg.setLooping(true);
        bg.play();

        rootNode.attachChild(audioSourceNode);
    }

    private float currentPower = 0f;

    float time = 0;
    @Override
    public void simpleRender(RenderManager rm) {
        super.simpleRender(rm);
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
        
        AudioNode engineAudioNode = ((AudioNode) audioSourceNode.getChild("Engine Audio Node"));
        currentPower = (currentPower + .1f) % 1f; 
        Phonon.setAudioNodeDipolePower(engineAudioNode, currentPower);
    }

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf;
        float speed=3;
        float radius = 10;
        // audioSourceNode.setLocalTranslation(new Vector3f(FastMath.sin(time*speed)*radius,0,FastMath.cos(time*speed)*radius));
        // System.out.println("Sound status: "+engine.getStatus());
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("PAUSE")&&isPressed) {
            if (engine.getStatus() != AudioSource.Status.Paused)
                engine.pause();
            else engine.play();
            System.out.println("Pause");
        } else if(name.equals("DIRECTIONAL") && isPressed) {
            engine.setDirectional(!engine.isDirectional());
            System.out.println("Engine is directional: " + engine.isDirectional());
        }
    }
}
