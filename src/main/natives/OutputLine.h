
#ifndef __OUTPUT_LINE__
#define __OUTPUT_LINE__

#include "memory_layout/OUTPUT_LINE_LAYOUT.h"
#include "Settings.h" 
#include <stdint.h>
#include "types.h"
#include "AudioSource.h"
#include "UList.h"
#include <stdlib.h>



struct OutputLine {
    jfloat *outputBuffer;  /*where to store processed audio. Readable and writable.*/
    struct AudioSource *sourcesSlots;
    struct UList* uList;
    jint numConnectedSources;
};



/**
 * Allocates one or more OutputLines
 */
struct OutputLine *olNew(struct GlobalSettings *settings, jint n);

/**
 * Deallocates one or more OutputLines
 * This function undoes olNew
 */
void olDestroy(struct GlobalSettings *settings, struct OutputLine *chan,jint n);

/**
 * Initialize the output line
 */
void olInit(struct GlobalSettings *settings, struct OutputLine *chan, jfloat *outputBuffer);

jboolean olIsInitialized(struct GlobalSettings *settings, struct OutputLine *line);

/**
 *
 */

/**
 * Connect one audio source to the best audio line
 * @return a pointer to the connected AudioSource
 */
struct AudioSource *olConnectSourceToBestLine(struct GlobalSettings *settings, struct OutputLine *lines,jint nLines, jfloat *data, jint sourceSamples);

/**
 * Disconnect the audio source from the line to which it is attached, if any, otherwise do nothing
 * NB. after this function is called, it is not safe to free the AudioSource, because it will be reused internally
 */
void olDisconnectSource(struct GlobalSettings *settings, struct AudioSource* source);

/**
 * Store index of the lastest processed frame in the output buffer.
 */
void olSetLastProcessedFrameId(struct GlobalSettings *settings, struct OutputLine *line, jint v);


/**
 * Retrieve the index of the latest processed frame from the output buffer
 */
jint olGetLastProcessedFrameId(struct GlobalSettings *settings, struct OutputLine *line);

/**
 * Retrieve the index of the latest played frame from the output buffer
 */
jint olGetLastPlayedFrameId(struct GlobalSettings *settings, struct OutputLine *line);

/**
*   Write one frame to the output buffer
*/
void olWriteFrame(struct GlobalSettings *settings, struct OutputLine *line, jint frameIndex, jfloat *frame, jint frameSize);

#endif