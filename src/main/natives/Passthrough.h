#ifndef __PASSTRHROUGH__
#define __PASSTRHROUGH__
#include "Common.h" 

static inline void passThrough(struct GlobalSettings *settings,jfloat *input, jfloat *output,jint inputChannels) {
    jint inputIndex = 0;
    jint outputIndex = 0;
    jint ml = settings->nOutputChannels / inputChannels;
    while (inputIndex < settings->frameSize*inputChannels) {
        for(jint j=0;j<ml;j++){
            output[outputIndex++] = input[inputIndex];
        }
        inputIndex++;
    }
}

static inline  void  passThroughMixer(struct GlobalSettings *settings,jfloat** inputs,jint nInputs,jfloat *output){
    for (jint i = 0; i < settings->frameSize * settings->nOutputChannels; i++) {
        jfloat res = 0;
        for (jint j = 0; j < nInputs; j++) {
            res += inputs[j][i];
        }
        res /= nInputs;
        output[i] = res;
    }
}

#endif