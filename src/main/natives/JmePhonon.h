
#ifndef __JMEPHONON_H__
#define __JMEPHONON_H__

#include <jni.h>
#include "phonon.h"
#include "Settings.h"
#include "types.h"
#include <stdlib.h>


void phInit(struct GlobalSettings *settings,jint mixQueueMaxSize);
void phProcessFrame(struct GlobalSettings *settings, jfloat *inFrame, jfloat *outFrame);
void phDestroy(struct GlobalSettings *settings);
void phUpdateListener(struct GlobalSettings *settings,
jfloat wposx,jfloat wposy,jfloat wposz,
jfloat wrotx,jfloat wroty,jfloat wrotz,jfloat wrotw

);
void phMixOutputBuffers(jfloat **input, jint numInputs, jfloat *output);
#endif