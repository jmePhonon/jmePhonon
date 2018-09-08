
#ifndef __JMEPHONON_H__
#define __JMEPHONON_H__

#include <jni.h>
#include "phonon.h"
#include "Settings.h"
#include "types.h"
#include <stdlib.h>
#include <math.h>
#include "AudioSource.h"
#include "memory_layout/LISTENER_LAYOUT.h" 

struct {
    IPLhandle context;
    IPLRenderingSettings settings;
    IPLSimulationSettings simulationSettings;

    IPLAudioFormat monoFormat;
    IPLAudioBuffer monoBuffer1;
    IPLAudioBuffer monoBuffer2;
    jfloat *auxMonoFrame;


    IPLAudioFormat outputFormat;
    IPLAudioBuffer outputBuffer;

    IPLVector3 listenerPosition;
    IPLVector3 listenerDirection;
    IPLVector3 listenerUp;

    IPLhandle scene;
    IPLMaterial *materials;

    IPLhandle environment;
    IPLhandle environmentalRenderer;

    IPLHrtfParams defaultHrtfParams;
    IPLhandle binauralRenderer;

    IPLAudioBuffer *mixerQueue;

    jfloat *listenerData;
    jfloat ***audioSourceData;
} PhSharedContext; // This context is shared between every source

struct PhContext { //nb for each source we need to create a new PhContext    
    IPLhandle binauralEffect;
    IPLhandle directSoundEffect;
    IPLDirectSoundEffectOptions directSoundEffectOptions;
};

void phInit(struct GlobalSettings *settings,jint mixQueueMaxSize,float *listenerData, float ***audioSourcesData);
void phProcessFrame(struct GlobalSettings *settings, struct AudioSource *source,jfloat *inFrame, jfloat *outFrame);
void phDestroy(struct GlobalSettings *settings);


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