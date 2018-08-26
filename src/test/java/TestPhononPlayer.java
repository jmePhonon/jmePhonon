
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.phonon.F32leAudioData;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.util.BufferUtils;

public class TestPhononPlayer extends SimpleApplication {
    public static void main(String[] args) {
        TestPhononPlayer app = new TestPhononPlayer();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        /*int bufferSize = 256;
        ByteBuffer testBuffer = randomDataBuffer(bufferSize);*/

        AudioData testAudioData = assetManager.loadAudio("399354__romariogrande__eastandw_mono.ogg");
        F32leAudioData convertedTestAudioData = new F32leAudioData(testAudioData);

        PhononPlayer player = new PhononPlayer(convertedTestAudioData, 16, 60, 1);
        player.play();
    }

    ByteBuffer randomDataBuffer(int bufferSize) {
        byte[] testData = new byte[bufferSize];
        for(int i = 0; i < bufferSize; ++i) {
            testData[i] = (byte) (i % 128);
        }
        return BufferUtils.createByteBuffer(testData);
    }
}