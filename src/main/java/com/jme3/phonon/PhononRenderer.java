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
import java.nio.ByteOrder;
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
import com.jme3.phonon.scene.material.PhononMaterial;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.utils.F32leCachedConverter;
import com.jme3.phonon.utils.JmeEnvToSndEnv;
import com.jme3.scene.Node;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;
import com.jme3.util.BufferUtils;

/**
 * PhononRenderer
 */
public class PhononRenderer implements AudioRenderer, Runnable {


	// private final List<PhononPlayer> enqueuedPlayers = new LinkedList<>();

	
	// Mixer lines
	private final PhononOutputLine[] OUTPUT_LINES;
	private final PhononSoundPlayer[] PLAYERS;
	private final PhononListener PHONON_LISTENER;
	private final PhononAudioSourcesDataManager PHONON_ASDATA_MANAGER;
	private PhononJavaThread decoderThread;
	private Listener jmeListener;



	private volatile boolean playing=false;

	
	private final PhononSettings SETTINGS;
	public PhononRenderer( PhononSettings settings)
			throws Exception{
				this.SETTINGS=settings;

		OUTPUT_LINES=new PhononOutputLine[SETTINGS.nOutputLines];
		

		PHONON_LISTENER=new PhononListener();
		PHONON_ASDATA_MANAGER=new PhononAudioSourcesDataManager(SETTINGS.nOutputLines,SETTINGS.nSourcesPerLine);

		PLAYERS=new PhononSoundPlayer[SETTINGS.nOutputLines];

		ByteBuffer materials=BufferUtils.createByteBuffer(settings.materialGenerator.getAllMaterials().size()*PhononMaterial.SERIALIZED_SIZE).order(ByteOrder.nativeOrder());
		for(PhononMaterial mat:settings.materialGenerator.getAllMaterials()){
			mat.serialize(materials);
		}
		// DELTA_S= 1./(44100 / settings.frameSize) ;
	
		initNative(SETTINGS.sampleRate,OUTPUT_LINES.length,SETTINGS.nSourcesPerLine,
				SETTINGS.nOutputChannels,SETTINGS.frameSize,SETTINGS.bufferSize,
		PHONON_LISTENER.getAddress(),PHONON_ASDATA_MANAGER.memoryAddresses(),
		// Effects
		SETTINGS.passThrough,settings.materialGenerator.getAllMaterials().size(),DirectBufferUtils.getAddr(materials));

		for(int i=0;i<OUTPUT_LINES.length;i++){
			OUTPUT_LINES[i]=new PhononOutputLine(SETTINGS.frameSize,SETTINGS.nOutputChannels,SETTINGS.bufferSize);
			initLineNative(i,OUTPUT_LINES[i].getAddress());
			if(settings.initPlayers){
				PLAYERS[i]=settings.system.newPlayer();
				PLAYERS[i].init(SETTINGS.system,SETTINGS.device,OUTPUT_LINES[i],SETTINGS.sampleRate,SETTINGS.nOutputChannels,SETTINGS.outputSampleSize,SETTINGS.maxPreBuffering);
			}
		}

	
	}
	

	public PhononOutputLine getLine(int i) {
		return OUTPUT_LINES[i];
	}
	
	@Override
	public void initialize() {
		if(SETTINGS.threadMode==ThreadMode.NONE) return;
		if (!SETTINGS.threadMode.isNative || SETTINGS.threadMode.isDecoupled) {
			decoderThread = new PhononJavaThread(this);

			decoderThread.setName("Phonon Java Thread");
			decoderThread.setPriority(Thread.MAX_PRIORITY);
			decoderThread.setDaemon(true);
			decoderThread.start();
		}
		
		if(SETTINGS.threadMode.isNative){
			startThreadNative(SETTINGS.threadMode.isDecoupled);
		}
	}

	@Override
	public void run() {
		do {
			if (!SETTINGS.threadMode.isNative || SETTINGS.threadMode.isDecoupled) {
				try {
					Thread.sleep(1);
				} catch (Exception e) { }
			}

			PHONON_LISTENER.finalizeUpdate();
			PHONON_ASDATA_MANAGER.finalizeDataUpdates();

			if (!SETTINGS.threadMode.isNative)
				updateNative();
	
			if(playing){
				for(PhononSoundPlayer player:PLAYERS){
					player.loop();
				}
			}
		} while ((!SETTINGS.threadMode.isNative || SETTINGS.threadMode.isDecoupled) && decoderThread.isUpdating());
	}

	@Override
	public void cleanup() {
		if(decoderThread != null) {
			decoderThread.stopUpdate();
			
			do {
				try {
					Thread.sleep(1);
				} catch(Exception e) {
					e.printStackTrace();
				}
			} while(decoderThread.isAlive());
		}

		for(PhononSoundPlayer p:PLAYERS){
			p.close();
		}
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
			int nOutputChannels, int frameSize, int bufferSize,  long listenerDataPointer, 
			long[] audioSourcesSceneDataArrayPointer,
			// effects
			boolean isPassThrough,int nMaterials,long materialsAddr);

	native void destroyNative();
	native void startThreadNative(boolean decoupled);


	native void setMeshNative(int nTris,int nVerts,long tris,long vert,long mats);
	native void unsetMeshNative();

	native void saveMeshAsObjNative(byte[] fileBaseName);

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

	PhononMesh sceneMesh;

	public void setMesh(PhononMesh mesh) {
		if(sceneMesh!=null)unsetMeshNative();		
		sceneMesh=mesh;
		if(mesh!=null){
			setMeshNative(mesh.numTriangles,mesh.numVertices,
					DirectBufferUtils.getAddr(mesh.indices),
					DirectBufferUtils.getAddr(mesh.vertices),
					DirectBufferUtils.getAddr(mesh.materials)
			);
		}
	}

	public void saveMeshAsObj(String nativeFileBaseName) {
		saveMeshAsObjNative(nativeFileBaseName.getBytes(Charset.forName("UTF-8")));
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
		F32leAudioData data = F32leCachedConverter.toF32le(src.getAudioData());
		int dataIndex = connectSource(data);
		PHONON_ASDATA_MANAGER.pairSourceAndData(OUTPUT_LINES[dataIndex/SETTINGS.nSourcesPerLine],src, dataIndex);
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
		playing=true;
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
