
#include "OutputLine.h"
#include <stdlib.h>



void olPreInit(struct GlobalSettings *settings,struct OutputLine *line){
    // line->lastReadFrame = NULL;
    line->sourceBuffer = NULL;
    line->sourceBufferNumSamples = 0;
}

void olInit(struct GlobalSettings *settings,struct OutputLine *line,jfloat *outputBuffer){
    line->outputBuffer = outputBuffer;
    // if (line->lastReadFrame != NULL)
    //     free(line->lastReadFrame);
    // line->lastReadFrame = (jfloat*)malloc(4 * settings->inputFrameSize);
    // line->lastWrittenFrame=(jfloat*)malloc(settings->nOutputChannels* 4 * settings->frameSize);
}

void olDestroy(struct GlobalSettings *settings,struct OutputLine *line){
    // if (line->lastReadFrame != NULL)
    //     free(line->lastReadFrame);
}


void olConnectSource(struct GlobalSettings *settings,struct OutputLine *line,jfloat *sourceBuffer,jint sourceSamples){
    line->sourceBuffer =sourceBuffer;
    line->sourceBufferNumSamples =sourceSamples;
}

void olDisconnectSource(struct GlobalSettings *settings,struct OutputLine *line){
    line->sourceBuffer = NULL;
    line->sourceBufferNumSamples = 0;
}


/**
 * Get float buffer of the connected source or NULL if there is no source connected
 */
jfloat *olGetConnectedSourceBuffer(struct GlobalSettings *settings,struct OutputLine *line) {
    return line->sourceBuffer;
}

/**
 * Get num of samples in connected source
 */
jint olGetConnectedSourceSamples(struct GlobalSettings *settings,struct OutputLine *line) {
    return line->sourceBufferNumSamples;
}


/**
 * Return the float buffer of the output channel
 */
jfloat *olGetOutputBuffer(struct GlobalSettings *settings,struct OutputLine *line) {
    return line->outputBuffer;
}


/**
 * Return true if any audio source is connected to the channel
 */
jboolean olHasConnectedSourceBuffer(struct GlobalSettings *settings,struct OutputLine *line) {
    return /*line->lastReadFrame != NULL &&*/ olGetConnectedSourceBuffer(settings,line) != NULL;
}


/**
 * Store index of the lastest processed frame in the output buffer.
 */
void olSetLastProcessedFrameId(struct GlobalSettings *settings,struct OutputLine *line, jint v) {
    ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)] = v;
}

/**
 * Retrieve the index of the latest processed frame from the output buffer
 */
jint olGetLastProcessedFrameId(struct GlobalSettings *settings,struct OutputLine *line) {
    jint n = ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)];
    if (n < 0)
        n = -n;
    return n;
}

/**
 * Check if processing is completed (ie the last source frame has been processed)
 */
jboolean olIsProcessingCompleted(struct GlobalSettings *settings,struct OutputLine *line) {
    jint n = ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)];
    return n < 0;
}


/**
 * Notify that the processing is completed by increasing the last processed frame 
 * index by 1 and making it negative.
 */
void olSetProcessingCompleted(struct GlobalSettings *settings,struct OutputLine *line) {
    ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)] = -(((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)] + 1);
}

/**
 * Retrieve the index of the latest played frame from the output buffer
 */
jint olGetLastPlayedFrameId(struct GlobalSettings *settings,struct OutputLine *line) {
    return ((jint *)line->outputBuffer)[olHeader(LAST_PLAYED_FRAME)];
}


/**
 * Read one frame from the audio source
 */
jfloat *olReadFrame(struct GlobalSettings *settings,struct OutputLine *line, jint frameIndex,jfloat *store) {
    jint frameSize = settings->inputFrameSize;
    jint sourceSamples = line->sourceBufferNumSamples;
    jfloat *source = line->sourceBuffer;
    for (jint i = 0; i < frameSize; i++) {
        jint sampleIndex = frameSize * frameIndex + i;
        jfloat v;
        if (sampleIndex >= sourceSamples) {  // Write 0s if the frame size exceed the remaining source's bytes
            // printf("Phonon: trying to read sample n%d but source contains only %d samples. A zero sample will be returned instead.\n ", sampleIndex, sourceSamples);
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
void olWriteFrame(struct GlobalSettings *settings,struct OutputLine *line, jint frameIndex, jfloat *frame,jint frameSize) {
    for (jint i = 0; i < frameSize; i++) {
        line->outputBuffer[olHeader(BODY)+frameSize * frameIndex + i] = frame[i];
    }
}
