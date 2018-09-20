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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;

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
import com.jme3.phonon.scene.PhononListener;
import com.jme3.phonon.scene.PhononMesh;
import com.jme3.phonon.thread.PhononExecutor;
import com.jme3.phonon.scene.PhononSourceSlot;
import com.jme3.phonon.scene.material.PhononMaterial;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.utils.F32leCachedConverter;
import com.jme3.phonon.utils.JmeEnvToSndEnv;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;
import com.jme3.util.BufferUtils;

/**
 * PhononRenderer
 */
public class PhononRenderer implements AudioRenderer, PhononUpdater {


	// private final List<PhononPlayer> enqueuedPlayers = new LinkedList<>();

	
	// Mixer lines
	private final PhononSettings SETTINGS;
	private final PhononOutputLine[] OUTPUT_LINES;
	private final PhononSourceSlot[] SOURCES;
	private final PhononSoundPlayer[] PLAYERS;
	private final PhononListener PHONON_LISTENER;
	private PhononMesh sceneMesh;
	private volatile boolean playing=false,renderedInitialized=false;
	
	public PhononRenderer(PhononSettings settings) throws Exception{
		SETTINGS=settings;
		settings.executor.setUpdater(this);
		OUTPUT_LINES=new PhononOutputLine[SETTINGS.nOutputLines];
		PHONON_LISTENER=new PhononListener();
		int nTotalSource = SETTINGS.nOutputLines * SETTINGS.nSourcesPerLine;
		SOURCES=new PhononSourceSlot[nTotalSource];
		PLAYERS=new PhononSoundPlayer[SETTINGS.nOutputLines];

		
	}
	

	public PhononOutputLine getLine(int i) {
		return OUTPUT_LINES[i];
	}
	


	public PhononSourceSlot getSource(int n) {
		return SOURCES[n];
	}

	public PhononSourceSlot getSource(AudioSource src) {
		if(src.getChannel()==-1) return null;
		PhononSourceSlot psrc= getSource(src.getChannel());
		assert psrc.isConnected():"Something is wrong. AudioSource is connected to a  disconnected slot";
		return psrc;
	}
	
	public PhononSourceSlot setSource(int index, AudioSource src) {
		assert index<SOURCES.length:"Not enought source slots. Trying to bind index "+index+" but only "+SOURCES.length+" available";
        SOURCES[index].setLine(OUTPUT_LINES[index/SETTINGS.nSourcesPerLine]);
		SOURCES[index].setSource(src);
		src.setChannel(index);
		return SOURCES[index];
	}
	
	@Override
	public void initialize() {
		SETTINGS.executor.startUpdate();
	}


	void initializeRenderer(){
		try{
			// Materials
			ByteBuffer materials=BufferUtils.createByteBuffer(SETTINGS.materialGenerator.getAllMaterials().size()*PhononMaterial.SERIALIZED_SIZE).order(ByteOrder.nativeOrder());
			for(PhononMaterial mat:SETTINGS.materialGenerator.getAllMaterials())mat.serialize(materials);
			
			// Source slots
			long srcAddrs[]=new long[SOURCES.length];
			for(int i=0;i<SOURCES.length;++i){
				SOURCES[i]=new PhononSourceSlot();
				srcAddrs[i]=SOURCES[i].getAddress();
			}	

			// init
			initNative(SETTINGS.sampleRate,
					OUTPUT_LINES.length,
					SETTINGS.nSourcesPerLine,
					SETTINGS.nOutputChannels,
					SETTINGS.frameSize,
					SETTINGS.bufferSize,
					PHONON_LISTENER.getAddress(),
					srcAddrs,
					SETTINGS.passThrough,
					SETTINGS.materialGenerator.getAllMaterials().size(),
					DirectBufferUtils.getAddr(materials)
			);

			// Output lines
			for(int i=0;i<OUTPUT_LINES.length;i++){
				OUTPUT_LINES[i]=new PhononOutputLine(SETTINGS.frameSize,SETTINGS.nOutputChannels,SETTINGS.bufferSize);
				initLineNative(i,OUTPUT_LINES[i].getAddress());
				if(SETTINGS.initPlayers){ // player
					PLAYERS[i]=SETTINGS.system.newPlayer();
					PLAYERS[i].init(SETTINGS.system,SETTINGS.device,OUTPUT_LINES[i],SETTINGS.sampleRate,SETTINGS.nOutputChannels,SETTINGS.outputSampleSize,SETTINGS.maxPreBuffering);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	@Override
	public void cleanup() {
		SETTINGS.executor.stopUpdate();
		
		for(PhononSoundPlayer p : PLAYERS){
			p.close();
		}

		destroyNative();
	}

	/*Phonon loop*/
	@Override
	public void phononUpdate() {
		if(!renderedInitialized){
			initializeRenderer();
			renderedInitialized=true;
		}

		PHONON_LISTENER.commit(0);

		for(PhononSourceSlot l:SOURCES){
			l.isConnected();
			l.commit(0);
		}

		updateNative();

		if(playing) {
			for(PhononSoundPlayer player:PLAYERS){
				player.loop();
			}
		}
	}

	/* Game loop */
	@Override
	public void update(float tpf) {
		// if(!renderedInitialized) return;
		PHONON_LISTENER.update(0);
		for(PhononSourceSlot sourceData:SOURCES){
			if(!sourceData.isConnected()) continue;
			sourceData.update(0);
			// Check if stopped & unpair
            if (sourceData.getSource() != null && sourceData.getSource().getStatus() == AudioSource.Status.Stopped) {                
		        stop(sourceData.getSource());
            }            
        }
		playing=true;
	}


	/**
	 * Actual method that play the source
	 * @param src
	 * @param instance
	 */
	private void play(AudioSource src,boolean instance){
		PhononSourceSlot psrc=getSource(src);
		if(psrc!=null&&!instance) return;
		if (!instance&& src.getStatus() == AudioSource.Status.Paused) {
			src.setStatus(Status.Playing);
			psrc.setFlagsUpdateNeeded();
			return;
		}
		F32leAudioData data = F32leCachedConverter.toF32le(src.getAudioData());
		int dataIndex=connectSourceData(data);
		
		setSource(dataIndex,src);

		
		if(!instance)src.setStatus(AudioSource.Status.Playing);
	}

	/**
	 * Actual method that stop the source
	 */
	private void stop(AudioSource src) {
		src.setStatus(Status.Stopped);
		SOURCES[src.getChannel()].setSource(null);
		src.setChannel(-1);
	}


	/**
	 * Actual method that pause the source
	 */
	private void pause(AudioSource src) {
		src.setStatus(Status.Paused);
		PhononSourceSlot psrc=getSource(src);
		if(psrc==null) return;
		psrc.setFlagsUpdateNeeded();
	}


	/**
	 * Set the scene mesh for sound occlusion and propagation
	 */
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

	/**
	 * Save the scene mesh as an obj file.
	 * DEBUG ONLY
	 */
	public void saveMeshAsObj(String nativeFileBaseName) {
		saveMeshAsObjNative(nativeFileBaseName.getBytes(Charset.forName("UTF-8")));
	}



	/****** JME INTERNALS ******/
	@Override
	public void setListener(Listener listener) {
		PHONON_LISTENER.setListener(listener);
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
		play(src,true);
	}

	@Override
	public void playSource(AudioSource src) {
		play(src,false);
	}
	
	@Override
	public void pauseSource(AudioSource src) {
		pause(src);
	}

	@Override
	public void stopSource(AudioSource src) {
		stop(src);
	}


	@Override
	public void pauseAll() {
		for(PhononSourceSlot source:SOURCES){
			if(source.isConnected()){
				pauseSource(source.getSource());
			}
		}
	}

	@Override
	public void resumeAll() {
		for(PhononSourceSlot source:SOURCES){
			if(source.isConnected()){
				playSource(source.getSource());
			}
		}
	}

	@Override
	public void updateSourceParam(AudioSource src, AudioParam param) {
		if(src.getChannel() < 0) {
			return;
		}
		PhononSourceSlot psrc=getSource(src);
		if(psrc==null) return;

		switch (param) {
			case IsPositional :
			case IsDirectional :
			case ReverbEnabled :
			case ReverbFilter:
			case Looping:
				psrc.setFlagsUpdateNeeded();
				break;
			case Position:
				if(src.isPositional()) {
					psrc.setPosUpdateNeeded();
				}
				break;
			case Direction:
				if(src.isDirectional()) {
					psrc.setDirUpdateNeeded();
				}
				break;
			case Volume:
				psrc.setVolUpdateNeeded();
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
		PhononSourceSlot psrc=getSource(src);
		if(psrc==null) return;
		switch(param) {
			case DipolePower:
				psrc.setDipolePowerUpdateNeeded();
				break;
			case DipoleWeight:
				psrc.setDipoleWeightUpdateNeeded();
				break;
			default:
				System.err.println("Unrecognized param while updating audio source.");
				return;	
		}
	}


	@Override
	public void updateListenerParam(Listener listener, ListenerParam param) {

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


	/****** HELPERS ******/ 
	/** This will connect a source to phonon renderer but will not play it */
	int connectSourceData(F32leAudioData audioData) {
		System.out.println("Connect source [" + audioData.getAddress() + "] of size "
				+ audioData.getSizeInSamples());
		int length = audioData.getSizeInSamples();
		long addr = audioData.getAddress();

		return connectSourceNative(length, addr);
	}

	/** This will connect a source to phonon renderer but will not play it */

	long connectSourceRaw(int length, ByteBuffer source) {
		long addr=DirectBufferUtils.getAddr(source);
		return connectSourceNative(length,addr);
	}
	
	/** This will  disconnect a source from phonon renderer*/
	void disconnectSourceRaw(long addr) {
		disconnectSourceNative(addr);
	}


	/**** NATIVE METHODS *****/
	native void setEnvironmentNative(float[] envdata);
	native int connectSourceNative(int length, long sourceAddr);
	native void disconnectSourceNative(long addr);
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


}
