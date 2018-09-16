/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package com.jme3.phonon;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sound.sampled.LineUnavailableException;

import com.jme3.audio.AudioData;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.AudioSource.Status;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.ListenerParam;
import com.jme3.phonon.Phonon.PhononAudioParam;
import com.jme3.phonon.format.F32leAudioData;
import com.jme3.phonon.scene.PhononAudioSourcesDataManager;
import com.jme3.phonon.scene.PhononListener;
import com.jme3.phonon.scene.PhononMesh;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.utils.JmeEnvToSndEnv;
import com.jme3.scene.Node;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;

/**
 * PhononRenderer
 */
public class PhononRenderer implements AudioRenderer {

	private final Map<AudioData, F32leAudioData> CONVERSION_CACHE =
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

		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Windows32,
				"windows-x86/phonon.dll");
		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Windows64,
				"windows-x86-64/phonon.dll");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Windows32,
				"windows-x86/jmephonon.dll");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Windows64,
				"windows-x86-64/jmephonon.dll");
		
		// TODO: OSX
		// MAYBE TODO: Android
	}

	// Mixer lines
	private final PhononOutputLine[] OUTPUT_LINES;
	private final PhononSoundPlayer[] PLAYERS;

	// Output channels, 1=mono, 2=stereo ..
	private final int OUTPUT_CHANNELS_NUM;
	private final int SOURCES_PER_OUTPUT_LINE;
	// How many samples per frame
	private final int FRAME_SIZE;
	// How many frames per buffer
	private final int BUFFER_SIZE;
	// Samplerate (eg 44100)
	private final int SAMPLE_RATE;
	private final int OUTPUT_SAMPLE_SIZE;
	private final int MAX_PLAYER_PREBUFFERING;

	private final PhononListener PHONON_LISTENER;
	private final PhononAudioSourcesDataManager PHONON_ASDATA_MANAGER;

	private Listener jmeListener;

	private Thread decoderThread;
	final ThreadMode THREAD_MODE;

	boolean SIMULATE_LOAD = false;


	private final PhononSoundSystem SOUND_SYSTEM;
	private final PhononSoundDevice SOUND_DEVICE;

	public PhononRenderer( PhononSettings settings)
			throws Exception{
		SOUND_SYSTEM=settings.system;
		SOUND_DEVICE=settings.device;
		SAMPLE_RATE=settings.sampleRate;
		OUTPUT_LINES=new PhononOutputLine[settings.nOutputLines];
		SOURCES_PER_OUTPUT_LINE=settings.nSourcesPerLine;
		OUTPUT_CHANNELS_NUM=settings.nOutputChannels;
		FRAME_SIZE=settings.frameSize;
		BUFFER_SIZE=settings.bufferSize;
		MAX_PLAYER_PREBUFFERING=settings.maxPreBuffering;
		THREAD_MODE=settings.threadMode;

		OUTPUT_SAMPLE_SIZE=settings.outputSampleSize;

		PHONON_LISTENER=new PhononListener();
		PHONON_ASDATA_MANAGER=new PhononAudioSourcesDataManager(settings.nOutputLines,settings.nSourcesPerLine);

		PLAYERS=new PhononSoundPlayer[settings.nOutputLines];

		NativeLibraryLoader.loadNativeLibrary("Phonon",true);
		NativeLibraryLoader.loadNativeLibrary("JMEPhonon",true);
		// DELTA_S= 1./(44100 / FRAME_SIZE) ;
		initNative(SAMPLE_RATE,OUTPUT_LINES.length,SOURCES_PER_OUTPUT_LINE,OUTPUT_CHANNELS_NUM,FRAME_SIZE,BUFFER_SIZE,THREAD_MODE.isNative,THREAD_MODE.isDecoupled,PHONON_LISTENER.getAddress(),PHONON_ASDATA_MANAGER.memoryAddresses(),
				// Effects
				settings.passThrough);

		for(int i=0;i<OUTPUT_LINES.length;i++){
			OUTPUT_LINES[i]=new PhononOutputLine(FRAME_SIZE,OUTPUT_CHANNELS_NUM,BUFFER_SIZE);
			initLineNative(i,OUTPUT_LINES[i].getAddress());
			if(settings.initPlayers){
				PLAYERS[i]=SOUND_SYSTEM.newPlayer();
				PLAYERS[i].init(SOUND_SYSTEM,SOUND_DEVICE,OUTPUT_LINES[i],SAMPLE_RATE,OUTPUT_CHANNELS_NUM,OUTPUT_SAMPLE_SIZE,MAX_PLAYER_PREBUFFERING);
			}
		}
	}
	

	public PhononOutputLine getLine(int i) {
		return OUTPUT_LINES[i];
	}

	@Override
	public void initialize() {
		if (!THREAD_MODE.isNative || THREAD_MODE.isDecoupled) {
			decoderThread = new Thread(() -> runDecoder());
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
		decoderThread.stop();

		for(PhononSoundPlayer p:PLAYERS){
			p.close();
		}

		unsetScene();
		destroyNative();	
	}

	native void setEnvironmentNative(float[] envdata);

	/**
	 * Connect a source to an outputline
	 * 
	 * @param length     Lenght of the source measured in samples
	 * @param sourceAddr Address of the source
	 * @return Source data buffer index 
	 */
	native int connectSourceNative(int length, long sourceAddr);

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
			boolean decoupledNativeThread, long listenerDataPointer, long[] audioSourcesSceneDataArrayPointer,
			// effects
			boolean isPassThrough);

	native void destroyNative();


	native long createStaticMeshNative(int nTris,int nVerts,long tris,long vert,long mats);
	native void destroyStaticMeshNative(long sceneAddr);

	native void saveSceneAsObjNative(long sceneAddr,byte[] fileBaseName);



	public int connectSource(F32leAudioData audioData) {
		System.out.println("Connect source [" + audioData.getAddress() + "] of size "
				+ audioData.getSizeInSamples());
		int length = audioData.getSizeInSamples();
		long addr = audioData.getAddress();

		return connectSourceNative(length, addr);
	}


	public long connectSourceRaw(int length, ByteBuffer source) {
		long addr = DirectBufferUtils.getAddr(source);
		return connectSourceNative(length, addr);
	}

	public void disconnectSourceRaw(long addr) {
		disconnectSourceNative(addr);
	}

	PhononMesh currentScene;

	public void setScene(PhononMesh mesh) {
		if(currentScene!=null){
			unsetScene();
		}
		currentScene=mesh;
		mesh.nativeAddr=createStaticMeshNative(mesh.numTriangles,
				mesh.numVertices,
				DirectBufferUtils.getAddr(mesh.indices),
				DirectBufferUtils.getAddr(mesh.vertices),
				DirectBufferUtils.getAddr(mesh.materials)
		);
	}

	public void unsetScene() {
		if(currentScene==null) return;
		destroyStaticMeshNative(currentScene.nativeAddr);
		currentScene=null;
	}

	public void saveSceneAsObj(String nativeFileBaseName) {
		saveSceneAsObjNative(currentScene.nativeAddr,nativeFileBaseName.getBytes(Charset.forName("UTF-8")));
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

			PHONON_LISTENER.finalizeUpdate();
			PHONON_ASDATA_MANAGER.finalizeDataUpdates();

			if (SIMULATE_LOAD) {
				try {
					Thread.sleep((int) (Math.random() * 10));
				} catch (Exception e) {
				}
			}
			if (!THREAD_MODE.isNative)
				updateNative();

			if (SIMULATE_LOAD) {
				try {
					Thread.sleep((int) (Math.random() * 10));
				} catch (Exception e) {
				}
			}
			for (PhononSoundPlayer player:PLAYERS){
				byte res = player.loop();
			}



		} while (!THREAD_MODE.isNative || THREAD_MODE.isDecoupled);


	}


	private F32leAudioData toF32leData(AudioData d) {
		F32leAudioData o = CONVERSION_CACHE.get(d);
		if (o == null) {
			o = new F32leAudioData(d);
			CONVERSION_CACHE.put(d, o);
		}
		return o;
	}



	@Override
	public void setListener(Listener listener) {
		jmeListener = listener;
	}

	@Override
	public void setEnvironment(Environment env) {
		setEnvironmentNative(JmeEnvToSndEnv.convert(env));
	}

	public void setEnvironment(float[] env) {
		setEnvironmentNative(env);
	}

	@Override
	public void playSourceInstance(AudioSource src) {
		/*if(!PHONON_AUDIOSOURCES_DATA.hasSourceData(src)) {
			F32leAudioData data = toF32leData(src.getAudioData());

			long slotAddress = this.connectSource(data);
			src.setChannel(1);
		}*/
	}

	@Override
	public void playSource(AudioSource src) {
		if ( src.getStatus() == AudioSource.Status.Paused) {
			src.setStatus(Status.Playing);
			PHONON_ASDATA_MANAGER.setSrcFlagsUpdateNeeded(src);
			return;
		}
		F32leAudioData data = toF32leData(src.getAudioData());
		int dataIndex = connectSource(data);
		PHONON_ASDATA_MANAGER.pairSourceAndData(OUTPUT_LINES[dataIndex/SOURCES_PER_OUTPUT_LINE],src, dataIndex);
		src.setStatus(AudioSource.Status.Playing);
	}

	@Override
	public void pauseSource(AudioSource src) {
		src.setStatus(Status.Paused);
		PHONON_ASDATA_MANAGER.setSrcFlagsUpdateNeeded(src);
	}

	@Override
	public void stopSource(AudioSource src) {
		src.setStatus(AudioSource.Status.Stopped);
	}

	@Override
	public void updateSourceParam(AudioSource src, AudioParam param) {
		if(src.getChannel() < 0) {
			return;
		}

		switch (param) {
			case IsPositional :
			case IsDirectional :
			case ReverbEnabled :
			case ReverbFilter:
			case Looping:
				PHONON_ASDATA_MANAGER.setSrcFlagsUpdateNeeded(src);
				break;
			case Position:
				if(src.isPositional()) {
					PHONON_ASDATA_MANAGER.setSrcPosUpdateNeeded(src);
				}
				break;
			case Direction:
				if(src.isDirectional()) {
					PHONON_ASDATA_MANAGER.setSrcDirUpdateNeeded(src);
				}
				break;
			case Volume:
				PHONON_ASDATA_MANAGER.setSrcVolUpdateNeeded(src);
				break;
			default:
				// System.err.println("Unrecognized param while updating audio source. "+param);
				return;	
		}
	}

	public void updateSourcePhononParam(AudioSource src, PhononAudioParam param) {
		if(src.getChannel() < 0) {
			return;
		}

		switch(param) {
			case DipolePower:
				PHONON_ASDATA_MANAGER.setSrcDipPowerUpdateNeeded(src);
				break;
			case DipoleWeight:
				PHONON_ASDATA_MANAGER.setSrcDipWeightUpdateNeeded(src);
				break;
			default:
				System.err.println("Unrecognized param while updating audio source.");
				return;	
		}
	}

	@Override
	public void update(float tpf) {
		PHONON_LISTENER.update(jmeListener);
		PHONON_ASDATA_MANAGER.updateData();
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
