#include "AudioSource.h"

void asInit(struct GlobalSettings *settings, struct AudioSource *source){
    source->data = NULL;
    source->lastReadFrameIndex = 0;
    source->position[0] = 0;
    source->position[1] = 0;
    source->position[2] = 0;

    source->rotation[0] = 0;
    source->rotation[1] = 0;
    source->rotation[2] = 0;
    source->rotation[3] = 0;

    source->velocity = 0;
    source->volume = 1;
    source->pitch = 1;

    source->connectedLine = NULL;
    source->phononContext = NULL;

    source->uNode = (struct UListNode*) malloc(sizeof(struct UListNode));
    ulistInitNode(source->uNode, source); 
}

struct AudioSource* asNew(struct GlobalSettings *settings, jint n){
    struct AudioSource *out = malloc(sizeof(struct AudioSource)*n);
    for (jint i = 0; i < n; i++) {
        // printf("Initialize source %d\n", i);
        asInit(settings, &out[i]);
    }
    return out;
}

void asDestroy(struct GlobalSettings *settings,struct AudioSource *source,jint n){
    ulistDestroyNode(source->uNode);
    free(source);
}

jboolean asIsConnected(struct AudioSource *source){
    // return source->connectedLine != NULL && source->data != NULL;
    return source->uNode->connected;
}
/**
 * Read the next frame from the audio source, restart from the beginning when the end is reached
 * @return true if the end of the source has been reached
 */
jboolean asReadNextFrame(struct GlobalSettings *settings,struct AudioSource *source,  jfloat *store) {
    jint frameSize = settings->inputFrameSize;
    jint sourceSamples = source->numSamples;
    jfloat *data = source->data;
    jboolean hasReachedEnd = false;
    for (jint i = 0; i < frameSize; i++) {
        jint sampleIndex = frameSize * source->lastReadFrameIndex + i;
        sampleIndex *= source->pitch;
        jfloat v;
        if (sampleIndex >= sourceSamples) {  // Write 0s if the frame size exceed the remaining source's bytes
            // printf("Phonon: trying to read sample n%d but source contains only %d samples. A zero sample will be returned instead.\n ", sampleIndex, sourceSamples);
            v = 0;
            hasReachedEnd = true;
        } else {
            v = data[sampleIndex];
        }
        store[i] = v*source->volume;
    }
    source->lastReadFrameIndex++;

    // Reset the frame index when the end is reached, useful if we intend to loop the sound
    if(source->lastReadFrameIndex>=source->numFrames){
        source->lastReadFrameIndex = 0;
    }

    return hasReachedEnd;
}
