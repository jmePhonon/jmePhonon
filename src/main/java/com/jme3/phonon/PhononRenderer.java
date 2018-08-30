package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.WeakHashMap;

import com.jme3.audio.AudioData;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.ListenerParam;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;

/**
 * PhononRenderer
 */
public class PhononRenderer extends Thread implements AudioRenderer {

	int CHANNEL_LIMIT = 1;
    private final Map<AudioData, F32leAudioData> conversionCache = new WeakHashMap<AudioData, F32leAudioData>();
	private final PhononOutputChannel[] channels = new PhononOutputChannel[CHANNEL_LIMIT];

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

	


	public PhononRenderer(int frameSize, int bufferSize) {
		_OUTPUT_FRAME_SIZE = frameSize;
		_OUTPUT_BUFFER_SIZE = bufferSize;
		NativeLibraryLoader.loadNativeLibrary("Phonon", true);
		NativeLibraryLoader.loadNativeLibrary("JMEPhonon", true);
	}

	
	public PhononOutputChannel getChannel(int i) {
		return channels[i];
	}

	public void preInit() {
		initNative();
		for (int i = 0; i < channels.length; i++) {
			channels[i] = new PhononOutputChannel(_OUTPUT_FRAME_SIZE, _OUTPUT_BUFFER_SIZE);
			loadChannelNative(i, channels[i].getAddress(), channels[i].getFrameSize(), channels[i].getBufferSize());
		}
	}

	@Override
	public void initialize() {
		preInit();		
		setDaemon(true);
		start();
	}

	@Override
	public void cleanup() {
		destroyNative();
	}

	public native void initNative();
	public native void updateNative();
	public native void destroyNative();

	/**
	 * @param addr Output buffer address
	 * @param frameSize samples per frame
	 * @param bufferSize total number of frames in this buffer
	 */
	public native void loadChannelNative(int id,long addr,int frameSize,int bufferSize);

	

	public void run() {
		long sleeptime = 1000 / (44100 / _OUTPUT_FRAME_SIZE);
		long lastUpdate = 0;
		while (true) {
			lastUpdate = System.currentTimeMillis();
		
			updateNative();
			long delay=(System.currentTimeMillis()-lastUpdate);
			delay=sleeptime-delay;
			if (delay < 0) {
				System.err.println("FIXME: Phonon is taking too long");
			} else {
				try {
					// System.out.println("Delay " + delay);
					// Thread.sleep(delay);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		
		}
	}


    private F32leAudioData toF32leData(AudioData d) {
        F32leAudioData o=conversionCache.get(d);
        if (o == null) {
            o = new F32leAudioData(d);
            conversionCache.put(d,o);
        }
		return o;
	}


	public PhononRenderer wire(AudioData audioData, int channelId) {
		return wire(toF32leData(audioData), channelId);
	}

	public PhononRenderer wire(F32leAudioData audioData, int channelId) {
		System.out.println("Connect source [" + audioData.getAddress() + "] of size " + audioData.getSizeInSamples()
				+ " samples, to channel " + channelId);

		channels[channelId].reset();
		connectSourceNative(channelId, audioData.getSizeInSamples(),audioData.getAddress());
		return this;
	}

	
	public void connectSourceRaw(int channelId, int length, ByteBuffer source) {
		long addr = DirectBufferUtils.getAddr(source);
		connectSourceNative(channelId, length, addr);
		channels[channelId].reset();

	}

	private native void connectSourceNative(int channelId, int length, long sourceAddr);
	public native void disconnectSourceNative(int channelId);
	
	
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