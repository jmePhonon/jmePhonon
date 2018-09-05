package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.util.Collection;
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

    private final Map<AudioData, F32leAudioData> conversionCache = new WeakHashMap<AudioData, F32leAudioData>();
	// private final List<PhononPlayer> enqueuedPlayers = new LinkedList<>();

	static {
		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Linux32,
				"linux-x86/libphonon.so");
		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Linux64,
				"linux-x86-64/libphonon.so");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Linux32,
				"linux-x86/libjmephonon.so");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Linux64,
				"linux-x86-64/libjmephonon.so");
		// TODO: Windows
		// TODO: OSX
		// MAYBE TODO: Android
	}
	
	// Mixer lines
	private final PhononChannel[] OUTPUT_LINES;
	// Output channels, 1=mono, 2=stereo ..
	private final int OUTPUT_CHANNELS_NUM;
	// How many samples per frame
	private final int FRAME_SIZE;
	// How many frames per buffer
	private final int BUFFER_SIZE;
	// Samplerate (eg 44100)
	private final int SAMPLE_RATE;


	private final Collection<PhononPlayer> PLAYERS = new ConcurrentLinkedQueue<>();


	public final PhononEffects effects=new PhononEffects();

	ThreadMode THREAD_MODE = ThreadMode.NATIVE_DECOUPLED;
	Clock CLOCK=Clock.HIGHRES;
	Sleeper WAIT_MODE = Sleeper.BUSYSLEEP;
	double DELTA_S;

	public PhononRenderer(int sampleRate, int nOutputLines, int nOutputChannels, int frameSize,
			int bufferSize) {
		OUTPUT_LINES = new PhononChannel[nOutputLines];
		OUTPUT_CHANNELS_NUM = nOutputChannels;
		SAMPLE_RATE = sampleRate;
		FRAME_SIZE = frameSize;
		BUFFER_SIZE = bufferSize;
		NativeLibraryLoader.loadNativeLibrary("Phonon", true);
		NativeLibraryLoader.loadNativeLibrary("JMEPhonon", true);
	}

	
	public PhononChannel getLine(int i) {
		return OUTPUT_LINES[i];
	}

	void preInit() {
		if (CLOCK == Clock.NATIVE && THREAD_MODE == ThreadMode.NATIVE_DECOUPLED) { // FIXME
			System.err
					.println("Error: can't use both  ThreadMode.NATIVE_DECOUPLED and Clock.NATIVE");
			System.exit(1);
		}
		DELTA_S=  1./(44100 / FRAME_SIZE) ;
		initNative(SAMPLE_RATE, OUTPUT_LINES.length, OUTPUT_CHANNELS_NUM, FRAME_SIZE, BUFFER_SIZE,
				DELTA_S, 
				THREAD_MODE.isNative,
				THREAD_MODE.isDecoupled,
				THREAD_MODE.isDecoupled||CLOCK==Clock.NATIVE,
		// Effects
		effects.passThrough
		
		);
		for (int i = 0; i < OUTPUT_LINES.length; i++) {
			OUTPUT_LINES[i] = new PhononChannel(FRAME_SIZE*OUTPUT_CHANNELS_NUM, BUFFER_SIZE);
			loadChannelNative(i, OUTPUT_LINES[i].getAddress());
		}
	}

	@Override
	public void initialize() {

		preInit();

  
		if (!THREAD_MODE.isNative||THREAD_MODE.isDecoupled) {
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

	native void initNative(int sampleRate,int nOutputLines,int nOutputChannels,int frameSize,int bufferSize,double updateRate,boolean nativeThread,boolean decoupledNativeThread,boolean nativeClock,
	// effects
	boolean isPassThrough
	);
	native void updateNative();
	native void destroyNative();
	native void connectSourceNative(int lineID, int length, long sourceAddr);
	native void disconnectSourceNative(int lineID);
	
	/**
	 * @param addr Output buffer address
	 * @param frameSize samples per frame
	 * @param bufferSize total number of frames in this buffer
	 */
	native void loadChannelNative(int id,long addr);


	public void connectSource(F32leAudioData audioData, int lineID) {
		System.out.println("Connect source [" + audioData.getAddress() + "] of size " + audioData.getSizeInSamples()
				+ " samples, to channel " + lineID);
		int length = audioData.getSizeInSamples();
		long addr = audioData.getAddress();

		OUTPUT_LINES[lineID].reset();
		connectSourceNative(lineID, length,addr);
	}

	
	public void connectSourceRaw(int lineID, int length, ByteBuffer source) {
		long addr = DirectBufferUtils.getAddr(source);
		connectSourceNative(lineID, length, addr);
		OUTPUT_LINES[lineID].reset();
	}

	public void disconnectSourceRaw(int lineID) {
		disconnectSourceNative(lineID);
	}

	public void attachPlayer(PhononPlayer player) {
		PLAYERS.add(player);
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


			if (!THREAD_MODE.isNative) {
				updateNative();
			}


				for (PhononPlayer player : PLAYERS) {
					byte res = player.playLoop();
					// if (res == 0) {
					// 	stalling--;
					// }

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
			

		} while (!THREAD_MODE.isNative||THREAD_MODE.isDecoupled);
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