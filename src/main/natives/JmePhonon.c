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
#define __JMEPHONON_INTERNAL__
#include "JmePhonon.h"
#include "settings/settings.h"

  struct {
        IPLhandle context;
        IPLRenderingSettings settings;
        IPLSimulationSettings simulationSettings;

        IPLAudioFormat monoFormat;

        IPLAudioBuffer monoBuffer1;
        IPLAudioBuffer monoBuffer2;

        jfloat *auxMonoFrame;

        IPLAudioFormat ambisonicsFormatD;
        IPLAudioBuffer ambisonicsBuffer;
        jfloat **auxAmbisonicsFrame1;

        IPLAudioFormat outputFormat;
        IPLAudioBuffer outputBuffer;

   
        IPLhandle scene;

        IPLhandle environment;
        IPLhandle environmentalRenderer;

        IPLHrtfParams defaultHrtfParams;
        IPLhandle binauralRenderer;

        IPLAudioBuffer *mixerQueue;

        IPLhandle sceneMesh;

        IPLhandle probeManager;

        IPLhandle ambisonicsBinauralEffect;

    } PhSharedContext; // This context is shared between every source

    struct PhContext { //nb for each source we need to create a new PhContext    
        IPLhandle binauralEffect;
        IPLhandle directSoundEffect;
        IPLhandle convolutionEffect;
        IPLDirectSoundEffectOptions directSoundEffectOptions;
        jboolean useConvolution;
    };
 
    /**
 * Allocates one PhContext for the audioSource
 */
    void phInitializeSource(struct GlobalSettings *settings, struct AudioSource *audioSource,jint id) {
        struct PhContext *context = malloc(sizeof(struct PhContext));

        // Direct sound
        iplCreateDirectSoundEffect(PhSharedContext.environmentalRenderer,
                                   PhSharedContext.monoFormat, PhSharedContext.monoFormat, &context->directSoundEffect);

        iplCreateBinauralEffect(PhSharedContext.binauralRenderer, PhSharedContext.monoFormat, PhSharedContext.outputFormat, &context->binauralEffect);
        audioSource->phononContext = context;

        // iplCreatePanningEffect(
        //     PhSharedContext.binauralRenderer,
        //     PhSharedContext.monoFormat,
        //     PhSharedContext.ambisonicsFormat,
        //     &context->panningEffect);
        context->useConvolution=id<PhSharedContext.simulationSettings.maxConvolutionSources;
        if(context->useConvolution){
            printf("2Init source %d with conv\n", id);

            IPLBakedDataIdentifier idf;
            idf.identifier=0; // TODO: Implement baking
            idf.type=IPL_BAKEDDATATYPE_REVERB;

            iplCreateConvolutionEffect(PhSharedContext.environmentalRenderer, idf,
                                    IPL_SIMTYPE_REALTIME /*TODO: add baking*/,
                                    PhSharedContext.monoFormat,
                                    PhSharedContext.ambisonicsFormatD,
                                    &context->convolutionEffect);
        }
    }

/**
 * Deallocates the PhContext of the audioSource
 */
void phDestroySource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    struct PhContext *context = audioSource->phononContext;

    if(context->useConvolution)iplDestroyConvolutionEffect(&context->convolutionEffect);
    // iplDestroyPanningEffect(&context->panningEffect);
    iplDestroyBinauralEffect(&context->binauralEffect);
    iplDestroyDirectSoundEffect(&context->directSoundEffect);

    free(audioSource->phononContext);
}


void phFlushSource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    struct PhContext *context = audioSource->phononContext;
    if(context->useConvolution)iplFlushConvolutionEffect(context->convolutionEffect);
    // iplFlushPanningEffect(context->panningEffect);

    iplFlushBinauralEffect(context->binauralEffect);
    iplFlushDirectSoundEffect(context->directSoundEffect);    
}



void phCreateSceneMesh(struct GlobalSettings *settings,jint numTriangles,
    jint numVertices,jint* indexBuffer,jfloat* vertexBuffer, jint* materialIndices){
    iplCreateStaticMesh(PhSharedContext.scene,
                        (IPLint32)numVertices,
                       (IPLint32) numTriangles,
                        (IPLVector3*)vertexBuffer,
                        (IPLTriangle*)indexBuffer,
                        (IPLint32*)materialIndices,
                        &PhSharedContext.sceneMesh);
}

void phDestroySceneMesh(struct GlobalSettings *settings){
    iplDestroyStaticMesh(&PhSharedContext.sceneMesh);
    PhSharedContext.sceneMesh = NULL;
}

void phSaveSceneMeshAsObj(struct GlobalSettings *settings,jbyte* path){
    iplSaveSceneAsObj(PhSharedContext.scene,(IPLstring) path);
    printf("Save scene in %s\n", path);
}

void phInit(struct GlobalSettings *settings,jint mixerQueueSize, jint nMaterials, jfloat* materials, JNIEnv* env, jobject jSettings){
    PhSharedContext.scene = NULL;

    jclass settingsClass = (*env)->GetObjectClass(env, jSettings);

    PhSharedContext.simulationSettings.sceneType = GET_SETTINGS_INT(jSettings, settingsClass, "sceneType");
    PhSharedContext.simulationSettings.numRays = GET_SETTINGS_INT(jSettings, settingsClass, "numRays");
    PhSharedContext.simulationSettings.numDiffuseSamples = GET_SETTINGS_INT(jSettings, settingsClass, "numDiffuseSamples");
    PhSharedContext.simulationSettings.numBounces = GET_SETTINGS_INT(jSettings, settingsClass, "numBounces");
    PhSharedContext.simulationSettings.numThreads = GET_SETTINGS_INT(jSettings, settingsClass, "numThreads");
    PhSharedContext.simulationSettings.irDuration = GET_SETTINGS_FLOAT(jSettings, settingsClass, "irDuration");
    PhSharedContext.simulationSettings.ambisonicsOrder = GET_SETTINGS_INT(jSettings, settingsClass, "ambisonicsOrder");
    PhSharedContext.simulationSettings.maxConvolutionSources = GET_SETTINGS_INT(jSettings, settingsClass, "maxConvolutionSources");
    PhSharedContext.simulationSettings.bakingBatchSize = GET_SETTINGS_INT(jSettings, settingsClass, "bakingBatchSize");
// #if defined(STEAMAUDIO_VERSION_MAJOR) && STEAMAUDIO_VERSION_MAJOR > 2 &&  STEAMAUDIO_VERSION_MINOR > 0 && STEAMAUDIO_VERSION_PATCH > 16
        PhSharedContext.simulationSettings.numOcclusionSamples=4; // todo configurable
    // #endif
        //////////////////

        iplCreateContext(NULL, NULL, NULL, &PhSharedContext.context);
        PhSharedContext.settings.samplingRate = settings->sampleRate;
        PhSharedContext.settings.frameSize = settings->frameSize;
        PhSharedContext.settings.convolutionType = IPL_CONVOLUTIONTYPE_PHONON;

        // FORMATS
        PhSharedContext.monoFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
        PhSharedContext.monoFormat.channelLayout = IPL_CHANNELLAYOUT_MONO;
        PhSharedContext.monoFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;
        PhSharedContext.monoFormat.speakerDirections = NULL;

        PhSharedContext.ambisonicsFormatD.channelLayoutType = IPL_CHANNELLAYOUTTYPE_AMBISONICS;
        // PhSharedContext.ambisonicsFormat.channelLayout = IPL_CHANNELLAYOUT_MONO;
        PhSharedContext.ambisonicsFormatD.channelOrder = IPL_CHANNELORDER_DEINTERLEAVED;
        PhSharedContext.ambisonicsFormatD.ambisonicsOrdering = IPL_AMBISONICSORDERING_ACN;
        PhSharedContext.ambisonicsFormatD.ambisonicsNormalization = IPL_AMBISONICSNORMALIZATION_N3D;
        PhSharedContext.ambisonicsFormatD.ambisonicsOrder = PhSharedContext.simulationSettings.ambisonicsOrder;
        PhSharedContext.ambisonicsFormatD.speakerDirections = NULL;

        PhSharedContext.outputFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
        PhSharedContext.outputFormat.channelLayout = IPL_CHANNELLAYOUT_STEREO;
        PhSharedContext.outputFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;
        PhSharedContext.outputFormat.speakerDirections = NULL;
        //////////////

        // BUFFERS
        PhSharedContext.monoBuffer1.format = PhSharedContext.monoFormat;
        PhSharedContext.monoBuffer1.numSamples = PhSharedContext.settings.frameSize;

        PhSharedContext.monoBuffer2.format = PhSharedContext.monoFormat;
        PhSharedContext.monoBuffer2.numSamples = PhSharedContext.settings.frameSize;

        PhSharedContext.ambisonicsBuffer.format = PhSharedContext.ambisonicsFormatD;
        PhSharedContext.ambisonicsBuffer.numSamples = PhSharedContext.settings.frameSize;

        PhSharedContext.outputBuffer.format = PhSharedContext.outputFormat;
        PhSharedContext.outputBuffer.numSamples = PhSharedContext.settings.frameSize;
        /////////////

        // FRAMES
        PhSharedContext.auxMonoFrame = malloc(4 * PhSharedContext.settings.frameSize);
        jint achannels = (PhSharedContext.simulationSettings.ambisonicsOrder + 1) * (PhSharedContext.simulationSettings.ambisonicsOrder + 1);
        PhSharedContext.auxAmbisonicsFrame1 = malloc(sizeof(jfloat *) * achannels);
        for (jint i = 0; i < achannels; i++) {
            PhSharedContext.auxAmbisonicsFrame1[i] = malloc(4 * PhSharedContext.settings.frameSize);
    }

    
    /////////

    iplCreateProbeManager(PhSharedContext.context,&PhSharedContext.probeManager);


    // Scene
    iplCreateScene(PhSharedContext.context, NULL /*compute device*/, 
                    PhSharedContext.simulationSettings, nMaterials,
                   (IPLMaterial*) materials,
                   NULL, NULL, NULL, NULL, NULL,
                   &PhSharedContext.scene);

    printf("Create scene with materials: \n");
    for (jint i = 0; i < nMaterials; i++) {
        IPLMaterial mat = ((IPLMaterial*)materials)[i];
        printf("Mat %d :\n\
        lowFreqAbsorption %f\n\
        midFreqAbsorption %f\n\
        highFreqAbsorption %f \n\
        scattering %f \n\
        lowFreqTransmission %f\n\
        midFreqTransmission %f\n\
        highFreqTransmission %f\n",
        i,
        mat.lowFreqAbsorption,
        mat.midFreqAbsorption,
        mat.highFreqAbsorption,
        mat.scattering, 
        mat.lowFreqTransmission, 
        mat.midFreqTransmission,
        mat.highFreqTransmission);
    }

    // Environment
    iplCreateEnvironment(PhSharedContext.context,
                        /*compute device*/ NULL,
                        PhSharedContext.simulationSettings,
                        PhSharedContext.scene,
                        PhSharedContext.probeManager,
                        &PhSharedContext.environment);

    // Environmental renderer
    iplCreateEnvironmentalRenderer(PhSharedContext.context, PhSharedContext.environment,
                                   PhSharedContext.settings, PhSharedContext.monoFormat,
                                   NULL, NULL, &PhSharedContext.environmentalRenderer);      
    
    PhSharedContext.defaultHrtfParams.type = IPL_HRTFDATABASETYPE_DEFAULT;
    PhSharedContext.defaultHrtfParams.hrtfData=NULL;

    iplCreateBinauralRenderer(PhSharedContext.context, 
                            PhSharedContext.settings, PhSharedContext.defaultHrtfParams, 
                            &PhSharedContext.binauralRenderer);

    iplCreateAmbisonicsBinauralEffect(PhSharedContext.binauralRenderer,
     PhSharedContext.ambisonicsFormatD, PhSharedContext.outputFormat, 
     &PhSharedContext.ambisonicsBinauralEffect);
    
    PhSharedContext.mixerQueue = malloc(sizeof(IPLAudioBuffer)*mixerQueueSize);
    for(jint i=0;i<mixerQueueSize;i++){
        PhSharedContext.mixerQueue[i].format = PhSharedContext.outputFormat;
        PhSharedContext.mixerQueue[i].numSamples = PhSharedContext.settings.frameSize ;        
    }
}

void phDestroy(struct GlobalSettings *settings){
    iplDestroyAmbisonicsBinauralEffect(&PhSharedContext.ambisonicsBinauralEffect);
    iplDestroyBinauralRenderer(&PhSharedContext.binauralRenderer);
    iplDestroyEnvironmentalRenderer(&PhSharedContext.environmentalRenderer);
    iplDestroyEnvironment(&PhSharedContext.environment);
    iplDestroyScene(&PhSharedContext.scene);
    if(PhSharedContext.sceneMesh !=NULL)phDestroySceneMesh(settings);
    iplDestroyProbeManager(&PhSharedContext.probeManager);
    iplDestroyContext(&PhSharedContext.context);
    iplCleanup();
    free(PhSharedContext.mixerQueue);
    free(PhSharedContext.auxMonoFrame);
    free(PhSharedContext.auxAmbisonicsFrame1);

    // free(PhSharedContext.materials);
}

void phProcessFrame(struct GlobalSettings *settings,struct Listener *listener,struct AudioSource *asource,jfloat *inFrame, jfloat *outFrame){
    struct PhContext *ctx=asource->phononContext;
    if(ctx==NULL){
        printf("FIXME: PhContext is null for this source?\n");
        return;
    }
    
    jint directOcclusionMode = asGetDirectOcclusionMode(settings, asource);
    jint directOcclusionMethod = asGetDirectOcclusionMethod(settings, asource);
    jfloat sourceRadius = asGetSourceRadius(settings, asource);

    ctx->directSoundEffectOptions.applyDirectivity = asHasFlag(settings, asource, DIRECTIONAL);
    ctx->directSoundEffectOptions.applyDistanceAttenuation = asHasFlag(settings, asource, POSITIONAL);
    ctx->directSoundEffectOptions.applyAirAbsorption = asHasFlag(settings, asource, AIRABSORPTION);
    ctx->directSoundEffectOptions.directOcclusionMode = directOcclusionMode;

    IPLSource source;
    source.position = (*asGetSourcePosition(settings, asource));
    source.ahead = (*asGetSourceDirection(settings, asource));
    source.up = (*asGetSourceUp(settings, asource));
    source.right = (*asGetSourceRight(settings, asource));
    source.directivity = (*asGetSourceDirectivity(settings, asource));
    
   
    IPLVector3 *listenerPos = lsGetPosition(settings, listener);
    IPLVector3 *listenerUp = lsGetUp(settings, listener);
    IPLVector3 *listenerDirection = lsGetDirection(settings, listener);

    IPLVector3 direction = iplCalculateRelativeDirection(source.position, (*listenerPos),
                                                        (*listenerDirection), (*listenerUp));

    // Find direct path
    IPLDirectSoundPath path = iplGetDirectSoundPath(PhSharedContext.environment,
                                                    (*listenerPos),
                                                    (*listenerDirection),
                                                    (*listenerUp),
                                                    source,
                                                    sourceRadius, //only for IPL_DIRECTOCCLUSION_VOLUMETRIC
                                                    directOcclusionMode,
                                                    directOcclusionMethod);
   
    PhSharedContext.monoBuffer1.interleavedBuffer = inFrame;
    PhSharedContext.monoBuffer2.interleavedBuffer = PhSharedContext.auxMonoFrame;

    iplApplyDirectSoundEffect(ctx->directSoundEffect,
                            PhSharedContext.monoBuffer1,
                            path,
                            ctx->directSoundEffectOptions,
                            PhSharedContext.monoBuffer2);


    if(ctx->useConvolution){
        IPLSource reverbSource;
        reverbSource.position = (*listenerPos);
        reverbSource.ahead = (*listenerDirection);
        reverbSource.up =  (*listenerUp);
        reverbSource.directivity = (IPLDirectivity){ 0.0f, 0.0f, NULL, NULL };

        iplSetDryAudioForConvolutionEffect(ctx->convolutionEffect,
                                        reverbSource,
                                        PhSharedContext.monoBuffer2);
    }

    PhSharedContext.outputBuffer.interleavedBuffer = outFrame;
// #if defined(STEAMAUDIO_VERSION_MAJOR) && STEAMAUDIO_VERSION_MAJOR > 2 &&  STEAMAUDIO_VERSION_MINOR > 0 && STEAMAUDIO_VERSION_PATCH > 16
        iplApplyBinauralEffect(ctx->binauralEffect,PhSharedContext.binauralRenderer, 
                        PhSharedContext.monoBuffer2, direction, 
                        IPL_HRTFINTERPOLATION_NEAREST, PhSharedContext.outputBuffer);
    // #else
    //     iplApplyBinauralEffect(ctx->binauralEffect, 
    //                     PhSharedContext.monoBuffer2, direction, 
    //                     IPL_HRTFINTERPOLATION_NEAREST, PhSharedContext.outputBuffer);

    // #endif
}

 
void phGetEnvFrame(struct GlobalSettings *settings,struct Listener *listener,jfloat *outFrame){
    IPLVector3 *listenerPos = lsGetPosition(settings, listener);
    IPLVector3 *listenerUp = lsGetUp(settings, listener);
    IPLVector3 *listenerDirection = lsGetDirection(settings, listener);

    PhSharedContext.ambisonicsBuffer.deinterleavedBuffer= PhSharedContext.auxAmbisonicsFrame1;
    PhSharedContext.outputBuffer.interleavedBuffer = outFrame;

    iplGetMixedEnvironmentalAudio(PhSharedContext.environmentalRenderer,
        (*listenerPos),
        (*listenerDirection),
        (*listenerUp),
        PhSharedContext.ambisonicsBuffer);

    iplApplyAmbisonicsBinauralEffect(PhSharedContext.ambisonicsBinauralEffect,PhSharedContext.binauralRenderer,
                PhSharedContext.ambisonicsBuffer, PhSharedContext.outputBuffer);
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