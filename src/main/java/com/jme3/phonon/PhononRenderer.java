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
import com.jme3.audio.AudioNode;
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
import com.jme3.phonon.manager.AudioManager;
import com.jme3.phonon.scene.PhononListener;
import com.jme3.phonon.scene.PhononMesh;
import com.jme3.phonon.thread.PhononExecutor;
import com.jme3.phonon.thread.ThreadSafeQueue;
import com.jme3.phonon.scene.PhononSourceSlot;
import com.jme3.phonon.scene.emitters.SoundEmitterControl;
import com.jme3.phonon.scene.material.PhononMaterial;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.utils.F32leCachedConverter;
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
	private final PhononOutputLine OUTPUT_LINE;
	private final PhononSourceSlot[] SOURCES;
	private final PhononSoundPlayer PLAYER;
	private final PhononListener PHONON_LISTENER;
	private PhononMesh sceneMesh;
	private volatile boolean renderedInitialized=false;
	
	private volatile Thread phononThread;
	private volatile Thread gameThread;

	private final ThreadSafeQueue GAME_QUEUE=new ThreadSafeQueue();
	private final ThreadSafeQueue PHONON_QUEUE=new ThreadSafeQueue();

	private AudioManager mng;

	public PhononRenderer(PhononSettings settings) throws Exception{
		SETTINGS=settings;
		settings.executor.setUpdater(this);
		OUTPUT_LINE=new PhononOutputLine(settings.frameSize,settings.nOutputChannels);
		PHONON_LISTENER=new PhononListener();
		SOURCES=new PhononSourceSlot[SETTINGS.nSourcesPerLine];
		PLAYER=settings.system.newPlayer();
		assert (gameThread!=null||(gameThread=Thread.currentThread())!=null);
	}

	public void setAudioManager(AudioManager mng) {
		this.mng=mng;
	}
	

	public PhononOutputLine getOutputLine() {
		assert Thread.currentThread()==gameThread;
		return OUTPUT_LINE;
	}
	


	// PhononSourceSlot getSource(int n) {
	// 	return ;
	// }

	
	PhononSourceSlot getSourceSlot(AudioSource src) {
		assert Thread.currentThread()==gameThread;
		if(src.getChannel()==-1) return null;
		PhononSourceSlot psrc= SOURCES[src.getChannel()];
		// assert psrc.isReady():"Something is wrong. AudioSource is connected to a  disconnected slot"; 
		return psrc;
	}
	

	/**
	 * Recycle source slot. Never attempt to pass an arbitrary index to this method, 
	 */

	
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
					OUTPUT_LINE.getAddress(),
					SETTINGS
			);

			if(SETTINGS.initPlayers){ // player
				PLAYER.init(SETTINGS,SETTINGS.system,SETTINGS.device,OUTPUT_LINE,SETTINGS.sampleRate,SETTINGS.nOutputChannels,SETTINGS.frameSize,SETTINGS.outputSampleSize);
			}
			
			

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	@Override
	public void cleanup() {
		assert Thread.currentThread()==gameThread;

		SETTINGS.executor.stopUpdate();
		
		PLAYER.close();
		

		destroyNative();
	}

	/*Phonon loop*/
	long avg_t_sum=0;
	long avg_t_count=0;

	@Override
	public void phononUpdate() {
	
		long t=System.currentTimeMillis();
		// assert (phononThread!=null||(phononThread=Thread.currentThread())!=null);
		if(!renderedInitialized){
			initializePhonon();
			renderedInitialized=true;
		}
				
		PHONON_QUEUE.run();

		PHONON_LISTENER.commit(0);

		for(PhononSourceSlot l:SOURCES){
			if(l.isReady()&&!l.isMarkedForDisconnection())l.commit(0);
		}

		updateNative();

		for(PhononSourceSlot l:SOURCES){
			if(l.isReady()&&!l.isMarkedForDisconnection()) l.checkIfMarkedForDisconnection(); // check if over
		}
		
		PLAYER.play(OUTPUT_LINE.getFrame(),OUTPUT_LINE.getFrameSize(),OUTPUT_LINE.getChannels());
		
		long pavg_t=avg_t_count==0?0:avg_t_sum/avg_t_count;
		avg_t_sum+=System.currentTimeMillis()-t;
		avg_t_count++;
		long cavg_t=avg_t_sum/avg_t_count;
		if(pavg_t!=0&&cavg_t-pavg_t>10){
			System.out.println("Phonon thread is slowing down. Last tick was "+cavg_t+"ms while avg is "+pavg_t+"ms ");
		}
	}

	/* Game loop */
	@Override
	public void update(float tpf) {
		assert Thread.currentThread()==gameThread;
		if(!renderedInitialized) return;


		GAME_QUEUE.run();
		PHONON_LISTENER.update(0);
		
		for(PhononSourceSlot sourceData:SOURCES){
			if(!sourceData.isReady()) continue;
			// Check if stopped & unpair
			if(sourceData.isMarkedForDisconnection()){
				// System.out.println("Recycle because its over");
				if(sourceData.isInstance()){
					stopInstance(sourceData.getId());
				}else if(sourceData.getSource()!=null&&sourceData.getSource().getStatus()!=AudioSource.Status.Stopped){
					stop(sourceData.getSource());
				}			
			}else{
				sourceData.update(0);		
			}
        }
	}



	/**
	 * Actual method that play the source
	 * @param src
	 * @param instance
	 */
	private void play(AudioSource src, boolean instance) {
		assert Thread.currentThread()==gameThread;
		
		int sampleRate;
		if(src instanceof SoundEmitterControl){
			sampleRate=((SoundEmitterControl)src).getF32leAudioData().getSampleRate();
		}else{
			sampleRate=src.getAudioData().getSampleRate();
		}

		if(sampleRate!=SETTINGS.sampleRate){ 
			throw new IllegalStateException("Input audio "+((src instanceof AudioNode)?((AudioNode)src).getName():src)+" sample rate is "+src.getAudioData().getSampleRate()+" but the renderer is configured to use "+SETTINGS.sampleRate); 
		}

		// You can't play a source twice unless it is an instance.
		if(!instance&&src.getStatus()==AudioSource.Status.Playing){
			// System.out.println(src+" is already playing");
			return;			 
		}		

		PhononSourceSlot psrc=getSourceSlot(src);

		// Save the current state
		AudioSource.Status currentStatus=src.getStatus();

		// We update the state to Playing.
		if(!instance)src.setStatus(AudioSource.Status.Playing);
		
		// If paused we just update the state flag 
		if(psrc!=null&&currentStatus==AudioSource.Status.Paused){
			psrc.setFlagsUpdateNeeded();
			return;
		}		


		// Retrieve the audio data
		final F32leAudioData data;		
		if(src instanceof SoundEmitterControl){
			SoundEmitterControl emitter=(SoundEmitterControl)src;
			data=emitter.getF32leAudioData();
		}else{
			data=F32leCachedConverter.toF32le(src.getAudioData());
		}

		// We need to run the following code on phonon's thread.
		PHONON_QUEUE.enqueue(new Runnable(){
			@Override
			public void run() {
				assert Thread.currentThread()==phononThread;
				// We connect the audio data to phonon.
				final int index=connectSourceData(data);


				if(index==-1){
					if(!instance){
						// Failed, something went wrong. Probably no suitable/free slot.
						GAME_QUEUE.enqueue(() -> {
							// For now, just stop it 
							// TODO
							src.setStatus(AudioSource.Status.Stopped);
						});
					}
				}else{
					// The source is connected, at slot n `index`
					SOURCES[index].waitingForFinalization(true); 
						GAME_QUEUE.enqueue(() -> {
							SOURCES[index].setSource(src,instance);
							if(!instance){
								Status st=src.getStatus();
								src.setStatus(AudioSource.Status.Stopped);
								src.setChannel(index);
								src.setStatus(st);
							}							
							SOURCES[index].waitingForFinalization(false);

					});
				}
			}
		});

	}


	private void stopInstance(int index) {
		assert Thread.currentThread()==gameThread;
		SOURCES[index].waitingForFinalization(true); 
		SOURCES[index].setSource(null,false);

		PHONON_QUEUE.enqueue(new Runnable(){
			@Override
			public void run() {
				// Disconnect the source from phonon
				SOURCES[index].commit(0);
				disconnectSourceNative(index);
				SOURCES[index].waitingForFinalization(false); 
		
			}
		});
	}

	/**
	 * Actual method that stop the source
	 */
	private void stop(AudioSource src) {
		assert Thread.currentThread()==gameThread;

		final int id=src.getChannel();
		if(id==-1) return; //nb channel is `volatile` in jme's audio node implementation
		SOURCES[id].waitingForFinalization(true);

		src.setStatus(Status.Stopped);

		SOURCES[id].setSource(null,false);
		
		src.setChannel(-1);

		PHONON_QUEUE.enqueue(new Runnable(){
			@Override
			public void run() {
				SOURCES[id].commit(0);

				// Disconnect the source from phonon
				disconnectSourceNative(id);
				SOURCES[id].waitingForFinalization(false); 	
			}
		});
	}


	/**
	 * Actual method that pause the source
	 */
	private void pause(AudioSource src) {
		assert Thread.currentThread()==gameThread;
		src.setStatus(Status.Paused);
	
		// Pause is just a flag.
		PhononSourceSlot psrc=getSourceSlot(src);
		if(psrc==null) return;
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
			if(source.isReady()){
				pauseSource(source.getSource());
			}
		}
	}

	@Override
	public void resumeAll() {
		assert Thread.currentThread()==gameThread;
		for(PhononSourceSlot source:SOURCES){
			if(source.isReady()){
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

		switch(param){
			case IsPositional:
			case IsDirectional:
			case ReverbEnabled:
			case ReverbFilter:
			case Looping:
				psrc.setFlagsUpdateNeeded();
				break;
			case Position:
				if(src.isPositional()){
					psrc.setPosUpdateNeeded();
				}
				break;
			case Direction:
				if(src.isDirectional()){
					psrc.setDirUpdateNeeded();
				}
				break;
			case Volume:
				psrc.setVolUpdateNeeded();
				break;
			case Pitch:
				psrc.setPitchUpdateNeeded();;
				break;
			// default:
			// 	return;
		}
		if(src instanceof SoundEmitterControl&&mng!=null){
			SoundEmitterControl emitter=(SoundEmitterControl)src;
			mng.updateSource(emitter);
		}

	}

	public void updateSourcePhononParam(AudioSource src, PhononAudioParam param) {
		assert Thread.currentThread()==gameThread;
		if(src.getChannel() < 0) {
			return;
		}
		PhononSourceSlot psrc=getSourceSlot(src);
		if(psrc==null) return;
		switch(param){
			case ApplyAirAbsorption:
				psrc.setFlagsUpdateNeeded();
				break;
			case DipolePower:
				psrc.setDipolePowerUpdateNeeded();
				break;
			case DipoleWeight:
				psrc.setDipoleWeightUpdateNeeded();
				break;
			case DirectOcclusionMode:
				psrc.setDirectOcclusionModeUpdateNeeded();
				break;
			case DirectOcclusionMethod:
				psrc.setDirectOcclusionMethodUpdateNeeded();
				break;
			case SourceRadius:
				psrc.setSourceRadiusUpdateNeeded();
				break;
			default:
				// System.err.println("Unrecognized Phonon param while updating audio source.");
				// return;
		}
		if(src instanceof SoundEmitterControl&&mng!=null){
			SoundEmitterControl emitter=(SoundEmitterControl)src;
			mng.updateSource(emitter);
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
	int connectSourceData(F32leAudioData audioData) {
		assert Thread.currentThread()==phononThread;

		// System.out.println("Connect source ["+audioData.getAddress()+"] of size "+audioData.getSizeInSamples());
		int length=audioData.getSizeInSamples();
		long addr=audioData.getAddress();
		
		return connectSourceNative(length,addr);
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
	native int connectSourceNative(int length, long sourceAddr);
	native void disconnectSourceNative(int id);
	native void updateNative();
	native void initNative(long listenerDataPointer, long[] audioSourcesSceneDataArrayPointer,
			int nMaterials,long materialsAddr,long outputLineAddr,
			PhononSettings settings);
	native void destroyNative();
	native void startThreadNative(boolean decoupled);
	native void setMeshNative(int nTris,int nVerts,long tris,long vert,long mats);
	native void unsetMeshNative();
	native void saveMeshAsObjNative(byte[] fileBaseName);

	public AudioManager getMng() {
		return mng;
	}


}
