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

#ifdef INCLUDE_SIMPLE_REVERB
#include "ext/ext_SimpleReverb.h"
#endif

struct GlobalSettings SETTINGS;
struct OutputLine *OUTPUT_LINES;
struct Listener *GLOBAL_LISTENER;

struct {
    jfloat *frame1;
    jfloat *monoFrame1;
    jfloat **mixerQueue;
#ifdef INCLUDE_SIMPLE_REVERB
    jfloat *frame2;
    // Since we apply simple reverb after the final mixing we need two queues to slit sounds with and without reverb
    // we will apply reverb only to the first queue, and then we will mix the two queues together.
    jfloat **reverbMixerQueue;
#endif
} Temp;

JNIEXPORT jint JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj, jint size, jlong sourceAddr) {
    struct AudioSource *source = olConnectSourceToBestLine(&SETTINGS, OUTPUT_LINES, SETTINGS.nOutputLines,
                                                           (jfloat *)(intptr_t)sourceAddr, size);
    // Reset stop at

    if (source == NULL){
        return -1;
    }else{
        phFlushSource(&SETTINGS, source);
        return source->sourceIndex;
    }
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative(JNIEnv *env, jobject obj, jint id) {
    jint lineId = id / SETTINGS.nSourcesPerLine;
    jint sourceId=id-(lineId*SETTINGS.nSourcesPerLine);
    struct AudioSource *source = &OUTPUT_LINES[lineId].sourcesSlots[sourceId];
    olDisconnectSource(&SETTINGS, source);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initLineNative(JNIEnv *env,
                                                                          jobject obj, jint lineId, jlong outputBufferAddr) {
    olInit(&SETTINGS, &OUTPUT_LINES[lineId], (jfloat *)(intptr_t)outputBufferAddr);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_setEnvironmentNative(JNIEnv *env, jobject obj, jfloatArray envdata) {
    #ifdef INCLUDE_SIMPLE_REVERB
    jfloat *envdataraw = (*env)->GetFloatArrayElements(env, envdata, JNI_FALSE);
    srSetEnvironment(&SETTINGS, envdataraw);
    #endif
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_setMeshNative(JNIEnv *env, jobject obj, jint nTris, jint nVerts, jlong tris, jlong verts, jlong mat) {
    jint *trisb = (jint *)(intptr_t)tris;
    jfloat *vertsb = (jfloat *)(intptr_t)verts;
    jint *matb = (jint *)(intptr_t)mat;
    phCreateSceneMesh(&SETTINGS, nTris, nVerts, trisb, vertsb, matb);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_unsetMeshNative(JNIEnv *env, jobject obj) {
    phDestroySceneMesh(&SETTINGS);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_saveMeshAsObjNative(JNIEnv *env, jobject obj, jbyteArray pathArray) {
    jbyte *path = (*env)->GetByteArrayElements(env, pathArray, 0);
    phSaveSceneMeshAsObj(&SETTINGS, path);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
    for (jint i = 0; i < SETTINGS.nOutputLines; i++) {
        struct OutputLine *line = &OUTPUT_LINES[i];
        if (!olIsInitialized(&SETTINGS, line))
            continue;

        jint frameIndex = olGetLastProcessedFrameId(&SETTINGS, line);
        jint lastPlayedFrameIndex = olGetLastPlayedFrameId(&SETTINGS, line);
        jint lineBufferSize = SETTINGS.bufferSize;

        // Processing is too fast, skip.
        if (frameIndex - lastPlayedFrameIndex >= SETTINGS.bufferSize ) {
            continue;
        }

        struct UList *uList = line->uList;
        struct UListNode *uNode = uList->head->next;
        jfloat *inFrame;
        jfloat *outFrame;

        jint mixerQueueSize = 0;
        #ifdef INCLUDE_SIMPLE_REVERB
        jint reverbMixerQueueSize = 0;
        #endif

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
                        asSetStopAt(&SETTINGS, audioSource, frameIndex);
                        // olDisconnectSource(&SETTINGS, audioSource);
                        ulistRemove(uNode);
                    }
                }
                jboolean isPositional = asHasFlag(&SETTINGS, audioSource, POSITIONAL);
                #ifdef INCLUDE_SIMPLE_REVERB
                jboolean hasReverb = asHasFlag(&SETTINGS, audioSource, REVERB);
                #endif
                if (SETTINGS.isPassthrough || !isPositional) {
                    passThrough(&SETTINGS, inFrame,
                    #ifdef INCLUDE_SIMPLE_REVERB
                                ((isPositional && hasReverb) ? Temp.reverbMixerQueue[reverbMixerQueueSize++] : Temp.mixerQueue[mixerQueueSize++])
                    #else
                                Temp.mixerQueue[mixerQueueSize++]
                    #endif
                                    ,
                                nchannels);
                } else {
                    //Positional source is always mono
                    phProcessFrame(&SETTINGS, GLOBAL_LISTENER, audioSource, inFrame,
                    #ifdef INCLUDE_SIMPLE_REVERB
                                   ((isPositional && hasReverb) ? Temp.reverbMixerQueue[reverbMixerQueueSize++] : Temp.mixerQueue[mixerQueueSize++])
                    #else
                                   Temp.mixerQueue[mixerQueueSize++]
                    #endif
                    );
                }
            }

            uNode = uNode->next;
        }

        if (mixerQueueSize == 1) {
            outFrame = Temp.mixerQueue[0];
        } else {
            outFrame = Temp.frame1;
            if (SETTINGS.isPassthrough) {
                passThroughMixer(&SETTINGS, Temp.mixerQueue, mixerQueueSize, outFrame);
            } else {
                phMixOutputBuffers(Temp.mixerQueue, mixerQueueSize, outFrame);
            }
        }

        // At this point outFrame points to the mixed output (that can be stored either in Temp.frame1 or Temp.mixerQueue[0])
        #ifdef INCLUDE_SIMPLE_REVERB
        // We need to mix the simple reverb queue to the outFrame

        if (reverbMixerQueueSize > 0) { // only if there is something to mix...
            // We will store the mixed result of reverbMixerQueue in Temp.frame2.
            if (SETTINGS.isPassthrough) {
                passThroughMixer(&SETTINGS, Temp.reverbMixerQueue, reverbMixerQueueSize, Temp.frame2);
            } else {
                phMixOutputBuffers(Temp.reverbMixerQueue, reverbMixerQueueSize, Temp.frame2);
            }

            // Now Temp.frame2 contains the mixed reverb queue.

            // For the next step we'll need to reuse the first three slots of the reverMixerQueue
            // The first two slots may be changed to point somewhere else, so we store here their actual addresses
            // to revert them back later
            jfloat *mixerQueue0 = Temp.reverbMixerQueue[0];
            jfloat *mixerQueue1 = Temp.reverbMixerQueue[1];

            if (srHasValidEnvironment(&SETTINGS)) {                              // if reverb is enabled
                srApplyReverb(&SETTINGS, Temp.frame2, Temp.reverbMixerQueue[0]); //we apply the reverb on the mixed result ( Temp.frame2) and we store the result on Temp.reverbMixerQueue[0]
            } else {                                                             // if reverb is disabled,we will just make the first slot of the reverbMixerQueue point to Temp.frame2
                Temp.reverbMixerQueue[0] = Temp.frame2;                          // thats why we saved the addr before.
            }
            Temp.reverbMixerQueue[1] = outFrame; // We make the second slot of reverbMixerQueue point to
                                                 // outFrame (that can be either a pointer to Temp.frame1 or a pointer to Temp.mixerQueue[0])
                                                 // thats why we saved the addr before...

            // Now we'll mix the first two slots of the reverbMixerQueue and we'll save the result on the third slot of reverbMixerQueue
            reverbMixerQueueSize = 2; // we need to mix only the first 2 slots

            if (SETTINGS.isPassthrough) {
                passThroughMixer(&SETTINGS, Temp.reverbMixerQueue, reverbMixerQueueSize, Temp.reverbMixerQueue[2]);
            } else {
                phMixOutputBuffers(Temp.reverbMixerQueue, reverbMixerQueueSize, Temp.reverbMixerQueue[2]);
            }

            // The final outFrame is Temp.reverbMixerQueue[2]
            outFrame = Temp.reverbMixerQueue[2];

            // we also need to revert reverbMixerQueue [0] and [1]  to their original addresses
            Temp.reverbMixerQueue[0] = mixerQueue0;
            Temp.reverbMixerQueue[1] = mixerQueue1;
        }
        #endif

        jfloat *masterVolume = lsGetVolume(&SETTINGS, GLOBAL_LISTENER);

        olWriteFrame(&SETTINGS, line, frameIndex % lineBufferSize, outFrame, SETTINGS.frameSize * SETTINGS.nOutputChannels, (*masterVolume));
        olSetLastProcessedFrameId(&SETTINGS, line, ++frameIndex);
    }
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env,
                                                                      jobject obj,

                                                                      jlong listenerDataPointer,
                                                                      jlongArray audioSourcesSceneDataArrayPointer,

                                                                      jint nMaterials,
                                                                      jlong materials,

                                                                      jobject jSettings) {

    jclass settingsClass = (*env)->GetObjectClass(env, jSettings);

    SETTINGS.nOutputLines = GET_SETTINGS_INT(jSettings, settingsClass, "nOutputLines");
    SETTINGS.nSourcesPerLine = GET_SETTINGS_INT(jSettings, settingsClass, "nSourcesPerLine");
    SETTINGS.nOutputChannels = GET_SETTINGS_INT(jSettings, settingsClass, "nOutputChannels");
    SETTINGS.frameSize = GET_SETTINGS_INT(jSettings, settingsClass, "frameSize");
    SETTINGS.sampleRate = GET_SETTINGS_INT(jSettings, settingsClass, "sampleRate");
    SETTINGS.bufferSize = GET_SETTINGS_INT(jSettings, settingsClass, "bufferSize");
    SETTINGS.isPassthrough = GET_SETTINGS_BOOL(jSettings, settingsClass, "passThrough");

    GLOBAL_LISTENER = lsNew(&SETTINGS, (jfloat *)(intptr_t)listenerDataPointer);

    OUTPUT_LINES = olNew(&SETTINGS, SETTINGS.nOutputLines);

    Temp.frame1 = (jfloat *)malloc(4 * SETTINGS.frameSize * SETTINGS.nOutputChannels);

    Temp.monoFrame1 = (jfloat *)malloc(4 * SETTINGS.frameSize);
    Temp.mixerQueue = (jfloat **)malloc(sizeof(jfloat *) * SETTINGS.nSourcesPerLine);
    for (jint i = 0; i < SETTINGS.nSourcesPerLine; i++) {
        Temp.mixerQueue[i] = (jfloat *)malloc(4 * SETTINGS.frameSize * SETTINGS.nOutputChannels);
    }
 
#ifdef INCLUDE_SIMPLE_REVERB
    Temp.frame2 = (jfloat *)malloc(4 * SETTINGS.frameSize * SETTINGS.nOutputChannels);

    Temp.reverbMixerQueue = (jfloat **)malloc(sizeof(jfloat *) * SETTINGS.nSourcesPerLine);
    for (jint i = 0; i < SETTINGS.nSourcesPerLine + 1; i++) {
        Temp.reverbMixerQueue[i] = (jfloat *)malloc(4 * SETTINGS.frameSize * SETTINGS.nOutputChannels);
    }
#endif

    phInit(&SETTINGS, SETTINGS.nSourcesPerLine, nMaterials, (jfloat *)(intptr_t)materials, env, jSettings);

    jlong *audioSourcesSceneDataArray = (*env)->GetLongArrayElements(env, audioSourcesSceneDataArrayPointer, 0);
    for (jint i = 0; i < SETTINGS.nOutputLines; i++) {
        for (jint j = 0; j < SETTINGS.nSourcesPerLine; j++) {
            jfloat *audioSourceSceneData = (jfloat *)(intptr_t)audioSourcesSceneDataArray[i * SETTINGS.nSourcesPerLine + j];
            asSetSceneData(&SETTINGS, &OUTPUT_LINES[i].sourcesSlots[j], audioSourceSceneData);
            phInitializeSource(&SETTINGS, &OUTPUT_LINES[i].sourcesSlots[j]);
        }
    }

#ifdef INCLUDE_SIMPLE_REVERB
    srInit(&SETTINGS);
#endif
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj) {
    for (jint i = 0; i < SETTINGS.nOutputLines; i++) {
        for (jint j = 0; j < SETTINGS.nSourcesPerLine; j++) {
            phDestroySource(&SETTINGS, &OUTPUT_LINES[i].sourcesSlots[j]);
        }
    }
    lsDestroy(&SETTINGS, GLOBAL_LISTENER);
    olDestroy(&SETTINGS, OUTPUT_LINES, SETTINGS.nOutputLines);
    phDestroy(&SETTINGS);
#ifdef INCLUDE_SIMPLE_REVERB
    srDestroy(&SETTINGS);
#endif
    free(Temp.frame1);

#ifdef INCLUDE_SIMPLE_REVERB
    free(Temp.frame2);
    for (jint i = 0; i < SETTINGS.nSourcesPerLine; i++) {
        free(Temp.reverbMixerQueue[i]);
    }
    free(Temp.reverbMixerQueue);
#endif

    free(Temp.monoFrame1);
    for (jint i = 0; i < SETTINGS.nSourcesPerLine; i++) {
        free(Temp.mixerQueue[i]);
    }
    free(Temp.mixerQueue);
}
