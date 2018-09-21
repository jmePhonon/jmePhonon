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
#include "AudioSource.h"
#include "memory_layout/AUDIOSOURCE_LAYOUT.h"

void asInit(struct GlobalSettings *settings, struct AudioSource *source){
    source->data = NULL;
    source->lastReadFrameIndex = 0;

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

void asSetSceneData(struct GlobalSettings *settings, struct AudioSource *source, jfloat *data){
    source->sceneData = data;
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
    jint frameSize = settings->frameSize*asGetNumChannels(settings, source);
    jint sourceSamples = source->numSamples;
    jfloat *data = source->data;
    jboolean hasReachedEnd = false;
    for (jint i = 0; i < frameSize; i++) {
        jint sampleIndex = frameSize * source->lastReadFrameIndex + i;
        sampleIndex *= asGetPitch(settings, source);

        jfloat v;
        if (sampleIndex >= sourceSamples) {  // Write 0s if the frame size exceed the remaining source's bytes
            // printf("Phonon: trying to read sample n%d but source contains only %d samples. A zero sample will be returned instead.\n ", sampleIndex, sourceSamples);
            v = 0;
            hasReachedEnd = true;
        } else {
            v = data[sampleIndex];
        }
        v *= asGetVolume(settings, source);

        store[i] = v ;
    }
    source->lastReadFrameIndex++;

    // Reset the frame index when the end is reached, useful if we intend to loop the sound
    if(hasReachedEnd){
        source->lastReadFrameIndex = 0;
    }

    return hasReachedEnd;
}


jfloat asGetVolume(struct GlobalSettings *settings,struct AudioSource *source) {
    return source->sceneData[asSourceField(VOLUME)];
}

jfloat asGetPitch(struct GlobalSettings *settings, struct AudioSource *source) {
    return source->sceneData[asSourceField(PITCH)];
}

vec3* asGetSourcePosition(struct GlobalSettings *settings,struct AudioSource *source) {
    source->_position.x =  source->sceneData[asSourceField(POSX)];
    source->_position.y= source->sceneData[asSourceField(POSY)];
    source->_position.z = source->sceneData[asSourceField(POSZ)];
    return &source->_position;
}

vec3* asGetSourceDirection(struct GlobalSettings *settings,struct AudioSource *source) {
    source->_direction.x = source->sceneData[asSourceField(AHEADX)];
    source->_direction.y= source->sceneData[asSourceField(AHEADY)];
    source->_direction.z = source->sceneData[asSourceField(AHEADZ)];
    return &source->_direction;
}

vec3* asGetSourceUp(struct GlobalSettings *settings,struct AudioSource *source) {
    source->_up.x = source->sceneData[asSourceField(UPX)];
    source->_up.y = source->sceneData[asSourceField(UPY)];
    source->_up.z = source->sceneData[asSourceField(UPZ)];
    return &source->_up;
}

vec3* asGetSourceRight(struct GlobalSettings *settings,struct AudioSource *source) {
    source->_right.x = source->sceneData[asSourceField(RIGHTX)];
    source->_right.y = source->sceneData[asSourceField(RIGHTY)];
    source->_right.z = source->sceneData[asSourceField(RIGHTZ)];
    return &source->_right;
}

jboolean _asHasFlag(struct GlobalSettings *settings,struct AudioSource *source,jint flag){
    jbyte *dataByte=(jbyte*)source->sceneData;
    jbyte flags = dataByte[asSourceFieldB(FLAGS)]; 
    return (flags & flag) == flag;
}
 
drt* asGetSourceDirectivity(struct GlobalSettings *settings,struct AudioSource *source) {
    source->_directivity.dipoleWeight = source->sceneData[asSourceField(DIPOLEWEIGHT)];
    source->_directivity.dipolePower = source->sceneData[asSourceField(DIPOLEPOWER)];
    
    source->_directivity.callback = NULL;
    source->_directivity.userData = NULL;

    return &source->_directivity;
}

jint asGetNumChannels(struct GlobalSettings *settings,struct AudioSource *source){
    jbyte *dataByte=(jbyte*)source->sceneData;
    jbyte numChannels = dataByte[asSourceFieldB(NUM_CHANNELS)];
    return (jint) numChannels; 
}

void asSetStopAt(struct GlobalSettings *settings, struct AudioSource *source, jint index){
    jint *data=(jint*)source->sceneData;
    data[asSourceField(STOPAT)] = index;
}