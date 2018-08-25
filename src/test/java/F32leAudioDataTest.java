

import java.io.FileOutputStream;
import java.io.IOException;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.sa.F32leAudioData;
import com.jme3.system.AppSettings;

public class F32leAudioDataTest extends SimpleApplication {
    public static void main(String[] args) {
        F32leAudioDataTest app = new F32leAudioDataTest();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        try {
            System.out.println("Input file 399354__romariogrande__eastandw_mono.ogg");
            AudioData ad = assetManager.loadAudio("399354__romariogrande__eastandw_mono.ogg");
            F32leAudioData f32le = new F32leAudioData(ad);
            FileOutputStream fo=new FileOutputStream("/tmp/f32.raw");
            f32le.writeRaw(fo);
            fo.close();
            System.out.println("Output file /tmp/f32.raw");
            stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}