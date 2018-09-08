#define __JMEPHONON_INTERNAL__
#include "JmePhonon.h"

#include "types.h"

struct {
    IPLhandle context;
    IPLRenderingSettings settings;
    IPLSimulationSettings simulationSettings;

    IPLAudioFormat monoFormat;
    IPLAudioBuffer monoBuffer1;
    IPLAudioBuffer monoBuffer2;
    jfloat *auxMonoFrame;


    IPLAudioFormat outputFormat;
    IPLAudioBuffer outputBuffer;

    IPLVector3 listenerPosition;
    IPLVector3 listenerDirection;
    IPLVector3 listenerUp;

    IPLhandle scene;
    IPLMaterial *materials;

    IPLhandle environment;
    IPLhandle environmentalRenderer;

    IPLHrtfParams defaultHrtfParams;
    IPLhandle binauralRenderer;

    IPLAudioBuffer *mixerQueue;

    jfloat *listenerData;
} PhSharedContext; // This context is shared between every source

struct PhContext { //nb for each source we need to create a new PhContext    
    IPLhandle binauralEffect;
    IPLhandle directSoundEffect;
    IPLDirectSoundEffectOptions directSoundEffectOptions;
};

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

jfloat phQtrNorm(IPLQuaternion *qtr) {
    jfloat x = qtr->x;
    jfloat y = qtr->y;
    jfloat z = qtr->z;
    jfloat w = qtr->w;
    
    return w * w + x * x + y * y + z * z;
}

void phQtrInverse(IPLQuaternion *qtr,IPLQuaternion *out){
    jfloat x = qtr->x;
    jfloat y = qtr->y;
    jfloat z = qtr->z;
    jfloat w = qtr->w;
    jfloat norm = phQtrNorm(qtr);
    if (norm > 0.0) {
        jfloat invNorm = 1.0f / norm;
        out->x *= -invNorm;
        out->y *= -invNorm;
        out->z *= -invNorm;
        out->w *= invNorm;
    }
    out->x = x;
    out->y = y;
    out->z = z;
    out->w = w;       
}

void phSubVecVec(IPLVector3 *v1, IPLVector3 *v2,IPLVector3 *store){
    jfloat x = v1->x, y = v1->y, z = v1->z;
    store->x = x-v2->x;
    store->y = y-v2->y;
    store->z = z-v2->z;
}

void phVecNormalize(IPLVector3 *v1,IPLVector3 *store){
    jfloat x = v1->x, y = v1->y, z = v1->z;
    jfloat length = x * x + y * y + z * z;
    if (length != 1.f && length != 0.f){
        length = 1.0f / sqrtf(length);
        store->x = x * length;
        store->y = y * length;
        store->z= z * length;
    }
    store->x = x;
    store->y = y;
    store->z = z;    
}



void phInit(struct GlobalSettings *settings,jint mixerQueueSize,float *listenerData){
    PhSharedContext.scene = NULL;

    /** TODO : make this configurable **/
    PhSharedContext.simulationSettings.sceneType = IPL_SCENETYPE_PHONON; // requires 64bit cpu
    PhSharedContext.simulationSettings.numRays = 1024;// typical values are in the range of 1024 to 131072
    PhSharedContext.simulationSettings.numDiffuseSamples = 32;//typical values are in the range of 32 to 4096. 
    PhSharedContext.simulationSettings.numBounces = 1;//typical values are in the range of 1 to 32. 
    PhSharedContext.simulationSettings.numThreads = 4;//The performance improves linearly with the number of threads upto the number of physical cores available on the CPU.
    PhSharedContext.simulationSettings.irDuration = 0.5; // 0.5 to 4.0.
    PhSharedContext.simulationSettings.ambisonicsOrder = 0;//Supported values are between 0 and 3.
    PhSharedContext.simulationSettings.maxConvolutionSources = 0; // TODO
    PhSharedContext.simulationSettings.bakingBatchSize=1;//IPL_SCENETYPE_RADEONRAYS


    //////////////////

    iplCreateContext(NULL, NULL, NULL, &PhSharedContext.context);
    PhSharedContext.settings.samplingRate = settings->sampleRate;
    PhSharedContext.settings.frameSize =settings->inputFrameSize;
    PhSharedContext.settings.convolutionType =IPL_CONVOLUTIONTYPE_PHONON ;
    
    PhSharedContext.monoFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhSharedContext.monoFormat.channelLayout = IPL_CHANNELLAYOUT_MONO;
    PhSharedContext.monoFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;

    PhSharedContext.outputFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhSharedContext.outputFormat.channelLayout = IPL_CHANNELLAYOUT_STEREO;
    PhSharedContext.outputFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;

    PhSharedContext.monoBuffer1.format = PhSharedContext.monoFormat;
    PhSharedContext.monoBuffer1.numSamples = PhSharedContext.settings.frameSize ;

    PhSharedContext.monoBuffer2.format = PhSharedContext.monoFormat;
    PhSharedContext.monoBuffer2.numSamples = PhSharedContext.settings.frameSize ;

    PhSharedContext.auxMonoFrame = malloc(4 * PhSharedContext.settings.frameSize);

    PhSharedContext.outputBuffer.format = PhSharedContext.outputFormat;
    PhSharedContext.outputBuffer.numSamples = PhSharedContext.settings.frameSize ;

    // Scene
    //{"generic",{0.10f,0.20f,0.30f,0.05f,0.100f,0.050f,0.030f}}
    PhSharedContext.materials = malloc(sizeof(IPLMaterial) * 1);
    PhSharedContext.materials[0].lowFreqAbsorption = .1f;
    PhSharedContext.  materials[0].midFreqAbsorption = .2f;
      PhSharedContext.materials[0].highFreqAbsorption =.3f;
      PhSharedContext.materials[0].scattering = .5f;
      PhSharedContext.materials[0].lowFreqTransmission = .1f;
      PhSharedContext.materials[0].midFreqTransmission = .04f;
      PhSharedContext.materials[0].highFreqTransmission = .03f;
    
    iplCreateScene(PhSharedContext.context, NULL /*compute device*/, PhSharedContext.simulationSettings, 1,
                    PhSharedContext.materials,
                   NULL, NULL, NULL, NULL, NULL,
                   &PhSharedContext.scene);

    // Environment
    iplCreateEnvironment(PhSharedContext.context,
    /*compute device*/ NULL,
    PhSharedContext.simulationSettings,
    PhSharedContext.scene,
    NULL,
    &PhSharedContext.environment);

    // Environmental renderer
    iplCreateEnvironmentalRenderer(PhSharedContext.context, PhSharedContext.environment,
                                   PhSharedContext.settings, PhSharedContext.monoFormat,
                                   NULL, NULL, &PhSharedContext.environmentalRenderer);

    PhSharedContext.defaultHrtfParams.type = IPL_HRTFDATABASETYPE_DEFAULT;
    iplCreateBinauralRenderer(PhSharedContext.context, PhSharedContext.settings, PhSharedContext.defaultHrtfParams, 
    &PhSharedContext.binauralRenderer);     
  


    PhSharedContext.listenerData = listenerData;


    PhSharedContext.mixerQueue = malloc(sizeof(IPLAudioBuffer)*mixerQueueSize);
    for(jint i=0;i<mixerQueueSize;i++){
        PhSharedContext.mixerQueue[i].format = PhSharedContext.outputFormat;
        PhSharedContext.mixerQueue[i].numSamples = PhSharedContext.settings.frameSize ;        
    }
}

void phDestroy(struct GlobalSettings *settings){
    free(PhSharedContext.mixerQueue);
    free(PhSharedContext.auxMonoFrame);
    free(PhSharedContext.materials);
    // TODO: properly destroy phonon
}



/**
 * Allocates one PhContext for the audioSource
 */
void phInitializeSource(struct GlobalSettings *settings, struct AudioSource *audioSource, float* audioSourceSceneData){
    struct PhContext *context = malloc(sizeof(struct PhContext));

    // TODO make this configurable
    context->directSoundEffectOptions.applyDistanceAttenuation = true;
    context->directSoundEffectOptions.applyAirAbsorption = false;
    context->directSoundEffectOptions.applyDirectivity = false;
    context->directSoundEffectOptions.directOcclusionMode = IPL_DIRECTOCCLUSION_NONE;

    // Direct sound
    iplCreateDirectSoundEffect(PhSharedContext.environmentalRenderer, 
    PhSharedContext.monoFormat,PhSharedContext.monoFormat, &context->directSoundEffect);

    iplCreateBinauralEffect(PhSharedContext.binauralRenderer, PhSharedContext.monoFormat, PhSharedContext.outputFormat, &context->binauralEffect);
    audioSource->phononContext = context;
    audioSource->sceneData = audioSourceSceneData;
}

/**
 * Deallocates the PhContext of the audioSource
 */
void phDestroySource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    free(audioSource->phononContext);
}

void phFlushSource(struct GlobalSettings *settings,struct AudioSource *audioSource){

}

IPLVector3 *phGetListenerPosition(){ 
    PhSharedContext.listenerPosition.x=PhSharedContext.listenerData[phListenerField(POSX)];
    PhSharedContext.listenerPosition.y=PhSharedContext.listenerData[phListenerField(POSY)];
    PhSharedContext.listenerPosition.z=PhSharedContext.listenerData[phListenerField(POSZ)];
    return &PhSharedContext.listenerPosition;
}


IPLVector3 *phGetListenerDirection(){ 
    PhSharedContext.listenerDirection.x=PhSharedContext.listenerData[phListenerField(DIRX)];
    PhSharedContext.listenerDirection.y=PhSharedContext.listenerData[phListenerField(DIRY)];
    PhSharedContext.listenerDirection.z=PhSharedContext.listenerData[phListenerField(DIRZ)];
    return &PhSharedContext.listenerDirection;
}

IPLVector3 *phGetListenerUp(){ 
    PhSharedContext.listenerUp.x=PhSharedContext.listenerData[phListenerField(UPX)];
    PhSharedContext.listenerUp.y=PhSharedContext.listenerData[phListenerField(UPY)];
    PhSharedContext.listenerUp.z=PhSharedContext.listenerData[phListenerField(UPZ)];
    return &PhSharedContext.listenerUp;
}

IPLVector3* phGetSourcePosition(IPLVector3* position, float *sourceData) {
    position->x = sourceData[phSourceField(POSX)];
    position->y = sourceData[phSourceField(POSY)];
    position->z = sourceData[phSourceField(POSZ)];

    return position;
}

IPLVector3* phGetSourceAhead(IPLVector3* ahead, float *sourceData) {
    ahead->x = sourceData[phSourceField(AHEADX)];
    ahead->y = sourceData[phSourceField(AHEADY)];
    ahead->z = sourceData[phSourceField(AHEADZ)];

    return ahead;
}

IPLVector3* phGetSourceUp(IPLVector3* up, float *sourceData) {
    up->x = sourceData[phSourceField(UPX)];
    up->y = sourceData[phSourceField(UPY)];
    up->z = sourceData[phSourceField(UPZ)];

    return up;
}

IPLVector3* phGetSourceRight(IPLVector3* right, float *sourceData) {
    right->x = sourceData[phSourceField(RIGHTX)];
    right->y = sourceData[phSourceField(RIGHTY)];
    right->z = sourceData[phSourceField(RIGHTZ)];

    return right;
}

IPLDirectivity* phGetSourceDirectivity(IPLDirectivity* directivity, float* sourceData) {
    directivity->dipoleWeight = sourceData[phSourceField(DIPOLEWEIGHT)];
    directivity->dipolePower = sourceData[phSourceField(DIPOLEPOWER)];

    // TODO: retrieve those directivity fields
    directivity->callback = NULL;
    directivity->userData = NULL;

    return directivity;
}

void phProcessFrame(struct GlobalSettings *settings,struct AudioSource *asource,jfloat *inFrame, jfloat *outFrame){
    // for(jint i=0;i<PhSharedContext.settings.frameSize;i++){
    //     outframe[i] = inframe[i];
    // }
    struct PhContext *ctx=asource->phononContext;
    if(ctx==NULL){
        printf("FIXME: PhContext is null for this source?\n");
        return;
    }
    
    IPLSource source;
    phGetSourcePosition(&source.position, asource->sceneData);
    phGetSourceAhead(&source.ahead, asource->sceneData);
    phGetSourceUp(&source.up, asource->sceneData);
    phGetSourceRight(&source.right, asource->sceneData);
    phGetSourceDirectivity(&source.directivity, asource->sceneData);    

    ///>>??????
    ///

    jfloat sourceRadius = 1;

    IPLVector3 *listenerPos = phGetListenerPosition();
    IPLVector3 *listenerUp = phGetListenerUp();
    IPLVector3 *listenerDirection = phGetListenerDirection();

    IPLVector3 direction= iplCalculateRelativeDirection(source.position, (*listenerPos),
        (*listenerDirection), (*listenerUp));

    

    PhSharedContext.monoBuffer1.interleavedBuffer = inFrame;
    PhSharedContext.monoBuffer2.interleavedBuffer = PhSharedContext.auxMonoFrame;

    // Find direct path
     IPLDirectSoundPath  path=iplGetDirectSoundPath(PhSharedContext.environment, 
   (*listenerPos),
     (*listenerDirection),
      (*listenerUp), 
      source,
      sourceRadius, //only for IPL_DIRECTOCCLUSION_VOLUMETRIC 
      IPL_DIRECTOCCLUSION_NONE , 
     IPL_DIRECTOCCLUSION_RAYCAST );

     iplApplyDirectSoundEffect(ctx->directSoundEffect,
                             PhSharedContext.monoBuffer1,
                               path,
                              ctx->directSoundEffectOptions,
                               PhSharedContext.monoBuffer2);


     PhSharedContext.outputBuffer.interleavedBuffer = outFrame;
     iplApplyBinauralEffect(ctx->binauralEffect, PhSharedContext.monoBuffer2, direction, IPL_HRTFINTERPOLATION_NEAREST, PhSharedContext.outputBuffer);

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