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
#include "OutputLine.h"
#include "memory_layout/OUTPUT_LINE_LAYOUT.h"

struct OutputLine *olNew(struct GlobalSettings *settings,jfloat *outputBuffer){
    struct OutputLine *line= malloc(sizeof(struct OutputLine) );
    line->sourcesSlots = asNew(settings, settings->nSourcesPerLine);
    line->outputBuffer = outputBuffer;    
    line->numConnectedSources = 0;
    line->uList = (struct UList*) malloc(sizeof(struct UList));
    ulistInit(line->uList);
    return line;
}

void olDestroy(struct GlobalSettings *settings,struct OutputLine *line){
    asDestroy(settings, line->sourcesSlots,settings->nSourcesPerLine);
    free(line);
    ulistDestroy(line->uList);
}




struct AudioSource *olConnectSource(struct GlobalSettings *settings, struct OutputLine *line,jfloat *data,jint sourceSamples){
    jint i=0;
    for (i = 0; i < settings->nSourcesPerLine; i++) {
        // find an empty source slot    
        if (asIsReady(&line->sourcesSlots[i])) {
            asConnect(settings,line->uList,&line->sourcesSlots[i], data, sourceSamples,0);
            line->numConnectedSources++;           
            return &line->sourcesSlots[i];
        }
    }       
    return NULL;
}

void olFinalizeDisconnection(struct GlobalSettings *settings,struct OutputLine *line,
struct AudioSource *source){
    asFinalizeDisconnection(settings, line->uList, source);
    line->numConnectedSources--;
}

void olDisconnectSource(struct GlobalSettings *settings,struct OutputLine *line,struct AudioSource *source,jint delayedToLineFrame){
    asScheduleDisconnection(settings, line->uList, source, delayedToLineFrame);
}

void olSetLastProcessedFrameId(struct GlobalSettings *settings,struct OutputLine *line, jint v) {
    ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)] = v;
}

jint olGetLastProcessedFrameId(struct GlobalSettings *settings,struct OutputLine *line) {
    jint n = ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)];
    if (n < 0)
        n = -n;
    return n;
}
 

jint olGetLastPlayedFrameId(struct GlobalSettings *settings,struct OutputLine *line) {
    return ((jint *)line->outputBuffer)[olHeader(LAST_PLAYED_FRAME)];
}

void olWriteFrame(struct GlobalSettings *settings,struct OutputLine *line, jint frameIndex, jfloat *frame,jint frameSize,jfloat volume) {
    for (jint i = 0; i < frameSize; i++) {
        line->outputBuffer[olHeader(BODY) + frameSize * frameIndex + i] = frame[i]*volume;
    }
}



