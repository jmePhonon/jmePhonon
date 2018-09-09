#include "OutputLine.h"
#include "memory_layout/OUTPUT_LINE_LAYOUT.h"

struct OutputLine *olNew(struct GlobalSettings *settings,jint nOutputLines){
    struct OutputLine *lines= malloc(sizeof(struct OutputLine) * nOutputLines);
    for(int i=0;i<nOutputLines;i++){
        // printf("Phonon: Initialize line id %d\n", i);
        lines[i].uList = (struct UList*) malloc(sizeof(struct UList));
        ulistInit(lines[i].uList);

        lines[i].sourcesSlots = asNew(settings, settings->nSourcesPerLine);
        lines[i].outputBuffer = NULL;
        lines[i].numConnectedSources = 0;
    }
    //    asInit(settings,&line->sourcesSlots[0]);
    return lines;
}




void olDestroy(struct GlobalSettings *settings,struct OutputLine *lines,jint nOutputLines){
    for(int i=0;i<nOutputLines;i++){
        asDestroy(settings, lines[i].sourcesSlots,settings->nSourcesPerLine);
        ulistDestroy(lines[i].uList);
    }
    free(lines);
}

void olInit(struct GlobalSettings *settings,struct OutputLine *line,jfloat *outputBuffer){
    line->outputBuffer = outputBuffer;    
}

jboolean olIsInitialized(struct GlobalSettings *settings, struct OutputLine *line){
    return line->outputBuffer != NULL;
}

struct AudioSource *olConnectSourceToBestLine(struct GlobalSettings *settings, struct OutputLine *lines,jint nLines,jfloat *data,jint sourceSamples){
    printf("Connect source to best line\n");
    jint sourceIndex = 0;
    struct OutputLine *bestLine = &lines[0];
    for(jint i=1;i<nLines;i++) {
        if(olIsInitialized(settings,&lines[i])&&lines[i].numConnectedSources<bestLine->numConnectedSources){
            bestLine = &lines[i];
            sourceIndex = i;
            if(bestLine->numConnectedSources == 0)
                break;
        }
    }

    sourceIndex *= settings->nSourcesPerLine;

    jint i=0;
    for (i = 0; i < settings->nSourcesPerLine; i++) {
        // find an empty source slot
    
        if (!asIsConnected(&bestLine->sourcesSlots[i])) {
        
            bestLine->sourcesSlots[i].data = data;
            bestLine->sourcesSlots[i].numSamples = sourceSamples;
            bestLine->sourcesSlots[i].connectedLine = bestLine;

            sourceIndex += i;
            bestLine->sourcesSlots[i].sourceIndex = sourceIndex;

            ulistAdd(bestLine->uList, bestLine->sourcesSlots[i].uNode);

            bestLine->numConnectedSources++;
            printf("Connect source to slot %d \n",i);
           
            return &bestLine->sourcesSlots[i];
        }else{
            printf("Source %d is connected\n", i);
        }
    }   
    
    printf("FIXME: Error. There is no space left for this source");
    

    return NULL;
}

void olDisconnectSource(struct GlobalSettings *settings,struct AudioSource *source){
    struct OutputLine *line = source->connectedLine; 
    line->numConnectedSources--; 
    source->data = NULL;
    source->connectedLine = NULL;
    ulistRemove(source->uNode);
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


// /**
//  * Check if processing is completed (ie the last source frame has been processed)
//  */
// jboolean olIsProcessingCompleted(struct GlobalSettings *settings,struct OutputLine *line) {
//     jint n = ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)];
//     return n < 0;
// }


// /**
//  * Notify that the processing is completed by increasing the last processed frame 
//  * index by 1 and making it negative.
//  */
// void olSetProcessingCompleted(struct GlobalSettings *settings,struct OutputLine *line) {
//     ((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)] = -(((jint *)line->outputBuffer)[olHeader(LAST_PROCESSED_FRAME)] + 1);
// }


void olWriteFrame(struct GlobalSettings *settings,struct OutputLine *line, jint frameIndex, jfloat *frame,jint frameSize,jfloat volume) {
    for (jint i = 0; i < frameSize; i++) {
        line->outputBuffer[olHeader(BODY) + frameSize * frameIndex + i] = frame[i]*volume;
    }
}



