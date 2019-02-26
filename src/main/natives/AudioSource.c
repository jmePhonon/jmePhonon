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

struct AudioSource* asNew(struct GlobalSettings *settings, jint n){
    struct AudioSource *out = malloc(sizeof(struct AudioSource)*n);
    for (jint i = 0; i < n; i++) {
        struct AudioSource* slot = &out[i];
        slot->data = NULL;
        slot->lastReadFrameIndex = 0;
        slot->numSamples = 0;
        slot->loop = false;
        slot->phononContext = NULL;
        slot->uNode = (struct UListNode*) malloc(sizeof(struct UListNode));
        slot->sceneData = NULL;
        slot->id = i;
        ulistInitNode(slot->uNode, slot);
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

jboolean asIsReady(struct GlobalSettings *settings,struct AudioSource *source){
    return source->data!=NULL&&!asHasFlag(settings,source,MARKED_FOR_DISCONNECTION);
}

jboolean asIsFree(struct GlobalSettings *settings,struct AudioSource *source){
    return source->data==NULL&&!asHasFlag(settings,source,MARKED_FOR_DISCONNECTION);
}


/**
 * Read the next frame from the audio source, restart from the beginning when the end is reached
 * @return true if the end of the source has been reached
 */
jboolean asReadNextFrame(struct GlobalSettings *settings,struct AudioSource *source,  jfloat master_volume,jfloat *store) {
    jint frameSize = settings->frameSize*asGetNumChannels(settings, source);
    jint sourceSamples = source->numSamples;
    jfloat *data = source->data;
    jboolean hasReachedEnd = false;

    jfloat pitch=asGetPitch(settings, source);
    jfloat volume=asGetVolume(settings, source);
    jboolean loop=asHasFlag(settings ,source, LOOP);

    jint read=0;
    jint i=0;
    while(read<frameSize){
        jint sampleIndex = frameSize * source->lastReadFrameIndex + i;
        sampleIndex *= pitch;
    
        jfloat v;

        if (sampleIndex >= sourceSamples ) {
            if(loop){ 
                asResetForLoop(settings,source);
                i=0;
                continue;
            }else{
                v=0;
                hasReachedEnd = true;
            }
        }else{
            v = data[sampleIndex];   
        }
   
        v *= volume;
        v *= master_volume;
        store[read] = v;
        read++; i++;
    }    
    if(!hasReachedEnd){      
        source->lastReadFrameIndex++;
    }
    return hasReachedEnd;
}

void asResetForLoop(struct GlobalSettings *settings,struct AudioSource *source){
    source->lastReadFrameIndex = 0;
}

jfloat asGetVolume(struct GlobalSettings *settings,struct AudioSource *source) {
    return source->sceneData[asSourceField(VOLUME)];
}

jfloat asGetSourceRadius(struct GlobalSettings *settings, struct AudioSource *source) {
    return source->sceneData[asSourceField(SOURCERADIUS)];
}

jint asGetDirectOcclusionMode(struct GlobalSettings *settings, struct AudioSource *source) {
    jbyte *dataByte=(jbyte*)source->sceneData;
    return (jint) (dataByte[asSourceFieldB(DIROCCMODE)]);
}

jint asGetDirectOcclusionMethod(struct GlobalSettings *settings, struct AudioSource *source) {
    jbyte *dataByte=(jbyte*)source->sceneData;
    return (jint) (dataByte[asSourceFieldB(DIROCCMETHOD)]);
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


void asConnect(struct GlobalSettings *settings,struct UList *updateList,struct AudioSource *slot, jfloat *data, jint samples,jint jumpToFrame){
    slot->data = data;
    slot->numSamples = samples;
    slot->lastReadFrameIndex = jumpToFrame;
    ulistAdd(updateList,slot->uNode);  
}

void asScheduleDisconnection(struct GlobalSettings *settings,struct UList* updateList,struct AudioSource *slot){
    jint *data=(jint*)slot->sceneData;
    data[asSourceField(FLAGS)] |= asFlag(MARKED_FOR_DISCONNECTION);
    ulistRemove(slot->uNode);
}

void asFinalizeDisconnection(struct GlobalSettings *settings,struct UList *updateList,struct AudioSource *slot){
    slot->data = NULL;
    ulistRemove(slot->uNode);
}