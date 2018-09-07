package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.ListenerParam;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.renderer.RenderManager;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;

/**
 * PhononRenderer
 */
public class PhononRenderer implements AudioRenderer {

	private final Map<AudioData, F32leAudioData> conversionCache =
			new WeakHashMap<AudioData, F32leAudioData>();
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
	private final PhononOutputLine[] OUTPUT_LINES;
	// Output channels, 1=mono, 2=stereo ..
	private final int OUTPUT_CHANNELS_NUM;
	private final int SOURCES_PER_OUTPUT_LINE;
	// How many samples per frame
	private final int FRAME_SIZE;
	// How many frames per buffer
	private final int BUFFER_SIZE;
	// Samplerate (eg 44100)
	private final int SAMPLE_RATE;

	private final PhononListener PHONON_LISTENER;
	private final Collection<PhononPlayer> PLAYERS = new ConcurrentLinkedQueue<>();
	public final PhononEffects effects = new PhononEffects();

	private Listener jmeListener;

	ThreadMode THREAD_MODE = ThreadMode.JAVA;

	Object MONITOR = new Object();
	boolean USE_SYNCHRONIZER = true;

	public PhononRenderer(int sampleRate, int nOutputLines, int nSourcesPerLine,
			int nOutputChannels, int frameSize, int bufferSize) {
		OUTPUT_LINES = new PhononOutputLine[nOutputLines];
		PHONON_LISTENER = new PhononListener();
		SOURCES_PER_OUTPUT_LINE = nSourcesPerLine;
		OUTPUT_CHANNELS_NUM = nOutputChannels;
		SAMPLE_RATE = sampleRate;
		FRAME_SIZE = frameSize;
		BUFFER_SIZE = bufferSize;
		NativeLibraryLoader.loadNativeLibrary("Phonon", true);
		NativeLibraryLoader.loadNativeLibrary("JMEPhonon", true);
	}


	public PhononOutputLine getLine(int i) {
		return OUTPUT_LINES[i];
	}

	void preInit() {

		// DELTA_S= 1./(44100 / FRAME_SIZE) ;
		initNative(SAMPLE_RATE, OUTPUT_LINES.length, SOURCES_PER_OUTPUT_LINE, OUTPUT_CHANNELS_NUM,
				FRAME_SIZE, BUFFER_SIZE, THREAD_MODE.isNative, THREAD_MODE.isDecoupled,
				PHONON_LISTENER.getAddress(),
				// Effects
				effects.passThrough

		);
		for (int i = 0; i < OUTPUT_LINES.length; i++) {
			OUTPUT_LINES[i] = new PhononOutputLine(FRAME_SIZE * OUTPUT_CHANNELS_NUM, BUFFER_SIZE);
			initLineNative(i, OUTPUT_LINES[i].getAddress());
		}
	}

	@Override
	public void initialize() {

		preInit();


		if (!THREAD_MODE.isNative || THREAD_MODE.isDecoupled) {
			Thread decoderThread = new Thread(() -> runDecoder());
			decoderThread.setName("Phonon Java Thread");
			decoderThread.setPriority(Thread.MAX_PRIORITY);
			decoderThread.setDaemon(true);
			decoderThread.start();
		}
		// playeThread = new Thread(() -> runPlayer());

		// playeThread.setDaemon(true);
		// playeThread.start();

	}

	@Override
	public void cleanup() {
		destroyNative();
	}

	/**
	 * Connect a source to an outputline
	 * 
	 * @param length     Lenght of the source measured in samples
	 * @param sourceAddr Address of the source
	 * @return 64 bit memory address of the source
	 */
	native long connectSourceNative(int length, long sourceAddr);

	/**
	 * Disconnect source from an output line
	 * 
	 * @param addr The memory address of the source
	 */
	native void disconnectSourceNative(long addr);

	/**
	 * Initialize an output line
	 * 
	 * @param addr       Output buffer address
	 * @param frameSize  samples per frame
	 * @param bufferSize total number of frames in this buffer
	 */
	native void initLineNative(int id, long addr);

	native void updateNative();



	native void initNative(int sampleRate, int nOutputLines, int nSourcesPerLine,
			int nOutputChannels, int frameSize, int bufferSize, boolean nativeThread,
			boolean decoupledNativeThread, long listenerDataPointer,
			// effects
			boolean isPassThrough);

	native void destroyNative();



	public long connectSource(F32leAudioData audioData) {
		System.out.println("Connect source [" + audioData.getAddress() + "] of size "
				+ audioData.getSizeInSamples());
		int length = audioData.getSizeInSamples();
		long addr = audioData.getAddress();

		// OUTPUT_LINES[lineID].reset();
		return connectSourceNative(length, addr);
	}


	public long connectSourceRaw(int length, ByteBuffer source) {
		long addr = DirectBufferUtils.getAddr(source);
		return connectSourceNative(length, addr);
	}

	public void disconnectSourceRaw(long addr) {
		disconnectSourceNative(addr);
	}

	public void attachPlayer(PhononPlayer player) {
		PLAYERS.add(player);
	}



	// long UPDATE_RATE = 50* 1000000l;
	public void runDecoder() {

		do {
			if (!THREAD_MODE.isNative || THREAD_MODE.isDecoupled) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {

				}
			}
			PHONON_LISTENER.updateNative();
			if (!THREAD_MODE.isNative)
				updateNative();
			for (PhononPlayer player : PLAYERS) {
				byte res = player.playLoop();
			}


		} while (!THREAD_MODE.isNative || THREAD_MODE.isDecoupled);

	}


	private F32leAudioData toF32leData(AudioData d) {
		F32leAudioData o = conversionCache.get(d);
		if (o == null) {
			o = new F32leAudioData(d);
			conversionCache.put(d, o);
		}
		return o;
	}



	@Override
	public void setListener(Listener listener) {
		jmeListener = listener;
	}

	@Override
	public void setEnvironment(Environment env) {

	}

	@Override
	public void playSourceInstance(AudioSource src) {
		F32leAudioData data = toF32leData(src.getAudioData());


	}

	@Override
	public void playSource(AudioSource src) {
		F32leAudioData data = toF32leData(src.getAudioData());


	}

	@Override
	public void pauseSource(AudioSource src) {
		F32leAudioData data = toF32leData(src.getAudioData());

	}

	@Override
	public void stopSource(AudioSource src) {

	}

	@Override
	public void updateSourceParam(AudioSource src, AudioParam param) {}


	@Override
	public void update(float tpf) {
		PHONON_LISTENER.update(jmeListener);

	}
	
	


	@Override
	public void updateListenerParam(Listener listener, ListenerParam param) {
		jmeListener = listener;
		switch (param) {
			case Position : {
				PHONON_LISTENER.setPosUpdateNeeded();
				break;
			}
			case Velocity : {
				PHONON_LISTENER.setVelUpdateNeeded();
				break;
			}
			case Rotation : {
				PHONON_LISTENER.setRotUpdateNeeded();
				break;
			}
			case Volume : {
				PHONON_LISTENER.setVolumeUpdateNeeded();
			}
		}
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
	public void pauseAll() {

	}

	@Override
	public void resumeAll() {

	}



}
