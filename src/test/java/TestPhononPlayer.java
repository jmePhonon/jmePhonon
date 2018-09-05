
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.phonon.format.F32leAudioData;
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

        AudioData testAudioData = assetManager.loadAudio("Juhani Junkala - Epic Boss Battle [Seamlessly Looping].wav");
        F32leAudioData convertedTestAudioData = new F32leAudioData(testAudioData);

        
        int bufferSize = 2048; 
        // bufferSize= convertedTestAudioData.getSampleRate(); 1 second
        // bufferSize = convertedTestAudioData.getSampleRate() * 60;
        
        float s = bufferSize / convertedTestAudioData.getSampleRate();
        
        System.out.println("Use bufferSize " + bufferSize+ " = "+s+" seconds");
		try {
            // PhononPlayer player = new PhononPlayer(convertedTestAudioData, 16,  bufferSize);
            // player.play(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 

    ByteBuffer randomDataBuffer(int bufferSize) {
        byte[] testData = new byte[bufferSize];
        for(int i = 0; i < bufferSize; ++i) {
            testData[i] = (byte) (i % 128);
        }
        return BufferUtils.createByteBuffer(testData);
    }
}