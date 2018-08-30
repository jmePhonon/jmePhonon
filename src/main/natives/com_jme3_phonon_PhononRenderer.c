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
#include "Channel.h"



struct GlobalSettings SETTINGS;
struct ChOutput *OUTPUT_LINES;

struct  {
    jfloat *outputFrame;
    jfloat *inputFrame;
} Temp;

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj){
    for(jint i=0;i<SETTINGS.nOutputLines;i++) chDestroy(&SETTINGS,&OUTPUT_LINES[i]);
    free(OUTPUT_LINES);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj,jint channelId,jint size,jlong sourceAddr){
    chConnectSource(&SETTINGS,&OUTPUT_LINES[channelId], (jfloat *)(intptr_t)sourceAddr,size);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative(JNIEnv *env, jobject obj, jint channelId) {
    chDisconnectSource(&SETTINGS,&OUTPUT_LINES[channelId]);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_loadChannelNative(JNIEnv *env, jobject obj,jint channelId, jlong outputBufferAddr) {
    chInit(&SETTINGS,&OUTPUT_LINES[channelId], (jfloat *)(intptr_t)outputBufferAddr);
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
        struct ChOutput *line = &OUTPUT_LINES[i];
        if (!chHasConnectedSourceBuffer(&SETTINGS,line)){
            // printf("Source not connected. Skip channel %d\n", i);
            continue;
        }

        if(chIsProcessingCompleted(&SETTINGS,line)){
            // printf("Processing completed in channel %d\n", i);
            continue;
        }

        jint frameIndex = chGetLastProcessedFrameId(&SETTINGS,line);
        jint lastPlayedFrameIndex = chGetLastPlayedFrameId(&SETTINGS,line);
        jint channelBufferSize = SETTINGS.bufferSize;
        // Processing is too fast, skip.
        if(frameIndex-lastPlayedFrameIndex>channelBufferSize/2){ 
            continue;
        }

        jint frameToRead = frameIndex;
        jint sourceFrames=(jint)ceil( chGetConnectedSourceSamples(&SETTINGS,line) / SETTINGS.inputFrameSize);

        jboolean loop = false;
        if (loop) {
            frameToRead = frameIndex %  sourceFrames;
        }else{
            if(frameIndex>=sourceFrames){
                chSetProcessingCompleted(&SETTINGS,line);
                continue;
            }
        }

        jfloat *inputFrame = Temp.inputFrame;
        jfloat *outputFrame = Temp.outputFrame;
        chReadFrame(&SETTINGS, line, frameToRead, inputFrame);

        passThrough(inputFrame, outputFrame);
        // processing
        // ....
        // ....
        // -----------
        chWriteFrame(&SETTINGS,line, frameIndex%channelBufferSize, outputFrame,SETTINGS.outputFrameSize);
        chSetLastProcessedFrameId(&SETTINGS,line,++frameIndex);
    }
}
#if defined(__linux__)
    #include "platform/linux/NativeUpdate.h"
#endif

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env, jobject obj, 
jint sampleRate,
jint nOutputLines,jint nOutputChannels,jint frameSize,jint bufferSize,
jdouble deltas, jboolean nativeThread, jboolean nativeClock) {
    
    OUTPUT_LINES = malloc(sizeof(struct ChOutput) * nOutputLines);
    SETTINGS.nOutputLines = nOutputLines;
    SETTINGS.nOutputChannels = nOutputChannels;
    SETTINGS.inputFrameSize = frameSize;
    SETTINGS.outputFrameSize = frameSize * nOutputChannels;
    SETTINGS.sampleRate = sampleRate;
    SETTINGS.bufferSize = bufferSize;

    Temp.outputFrame= (jfloat*)malloc(4 * SETTINGS.outputFrameSize);
    Temp.inputFrame= (jfloat*)malloc(4 * SETTINGS.inputFrameSize);


    for (jint i = 0; i < SETTINGS.nOutputLines ; i++)
        chPreInit(&SETTINGS,&OUTPUT_LINES[i]);

#ifdef HAS_NATIVE_THREAD_SUPPORT
        if(nativeThread){
            nuInit(env,&obj,nativeClock,deltas);
        }
    #endif
}
