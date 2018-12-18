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
#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
#error This implementation runs only on little endian machines
#endif

#include "Common.h"

#include "com_jme3_phonon_PhononRenderer.h"

#include "AudioSource.h"
#include "JmePhonon.h"
#include "Listener.h"
#include "OutputLine.h"
#include "Passthrough.h"
#include "UList.h"
#include "settings/settings.h"


struct GlobalSettings SETTINGS;
struct OutputLine *OUTPUT_LINE;
struct Listener *GLOBAL_LISTENER;

struct {
    jfloat *zeroFill;
    jfloat *frame1;
    jfloat *monoFrame1;
    jfloat **mixerQueue;
    jfloat *envframe;

} Temp;



JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env,
                                                                      jobject obj,

                                                                      jlong listenerDataPointer,
                                                                      jlongArray audioSourcesSceneDataArrayPointer,

                                                                      jint nMaterials,
                                                                      jlong materials,
                                                                      jlong outputLineAddr,

                                                                      jobject jSettings) {

    // Collect settings from java                                                                          
    jclass settingsClass = (*env)->GetObjectClass(env, jSettings);
    SETTINGS.nSourcesPerLine = GET_SETTINGS_INT(jSettings, settingsClass, "nSourcesPerLine");
    SETTINGS.nOutputChannels = GET_SETTINGS_INT(jSettings, settingsClass, "nOutputChannels");
    SETTINGS.frameSize = GET_SETTINGS_INT(jSettings, settingsClass, "frameSize");
    SETTINGS.sampleRate = GET_SETTINGS_INT(jSettings, settingsClass, "sampleRate");
    SETTINGS.bufferSize = GET_SETTINGS_INT(jSettings, settingsClass, "bufferSize");
    SETTINGS.isPassthrough = GET_SETTINGS_BOOL(jSettings, settingsClass, "passThrough");

    // Create the listener (from where the sounds are heard)
    GLOBAL_LISTENER = lsNew(&SETTINGS, (jfloat *)(intptr_t)listenerDataPointer);

    // Create and initialize the output line
    OUTPUT_LINE = olNew(&SETTINGS,(jfloat *)(intptr_t)outputLineAddr);
      
    // Allocate the effects frames
    Temp.zeroFill = (jfloat *)calloc( SETTINGS.frameSize * SETTINGS.nOutputChannels,4);
    Temp.frame1 = (jfloat *)calloc( SETTINGS.frameSize * SETTINGS.nOutputChannels,4);
    Temp.envframe = (jfloat *)malloc( SETTINGS.frameSize * SETTINGS.nOutputChannels*4);
    Temp.monoFrame1 = (jfloat *)calloc( SETTINGS.frameSize,4);

    // Allocate the mixer frames
    Temp.mixerQueue = (jfloat **)calloc( SETTINGS.nSourcesPerLine,sizeof(jfloat *));
    for (jint i = 0; i < SETTINGS.nSourcesPerLine; i++) {
        Temp.mixerQueue[i] = (jfloat *)calloc(SETTINGS.frameSize * SETTINGS.nOutputChannels,4);
    }
 
    // Initialize phonon
    phInit(&SETTINGS, SETTINGS.nSourcesPerLine, nMaterials, (jfloat *)(intptr_t)materials, env, jSettings);

    // Allocate and initialize the scene data
    jlong *audioSourcesSceneDataArray = (*env)->GetLongArrayElements(env, audioSourcesSceneDataArrayPointer, 0);
    for (jint j = 0; j < SETTINGS.nSourcesPerLine; j++) {
        jfloat *audioSourceSceneData = (jfloat *)(intptr_t)audioSourcesSceneDataArray[j];
        asSetSceneData(&SETTINGS, &OUTPUT_LINE->sourcesSlots[j], audioSourceSceneData);           
        phInitializeSource(&SETTINGS, &OUTPUT_LINE->sourcesSlots[j]);
    }
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj) {
    // Destroy the sources
    for (jint j = 0; j < SETTINGS.nSourcesPerLine; j++) {
        phDestroySource(&SETTINGS, &OUTPUT_LINE->sourcesSlots[j]);
    }
    
    lsDestroy(&SETTINGS, GLOBAL_LISTENER);
    olDestroy(&SETTINGS, OUTPUT_LINE);
    phDestroy(&SETTINGS);

    free(Temp.frame1);
    free(Temp.envframe);    
    free(Temp.zeroFill);


    free(Temp.monoFrame1);
    for (jint i = 0; i < SETTINGS.nSourcesPerLine; i++) {
        free(Temp.mixerQueue[i]);
    }
    free(Temp.mixerQueue);
}


/**
 * Called by the engine to connect a source
 */
JNIEXPORT jint JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj, jint sizeInSamples, jlong sourceAddr) {
    struct AudioSource *source = olConnectSource(&SETTINGS, OUTPUT_LINE, (jfloat *)(intptr_t)sourceAddr,sizeInSamples);
    phConnectSource(&SETTINGS, source); // We connect the source to phonon
    if (source == NULL) {
        return -1;
    }else{
        return source->id;
    }
}

/**
 * Called by the engine to disconnect a source
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative(JNIEnv *env, jobject obj, jint id) {
    jint sourceId = id;
    struct AudioSource *source = &OUTPUT_LINE->sourcesSlots[sourceId];
    phDisconnectSource(&SETTINGS, source); // This will recycle the phonon slot for other sounds.
    olFinalizeDisconnection(&SETTINGS,OUTPUT_LINE,  source); // This will recycle the audio slot for other sounds.
}


/**
 * This function is used to load the scene mesh for phonon (raytracing)
 */ 
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_setMeshNative(JNIEnv *env, jobject obj, jint nTris, jint nVerts, jlong tris, jlong verts, jlong mat) {
    jint *trisb = (jint *)(intptr_t)tris;
    jfloat *vertsb = (jfloat *)(intptr_t)verts;
    jint *matb = (jint *)(intptr_t)mat;
    phCreateSceneMesh(&SETTINGS, nTris, nVerts, trisb, vertsb, matb);
}

/**
 * This function will unload the mesh
 */ 
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_unsetMeshNative(JNIEnv *env, jobject obj) {
    phDestroySceneMesh(&SETTINGS);
}

/**
 * Debug only function, will write the loaded mesh as OBJ in the specified output path
 * Useful to see if the mesh is loaded correctly.
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_saveMeshAsObjNative(JNIEnv *env, jobject obj, jbyteArray pathArray) {
    jbyte *path = (*env)->GetByteArrayElements(env, pathArray, 0);
    phSaveSceneMeshAsObj(&SETTINGS, path);
}

/**
 * Update loop.
 * It is called by the engine as fast as possible, this function takes care of
 * feeding the data to phonon, get the processed output and write it on the output line. 
 * This function will skip if phonon is processing more data than what the player is able to play.
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
        struct OutputLine *line = OUTPUT_LINE;
        

        jint frameIndex = olGetLastProcessedFrameId(&SETTINGS, line);
        jint lastPlayedFrameIndex = olGetLastPlayedFrameId(&SETTINGS, line);
        jint lineBufferSize = SETTINGS.bufferSize;

        // Processing is too fast, skip.
        if (frameIndex - lastPlayedFrameIndex >= SETTINGS.bufferSize ) {
            return;
        }

        struct UList *uList = line->uList;
        struct UListNode *uNode = uList->head->next;
        jfloat *inFrame;
        jfloat *outFrame;

        jint mixerQueueSize = 0;    
        while (!ulistIsTail(uList, uNode)) {
            struct AudioSource *audioSource = uNode->audioSource;
            jboolean isPlaying = asHasFlag(&SETTINGS, audioSource, PLAYING);
            if (isPlaying) {

                jboolean loop = asHasFlag(&SETTINGS, audioSource, LOOP);
                jint nchannels = asGetNumChannels(&SETTINGS, audioSource);

                inFrame = nchannels == 1 ? Temp.monoFrame1 : Temp.frame1;

                if (asReadNextFrame(&SETTINGS, audioSource, inFrame)) {
                    // Reached end
                    if (!loop) {                       
                        olDisconnectSource(&SETTINGS, OUTPUT_LINE, audioSource, frameIndex);
                    }
                }else{
                    jboolean isPositional = asHasFlag(&SETTINGS, audioSource, POSITIONAL);
                    if (SETTINGS.isPassthrough || !isPositional) {
                        passThrough(&SETTINGS, inFrame,
                        Temp.mixerQueue[mixerQueueSize++],nchannels);
                    } else {
                        //Positional source is always mono
                        phProcessFrame(&SETTINGS, GLOBAL_LISTENER, audioSource, inFrame,Temp.mixerQueue[mixerQueueSize++]);
                    }
                }
            }
            uNode = uNode->next;
        }

        jfloat *masterVolume = lsGetVolume(&SETTINGS, GLOBAL_LISTENER);

        if(mixerQueueSize!=0){
            if(mixerQueueSize==0){
                outFrame = Temp.zeroFill;
            }else if (mixerQueueSize == 1) {
                outFrame = Temp.mixerQueue[0];
            } else {
                outFrame = Temp.frame1;
                if (SETTINGS.isPassthrough) {
                    passThroughMixer(&SETTINGS, Temp.mixerQueue, mixerQueueSize, outFrame);
                } else {
                    phMixOutputBuffers(Temp.mixerQueue, mixerQueueSize, outFrame);
                }
            }


            phGetEnvFrame(&SETTINGS,GLOBAL_LISTENER,Temp.envframe);      

            jfloat *mixerQueue0 = Temp.mixerQueue[0];
            jfloat *mixerQueue1 = Temp.mixerQueue[1];
            Temp.mixerQueue[0] = outFrame;
            Temp.mixerQueue[1] = Temp.envframe;
            
            phMixOutputBuffers(Temp.mixerQueue, 2, Temp.mixerQueue[2]);
            outFrame=Temp.mixerQueue[2];
            
            Temp.mixerQueue[0] = mixerQueue0;
            Temp.mixerQueue[1] = mixerQueue1;
            
                        // outFrame=Temp.envframe;

            olWriteFrame(&SETTINGS, line, frameIndex % lineBufferSize, outFrame, SETTINGS.frameSize * SETTINGS.nOutputChannels, (*masterVolume));
            olSetLastProcessedFrameId(&SETTINGS, line, ++frameIndex);
        }else{
            phGetEnvFrame(&SETTINGS,GLOBAL_LISTENER,Temp.envframe);
            olWriteFrame(&SETTINGS, line, frameIndex % lineBufferSize, Temp.envframe, SETTINGS.frameSize * SETTINGS.nOutputChannels, (*masterVolume));
            olSetLastProcessedFrameId(&SETTINGS, line, ++frameIndex);
        }
    
}
