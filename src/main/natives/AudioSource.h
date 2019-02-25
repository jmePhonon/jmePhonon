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
#ifndef __AUDIO_SLOT__
#define __AUDIO_SLOT__ 1

#include "Common.h"
#include "UList.h"
#include "memory_layout/AUDIOSOURCE_LAYOUT.h"

struct AudioSource {
    jfloat *data; // float 32 little endian, multiple sources can share the same audio data
    jint numSamples;// length of audio data in samples
    jint lastReadFrameIndex; // Position where we were the last time we read the source

    vec3 _position; 
    vec3 _direction;   
    vec3 _up;         
    vec3 _right;    
    drt _directivity;

    jboolean loop;

    void *phononContext; // Pointer to the phonon context (nb. must be manually freed)
    struct UListNode* uNode; // U-List node

    jint id;

    jfloat* sceneData; // Physical data, passed by Java thread
};

/**
 * Allocate and initialize one or more AudioSources
 */
struct AudioSource *asNew(struct GlobalSettings *settings, jint n);

/**
 * Deallocates one or more AudioSources
 * This function undoes asNew
 */
void asDestroy(struct GlobalSettings *settings, struct AudioSource *source, jint n);

/**
 * Check if connected
 */
jboolean asIsReady(struct GlobalSettings *settings,struct AudioSource *source);

jboolean asIsFree(struct GlobalSettings *settings,struct AudioSource *source);

/**
 * Read the next frame from the audio source, restart from the beginning when the end is reached
 * @return true if the end of the source has been reached
 */
jboolean asReadNextFrame(struct GlobalSettings *settings, struct AudioSource *source, jfloat master_volume, jfloat *store);

jfloat asGetVolume(struct GlobalSettings *settings, struct AudioSource *source);
jint asGetDirectOcclusionMode(struct GlobalSettings *settings, struct AudioSource *source);
jint asGetDirectOcclusionMethod(struct GlobalSettings *settings, struct AudioSource *source);
jfloat asGetSourceRadius(struct GlobalSettings *settings, struct AudioSource *source);
jfloat asGetPitch(struct GlobalSettings *settings, struct AudioSource *source);
vec3 *asGetSourcePosition(struct GlobalSettings *settings, struct AudioSource *source);
vec3 *asGetSourceDirection(struct GlobalSettings *settings, struct AudioSource *source);

vec3 *asGetSourceUp(struct GlobalSettings *settings, struct AudioSource *source);
vec3 *asGetSourceRight(struct GlobalSettings *settings, struct AudioSource *source);

drt *asGetSourceDirectivity(struct GlobalSettings *settings, struct AudioSource *source);

void asSetSceneData(struct GlobalSettings *settings, struct AudioSource *source, jfloat *data);

jint asGetNumChannels(struct GlobalSettings *settings, struct AudioSource *source);
#define asHasFlag(settings, source, flag) (_asHasFlag(settings, source, asFlag(flag)))
jboolean _asHasFlag(struct GlobalSettings *settings, struct AudioSource *source, jint flag);
void asConnect(struct GlobalSettings *settings,struct UList *updateList,struct AudioSource *slot, jfloat *data, jint samples,jint jumpToFrame);
void asScheduleDisconnection(struct GlobalSettings *settings,struct UList *updateList,struct AudioSource *slot);
void asFinalizeDisconnection(struct GlobalSettings *settings, struct UList *updateList, struct AudioSource *slot);
void asResetForLoop(struct GlobalSettings *settings,struct AudioSource *source);
#endif