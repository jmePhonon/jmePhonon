
#ifndef __JMEPHONON_H__
#define __JMEPHONON_H__

#include "phonon.h"

#include "Common.h"
#include "AudioSource.h"
#include "Listener.h"



void phInit(struct GlobalSettings *settings,jint mixQueueMaxSize);
void phProcessFrame(struct GlobalSettings *settings,struct Listener *listener , struct AudioSource *source,jfloat *inFrame, jfloat *outFrame);
void phDestroy(struct GlobalSettings *settings);
 
 
/** 
 * Allocates one PhContext for the audioSour ce
 */
void phInitializeSource(struct GlobalSettings *settings, struct AudioSource *audioSource);


/**
 * Deallocates the PhContext of the audioSource
 */
void phDestroySource(struct GlobalSettings *settings,struct AudioSource *audioSource);

/**
 * Reset the internal state for the audioSource.
 * This is used when the audioData for the source is changed
 */
void phFlushSource(struct GlobalSettings *settings,struct AudioSource *audioSource);


void phMixOutputBuffers(jfloat **input, jint numInputs, jfloat *output);
#endif