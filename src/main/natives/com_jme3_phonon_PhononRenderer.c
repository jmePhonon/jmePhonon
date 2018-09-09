#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #error This implementation runs only on little endian machines
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
    jfloat *outputFrame1;
    jfloat *outputFrame2;
    jfloat *inputFrame;
    jfloat **mixerQueue;
} Temp;

#if defined(__linux__)
    #include "platform/linux/NativeUpdate.h"
#endif



JNIEXPORT jlong JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj,jint size,jlong sourceAddr){
    struct AudioSource* source= olConnectSourceToBestLine(&SETTINGS,OUTPUT_LINES,SETTINGS.nOutputLines,
      (jfloat *)(intptr_t)sourceAddr,size);
    phFlushSource(&SETTINGS,source);
    if (source == NULL)
        return -1;
    else
        return (intptr_t)source;
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

        jboolean loop = false;

        jint mixerQueueSize = 0;

        struct UList* uList = line->uList;
        struct UListNode* uNode = uList->head->next;

        while(!ulistIsTail(uList, uNode)) {
            struct AudioSource *audioSource = uNode->audioSource;
                if(asReadNextFrame(&SETTINGS, audioSource, Temp.inputFrame)) {
                    // Reached end
                    if(loop){

                    } else {
                        ulistRemove(uNode);
                    }
                }

                if(SETTINGS.isPassthrough) {
                    passThrough(&SETTINGS,Temp.inputFrame, Temp.mixerQueue[mixerQueueSize++]);
                } else {
                    phProcessFrame(&SETTINGS, GLOBAL_LISTENER,audioSource, Temp.inputFrame, Temp.mixerQueue[mixerQueueSize++]);
                }

                uNode = uNode->next;
        }


            jfloat *output = Temp.outputFrame1;
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
                    srApplyReverb(&SETTINGS,output, Temp.outputFrame2);
                    output = Temp.outputFrame2;
                }
            #endif
                jfloat *masterVolume = lsGetVolume(&SETTINGS,GLOBAL_LISTENER);

                olWriteFrame(&SETTINGS, line, frameIndex % lineBufferSize, output, SETTINGS.inputFrameSize * SETTINGS.nOutputChannels, (*masterVolume));
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
// effects
jboolean isPassthrough
) {

    SETTINGS.nOutputLines = nOutputLines;
    SETTINGS.nSourcesPerLine = nSourcesPerLine;
    SETTINGS.nOutputChannels = nOutputChannels;
    SETTINGS.inputFrameSize = frameSize;
    // SETTINGS.outputFrameSize = frameSize * nOutputChannels;
    SETTINGS.sampleRate = sampleRate;
    SETTINGS.bufferSize = bufferSize;
    SETTINGS.isPassthrough = isPassthrough;

    GLOBAL_LISTENER = lsNew(&SETTINGS, (jfloat*)(intptr_t)listenerDataPointer);
    OUTPUT_LINES = olNew(&SETTINGS, nOutputLines);

    Temp.outputFrame1= (jfloat*)malloc(4 * SETTINGS.inputFrameSize*nOutputChannels);
        Temp.outputFrame2= (jfloat*)malloc(4 * SETTINGS.inputFrameSize*nOutputChannels);

    Temp.inputFrame= (jfloat*)malloc(4 * SETTINGS.inputFrameSize);
    Temp.mixerQueue=(jfloat**)malloc(sizeof(jfloat*) * nSourcesPerLine );
    for(jint i=0;i<SETTINGS.nSourcesPerLine;i++){
        Temp.mixerQueue[i]=(jfloat*)malloc(4 * SETTINGS.inputFrameSize*nOutputChannels);
    }



    phInit(&SETTINGS,nSourcesPerLine); 
    for(jint i=0;i<SETTINGS.nOutputLines;i++){
        for(jint j=0;j<SETTINGS.nSourcesPerLine;j++){
            phInitializeSource(&SETTINGS,&OUTPUT_LINES[i].sourcesSlots[j]);
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
    free(Temp.outputFrame1);
    free(Temp.outputFrame2);

    free(Temp.inputFrame);
    for(jint i=0;i<SETTINGS.nSourcesPerLine;i++){
        free(Temp.mixerQueue[i]);
    }
    free(Temp.mixerQueue);
}
