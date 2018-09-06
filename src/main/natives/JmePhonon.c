#define __JMEPHONON_INTERNAL__
#include "JmePhonon.h"

#include "types.h"

  struct {
        IPLhandle context;
        IPLRenderingSettings settings;

        IPLAudioFormat inputFormat;
        IPLAudioFormat outputFormat;
        IPLAudioBuffer inputBuffer;
        IPLAudioBuffer outputBuffer;

        IPLVector3 listenerPosition;
        IPLQuaternion listenerRotation;

        IPLHrtfParams defaultHrtfParams;
        IPLhandle binauralRenderer;

        IPLAudioBuffer *mixerQueue;
    
    } PhSharedContext; // This context is shared between every source

    struct PhContext { //nb for each source we need to create a new PhContext    
        IPLhandle binauralEffect;
    
    } ;

/** Adapted from jmonkeyengine's Quaternion.java */
void phMultQtrVec(IPLQuaternion *qtr, IPLVector3 *v, IPLVector3 *store) {
    jfloat vx = v->x, vy = v->y, vz = v->z;
    if (vx == 0.f && vy == 0.f && vz == 0.f) {
        store->x = 0;
        store->y = 0;
        store->z = 0;
    } else {
        jfloat x = qtr->x;
        jfloat y = qtr->y;
        jfloat z = qtr->z;
        jfloat w = qtr->w;

        store->x = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x * vx + 2 * y * x * vy + 2 * z * x * vz - z * z * vx - y * y * vx;
        store->y = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w * z * vx - z * z * vy + w * w * vy - 2 * x * w * vz - x * x * vy;
        store->z = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w * y * vx - y * y * vz + 2 * w * x * vy - x * x * vz + w * w * vz;
    }
}

void phSubVecVec(IPLVector3 *v1, IPLVector3 *v2,IPLVector3 *store){
    jfloat x = v1->x, y = v1->y, z = v1->z;
    store->x = x-v2->x;
    store->y = y-v2->y;
    store->z = z-v2->z;
}

void phInit(struct GlobalSettings *settings,jint mixerQueueSize){
    iplCreateContext(NULL, NULL, NULL, &PhSharedContext.context);
    PhSharedContext.settings.samplingRate = settings->sampleRate;
    PhSharedContext.settings.frameSize =settings->inputFrameSize;

    PhSharedContext.defaultHrtfParams.type = IPL_HRTFDATABASETYPE_DEFAULT;
    iplCreateBinauralRenderer(PhSharedContext.context, PhSharedContext.settings, PhSharedContext.defaultHrtfParams, &PhSharedContext.binauralRenderer);     
        
    PhSharedContext.inputFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhSharedContext.inputFormat.channelLayout = IPL_CHANNELLAYOUT_MONO;
    PhSharedContext.inputFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;

    PhSharedContext.outputFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhSharedContext.outputFormat.channelLayout = IPL_CHANNELLAYOUT_STEREO;
    PhSharedContext.outputFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;


    PhSharedContext.inputBuffer.format = PhSharedContext.inputFormat;
    PhSharedContext.outputBuffer.format = PhSharedContext.outputFormat;

    PhSharedContext.inputBuffer.numSamples = PhSharedContext.settings.frameSize ;
    PhSharedContext.outputBuffer.numSamples = PhSharedContext.settings.frameSize ;

    phUpdateListener(settings,0, 0, 0,     0, 0, 0, 0);



    PhSharedContext.mixerQueue = malloc(sizeof(IPLAudioBuffer)*mixerQueueSize);
    for(jint i=0;i<mixerQueueSize;i++){
        PhSharedContext.mixerQueue[i].format = PhSharedContext.outputFormat;
        PhSharedContext.mixerQueue[i].numSamples = PhSharedContext.settings.frameSize ;        
    }
}

void phDestroy(struct GlobalSettings *settings){
    free(PhSharedContext.mixerQueue);
    // TODO: properly destroy phonon
}



/**
 * Allocates one PhContext for the audioSource
 */
void phInitializeSource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    struct PhContext *context = malloc(sizeof(struct PhContext));
    iplCreateBinauralEffect(PhSharedContext.binauralRenderer, PhSharedContext.inputFormat, PhSharedContext.outputFormat, &context->binauralEffect);
    audioSource->phononContext = context;

}

/**
 * Deallocates the PhContext of the audioSource
 */
void phDestroySource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    free(audioSource->phononContext);
}

void phFlushSource(struct GlobalSettings *settings,struct AudioSource *audioSource){

}

void phProcessFrame(struct GlobalSettings *settings,struct AudioSource *source,jfloat *inFrame, jfloat *outFrame){
    // for(jint i=0;i<PhSharedContext.settings.frameSize;i++){
    //     outframe[i] = inframe[i];
    // }
    struct PhContext *ctx=source->phononContext;
    if(ctx==NULL){
        printf("FIXME: PhContext is null for this source?\n");
        return;
    }

    IPLVector3 direction;
    // phMultQtrVec(PhSharedContext.listenerRotation, audiosourceposition,&direction
    // phSubVecVec(direction,PhSharedContext.listenerPosition,&direction);
    direction.x = 1.0;
    direction.y = 1.0;
    direction.z = 1.0;
    

    PhSharedContext.inputBuffer.interleavedBuffer = inFrame;
    PhSharedContext.outputBuffer.interleavedBuffer = outFrame;

    iplApplyBinauralEffect(ctx->binauralEffect,PhSharedContext.inputBuffer, direction, IPL_HRTFINTERPOLATION_NEAREST, PhSharedContext.outputBuffer);
}


void phUpdateListener(struct GlobalSettings *settings,
jfloat wposx,jfloat wposy, jfloat wposz,
jfloat wrotx,jfloat wroty,jfloat wrotz,jfloat wrotw

){
    PhSharedContext.listenerPosition.x = wposx;
    PhSharedContext.listenerPosition.y = wposy;
    PhSharedContext.listenerPosition.z = wposz;

    PhSharedContext.listenerRotation.x = wrotx;
    PhSharedContext.listenerRotation.y = wroty;
    PhSharedContext.listenerRotation.z = wrotz;
    PhSharedContext.listenerRotation.w = wrotw;

}

/**
 * Mix multiple outputBuffers
 */
void phMixOutputBuffers(jfloat **input,jint numInputs,jfloat *output){
    for(jint i=0;i<numInputs;i++){
        PhSharedContext.mixerQueue[i].interleavedBuffer = input[i];
    }
    PhSharedContext.outputBuffer.interleavedBuffer = output;
    iplMixAudioBuffers(numInputs, PhSharedContext.mixerQueue, PhSharedContext.outputBuffer);
}