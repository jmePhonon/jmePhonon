#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #error This implementation runs only on little endian machines
#endif

#include <jni.h>
#include "com_jme3_phonon_PhononRenderer.h"
#include <math.h>
#include <stdint.h>

#include "types.h"
#include "Channel.h"


#define _MAX_CHANNELS 16

struct ChOutput CHANNELS[_MAX_CHANNELS];


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative(JNIEnv *env, jobject obj){
    for(jint i=0;i<_MAX_CHANNELS;i++) chPreInit(&CHANNELS[i]);    
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative(JNIEnv *env, jobject obj){
    for(jint i=0;i<_MAX_CHANNELS;i++) chDestroy(&CHANNELS[i]);    
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative(JNIEnv *env, jobject obj,jint channelId,jint size,jlong sourceAddr){
    chConnectSource(&CHANNELS[channelId], (jfloat *)(intptr_t)sourceAddr,size);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative(JNIEnv *env, jobject obj, jint channelId) {
    chDisconnectSource(&CHANNELS[channelId]);
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_loadChannelNative(JNIEnv *env, jobject obj,jint channelId, jlong outputBufferAddr, jint frameSize, jint bufferSize) {
    chInit(&CHANNELS[channelId], (jfloat *)(intptr_t)outputBufferAddr, bufferSize, frameSize);
    printf("Phonon: Load channel id %d with frame size %d and length %d\n", channelId, frameSize, bufferSize);
}


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
    for (jint i = 0; i < _MAX_CHANNELS; i++) {
        struct ChOutput *channel = &CHANNELS[i];
        if (!chHasConnectedSourceBuffer(channel)){
            printf("Source not connected. Skip channel %d\n", i);
            continue;
        }

        if(chIsProcessingCompleted(channel)){
            printf("Processing completed in channel %d\n", i);
            continue;
        }

        jint frameIndex = chGetLastProcessedFrameId(channel);
        jint lastPlayedFrameIndex = chGetLastPlayedFrameId(channel);
        jint channelBufferSize = chGetOutputBufferSize(channel);
        // Processing is too fast, skip.
        if(frameIndex-lastPlayedFrameIndex>channelBufferSize/2){ 
            continue;
        }

        jint frameToRead = frameIndex;
        jint sourceFrames=(jint)ceil( chGetConnectedSourceSamples(channel) / chGetFrameSize(channel));

        jboolean loop = false;
        if (loop) {
            frameToRead = frameIndex %  sourceFrames;
        }else{
            if(frameIndex>=sourceFrames){
                chSetProcessingCompleted(channel);
                continue;
            }
        }

        jfloat *frame= chReadFrame(channel, frameToRead);
        // processing
        // ....
        // ....
        // -----------
        chWriteFrame(channel, frameIndex%channelBufferSize, frame);
        chSetLastProcessedFrameId(channel,++frameIndex);

        if(i == 1)
            printf("processing frame %d in channel %d\n", frameIndex, i);
    }
}