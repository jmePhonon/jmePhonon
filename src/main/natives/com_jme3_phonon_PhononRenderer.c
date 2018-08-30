#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #error Error.
#endif


#include "com_jme3_phonon_PhononRenderer.h"
#include <math.h>
#include <stdlib.h>
#include <stdint.h>

#include "types.h"


#define _BUFFER_HEADER_32bit 2+1+1 +1 // connected input addr + source size + write index + read index
#define _MAX_CHANNELS 1



struct OutputChannel {
    jfloat *outputBuffer;
    jfloat *inputBuffer;
    jint inputSize;
    jint frameSize;
    jint bufferSize;
    jfloat *lastReadFrame;
} CHANNELS [_MAX_CHANNELS];



/**
 * Return true of any audio source is connected to this channel
 */
jboolean chHasConnectedSourceBuffer(struct OutputChannel *chan){
    
    // 
    // if(chan->lastReadFrame==NULL){
    //     printf("Phonon: channel not initialized\n");
    // }else{
    //     // printf("Phonon: source address %ld \n",((jlong*)chan->outputBuffer)[0]);
    // }
    return chan->lastReadFrame != NULL && chan->inputBuffer != NULL;
    //((jlong *)chan->outputBuffer)[0] != -1;
}

/**
* Return the jfloat buffer of the connected audio source
*/
jfloat *chGetConnectedSourceBuffer(struct OutputChannel *chan) {
    // return (void *) ((jlong*)chan->outputBuffer)[0];
    return (jfloat *)chan->inputBuffer;
}

/**
 * Return how many samples there are in the source audio.
 */
jint chGetSourceSizeInSamples(struct OutputChannel *chan) {
    return ((jint *)chan->outputBuffer)[2 /*8bytes*/];
}


void chSetLastProcessedFrameId(struct OutputChannel *chan,jint v) {
    // printf("Phonon: set last processed frame id to %d\n",v);
    ((jint *)chan->outputBuffer)[2+1 /*8bytes + 4*/] = v;
}


jint chGetLastProcessedFrameId(struct OutputChannel *chan) {
    jint n= ((jint *)chan->outputBuffer)[2+1 /*8bytes + 4*/];
    if(n<0)
        n = -n;
    return n;
}

jboolean chIsProcessingCompleted(struct OutputChannel *chan) {
    jint n= ((jint *)chan->outputBuffer)[2+1 /*8bytes + 4*/];
    return n<0;
}

/**
 * Increase last processed frame index by 1 and make it negative
 */
void chSetProcessingCompleted(struct OutputChannel *chan) {
    ((jint *)chan->outputBuffer)[2 + 1 /*8bytes + 4*/] = - ( ((jint *)chan->outputBuffer)[2 + 1 /*8bytes + 4*/] + 1);
}

jint chGetLastPlayedFrameId(struct OutputChannel *chan) {
    return ((jint *)chan->outputBuffer)[2+1+1 /*8bytes + 4 + 4*/];
}



jfloat chReadInputSample(struct OutputChannel *chan,jfloat *input,jint sampleIndex){
    return input[sampleIndex];
}

/**
 * Read frame from audio source
 */
jfloat* chReadFrame(struct OutputChannel *chan,jint frameIndex) {
    jint frameSize = chan->frameSize;
    jint sourceSize = chan->inputSize;
    jfloat *input = (jfloat *)chan->inputBuffer;

    for (jint i = 0; i < frameSize; i++) {
        jint sampleIndex = frameSize * frameIndex + i;
        /**
         *  Write 0s if the frame size exceed the remaining source's bytes
         */
        jfloat v = chReadInputSample(chan, input, sampleIndex);
        // if (sampleIndex >= sourceSize) {
        //     printf("Phonon: trying to read sample n%d but source contains only %d samples. A zero sample will be returned instead.\n ", sampleIndex, sourceSize);
        //     v = 0;
        // } else {
        //     v = input[sampleIndex];
        // }

        chan->lastReadFrame[i] = v;
        // frame[i] = v;
    }
    // return f;
    return chan->lastReadFrame;
}

/**
*   Write frame to output buffer
*/
void chWriteFrame(struct OutputChannel *chan,jint frameIndex, jfloat* frame) {
    jint frameSize = chan->frameSize;
    for(jint i=0;i<frameSize;i++){
        ((jfloat *)chan->outputBuffer)[_BUFFER_HEADER_32bit+frameSize * frameIndex+i]=frame[i];
    }
}


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative
  (JNIEnv *env, jobject obj){
    for(jint i=0;i<_MAX_CHANNELS;i++){
        CHANNELS[i].lastReadFrame = NULL;
        CHANNELS[i].inputBuffer = NULL;
    }

  }

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative
  (JNIEnv *env, jobject obj){

  }

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative
  (JNIEnv *env, jobject obj,jint channelId,jint size,jlong sourceAddr){
    CHANNELS[channelId].inputBuffer = (jfloat *)(intptr_t)sourceAddr;
    CHANNELS[channelId].inputSize = size;
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative(JNIEnv *env, jobject obj, jint channelId) {
    CHANNELS[channelId].inputBuffer = NULL;
  } 

JNIEXPORT void JNICALL
Java_com_jme3_phonon_PhononRenderer_loadChannelNative(JNIEnv *env, jobject obj,jint channelId, jlong outputBufferAddr, jint frameSize, jint bufferSize) {
    CHANNELS[channelId].outputBuffer = (jfloat *)(intptr_t)outputBufferAddr;
    CHANNELS[channelId].frameSize = frameSize;
    CHANNELS[channelId].bufferSize = bufferSize;
    CHANNELS[channelId].inputBuffer = NULL;
    if (CHANNELS[channelId].lastReadFrame != NULL)
        free(CHANNELS[channelId].lastReadFrame);
    CHANNELS[channelId].lastReadFrame = (jfloat*)malloc(4 * frameSize);
    printf("Phonon: Load channel id %d with frame size %d and length %d\n", channelId, frameSize, bufferSize);
}
jint callN = 0;
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
    callN++;
    for (jint i = 0; i < _MAX_CHANNELS; i++) {
        struct OutputChannel *channel = &CHANNELS[i];
        if (!chHasConnectedSourceBuffer(channel)){
            // printf("Source not connected. Skip channel %d\n", i);
            continue;
        }

        if(chIsProcessingCompleted(channel)){
            // printf("Processing completed in channel %d\n", i);
            continue;
        }

        jint frameIndex = chGetLastProcessedFrameId(channel);
        jint frameToRead = frameIndex;
        jint sourceFrames=(jint)ceil( chGetSourceSizeInSamples(channel) / channel->frameSize);

        jboolean loop = false;
        if (loop) {
            frameToRead = frameIndex %  sourceFrames;
        }else{
            if(frameIndex>=sourceFrames){
                chSetProcessingCompleted(channel);
                continue;
            }
        }
        // printf("Phonon: Read frame %d [%d] of %d for channel %d, call: %d\n", frameIndex,frameToRead,sourceFrames,i,callN);
        // 
        // jfloat frame[channel->frameSize]; 
        // for(jint j=0;j<channel->frameSize;j++){
        //     frame[j] = (jfloat)(rand() / RAND_MAX);
        // }
        // jfloat frame[channel->frameSize];
        jfloat *frame= chReadFrame(channel, frameToRead);
        // processing
        // ....
        // ....
        // -----------
        chWriteFrame(channel, frameIndex%channel->bufferSize, frame);
        chSetLastProcessedFrameId(channel,++frameIndex);
    }
}