package test;

import java.nio.ByteBuffer;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.steamaudio.F32leAudioData;
import com.jme3.steamaudio.player.SteamAudioPlayer;
import com.jme3.util.BufferUtils;

public class SteamPlayerTest extends SimpleApplication {
    public static void main(String[] args) {
        SteamPlayerTest app = new SteamPlayerTest();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        /*int bufferSize = 256;
        ByteBuffer testBuffer = randomDataBuffer(bufferSize);*/

        AudioData testAudioData = assetManager.loadAudio("399354__romariogrande__eastandw_mono.ogg");
        F32leAudioData convertedTestAudioData = new F32leAudioData(testAudioData);

        ByteBuffer testBuffer = convertedTestAudioData.getData();


        SteamAudioPlayer player = new SteamAudioPlayer(convertedTestAudioData, 16, 60);
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