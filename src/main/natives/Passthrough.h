#ifndef __PASSTRHROUGH__
#define __PASSTRHROUGH__
#include "Common.h" 

static inline void passThrough(struct GlobalSettings *settings,jfloat *input, jfloat *output) {
    int inputIndex = 0;
    int outputIndex = 0;
    while(inputIndex<settings->inputFrameSize){
        for(int j=0;j<settings->nOutputChannels;j++){
            output[outputIndex++] = input[inputIndex];
        }
        inputIndex++;
    }
}

static inline  void  passThroughMixer(struct GlobalSettings *settings,jfloat** inputs,jint nInputs,jfloat *output){
    for (jint i = 0; i < settings->inputFrameSize * settings->nOutputChannels; i++) {
        jfloat res = 0;
        for (jint j = 0; j < nInputs; j++) {
            res += inputs[j][i];
        }
        res /= nInputs;
        output[i] = res;
    }
}

#endif