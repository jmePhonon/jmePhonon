
#ifndef __JMEPHONON_H__
#define __JMEPHONON_H__

#include <jni.h>
#include "phonon.h"
#include "Settings.h"
#include "types.h"
#include <stdlib.h>
#include "AudioSource.h"

void phInit(struct GlobalSettings *settings,jint mixQueueMaxSize);
void phProcessFrame(struct GlobalSettings *settings, struct AudioSource *source,jfloat *inFrame, jfloat *outFrame);
void phDestroy(struct GlobalSettings *settings);
void phUpdateListener(struct GlobalSettings *settings,
jfloat wposx,jfloat wposy,jfloat wposz,
jfloat wrotx,jfloat wroty,jfloat wrotz,jfloat wrotw

);


/**
 * Allocates one PhContext for the audioSource
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