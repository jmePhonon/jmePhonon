
#include "Channel.h"
#include <stdlib.h>




void chPreInit(struct ChOutput *chan){
    chan->lastReadFrame = NULL;
    chan->sourceBuffer = NULL;
    chan->sourceBufferNumSamples = 0;
}

void chInit(struct ChOutput *chan,jfloat *outputBuffer,jint outputBufferSize,jint frameSize){
    chan->outputBuffer = outputBuffer;
    chan->outputBufferSize = outputBufferSize;
    chan->frameSize = frameSize;
    if (chan->lastReadFrame != NULL)
        free(chan->lastReadFrame);
    chan->lastReadFrame = (jfloat*)malloc(4 * frameSize);

}

void chDestroy(struct ChOutput *chan){

}


void chConnectSource(struct ChOutput *chan,jfloat *sourceBuffer,jint sourceSamples){
    chan->sourceBuffer =sourceBuffer;
    chan->sourceBufferNumSamples =sourceSamples;
}

void chDisconnectSource(struct ChOutput *chan){
    chan->sourceBuffer = NULL;
    chan->sourceBufferNumSamples = 0;
}


/**
 * Get float buffer of the connected source or NULL if there is no source connected
 */
jfloat *chGetConnectedSourceBuffer(struct ChOutput *chan) {
    return chan->sourceBuffer;
}

/**
 * Get num of samples in connected source
 */
jint chGetConnectedSourceSamples(struct ChOutput *chan) {
    return chan->sourceBufferNumSamples;
}


/**
 * Return the float buffer of the output channel
 */
jfloat *chGetOutputBuffer(struct ChOutput *chan) {
    return chan->outputBuffer;
}

/**
 * Get num of frames in output buffer
 */
jint chGetOutputBufferSize(struct ChOutput *chan) {
    return chan->outputBufferSize;
}


/**
 * Get num of sample in one output buffer frame
 */
jint chGetFrameSize(struct ChOutput *chan) {
    return chan->frameSize;
}

/**
 * Return true if any audio source is connected to the channel
 */
jboolean chHasConnectedSourceBuffer(struct ChOutput *chan) {
    return chan->lastReadFrame != NULL && chGetConnectedSourceBuffer(chan) != NULL;
}


/**
 * Store index of the lastest processed frame in the output buffer.
 */
void chSetLastProcessedFrameId(struct ChOutput *chan, jint v) {
    ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)] = v;
}

/**
 * Retrieve the index of the latest processed frame from the output buffer
 */
jint chGetLastProcessedFrameId(struct ChOutput *chan) {
    jint n = ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)];
    if (n < 0)
        n = -n;
    return n;
}

/**
 * Check if processing is completed (ie the last source frame has been processed)
 */
jboolean chIsProcessingCompleted(struct ChOutput *chan) {
    jint n = ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)];
    return n < 0;
}


/**
 * Notify that the processing is completed by increasing the last processed frame 
 * index by 1 and making it negative.
 */
void chSetProcessingCompleted(struct ChOutput *chan) {
    ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)] = -(((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)] + 1);
}

/**
 * Retrieve the index of the latest played frame from the output buffer
 */
jint chGetLastPlayedFrameId(struct ChOutput *chan) {
    return ((jint *)chan->outputBuffer)[chHeader(LAST_PLAYED_FRAME)];
}


/**
 * Read one frame from the audio source
 */
jfloat *chReadFrame(struct ChOutput *chan, jint frameIndex) {
    jint frameSize = chan->frameSize;
    jint sourceSamples = chan->sourceBufferNumSamples;
    jfloat *source = chan->sourceBuffer;
    for (jint i = 0; i < frameSize; i++) {
        jint sampleIndex = frameSize * frameIndex + i;
        jfloat v;
        if (sampleIndex >= sourceSamples) {  // Write 0s if the frame size exceed the remaining source's bytes
            printf("Phonon: trying to read sample n%d but source contains only %d samples. A zero sample will be returned instead.\n ", sampleIndex, sourceSamples);
            v = 0;
        } else {
            v = source[sampleIndex];
        }
        chan->lastReadFrame[i] = v;
    }
    return chan->lastReadFrame;
}

/**
*   Write one frame to the output buffer
*/
void chWriteFrame(struct ChOutput *chan, jint frameIndex, jfloat *frame) {
    jint frameSize = chan->frameSize;
    for (jint i = 0; i < frameSize; i++) {
        chan->outputBuffer[chHeader(BODY)+frameSize * frameIndex + i] = frame[i];
    }
}
