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
    // jfloat *zeroFill;
    jfloat *tmpFrame;
    jfloat *tmpSkipEnvFrame;
    jfloat *monoFrame1;
    jfloat **mixerQueue;
    jfloat *envframe;

    JNIEnv *javaEnv;
    jobject renderer;
    jmethodID transformDirectSoundCallbackId;
} Temp;

void _java_computeDirectPath(struct GlobalSettings *settings, struct AudioSource *source,drpath* directPath) { 
    asSetDirectPath(settings,source,directPath);
    (*Temp.javaEnv)->CallVoidMethod(Temp.javaEnv, Temp.renderer, Temp.transformDirectSoundCallbackId, source->id);
    asGetDirectPath(settings,source,directPath);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env,
                                                                 jobject renderer,

                                                                      jlong listenerDataPointer,
                                                                      jlongArray audioSourcesSceneDataArrayPointer,

                                                                      jint nMaterials,
                                                                      jlong materials,
                                                                      jlong outputLineAddr,

                                                                      jobject jSettings) {

    // Store env & renderer                                                                          
    Temp.javaEnv=env;
    Temp.renderer=(*env)->NewWeakGlobalRef(env,renderer);

    // Collect settings from java                                                                          
    jclass settingsClass = (*env)->GetObjectClass(env, jSettings);
    SETTINGS.nSourcesPerLine = GET_SETTINGS_INT(jSettings, settingsClass, "nSourcesPerLine");
    SETTINGS.nOutputChannels = GET_SETTINGS_INT(jSettings, settingsClass, "nOutputChannels");
    SETTINGS.frameSize = GET_SETTINGS_INT(jSettings, settingsClass, "frameSize");
    SETTINGS.sampleRate = GET_SETTINGS_INT(jSettings, settingsClass, "sampleRate");
    SETTINGS.isPassthrough = GET_SETTINGS_BOOL(jSettings, settingsClass, "passThrough");

    // Create the listener (from where the sounds are heard)
    GLOBAL_LISTENER = lsNew(&SETTINGS, (jfloat *)(intptr_t)listenerDataPointer);

    // Create and initialize the output line
    OUTPUT_LINE = olNew(&SETTINGS,(jfloat *)(intptr_t)outputLineAddr);
      
    // Allocate the effects frames
    // Temp.zeroFill = (jfloat *)malloc( SETTINGS.frameSize * SETTINGS.nOutputChannels*4);
    // for(jint i=0;i< SETTINGS.frameSize * SETTINGS.nOutputChannels;i++){
    //     Temp.zeroFill[i]=0.f;
    // }
    Temp.tmpFrame = (jfloat *)malloc( SETTINGS.frameSize * SETTINGS.nOutputChannels*4);
    Temp.tmpSkipEnvFrame = (jfloat *)malloc( SETTINGS.frameSize * SETTINGS.nOutputChannels*4);
    Temp.envframe = (jfloat *)malloc( SETTINGS.frameSize * SETTINGS.nOutputChannels*4);
    for(jint i=0;i< SETTINGS.frameSize * SETTINGS.nOutputChannels;i++) Temp.envframe[i]=0.f;
    
    Temp.monoFrame1 = (jfloat *)malloc( SETTINGS.frameSize*4);

    // Allocate the mixer frames
    Temp.mixerQueue = (jfloat **)malloc( SETTINGS.nSourcesPerLine*sizeof(jfloat *));
    for (jint i = 0; i < SETTINGS.nSourcesPerLine; i++) {
        Temp.mixerQueue[i] = (jfloat *)malloc(SETTINGS.frameSize * SETTINGS.nOutputChannels*4);
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

    // Callbacks
    jclass clazz = (*env)->GetObjectClass(env, renderer);
    Temp.transformDirectSoundCallbackId = (*env)->GetMethodID(env, clazz, "_native_transformDirectSoundPath", "(I)V");
    if(Temp.transformDirectSoundCallbackId==0){
        printf("Error! Can't obtain _native_transformDirectSoundPath method id.\n");
        exit(1);
    }
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj) {
    (*env)->DeleteWeakGlobalRef(env,Temp.renderer);

    // Destroy the sources
    for (jint j = 0; j < SETTINGS.nSourcesPerLine; j++) {
        phDestroySource(&SETTINGS, &OUTPUT_LINE->sourcesSlots[j]);
    }
    
    lsDestroy(&SETTINGS, GLOBAL_LISTENER);
    olDestroy(&SETTINGS, OUTPUT_LINE);
    phDestroy(&SETTINGS);

    free(Temp.tmpFrame);
    free(Temp.tmpSkipEnvFrame);

    free(Temp.envframe);    
    // free(Temp.zeroFill);


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
    if (source == NULL) {
        return -1;
    }else{
        phConnectSource(&SETTINGS, source); // We connect the source to phonon
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
        

        jfloat *inFrame;

        jint mixerQueueIndex = 0;    
        jint skipEnvMixerQueueIndex = 0;    
        jfloat *masterVolume = lsGetVolume(&SETTINGS, GLOBAL_LISTENER);

        struct UList *uList = line->uList;
        struct UListNode *uNode = uList->head->next;
        // for(jint si=0;si<SETTINGS.nSourcesPerLine;si++){            
        while (!ulistIsTail(uList, uNode)) {
            // struct AudioSource *audioSource = &line->sourcesSlots[si];
            struct AudioSource *audioSource = uNode->audioSource;
            if(asIsReady(&SETTINGS,audioSource)){
                jboolean isPlaying = asHasFlag(&SETTINGS, audioSource, PLAYING);
                if (isPlaying ) {
                    // jboolean loop = asHasFlag(&SETTINGS, audioSource, LOOP);
                    jint nchannels = asGetNumChannels(&SETTINGS, audioSource);
                    inFrame = nchannels == 1 ? Temp.monoFrame1 : Temp.tmpFrame;
                    if (asReadNextFrame(&SETTINGS, audioSource,  (*masterVolume), inFrame)) {
                        // if (!loop) {                       
                        olDisconnectSource(&SETTINGS, OUTPUT_LINE, audioSource);
                        // }else{
                        //     asResetForLoop(&SETTINGS,audioSource);
                        // }
                    }    

                    jboolean isPositional = asHasFlag(&SETTINGS, audioSource, POSITIONAL);
                    if(!isPositional){
                        jint nchannels = asGetNumChannels(&SETTINGS, audioSource);
                        passThrough(&SETTINGS, inFrame, Temp.mixerQueue[(SETTINGS.nSourcesPerLine ) - (++skipEnvMixerQueueIndex)  ], nchannels);
                    } else { // Positionals are always mono                    
                        void (*directPathFun)(struct GlobalSettings*,struct AudioSource*,drpath*)=NULL;
                        if(asHasFlag(&SETTINGS,audioSource,USE_DIRECTPATH_FUNCTION)) directPathFun=&_java_computeDirectPath;                  
                        phProcessFrame(&SETTINGS, GLOBAL_LISTENER, audioSource, inFrame,Temp.mixerQueue[mixerQueueIndex++],directPathFun);
                    }
                }
            }            
            uNode = uNode->next;
        }

  
    
    
        jfloat *outFrame;
        jfloat *outSkipEnvFrame;

        jboolean hasFrame1=false;
        jboolean hasFrame2=false;

        if (mixerQueueIndex != 0) {
            hasFrame1 = true;
            if (mixerQueueIndex == 1) {
                outFrame = Temp.mixerQueue[0];
            } else {
                outFrame = Temp.tmpFrame;
                phMixOutputBuffers(Temp.mixerQueue, 0,mixerQueueIndex, outFrame);
            }
            phCalculateListenerCentricReverb(&SETTINGS, GLOBAL_LISTENER, outFrame);
        }
        if(skipEnvMixerQueueIndex!=0){
            hasFrame2=true;
            if (skipEnvMixerQueueIndex ==1) {
                outSkipEnvFrame = Temp.mixerQueue[SETTINGS.nSourcesPerLine - skipEnvMixerQueueIndex ];
            } else {
                outSkipEnvFrame = Temp.tmpSkipEnvFrame;
                phMixOutputBuffers(Temp.mixerQueue,SETTINGS.nSourcesPerLine  - skipEnvMixerQueueIndex , skipEnvMixerQueueIndex, outSkipEnvFrame);
            }
        }
        if(hasFrame1||hasFrame2){
            phGetEnvFrame(&SETTINGS,GLOBAL_LISTENER,Temp.envframe);
            
            jfloat *mixerQueue0 = Temp.mixerQueue[0];
            jfloat *mixerQueue1 = Temp.mixerQueue[1];
            jfloat *mixerQueue2 = Temp.mixerQueue[2];

            jint qi = 0;
            if(hasFrame1)Temp.mixerQueue[qi++] = outFrame;            
            if(hasFrame2)Temp.mixerQueue[qi++] = outSkipEnvFrame;
            Temp.mixerQueue[qi++] = Temp.envframe;

            phMixOutputBuffers(Temp.mixerQueue,0, qi, Temp.mixerQueue[qi]);
            outFrame=Temp.mixerQueue[qi];

            Temp.mixerQueue[0] = mixerQueue0;
            Temp.mixerQueue[1] = mixerQueue1;
            Temp.mixerQueue[2] = mixerQueue2;
            
            olWriteFrame(&SETTINGS, line, outFrame, SETTINGS.frameSize * SETTINGS.nOutputChannels,1.f);
        }else{
            phGetEnvFrame(&SETTINGS, GLOBAL_LISTENER, Temp.envframe);
            outFrame = Temp.envframe;
            olWriteFrame(&SETTINGS, line, outFrame, SETTINGS.frameSize * SETTINGS.nOutputChannels,1.f);
        }
        // else{
        //     outFrame = Temp.zeroFill;
        //     olWriteFrame(&SETTINGS, line, outFrame, SETTINGS.frameSize * SETTINGS.nOutputChannels, (*masterVolume));
        // }
    
}

