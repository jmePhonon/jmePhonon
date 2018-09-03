package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.jme3.audio.AudioData;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.ListenerParam;
import com.jme3.phonon.utils.Clock;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.utils.Sleeper;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;

/**
 * PhononRenderer
 */
public class PhononRenderer implements AudioRenderer {
	int CHANNEL_LIMIT = 16;

    private final Map<AudioData, F32leAudioData> conversionCache = new WeakHashMap<AudioData, F32leAudioData>();
	private final PhononChannel[] channels = new PhononChannel[CHANNEL_LIMIT];
	// private final List<PhononPlayer> enqueuedPlayers = new LinkedList<>();
	private final LinkedList<PhononPlayer> players = new LinkedList<>();

	static{
		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Linux32, "linux-x86/libphonon.so");
		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Linux64, "linux-x86-64/libphonon.so");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Linux32, "linux-x86/libjmephonon.so");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Linux64, "linux-x86-64/libjmephonon.so");
		// TODO: Windows
		// TODO: OSX
		// MAYBE TODO: Android
	}
	final int _OUTPUT_FRAME_SIZE;
	final int _OUTPUT_BUFFER_SIZE ;



	volatile boolean attachingPlayers = false;
	volatile boolean updatingPlayers = false;

	Clock CLOCK=Clock.NATIVE;
	Sleeper WAIT_MODE = Sleeper.NATIVE;
	boolean useNativeThreads;
	double DELTA_S;

	public PhononRenderer(int frameSize, int bufferSize) {
		_OUTPUT_FRAME_SIZE = frameSize;
		_OUTPUT_BUFFER_SIZE = bufferSize;
		NativeLibraryLoader.loadNativeLibrary("Phonon", true);
		NativeLibraryLoader.loadNativeLibrary("JMEPhonon", true);
	}

	
	public PhononChannel getChannel(int i) {
		return channels[i];
	}

	void preInit() {
		DELTA_S=  1./(44100 / _OUTPUT_FRAME_SIZE) ;
		useNativeThreads=CLOCK==Clock.NATIVE||WAIT_MODE==Sleeper.NATIVE;
		initNative(DELTA_S, useNativeThreads,CLOCK==Clock.NATIVE);
		for (int i = 0; i < channels.length; i++) {
			channels[i] = new PhononChannel(_OUTPUT_FRAME_SIZE*2, _OUTPUT_BUFFER_SIZE);
			loadChannelNative(i, channels[i].getAddress(), channels[i].getFrameSize(), channels[i].getBufferSize());
		}
	}

	Thread playeThread;
	@Override
	public void initialize() {

		preInit();

  
		if (!useNativeThreads) {
			Thread decoderThread = new Thread(() -> runDecoder());

			decoderThread.setPriority(Thread.NORM_PRIORITY + 2);
			decoderThread.setDaemon(true);
			decoderThread.start();
		}
		//  playeThread = new Thread(() -> runPlayer());
	
		// playeThread.setDaemon(true);
		// playeThread.start();

	}

	@Override
	public void cleanup() {
		destroyNative();
	}

	native void initNative(double updateRate,boolean nativeThread,boolean nativeClock);
	native void updateNative();
	native void destroyNative();
	native void connectSourceNative(int channelId, int length, long sourceAddr);
	native void disconnectSourceNative(int channelId);
	
	/**
	 * @param addr Output buffer address
	 * @param frameSize samples per frame
	 * @param bufferSize total number of frames in this buffer
	 */
	native void loadChannelNative(int id,long addr,int frameSize,int bufferSize);


	public void connectSource(F32leAudioData audioData, int channelId) {
		System.out.println("Connect source [" + audioData.getAddress() + "] of size " + audioData.getSizeInSamples()
				+ " samples, to channel " + channelId);
		int length = audioData.getSizeInSamples();
		long addr = audioData.getAddress();

		channels[channelId].reset();
		connectSourceNative(channelId, length,addr);
	}

	
	public void connectSourceRaw(int channelId, int length, ByteBuffer source) {
		long addr = DirectBufferUtils.getAddr(source);
		connectSourceNative(channelId, length, addr);
		channels[channelId].reset();
	}

	public void disconnectSourceRaw(int channelId) {
		disconnectSourceNative(channelId);
	}

	public void attachPlayer(PhononPlayer player) {

		do {
			try{
			Thread.sleep(10);
			} catch (Exception e) {
			}
		} while (updatingPlayers);
		attachingPlayers = true;
		players.add(player);
		attachingPlayers = false;
	}	

	public void runPlayer() {
	// 	while (true) {
	// 		while(!enqueuedPlayers.isEmpty()) {
	// 			players.add(enqueuedPlayers.poll());
	// 		}

	// 		int stalling = players.size();
	// 		for (PhononPlayer player : players) {
	// 			byte res = player.playLoop();
	// 			if (res == 0) {
	// 				stalling--;
	// 			}
	// 		}

	// 		if (stalling == players.size()) {
	// 			try {
	// 				synchronized(playeThread){
	// 				playeThread.wait();
	// 				}
	// 			} catch (Exception e) {
	// 				e.printStackTrace();
	// 			}
	// 		}

	// 	// 	try{
	// 	// 	Thread.sleep(10);
	// 	// 	} catch (Exception e) {
	// 	// 	}
	// 	}
	}


	
	

	// long UPDATE_RATE = 50* 1000000l;
	public void runDecoder() {

		long UPDATE_RATE= CLOCK.getExpectedTimeDelta(DELTA_S);

				// if () {

		long startTime = 0;
		do {
			startTime = CLOCK.measure();
		
			// while(!enqueuedPlayers.isEmpty()) {
			// 	players.add(enqueuedPlayers.remove(0));
			// }


			if (!useNativeThreads) {
				updateNative();
			}

			if (!attachingPlayers) {
				updatingPlayers = true;
				for (PhononPlayer player : players) {
					byte res = player.playLoop();
					// if (res == 0) {
					// 	stalling--;
					// }

				}
				updatingPlayers = false;
			}




			// synchronized(playeThread){
			// 	playeThread.notify();
			// 	}


				try {
					// Thread.yield();
					WAIT_MODE.wait(CLOCK, startTime, UPDATE_RATE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			

		} while (!useNativeThreads);
	}


    private F32leAudioData toF32leData(AudioData d) {
        F32leAudioData o=conversionCache.get(d);
        if (o == null) {
            o = new F32leAudioData(d);
            conversionCache.put(d,o);
        }
		return o;
	}




	
	@Override
	public void setListener(Listener listener) {
		
	}

	@Override
	public void setEnvironment(Environment env) {
		
	}

	@Override
    public void playSourceInstance(AudioSource src) {
        F32leAudioData data=toF32leData(src.getAudioData());

		
	}

	@Override
    public void playSource(AudioSource src) {
        F32leAudioData data=toF32leData(src.getAudioData());

		
	}

	@Override
	public void pauseSource(AudioSource src) {
		F32leAudioData data=toF32leData(src.getAudioData());

	}

	@Override
	public void stopSource(AudioSource src) {
		
	}

	@Override
	public void updateSourceParam(AudioSource src, AudioParam param) {
		
	}

	@Override
	public void updateListenerParam(Listener listener, ListenerParam param) {
		
	}

	@Override
	public float getSourcePlaybackTime(AudioSource src) {
		return 0;
	}

	@Override
	public void deleteFilter(Filter filter) {
		
	}

	@Override
	public void deleteAudioData(AudioData ad) {
		
	}



	@Override
	public void update(float tpf) {
		
	}

	@Override
	public void pauseAll() {
		
	}

	@Override
	public void resumeAll() {
		
	}


    
}