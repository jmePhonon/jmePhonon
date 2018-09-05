
#ifndef __PHONON_H__
#define __PHONON_H__

#include <jni.h>
#include "phonon.h"
#include "Settings.h"
#include "OutputLine.h"
struct {
    IPLhandle context;
    IPLRenderingSettings settings;

    IPLAudioFormat inputFormat;
    IPLAudioFormat outputFormat;
    IPLAudioBuffer inputBuffer;
    IPLAudioBuffer outputBuffer;

    IPLVector3 listenerPosition;
    IPLQuaternion listenerRotation;

    IPLHrtfParams hrtfParams;
    IPLhandle binauralRenderer;
    IPLhandle binauralEffect;
} PhContext;

void phInit(struct GlobalSettings *settings);
void phProcessFrame(struct GlobalSettings *settings,jfloat *inFrame,jfloat *outFrame);
void phDestroy(struct GlobalSettings *settings);
void phUpdateListener(struct GlobalSettings *settings,
jfloat wposx,jfloat wposy,jfloat wposz,
jfloat wrotx,jfloat wroty,jfloat wrotz,jfloat wrotw

);
#endif