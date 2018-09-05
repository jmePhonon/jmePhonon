
#ifndef __CHANNEL__
#define __CHANNEL__
#include "com_jme3_phonon_CHANNEL_LAYOUT.h"
#include "Settings.h"

/**
 * Returns position of first byte of the field
 */
#define olHeaderB(field) com_jme3_phonon_CHANNEL_LAYOUT_##field

/**
 * Returns field length in bytes
 */
#define olHeaderSizeB(field) com_jme3_phonon_CHANNEL_LAYOUT_##field##_fieldsize

/**
 * Returns position of first 4bytes of the field
 */
#define olHeader(field) (olHeaderB(field)/4)

/**
 * Returns field length 
 */
#define olHeaderSize(field) (olHeaderSizeB(field)/4)


struct OutputLine {
    jfloat *outputBuffer;  /*where to store processed audio. Readable and writable.*/


    // These values are set to default when source is disconnected.
    jfloat *sourceBuffer;        /*where to read input audio (default=NULL). Read only*/
    jint sourceBufferNumSamples; /*Num of samples in input audio (default=0)*/
};


void olPreInit(struct GlobalSettings *settings,struct OutputLine *chan);

void olDestroy(struct GlobalSettings *settings,struct OutputLine *chan);

void olInit(struct GlobalSettings *settings,struct OutputLine *chan, jfloat *outputBuffer);

void olDisconnectSource(struct GlobalSettings *settings,struct OutputLine *chan);

void olConnectSource(struct GlobalSettings *settings,struct OutputLine *chan, jfloat *sourceBuffer, jint sourceSamples);

/**
 * Get float buffer of the connected source or NULL if there is no source connected
 */
jfloat *olGetConnectedSourceBuffer(struct GlobalSettings *settings,struct OutputLine *chan);
/**
 * Get num of samples in connected source
 */
jint olGetConnectedSourceSamples(struct GlobalSettings *settings,struct OutputLine *chan);

/**
 * Return the float buffer of the output channel
 */
jfloat *olGetOutputBuffer(struct GlobalSettings *settings,struct OutputLine *chan);


/**
 * Return true if any audio source is connected to the channel
 */
jboolean olHasConnectedSourceBuffer(struct GlobalSettings *settings,struct OutputLine *chan);

/**
 * Store index of the lastest processed frame in the output buffer.
 */
void olSetLastProcessedFrameId(struct GlobalSettings *settings,struct OutputLine *chan, jint v);

/**
 * Retrieve the index of the latest processed frame from the output buffer
 */
jint olGetLastProcessedFrameId(struct GlobalSettings *settings,struct OutputLine *chan);

/**
 * Retrieve the index of the latest played frame from the output buffer
 */
jint olGetLastPlayedFrameId(struct GlobalSettings *settings,struct OutputLine *chan);

/**
 * Check if processing is completed (ie the last source frame has been processed)
 */
jboolean olIsProcessingCompleted(struct GlobalSettings *settings,struct OutputLine *chan);

/**
 * Notify that the processing is completed by increasing the last processed frame 
 * index by 1 and making it negative.
 */
void olSetProcessingCompleted(struct GlobalSettings *settings,struct OutputLine *chan);

/**
 * Read one frame from the audio source
 */
jfloat *olReadFrame(struct GlobalSettings *settings,struct OutputLine *chan, jint frameIndex,jfloat *store);
/**
*   Write one frame to the output buffer
*/
void olWriteFrame(struct GlobalSettings *settings,struct OutputLine *chan, jint frameIndex, jfloat *frame,jint frameSize);

#endif