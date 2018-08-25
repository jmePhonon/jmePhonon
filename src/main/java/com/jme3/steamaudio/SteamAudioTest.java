package com.jme3.steamaudio;

import java.nio.ByteBuffer;

import com.jme3.app.SimpleApplication;
import com.jme3.util.BufferUtils;
import com.jme3.steamaudio.player.SteamAudioPlayer;

public class SteamAudioTest extends SimpleApplication {
    public static void main(String[] args) {
        SteamAudioTest app = new SteamAudioTest();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        int bufferSize = 256;
        byte[] testData = new byte[bufferSize];
        for(int i = 0; i < bufferSize; ++i) {
            testData[i] = (byte) (i % 128);
        }
        ByteBuffer testBuffer = BufferUtils.createByteBuffer(testData);
        SteamAudioPlayer player = new SteamAudioPlayer(testBuffer, bufferSize / SteamAudioPlayer.FRAME_SIZE, 10, 8);
        player.play();
    }
}