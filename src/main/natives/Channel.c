
#include "Channel.h"
#include <stdlib.h>



void chPreInit(struct GlobalSettings *settings,struct ChOutput *chan){
    // chan->lastReadFrame = NULL;
    chan->sourceBuffer = NULL;
    chan->sourceBufferNumSamples = 0;
}

void chInit(struct GlobalSettings *settings,struct ChOutput *chan,jfloat *outputBuffer){
    chan->outputBuffer = outputBuffer;
    // if (chan->lastReadFrame != NULL)
    //     free(chan->lastReadFrame);
    // chan->lastReadFrame = (jfloat*)malloc(4 * settings->inputFrameSize);
    // chan->lastWrittenFrame=(jfloat*)malloc(settings->nOutputChannels* 4 * settings->frameSize);
}

void chDestroy(struct GlobalSettings *settings,struct ChOutput *chan){
    // if (chan->lastReadFrame != NULL)
    //     free(chan->lastReadFrame);
}


void chConnectSource(struct GlobalSettings *settings,struct ChOutput *chan,jfloat *sourceBuffer,jint sourceSamples){
    chan->sourceBuffer =sourceBuffer;
    chan->sourceBufferNumSamples =sourceSamples;
}

void chDisconnectSource(struct GlobalSettings *settings,struct ChOutput *chan){
    chan->sourceBuffer = NULL;
    chan->sourceBufferNumSamples = 0;
}


/**
 * Get float buffer of the connected source or NULL if there is no source connected
 */
jfloat *chGetConnectedSourceBuffer(struct GlobalSettings *settings,struct ChOutput *chan) {
    return chan->sourceBuffer;
}

/**
 * Get num of samples in connected source
 */
jint chGetConnectedSourceSamples(struct GlobalSettings *settings,struct ChOutput *chan) {
    return chan->sourceBufferNumSamples;
}


/**
 * Return the float buffer of the output channel
 */
jfloat *chGetOutputBuffer(struct GlobalSettings *settings,struct ChOutput *chan) {
    return chan->outputBuffer;
}


/**
 * Return true if any audio source is connected to the channel
 */
jboolean chHasConnectedSourceBuffer(struct GlobalSettings *settings,struct ChOutput *chan) {
    return /*chan->lastReadFrame != NULL &&*/ chGetConnectedSourceBuffer(settings,chan) != NULL;
}


/**
 * Store index of the lastest processed frame in the output buffer.
 */
void chSetLastProcessedFrameId(struct GlobalSettings *settings,struct ChOutput *chan, jint v) {
    ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)] = v;
}

/**
 * Retrieve the index of the latest processed frame from the output buffer
 */
jint chGetLastProcessedFrameId(struct GlobalSettings *settings,struct ChOutput *chan) {
    jint n = ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)];
    if (n < 0)
        n = -n;
    return n;
}

/**
 * Check if processing is completed (ie the last source frame has been processed)
 */
jboolean chIsProcessingCompleted(struct GlobalSettings *settings,struct ChOutput *chan) {
    jint n = ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)];
    return n < 0;
}


/**
 * Notify that the processing is completed by increasing the last processed frame 
 * index by 1 and making it negative.
 */
void chSetProcessingCompleted(struct GlobalSettings *settings,struct ChOutput *chan) {
    ((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)] = -(((jint *)chan->outputBuffer)[chHeader(LAST_PROCESSED_FRAME)] + 1);
}

/**
 * Retrieve the index of the latest played frame from the output buffer
 */
jint chGetLastPlayedFrameId(struct GlobalSettings *settings,struct ChOutput *chan) {
    return ((jint *)chan->outputBuffer)[chHeader(LAST_PLAYED_FRAME)];
}


/**
 * Read one frame from the audio source
 */
jfloat *chReadFrame(struct GlobalSettings *settings,struct ChOutput *chan, jint frameIndex,jfloat *store) {
    jint frameSize = settings->inputFrameSize;
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
        store[i] = v;
    }
    return store;
}

/**
*   Write one frame to the output buffer
*/
void chWriteFrame(struct GlobalSettings *settings,struct ChOutput *chan, jint frameIndex, jfloat *frame,jint frameSize) {
    for (jint i = 0; i < frameSize; i++) {
        chan->outputBuffer[chHeader(BODY)+frameSize * frameIndex + i] = frame[i];
    }
}
