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
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.jme3.phonon.thread.ThreadSafeQueue;
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
	
	private volatile Thread phononThread;
	private volatile Thread gameThread;

	private final ThreadSafeQueue GAME_QUEUE=new ThreadSafeQueue();
	private final ThreadSafeQueue PHONON_QUEUE=new ThreadSafeQueue();

	public PhononRenderer(PhononSettings settings) throws Exception{
		SETTINGS=settings;
		settings.executor.setUpdater(this);
		OUTPUT_LINES=new PhononOutputLine[SETTINGS.nOutputLines];
		PHONON_LISTENER=new PhononListener();
		int nTotalSource = SETTINGS.nOutputLines * SETTINGS.nSourcesPerLine;
		SOURCES=new PhononSourceSlot[nTotalSource];
		PLAYERS=new PhononSoundPlayer[SETTINGS.nOutputLines];
		assert (gameThread!=null||(gameThread=Thread.currentThread())!=null);

		
	}
	

	public PhononOutputLine getLine(int i) {
		assert Thread.currentThread()==gameThread;

		return OUTPUT_LINES[i];
	}
	


	// PhononSourceSlot getSource(int n) {
	// 	return ;
	// }

	
	PhononSourceSlot getSourceSlot(AudioSource src) {
		assert Thread.currentThread()==gameThread;

		if(src.getChannel()==-1) return null;
		PhononSourceSlot psrc= SOURCES[src.getChannel()];
		assert psrc.isConnected():"Something is wrong. AudioSource is connected to a  disconnected slot";
		return psrc;
	}
	

	/**
	 * Recycle source slot. Never attempt to pass an arbitrary index to this method, 
	 */
	void recycleSourceSlot(int index) {
		assert Thread.currentThread()==gameThread;        
		SOURCES[index].setSource(null,false);
	}

	
	@Override
	public void initialize() {
		assert Thread.currentThread()==gameThread;
		SETTINGS.executor.startUpdate();		
	}


	void initializePhonon() {
		assert (phononThread!=null||(phononThread=Thread.currentThread())!=null);

		assert Thread.currentThread()==phononThread;

		try{

			// Materials
			ByteBuffer materials=BufferUtils.createByteBuffer(SETTINGS.materialGenerator.getAllMaterials().size()*PhononMaterial.SERIALIZED_SIZE).order(ByteOrder.nativeOrder());
			for(PhononMaterial mat:SETTINGS.materialGenerator.getAllMaterials())mat.serialize(materials);
			
			// Source slots
			long srcAddrs[]=new long[SOURCES.length];
			for(int i=0;i<SOURCES.length;++i){
				SOURCES[i]=new PhononSourceSlot(i);
				srcAddrs[i]=SOURCES[i].getDataAddress();
			}	

			// init
			initNative(
					PHONON_LISTENER.getAddress(),
					srcAddrs,
					SETTINGS.materialGenerator.getAllMaterials().size(),
					DirectBufferUtils.getAddr(materials),
					SETTINGS
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


			for(int i=0;i<SOURCES.length;i++){
				SOURCES[i].setLine(OUTPUT_LINES[i/SETTINGS.nSourcesPerLine]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	@Override
	public void cleanup() {
		assert Thread.currentThread()==gameThread;

		SETTINGS.executor.stopUpdate();
		
		for(PhononSoundPlayer p : PLAYERS){
			p.close();
		}

		destroyNative();
	}

	/*Phonon loop*/
	@Override
	public void phononUpdate() {
		assert (phononThread!=null||(phononThread=Thread.currentThread())!=null);
		if(!renderedInitialized){
			initializePhonon();
			renderedInitialized=true;
		}

		PHONON_QUEUE.run();

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
		assert Thread.currentThread()==gameThread;
		if(!renderedInitialized) return;


		GAME_QUEUE.run();
		PHONON_LISTENER.update(0);
		int i=0;
		for(PhononSourceSlot sourceData:SOURCES){
			if(!sourceData.isConnected()) continue;
			sourceData.update(0);
			// Check if stopped & unpair
			if(sourceData.isOver()||
					(!sourceData.isInstance()&&sourceData.getSource()!=null&&sourceData.getSource().getStatus()==AudioSource.Status.Stopped)){
				// if(!sourceData.isInstance()){
				// 	sourceData.getSource().setStatus(AudioSource.Status.Stopped);
				// 	// sourceData.getSource().setChannel(-1);
				// }
				recycleSourceSlot(i);
				
			}
			i++;    
        }
		playing=true;
	}



	/**
	 * Actual method that play the source
	 * @param src
	 * @param instance
	 */
	private void play(AudioSource src, boolean instance) {
		assert Thread.currentThread()==gameThread;
		

		PhononSourceSlot psrc=getSourceSlot(src);

		if(psrc!=null&&src.getStatus()==AudioSource.Status.Paused){
			src.setStatus(Status.Playing);
			psrc.setFlagsUpdateNeeded();
			return;
		}
		if(psrc!=null&&!instance) return;

		if(src.getStatus()==AudioSource.Status.Playing&&!instance) return;
		src.setStatus(AudioSource.Status.Playing);

		final F32leAudioData data=F32leCachedConverter.toF32le(src.getAudioData());
		PHONON_QUEUE.enqueue(new Runnable(){

			@Override
			public void run() {
				assert Thread.currentThread()==phononThread;
				final int index=playSourceData(data);
				GAME_QUEUE.enqueue(new Runnable(){
					@Override
					public void run() {
						assert Thread.currentThread()==gameThread;
						if(index==-1){ // not enought slots 
							if(!instance)src.setStatus(Status.Stopped);					
						}else{
							assert index<SOURCES.length:"Not enought source slots. Trying to bind index "+index+" but only "+SOURCES.length+" available";
							AudioSource.Status cs=src.getStatus();
							src.setStatus(AudioSource.Status.Stopped);
							SOURCES[index].setSource(src,instance);
							src.setStatus(cs);
						}
					}
				});
			}
		});

	}

	/**
	 * Actual method that stop the source
	 */
	private void stop(AudioSource src) {
		assert Thread.currentThread()==gameThread;

		if(src.getChannel()==-1)return;
		final int id=src.getChannel();
		src.setStatus(Status.Stopped);

		// recycleSourceSlot(id);
		
		PHONON_QUEUE.enqueue(new Runnable(){
			@Override
			public void run() {
				stopSourceData(id);
			}
		});
		// src.setChannel(-1);
	}


	/**
	 * Actual method that pause the source
	 */
	private void pause(AudioSource src) {
		assert Thread.currentThread()==gameThread;
		src.setStatus(Status.Paused);
		PhononSourceSlot psrc=getSourceSlot(src);
		if(psrc==null)return;
		psrc.setFlagsUpdateNeeded();
	}


	/**
	 * Set the scene mesh for sound occlusion and propagation
	 */
	public void setMesh(PhononMesh mesh) {
		assert Thread.currentThread()==gameThread;
		if(sceneMesh!=null){
			PHONON_QUEUE.enqueue(new Runnable(){
				public void run() {
					assert Thread.currentThread()==phononThread;
					unsetMeshNative();
				}
			});
		}

		sceneMesh=mesh;
	
		if(mesh!=null){
			long addr1=DirectBufferUtils.getAddr(mesh.indices),addr2=
			DirectBufferUtils.getAddr(mesh.vertices),
			addr3=DirectBufferUtils.getAddr(mesh.materials);
			PHONON_QUEUE.enqueue(new Runnable(){
				public void run() {
					assert Thread.currentThread()==phononThread;
					setMeshNative(mesh.numTriangles,mesh.numVertices,
						addr1,addr2,addr3
					);
				}
			});
			
		}
	}

	/**
	 * Save the scene mesh as an obj file.
	 * DEBUG ONLY
	 */
	public void saveMeshAsObj(String nativeFileBaseName) {
		assert Thread.currentThread()==gameThread;
		PHONON_QUEUE.enqueue(new Runnable(){
			public void run() {
				assert Thread.currentThread()==phononThread;
				saveMeshAsObjNative(nativeFileBaseName.getBytes(Charset.forName("UTF-8")));
			}
		});
	}



	/****** JME INTERNALS ******/
	@Override
	public void setListener(Listener listener) {
		assert Thread.currentThread()==gameThread;
		PHONON_LISTENER.setListener(listener);
	}

	@Override
	public void setEnvironment(Environment env) {
		assert Thread.currentThread()==gameThread;
		setEnvironmentNative(JmeEnvToSndEnv.convert(env));
	}

	public void setEnvironment(float[] env) {
		assert Thread.currentThread()==gameThread;
		setEnvironmentNative(env);
	}

	
	@Override
	public void playSourceInstance(AudioSource src) {
		assert Thread.currentThread()==gameThread;
		play(src,true);
	}

	@Override
	public void playSource(AudioSource src) {
		assert Thread.currentThread()==gameThread;
		play(src,false);
	}
	
	@Override
	public void pauseSource(AudioSource src) {
		assert Thread.currentThread()==gameThread;
		pause(src);
	}

	@Override
	public void stopSource(AudioSource src) {
		assert Thread.currentThread()==gameThread;
		stop(src);
	}


	@Override
	public void pauseAll() {
		assert Thread.currentThread()==gameThread;
		for(PhononSourceSlot source:SOURCES){
			if(source.isConnected()){
				pauseSource(source.getSource());
			}
		}
	}

	@Override
	public void resumeAll() {
		assert Thread.currentThread()==gameThread;
		for(PhononSourceSlot source:SOURCES){
			if(source.isConnected()){
				playSource(source.getSource());
			}
		}
	}

	@Override
	public void updateSourceParam(AudioSource src, AudioParam param) {
		assert Thread.currentThread()==gameThread;
		if(src.getChannel() < 0) {
			return;
		}
		PhononSourceSlot psrc=getSourceSlot(src);
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
			case Pitch:
				psrc.setPitchUpdateNeeded();;
				break;
			default:
				return;	
		}
	}

	public void updateSourcePhononParam(AudioSource src, PhononAudioParam param) {
		assert Thread.currentThread()==gameThread;
		if(src.getChannel() < 0) {
			return;
		}
		PhononSourceSlot psrc=getSourceSlot(src);
		if(psrc==null) return;
		switch(param) {
			case ApplyAirAbsorption:
			case ApplyDistanceAttenuation:
				psrc.setFlagsUpdateNeeded();
				break;
			case DipolePower:
				psrc.setDipolePowerUpdateNeeded();
				break;
			case DipoleWeight:
				psrc.setDipoleWeightUpdateNeeded();
				break;
			case DirectOcclusionMode:
				psrc.setDirectOcclusionModeNeeded();
				break;
			default:
				System.err.println("Unrecognized Phonon param while updating audio source.");
				return;	
		}
	}


	@Override
	public void updateListenerParam(Listener listener, ListenerParam param) {
		assert Thread.currentThread()==gameThread;
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
		assert Thread.currentThread()==gameThread;
		return 0;
	}

	@Override
	public void deleteFilter(Filter filter) {
		assert Thread.currentThread()==gameThread;

	}

	@Override
	public void deleteAudioData(AudioData ad) {
		assert Thread.currentThread()==gameThread;

	}


	/****** HELPERS ******/ 
	/** This will connect a source to phonon renderer but will not play it */
	int playSourceData(F32leAudioData audioData) {
		assert Thread.currentThread()==phononThread;

		// System.out.println("Connect source ["+audioData.getAddress()+"] of size "+audioData.getSizeInSamples());
		int length=audioData.getSizeInSamples();
		long addr=audioData.getAddress();
		
		return connectSourceNative(length,addr);
	}
	
	void stopSourceData(int id){
		assert Thread.currentThread()==phononThread;		
		disconnectSourceNative(id);			
	}

	/** This will connect a source to phonon renderer but will not play it */

	long playSourceDataRaw(int length, ByteBuffer source) {
		long addr=DirectBufferUtils.getAddr(source);
		return connectSourceNative(length,addr);
	}
	
	/** This will  disconnect a source from phonon renderer*/
	// void stopSourceRaw(long addr) {
	// 	disconnectSourceNative(addr);
	// }


	/**** NATIVE METHODS *****/
	native void setEnvironmentNative(float[] envdata);
	native int connectSourceNative(int length, long sourceAddr);
	native void disconnectSourceNative(int id);
	native void initLineNative(int id, long addr);
	native void updateNative();
	native void initNative(long listenerDataPointer, long[] audioSourcesSceneDataArrayPointer,
			int nMaterials,long materialsAddr,
			PhononSettings settings);
	native void destroyNative();
	native void startThreadNative(boolean decoupled);
	native void setMeshNative(int nTris,int nVerts,long tris,long vert,long mats);
	native void unsetMeshNative();
	native void saveMeshAsObjNative(byte[] fileBaseName);


}
