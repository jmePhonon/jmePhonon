#include "com_jme3_phonon_CHANNEL_LAYOUT.h"

/**
 * Returns position of first byte of the field
 */
#define chHeaderB(field) com_jme3_phonon_CHANNEL_LAYOUT_##field

/**
 * Returns field length in bytes
 */
#define chHeaderSizeB(field) com_jme3_phonon_CHANNEL_LAYOUT_##field##_fieldsize

/**
 * Returns position of first 4bytes of the field
 */
#define chHeader(field) (chHeaderB(field)/4)

/**
 * Returns field length 
 */
#define chHeaderSize(field) (chHeaderSizeB(field)/4)


struct ChOutput {
    jfloat *outputBuffer;  /*where to store processed audio. Readable and writable.*/
    jint outputBufferSize; /*Num of frames for outputBuffer*/

    jint frameSize; /*Num of samples for each frame*/
    jfloat *lastReadFrame;

    // These values are set to default when source is disconnected.
    jfloat *sourceBuffer;        /*where to read input audio (default=NULL). Read only*/
    jint sourceBufferNumSamples; /*Num of samples in input audio (default=0)*/
};


void chPreInit(struct ChOutput *chan);

void chDestroy(struct ChOutput *chan);

void chInit(struct ChOutput *chan, jfloat *outputBuffer, jint outputBufferSize, jint frameSize);

void chDisconnectSource(struct ChOutput *chan);

void chConnectSource(struct ChOutput *chan, jfloat *sourceBuffer, jint sourceSamples);

/**
 * Get float buffer of the connected source or NULL if there is no source connected
 */
jfloat *chGetConnectedSourceBuffer(struct ChOutput *chan);
/**
 * Get num of samples in connected source
 */
jint chGetConnectedSourceSamples(struct ChOutput *chan);

/**
 * Return the float buffer of the output channel
 */
jfloat *chGetOutputBuffer(struct ChOutput *chan);

/**
 * Get num of frames in output buffer
 */
jint chGetOutputBufferSize(struct ChOutput *chan);

/**
 * Get num of sample in one output buffer frame
 */
jint chGetFrameSize(struct ChOutput *chan);
/**
 * Return true if any audio source is connected to the channel
 */
jboolean chHasConnectedSourceBuffer(struct ChOutput *chan);

/**
 * Store index of the lastest processed frame in the output buffer.
 */
void chSetLastProcessedFrameId(struct ChOutput *chan, jint v);

/**
 * Retrieve the index of the latest processed frame from the output buffer
 */
jint chGetLastProcessedFrameId(struct ChOutput *chan);

/**
 * Retrieve the index of the latest played frame from the output buffer
 */
jint chGetLastPlayedFrameId(struct ChOutput *chan);

/**
 * Check if processing is completed (ie the last source frame has been processed)
 */
jboolean chIsProcessingCompleted(struct ChOutput *chan);

/**
 * Notify that the processing is completed by increasing the last processed frame 
 * index by 1 and making it negative.
 */
void chSetProcessingCompleted(struct ChOutput *chan);

/**
 * Read one frame from the audio source
 */
jfloat *chReadFrame(struct ChOutput *chan, jint frameIndex);
/**
*   Write one frame to the output buffer
*/
void chWriteFrame(struct ChOutput *chan, jint frameIndex, jfloat *frame);