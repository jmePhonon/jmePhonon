#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
    #error Error.
#endif

#include "com_jme3_phonon_PhononRenderer.h"
#include <math.h>
#include <stdlib.h>

#define _BUFFER_HEADER 8+4+4 +4 // connected input addr + source size + write index + read index
#define _MAX_CHANNELS 1

#define bool int
#define true 1
#define false 0

struct OutputChannel {
    void *outputBuffer;
    int bufferCapacity;
    int frameSize;
    int bufferSize;
    float *lastReadFrame;
} CHANNELS [_MAX_CHANNELS];



/**
 * Return true of any audio source is connected to this channel
 */
bool chHasConnectedSourceBuffer(struct OutputChannel *chan){
    
    // 
    if(chan->lastReadFrame==NULL){
        printf("Phonon: channel not initialized\n");
    }else{
        printf("Phonon: source address %ld \n",((long*)chan->outputBuffer)[0]);
    }
    return chan->lastReadFrame!=NULL&&((long*)chan->outputBuffer)[0]!=-1;

}

/**
* Return the float buffer of the connected audio source
*/
float* chGetConnectedSourceBuffer(struct OutputChannel *chan){
    return (float*)(((long*)chan->outputBuffer)[0]);
}


/**
 * Return how many samples there are in the source audio.
 */
int chGetSourceSizeInSamples(struct OutputChannel *chan) {
    return ((int *)chan->outputBuffer)[2 /*8bytes*/];
}


void chSetLastProcessedFrameId(struct OutputChannel *chan,int v) {
    printf("Phonon: set last processed frame id to %d\n",v);
    ((int *)chan->outputBuffer)[2+1 /*8bytes + 4*/] = v;
}


int chGetLastProcessedFrameId(struct OutputChannel *chan) {
    int n= ((int *)chan->outputBuffer)[2+1 /*8bytes + 4*/];
    if(n<0)
        n = -n;
    return n;
}

bool chIsProcessingCompleted(struct OutputChannel *chan) {
    int n= ((int *)chan->outputBuffer)[2+1 /*8bytes + 4*/];
    return n<0;
}

/**
 * Increase last processed frame index by 1 and make it negative
 */
void chSetProcessingCompleted(struct OutputChannel *chan) {
    ((int *)chan->outputBuffer)[2 + 1 /*8bytes + 4*/] = - ( ((int *)chan->outputBuffer)[2 + 1 /*8bytes + 4*/] + 1);
}

int chGetLastPlayedFrameId(struct OutputChannel *chan) {
    return ((int *)chan->outputBuffer)[2+1+1 /*8bytes + 4 + 4*/];
}

/**
 * Read frame from audio source
 */
float* chReadFrame(struct OutputChannel *chan,int frameIndex) {
    int frameSize = chan->frameSize;
    int sourceSize = chGetSourceSizeInSamples(chan);
    for (int i = 0; i < frameSize; i++) {
        int sampleIndex = frameSize * frameIndex + i;
        /**
         *  Write 0s if the frame size exceed the remaining source's bytes
         */
        float v;
        if (sampleIndex >= sourceSize) {
            printf("Phonon: trying to read sample n%d but source contains only %d samples. A zero sample will be returned instead. ", sampleIndex, sourceSize);
            v = 0;
        } else {
            v = chGetConnectedSourceBuffer(chan)[sampleIndex];
        }
        chan->lastReadFrame[i] = v;
    }
    return chan->lastReadFrame;
}

/**
*   Write frame to output buffer
*/
void chWriteFrame(struct OutputChannel *chan,int frameIndex, float* frame) {
    int frameSize = chan->frameSize;
    for(int i=0;i<frameSize;i++){
        ((float *)chan->outputBuffer)[_BUFFER_HEADER+frameSize * frameIndex+i]=frame[i];
    }
}


JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative
  (JNIEnv *env, jobject obj){
    for(int i=0;i<_MAX_CHANNELS;i++){
        CHANNELS[i].lastReadFrame = NULL;
    }

  }

JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative
  (JNIEnv *env, jobject obj){

  }

JNIEXPORT void JNICALL
Java_com_jme3_phonon_PhononRenderer_loadChannel(JNIEnv *env, jobject obj,jint channelId, jlong outputBufferAddr, jint frameSize, jint bufferSize) {
    CHANNELS[channelId].outputBuffer = (void *)outputBufferAddr;
    CHANNELS[channelId].bufferCapacity = _BUFFER_HEADER + frameSize * bufferSize;
    CHANNELS[channelId].frameSize = frameSize;
    CHANNELS[channelId].bufferSize = bufferSize;
    if(CHANNELS[channelId].lastReadFrame!=NULL)
        free(CHANNELS[channelId].lastReadFrame);
    CHANNELS[channelId].lastReadFrame = (float*)malloc(4 * frameSize);
    printf("Phonon: Load channel id %d with frame size %d and length %d\n", channelId, frameSize, bufferSize);
}
int callN = 0;
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative(JNIEnv *env, jobject obj) {
    callN++;
    for (int i = 0; i < _MAX_CHANNELS; i++) {
        struct OutputChannel *channel = &CHANNELS[i];
        if (!chHasConnectedSourceBuffer(channel)||chIsProcessingCompleted(channel))
            continue;

        int frameIndex = chGetLastProcessedFrameId(channel);
        int frameToRead = frameIndex;
        int sourceFrames=(int)ceil( chGetSourceSizeInSamples(channel) / channel->frameSize);

        bool loop = false;
        if (loop) {
            frameToRead = frameIndex %  sourceFrames;
        }else{
            if(frameIndex>=sourceFrames){
                chSetProcessingCompleted(channel);
                continue;
            }
        }
        printf("Phonon: Read frame %d [%d] of %d for channel %d, call: %d\n", frameIndex,frameToRead,sourceFrames,i,callN);
        float *frame=chReadFrame(channel, frameToRead);
        // processing
        // ....
        // ....
        // -----------
        chWriteFrame(channel,frameIndex%channel->bufferSize,frame);
        chSetLastProcessedFrameId(channel,++frameIndex);
    }
}