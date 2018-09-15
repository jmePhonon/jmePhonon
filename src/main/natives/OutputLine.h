/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/

#ifndef __OUTPUT_LINE__
#define __OUTPUT_LINE__

#include "Common.h" 

#include "AudioSource.h"
#include "UList.h"



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
void olWriteFrame(struct GlobalSettings *settings, struct OutputLine *line, jint frameIndex, jfloat *frame, jint frameSize,jfloat volume);

#endif