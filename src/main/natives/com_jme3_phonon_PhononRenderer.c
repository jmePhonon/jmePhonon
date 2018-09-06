#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #error This implementation runs only on little endian machines
#endif

#include <jni.h>
#include <math.h>
#include <stdint.h>
#include <stdlib.h>
#include "com_jme3_phonon_PhononRenderer.h"
#include "Settings.h"
#include "types.h"
#include "JmePhonon.h"
#include "OutputLine.h"
#include "AudioSource.h"


struct GlobalSettings SETTINGS;
struct OutputLine *OUTPUT_LINES;

struct  {
    jfloat *outputFrame;
    jfloat *inputFrame;
    jfloat **mixerQueue;
} Temp;

#if defined(__linux__)
    #include "platform/linux/NativeUpdate.h"
#endif


void passThrough(jfloat *input, jfloat *output) {
    int inputIndex = 0;
    int outputIndex = 0;
    while(inputIndex<SETTINGS.inputFrameSize){
        for(int j=0;j<SETTINGS.nOutputChannels;j++){
            output[outputIndex++] = input[inputIndex];
        }
        inputIndex++;
    }
}
void  passThroughMixer(jfloat** inputs,jint nInputs,jfloat *output){
    for (jint i = 0; i < SETTINGS.inputFrameSize * SETTINGS.nOutputChannels; i++) {
        jfloat res = 0;
        for (jint j = 0; j < nInputs; j++) {
            res += inputs[j][i];
        }
        res /= nInputs;
        output[i] = res;
    }
}



JNIEXPORT jlong JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj,jint size,jlong sourceAddr){
    struct AudioSource* source= olConnectSourceToBestLine(&SETTINGS,OUTPUT_LINES,SETTINGS.nOutputLines,
      (jfloat *)(intptr_t)sourceAddr,size);
    if(source==NULL)
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


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
    for (jint i = 0; i < SETTINGS.nOutputLines; i++) {
        struct OutputLine *line = &OUTPUT_LINES[i];
        if (!olIsInitialized(&SETTINGS, line))
            continue;


        jint frameIndex = olGetLastProcessedFrameId(&SETTINGS,line);
        jint lastPlayedFrameIndex = olGetLastPlayedFrameId(&SETTINGS,line);
        jint lineBufferSize = SETTINGS.bufferSize;


        // Processing is too fast, skip.
        if(frameIndex-lastPlayedFrameIndex>2){
            continue;
        }
        
        jboolean loop = false;

        jint mixerQueueSize = 0;
        for(jint j=0;j<SETTINGS.nSourcesPerLine;j++){
            struct AudioSource *audioSource = &line->sourcesSlots[j];
            if(asIsConnected(&SETTINGS,audioSource)){
                if(asReadNextFrame(&SETTINGS,audioSource,Temp.inputFrame)){
                    // Reached end
                    if(loop){

                    }else{

                    }
                }

                if(SETTINGS.isPassthrough){
                    passThrough(Temp.inputFrame, Temp.mixerQueue[mixerQueueSize++]);
                }else{
                    phProcessFrame(&SETTINGS,Temp.inputFrame,Temp.mixerQueue[mixerQueueSize++]);
                }

            }
        }

            jfloat *output = Temp.outputFrame;
            if(mixerQueueSize==1){
                output = Temp.mixerQueue[0];
            } else {
                if(SETTINGS.isPassthrough){
                    passThroughMixer(Temp.mixerQueue,mixerQueueSize, output);
                }else{
                    phMixOutputBuffers(Temp.mixerQueue, mixerQueueSize , output);
                }
            }

            olWriteFrame(&SETTINGS, line, frameIndex % lineBufferSize, output, SETTINGS.inputFrameSize * SETTINGS.nOutputChannels);
            olSetLastProcessedFrameId(&SETTINGS, line, ++frameIndex);
    }
}


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env, jobject obj, 
jint sampleRate,
jint nOutputLines,
jint nSourcesPerLine,
jint nOutputChannels,
jint frameSize,
jint bufferSize,
 jboolean nativeThread,
 jboolean decoupledNativeThread,
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

    OUTPUT_LINES = olNew(&SETTINGS,nOutputLines);

    Temp.outputFrame= (jfloat*)malloc(4 * SETTINGS.inputFrameSize*nOutputChannels);
    Temp.inputFrame= (jfloat*)malloc(4 * SETTINGS.inputFrameSize);
    Temp.mixerQueue=(jfloat**)malloc(sizeof(jfloat*) * nSourcesPerLine );
    for(jint i=0;i<nSourcesPerLine;i++){
        Temp.mixerQueue[i]=(jfloat*)malloc(4 * SETTINGS.inputFrameSize*nOutputChannels);
    }


  
    phInit(&SETTINGS,nSourcesPerLine);

    #ifdef HAS_NATIVE_THREAD_SUPPORT    
        if(nativeThread){
            nuInit(env, &obj, decoupledNativeThread, Java_com_jme3_phonon_PhononRenderer_updateNative);
        }
    #endif
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj){
    for(jint i=0;i<SETTINGS.nOutputLines;i++) olDestroy(&SETTINGS,&OUTPUT_LINES[i]);    
    phDestroy(&SETTINGS);
    free(OUTPUT_LINES);
    free(Temp.outputFrame);
    free(Temp.inputFrame);
    free(Temp.mixerQueue);
}
