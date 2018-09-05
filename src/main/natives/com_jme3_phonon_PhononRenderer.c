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
#include "OutputLine.h"
#include "JmePhonon.h"


struct GlobalSettings SETTINGS;
struct OutputLine *OUTPUT_LINES;

struct  {
    jfloat *outputFrame;
    jfloat *inputFrame;
} Temp;


#if defined(__linux__)
    #include "platform/linux/NativeUpdate.h"
#endif

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env, jobject obj, 
jint sampleRate,
jint nOutputLines,jint nOutputChannels,jint frameSize,jint bufferSize,
jdouble deltas, jboolean nativeThread,jboolean decoupledNativeThread, jboolean nativeClock,
// effects
jboolean isPassthrough
) {
    
    OUTPUT_LINES = malloc(sizeof(struct OutputLine) * nOutputLines);
    SETTINGS.nOutputLines = nOutputLines;
    SETTINGS.nOutputChannels = nOutputChannels;
    SETTINGS.inputFrameSize = frameSize;
    // SETTINGS.outputFrameSize = frameSize * nOutputChannels;
    SETTINGS.sampleRate = sampleRate;
    SETTINGS.bufferSize = bufferSize;

    SETTINGS.isPassthrough = isPassthrough;

    Temp.outputFrame= (jfloat*)malloc(4 * SETTINGS.inputFrameSize*nOutputChannels);
    Temp.inputFrame= (jfloat*)malloc(4 * SETTINGS.inputFrameSize);


    for (jint i = 0; i < SETTINGS.nOutputLines ; i++)
        olPreInit(&SETTINGS,&OUTPUT_LINES[i]);
    phInit(&SETTINGS);

    #ifdef HAS_NATIVE_THREAD_SUPPORT

        if(nativeThread){
            nuInit(env, &obj, nativeClock, deltas,decoupledNativeThread, Java_com_jme3_phonon_PhononRenderer_updateNative);
        }
    #endif
}


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj){
    for(jint i=0;i<SETTINGS.nOutputLines;i++) olDestroy(&SETTINGS,&OUTPUT_LINES[i]);    
    phDestroy(&SETTINGS);
    free(OUTPUT_LINES);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj,jint channelId,jint size,jlong sourceAddr){
    olConnectSource(&SETTINGS,&OUTPUT_LINES[channelId], (jfloat *)(intptr_t)sourceAddr,size);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative(JNIEnv *env, jobject obj, jint channelId) {
    olDisconnectSource(&SETTINGS,&OUTPUT_LINES[channelId]);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_loadChannelNative(JNIEnv *env, jobject obj,jint channelId, jlong outputBufferAddr) {
    olInit(&SETTINGS,&OUTPUT_LINES[channelId], (jfloat *)(intptr_t)outputBufferAddr);
    printf("Phonon: Load channel id %d\n", channelId);
}


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

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
    for (jint i = 0; i < SETTINGS.nOutputLines; i++) {    
        struct OutputLine *line = &OUTPUT_LINES[i];
        if (!olHasConnectedSourceBuffer(&SETTINGS,line)){
            // printf("Source not connected. Skip channel %d\n", i);
            continue;
        }

        if(olIsProcessingCompleted(&SETTINGS,line)){
            // printf("Processing completed in channel %d\n", i);
            continue;
        }

        jint frameIndex = olGetLastProcessedFrameId(&SETTINGS,line);
        jint lastPlayedFrameIndex = olGetLastPlayedFrameId(&SETTINGS,line);
        jint channelBufferSize = SETTINGS.bufferSize;
        // // Processing is too fast, skip.
        // if(frameIndex-lastPlayedFrameIndex>channelBufferSize/2){ 
        //     continue;
        // }

        jint frameToRead = frameIndex;
        jint sourceFrames=(jint)ceil( olGetConnectedSourceSamples(&SETTINGS,line) / SETTINGS.inputFrameSize);

        jboolean loop = false;
        if (loop) {
            frameToRead = frameIndex %  sourceFrames;
        }else{
            if(frameIndex>=sourceFrames){
                olSetProcessingCompleted(&SETTINGS,line);
                continue;
            }
        }

        jfloat *inputFrame = Temp.inputFrame;
        jfloat *outputFrame = Temp.outputFrame;
        olReadFrame(&SETTINGS, line, frameToRead, inputFrame);

        if(SETTINGS.isPassthrough){
            passThrough(inputFrame, outputFrame);
        }else{
            phProcessFrame(&SETTINGS,inputFrame,outputFrame);
        }
        
        olWriteFrame(&SETTINGS,line, frameIndex%channelBufferSize, outputFrame,SETTINGS.inputFrameSize*SETTINGS.nOutputChannels);
        olSetLastProcessedFrameId(&SETTINGS,line,++frameIndex);
    }
}
