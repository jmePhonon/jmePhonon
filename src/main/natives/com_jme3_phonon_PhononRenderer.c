#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #error This implementation runs only on little endian machines
#endif

#if defined(__linux__)
    #include "platform/linux/NativeUpdate.h"
#endif

#include "Common.h" 



#include "com_jme3_phonon_PhononRenderer.h"

#include "JmePhonon.h"
#include "OutputLine.h"
#include "AudioSource.h"
#include "UList.h"
#include "Listener.h"
#include "Passthrough.h"

#ifdef INCLUDE_SIMPLE_REVERB
    #include "ext/ext_SimpleReverb.h"
#endif

struct GlobalSettings SETTINGS;
struct OutputLine *OUTPUT_LINES;
struct Listener *GLOBAL_LISTENER;

struct {
    jfloat *frame1;
    jfloat *frame2;
    jfloat *monoFrame1;
    jfloat **mixerQueue;
} Temp;




JNIEXPORT jint JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj,jint size,jlong sourceAddr){
    struct AudioSource* source = olConnectSourceToBestLine(&SETTINGS, OUTPUT_LINES, SETTINGS.nOutputLines,
      (jfloat *)(intptr_t)sourceAddr, size);

    phFlushSource(&SETTINGS, source);

    if (source == NULL)
        return -1;
    else
        return source->sourceIndex;
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative(JNIEnv *env, jobject obj, jlong addr) {
    struct AudioSource *source = (struct AudioSource *)(intptr_t)addr;
    olDisconnectSource(&SETTINGS, source);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initLineNative(JNIEnv *env, jobject obj,jint lineId, jlong outputBufferAddr) {
    olInit(&SETTINGS,&OUTPUT_LINES[lineId], (jfloat *)(intptr_t)outputBufferAddr);
}


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_setEnvironmentNative(JNIEnv *env, jobject obj, jfloatArray envdata) {
    #ifdef INCLUDE_SIMPLE_REVERB
        jfloat* envdataraw = (*env)->GetFloatArrayElements( env,envdata,JNI_FALSE);
        srSetEnvironment(&SETTINGS, envdataraw);
    #endif
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
    for (jint i = 0; i < SETTINGS.nOutputLines; i++) {
        struct OutputLine *line = &OUTPUT_LINES[i];
        if (!olIsInitialized(&SETTINGS, line))
            continue;


        jint frameIndex = olGetLastProcessedFrameId(&SETTINGS,line);
        jint lastPlayedFrameIndex = olGetLastPlayedFrameId(&SETTINGS,line);
        jint lineBufferSize = SETTINGS.bufferSize;


        // Processing is too fast, skip.
        if(frameIndex-lastPlayedFrameIndex>SETTINGS.bufferSize-1){
            continue;
        }


        jint mixerQueueSize = 0;
 
        struct UList* uList = line->uList;
        struct UListNode* uNode = uList->head->next;

        while(!ulistIsTail(uList, uNode)) {
            struct AudioSource *audioSource = uNode->audioSource;
            jboolean loop = asHasFlag(&SETTINGS,audioSource,LOOP);
            jint nchannels = asGetNumChannels(&SETTINGS,audioSource);

            jfloat *inFrame = nchannels==1?Temp.monoFrame1:Temp.frame1;


            if (asReadNextFrame(&SETTINGS, audioSource,inFrame)) {
                // Reached end
                if (loop) {
                    printf("Looping \n");

                } else {
                    ulistRemove(uNode);
                }
            }                
            jboolean isPositional = asHasFlag(&SETTINGS, audioSource, POSITIONAL);

            if (SETTINGS.isPassthrough || !isPositional) {
                passThrough(&SETTINGS, inFrame, Temp.mixerQueue[mixerQueueSize++],nchannels);
            } else {
                //Positional source is always mono
                phProcessFrame(&SETTINGS, GLOBAL_LISTENER,audioSource, inFrame, Temp.mixerQueue[mixerQueueSize++]);
            }

            uNode = uNode->next;
        }  
        


        jfloat *output = Temp.frame1;
        if(mixerQueueSize==1){
            output = Temp.mixerQueue[0];
        } else {
            if(SETTINGS.isPassthrough){
                passThroughMixer(&SETTINGS,Temp.mixerQueue,mixerQueueSize, output);
            }else{
                phMixOutputBuffers(Temp.mixerQueue, mixerQueueSize , output);
            }
        }
        


        #ifdef INCLUDE_SIMPLE_REVERB 
            if(srHasValidEnvironment(&SETTINGS)){
                srApplyReverb(&SETTINGS,output, Temp.frame2);
                output = Temp.frame2;
            }
        #endif
        jfloat *masterVolume = lsGetVolume(&SETTINGS,GLOBAL_LISTENER);

        olWriteFrame(&SETTINGS, line, frameIndex % lineBufferSize, output, SETTINGS.frameSize * SETTINGS.nOutputChannels, (*masterVolume));
        olSetLastProcessedFrameId(&SETTINGS, line, ++frameIndex);
    }
}


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env, 
    jobject obj, 
    jint sampleRate,
    jint nOutputLines,
    jint nSourcesPerLine,
    jint nOutputChannels,
    jint frameSize,
    jint bufferSize,
    jboolean nativeThread,
    jboolean decoupledNativeThread,

    jlong listenerDataPointer,
    jlongArray audioSourcesSceneDataArrayPointer,

    // effects
    jboolean isPassthrough
) {

    SETTINGS.nOutputLines = nOutputLines;
    SETTINGS.nSourcesPerLine = nSourcesPerLine;
    SETTINGS.nOutputChannels = nOutputChannels;
    SETTINGS.frameSize = frameSize;
    // SETTINGS.frameSize = frameSize * nOutputChannels;
    SETTINGS.sampleRate = sampleRate;
    SETTINGS.bufferSize = bufferSize;
    SETTINGS.isPassthrough = isPassthrough;

    GLOBAL_LISTENER = lsNew(&SETTINGS, (jfloat*)(intptr_t)listenerDataPointer);




    OUTPUT_LINES = olNew(&SETTINGS, nOutputLines);

   

    Temp.frame1= (jfloat*)malloc(4 * SETTINGS.frameSize*nOutputChannels);
        Temp.frame2= (jfloat*)malloc(4 * SETTINGS.frameSize*nOutputChannels);

    Temp.monoFrame1= (jfloat*)malloc(4 * SETTINGS.frameSize);
    Temp.mixerQueue=(jfloat**)malloc(sizeof(jfloat*) * nSourcesPerLine );
    for(jint i=0;i<SETTINGS.nSourcesPerLine;i++){
        Temp.mixerQueue[i]=(jfloat*)malloc(4 * SETTINGS.frameSize*nOutputChannels);
    }

    phInit(&SETTINGS,nSourcesPerLine); 

    jlong* audioSourcesSceneDataArray = (*env)->GetLongArrayElements(env, audioSourcesSceneDataArrayPointer, 0); 
    for(jint i=0;i<SETTINGS.nOutputLines;i++){
        for(jint j=0;j<SETTINGS.nSourcesPerLine;j++){
            jfloat* audioSourceSceneData = (jfloat*)(intptr_t) audioSourcesSceneDataArray[i * nSourcesPerLine + j];
            asSetSceneData(&SETTINGS, &OUTPUT_LINES[i].sourcesSlots[j], audioSourceSceneData);
            phInitializeSource(&SETTINGS, &OUTPUT_LINES[i].sourcesSlots[j]);
        }
    }


    
    #ifdef INCLUDE_SIMPLE_REVERB
        srInit(&SETTINGS);
    #endif

#ifdef HAS_NATIVE_THREAD_SUPPORT    
        if(nativeThread){
            nuInit(env, &obj, decoupledNativeThread, Java_com_jme3_phonon_PhononRenderer_updateNative);
        }
    #endif
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj){
    for(jint i=0;i<SETTINGS.nOutputLines;i++){
        for(jint j=0;j<SETTINGS.nSourcesPerLine;j++){
            phDestroySource(&SETTINGS,&OUTPUT_LINES[i].sourcesSlots[j]);
        }
    }
    lsDestroy(&SETTINGS, GLOBAL_LISTENER);
    olDestroy(&SETTINGS, OUTPUT_LINES, SETTINGS.nOutputLines);
    phDestroy(&SETTINGS);
    #ifdef INCLUDE_SIMPLE_REVERB
        srDestroy(&SETTINGS);
    #endif
    free(Temp.frame1);
    free(Temp.frame2);

    free(Temp.monoFrame1);
    for(jint i=0;i<SETTINGS.nSourcesPerLine;i++){
        free(Temp.mixerQueue[i]);
    }
    free(Temp.mixerQueue);
}
