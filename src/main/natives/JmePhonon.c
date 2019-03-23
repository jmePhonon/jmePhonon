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
#include "AudioSource.h"

  struct {
        IPLhandle context;
        IPLRenderingSettings settings;
        IPLSimulationSettings simulationSettings;

        IPLAudioFormat monoFormat;

    
        jfloat *auxMonoFrame;

        IPLAudioFormat ambisonicsFormatD;
        jfloat **auxAmbisonicsFrame;

        IPLAudioFormat stereoFormat;

        IPLhandle scene;

        IPLhandle environment;
        IPLhandle environmentalRenderer;

        IPLHrtfParams defaultHrtfParams;
        IPLhandle binauralRenderer;

        IPLAudioBuffer *mixerQueue;

        IPLhandle sceneMesh;

        IPLhandle probeManager;

        IPLhandle ambisonicsBinauralEffect;

        IPLhandle listenerCentricReverb;

        jint nSourceDependentConvolutionEffects;
        struct ConvolutionEffect *convolutionEffectsQueue;
        jboolean useBinauralRendererForSources;

    } PhSharedContext; // This context is shared between every source

    struct ConvolutionEffect {
        void* effect;
        jboolean inUse;
        jint id;
    };

    struct PhContext { //nb for each source we need to create a new PhContext    
        IPLhandle binauralEffect;
        IPLhandle panningEffect;
        IPLhandle directSoundEffect;
        IPLDirectSoundEffectOptions directSoundEffectOptions;
        jboolean needInitialization;
        struct ConvolutionEffect *convolutionEffect;
    };

    /**
     * Allocates one PhContext for the audioSource
     */
    void phInitializeSource(struct GlobalSettings *settings, struct AudioSource *audioSource) {
        struct PhContext *context = malloc(sizeof(struct PhContext));
        audioSource->phononContext = context;

        context->convolutionEffect = NULL;

        // Direct sound
        iplCreateDirectSoundEffect(PhSharedContext.environmentalRenderer,
                                   PhSharedContext.monoFormat, PhSharedContext.monoFormat, &context->directSoundEffect);

        // Rendering 
        if(PhSharedContext.useBinauralRendererForSources){
            iplCreateBinauralEffect(PhSharedContext.binauralRenderer, PhSharedContext.monoFormat, 
                PhSharedContext.stereoFormat, &context->binauralEffect);  
        }

        iplCreatePanningEffect(PhSharedContext.binauralRenderer, PhSharedContext.monoFormat, 
            PhSharedContext.stereoFormat, &context->panningEffect);  
    }

    void phConnectSource(struct GlobalSettings *settings, struct AudioSource *audioSource){
        struct PhContext *context = audioSource->phononContext ;
        // Note / FIXME: due to the way non blocking updates are performed across game and audio threads,
        // the scene data is not refreshed when this method is called, so we need to delay the initialization
        // to the beginning of phProcessFrame
        context->needInitialization = true;
      
    }


void phDisconnectSource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    struct PhContext *context = audioSource->phononContext;
    if(context->convolutionEffect!=NULL){
        iplFlushConvolutionEffect(context->convolutionEffect->effect);
        context->convolutionEffect->inUse = false;
        context->convolutionEffect = NULL;
    }
    if(PhSharedContext.useBinauralRendererForSources)iplFlushBinauralEffect(context->binauralEffect);
    iplFlushPanningEffect(context->panningEffect);
    iplFlushDirectSoundEffect(context->directSoundEffect); 
}


/**
 * Deallocates the PhContext of the audioSource
 */
void phDestroySource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    struct PhContext *context = audioSource->phononContext;
    iplDestroyPanningEffect(&context->panningEffect);
    if(PhSharedContext.useBinauralRendererForSources)iplDestroyBinauralEffect(&context->binauralEffect);
    iplDestroyDirectSoundEffect(&context->directSoundEffect);
    free(audioSource->phononContext);
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

    // Load phonon settings
    jclass settingsClass = (*env)->GetObjectClass(env, jSettings);
    PhSharedContext.simulationSettings.sceneType = GET_SETTINGS_INT(jSettings, settingsClass, "sceneType");
    PhSharedContext.simulationSettings.numRays = GET_SETTINGS_INT(jSettings, settingsClass, "numRays");
    PhSharedContext.simulationSettings.numDiffuseSamples = GET_SETTINGS_INT(jSettings, settingsClass, "numDiffuseSamples");
    PhSharedContext.simulationSettings.numBounces = GET_SETTINGS_INT(jSettings, settingsClass, "numBounces");
    PhSharedContext.simulationSettings.numThreads = GET_SETTINGS_INT(jSettings, settingsClass, "numThreads");
    PhSharedContext.simulationSettings.irDuration = GET_SETTINGS_FLOAT(jSettings, settingsClass, "irDuration");
    PhSharedContext.simulationSettings.ambisonicsOrder = GET_SETTINGS_INT(jSettings, settingsClass, "ambisonicsOrder");
    
    PhSharedContext.nSourceDependentConvolutionEffects=GET_SETTINGS_INT(jSettings, settingsClass, "maxConvolutionSources");
    PhSharedContext.simulationSettings.maxConvolutionSources = PhSharedContext.nSourceDependentConvolutionEffects;

    jboolean listenerCentricReverb=GET_SETTINGS_BOOL(jSettings, settingsClass, "listenerCentricReverb");
    if(listenerCentricReverb){
        PhSharedContext.simulationSettings.maxConvolutionSources++;
    }

    PhSharedContext.simulationSettings.bakingBatchSize = GET_SETTINGS_INT(jSettings, settingsClass, "bakingBatchSize");
    PhSharedContext.simulationSettings.sceneType = IPL_SCENETYPE_PHONON;
    PhSharedContext.useBinauralRendererForSources=GET_SETTINGS_BOOL(jSettings, settingsClass, "useBinauralRendererForSources");
    PhSharedContext.simulationSettings.numOcclusionSamples = GET_SETTINGS_INT(jSettings, settingsClass, "numOcclusionSamples");
    PhSharedContext.simulationSettings.irradianceMinDistance=GET_SETTINGS_FLOAT(jSettings, settingsClass, "irradianceMinDistance");
    //////////////////

  

    // Initialize context
    iplCreateContext(NULL, NULL, NULL, &PhSharedContext.context);
    PhSharedContext.settings.samplingRate = settings->sampleRate;
    PhSharedContext.settings.frameSize = settings->frameSize;
    PhSharedContext.settings.convolutionType = IPL_CONVOLUTIONTYPE_PHONON;

    // All the formats used by the effects
    PhSharedContext.monoFormat.channelLayout = IPL_CHANNELLAYOUT_MONO;
    PhSharedContext.monoFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhSharedContext.monoFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;
    PhSharedContext.monoFormat.numSpeakers = 1;
    PhSharedContext.monoFormat.speakerDirections = NULL;
    PhSharedContext.monoFormat.ambisonicsOrder = -1;
    PhSharedContext.monoFormat.ambisonicsNormalization = IPL_AMBISONICSNORMALIZATION_N3D;
    PhSharedContext.monoFormat.ambisonicsOrdering = IPL_AMBISONICSORDERING_ACN;

	PhSharedContext.ambisonicsFormatD.channelLayout = IPL_CHANNELLAYOUT_STEREO;
    PhSharedContext.ambisonicsFormatD.channelLayoutType = IPL_CHANNELLAYOUTTYPE_AMBISONICS;
    PhSharedContext.ambisonicsFormatD.channelOrder = IPL_CHANNELORDER_DEINTERLEAVED;
    PhSharedContext.ambisonicsFormatD.numSpeakers = (PhSharedContext.simulationSettings.ambisonicsOrder + 1) * (PhSharedContext.simulationSettings.ambisonicsOrder + 1);
    PhSharedContext.ambisonicsFormatD.speakerDirections = NULL;
    PhSharedContext.ambisonicsFormatD.ambisonicsOrder = PhSharedContext.simulationSettings.ambisonicsOrder;
    PhSharedContext.ambisonicsFormatD.ambisonicsNormalization = IPL_AMBISONICSNORMALIZATION_N3D;
    PhSharedContext.ambisonicsFormatD.ambisonicsOrdering = IPL_AMBISONICSORDERING_ACN;

    PhSharedContext.stereoFormat.channelLayout = IPL_CHANNELLAYOUT_STEREO;
    PhSharedContext.stereoFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhSharedContext.stereoFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;
    PhSharedContext.stereoFormat.numSpeakers = 2;
    PhSharedContext.stereoFormat.speakerDirections = NULL;
    PhSharedContext.stereoFormat.ambisonicsOrder = -1;
    PhSharedContext.stereoFormat.ambisonicsNormalization = IPL_AMBISONICSNORMALIZATION_N3D;
    PhSharedContext.stereoFormat.ambisonicsOrdering = IPL_AMBISONICSORDERING_ACN;


    // Auxiliary frames used by the effects
    PhSharedContext.auxMonoFrame = malloc(4 * PhSharedContext.settings.frameSize);
    // This one is deinterleaved
    PhSharedContext.auxAmbisonicsFrame = malloc(sizeof(jfloat *) * PhSharedContext.ambisonicsFormatD.numSpeakers);
    for (jint i = 0; i < PhSharedContext.ambisonicsFormatD.numSpeakers; i++) {
        PhSharedContext.auxAmbisonicsFrame[i] = malloc(4 * PhSharedContext.settings.frameSize);
    }   

    // The probe manager, not used for now.
    iplCreateProbeManager(PhSharedContext.context,&PhSharedContext.probeManager);


    // The scene
    // Materials will be loaded only once for the entire life of the application
    iplCreateScene(PhSharedContext.context, NULL /*compute device*/, 
                    PhSharedContext.simulationSettings, nMaterials,
                   (IPLMaterial*) materials,
                   NULL, NULL, NULL, NULL, NULL,
                   &PhSharedContext.scene);

    // Debug: print all the materials
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

    // The environment
    iplCreateEnvironment(PhSharedContext.context,
                        /*compute device*/ NULL,
                        PhSharedContext.simulationSettings,
                        PhSharedContext.scene,
                        PhSharedContext.probeManager,
                        &PhSharedContext.environment);

    // The Environmental renderer
    iplCreateEnvironmentalRenderer(PhSharedContext.context, PhSharedContext.environment,
                                   PhSharedContext.settings, PhSharedContext.ambisonicsFormatD,
                                   NULL, NULL, &PhSharedContext.environmentalRenderer);      


    // The binaural renderer, will be used to render the sounds to headphones
    PhSharedContext.defaultHrtfParams.type = IPL_HRTFDATABASETYPE_DEFAULT;
    PhSharedContext.defaultHrtfParams.hrtfData=NULL;
    iplCreateBinauralRenderer(PhSharedContext.context, 
                            PhSharedContext.settings, PhSharedContext.defaultHrtfParams, 
                            &PhSharedContext.binauralRenderer);

    // Another binaural renderer, but this can be used to render the output of the convolution effects
    iplCreateAmbisonicsBinauralEffect(PhSharedContext.binauralRenderer,
                                    PhSharedContext.ambisonicsFormatD, PhSharedContext.stereoFormat, 
                                    &PhSharedContext.ambisonicsBinauralEffect);
    
    // Build convolution effects queue
    // When a sound is complete, the effect will be released, flushed and recycled for the next sound.
    if(PhSharedContext.nSourceDependentConvolutionEffects>0){
        PhSharedContext.convolutionEffectsQueue = malloc(sizeof(struct ConvolutionEffect)*PhSharedContext.nSourceDependentConvolutionEffects);
        for(jint i=0;i<PhSharedContext.nSourceDependentConvolutionEffects;i++){
            IPLBakedDataIdentifier idf;
            idf.identifier=0; // TODO: Implement baking
            idf.type=IPL_BAKEDDATATYPE_REVERB;
            iplCreateConvolutionEffect(PhSharedContext.environmentalRenderer, idf,
                                    IPL_SIMTYPE_REALTIME /*TODO: add baking*/,
                                    PhSharedContext.monoFormat,
                                    PhSharedContext.ambisonicsFormatD,
                                    & PhSharedContext.convolutionEffectsQueue[i].effect);
            PhSharedContext.convolutionEffectsQueue[i].inUse = false;
            PhSharedContext.convolutionEffectsQueue[i].id = i;
        }
    }

    if(listenerCentricReverb){
        IPLBakedDataIdentifier idf1;
        idf1.identifier=0; // TODO: Implement baking
        idf1.type=IPL_BAKEDDATATYPE_REVERB;
        iplCreateConvolutionEffect(PhSharedContext.environmentalRenderer, idf1,
                                    IPL_SIMTYPE_REALTIME /*TODO: add baking*/,
                                        PhSharedContext.stereoFormat,
                                        PhSharedContext.ambisonicsFormatD,
                                        & PhSharedContext.listenerCentricReverb);    
    }else{
        PhSharedContext.listenerCentricReverb = NULL;
    }

    // Queue used to mix the frames
    PhSharedContext.mixerQueue = malloc(sizeof(IPLAudioBuffer)*mixerQueueSize);
    for(jint i=0;i<mixerQueueSize;i++){
        PhSharedContext.mixerQueue[i].format = PhSharedContext.stereoFormat;
        PhSharedContext.mixerQueue[i].numSamples = PhSharedContext.settings.frameSize ;        
    }
}

void phDestroy(struct GlobalSettings *settings){
    // Destroy everything

    if(PhSharedContext.listenerCentricReverb!=NULL){
        iplDestroyConvolutionEffect(&PhSharedContext.listenerCentricReverb);
    }

    if (PhSharedContext.nSourceDependentConvolutionEffects > 0) {
        for(jint i=0;i<PhSharedContext.nSourceDependentConvolutionEffects;i++){
            iplDestroyConvolutionEffect(&PhSharedContext.convolutionEffectsQueue[i].effect);    
        }
    }
    free(PhSharedContext.convolutionEffectsQueue);

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
    for (jint i = 0; i < PhSharedContext.ambisonicsFormatD.numSpeakers; i++) {
        free(PhSharedContext.auxAmbisonicsFrame[i]);
    }
    free(PhSharedContext.auxAmbisonicsFrame);
    // free(PhSharedContext.materials);
}

void phProcessFrame(struct GlobalSettings *settings,struct Listener *listener,struct AudioSource *asource,jfloat *inFrame, jfloat *outFrame){
    struct PhContext *ctx=asource->phononContext;

    // Postponed initialization
    if(ctx->needInitialization){
        ctx->convolutionEffect = NULL;
        if (PhSharedContext.nSourceDependentConvolutionEffects> 0) {
            if(asHasFlag(settings,asource,REVERB)){
                for(jint i=0;i<PhSharedContext.nSourceDependentConvolutionEffects;i++){
                    if(!PhSharedContext.convolutionEffectsQueue[i].inUse){
                        ctx->convolutionEffect = &PhSharedContext.convolutionEffectsQueue[i];
                        ctx->convolutionEffect->inUse=true;
                        break;
                    }
                }
            }
        }
        ctx->needInitialization = false;
    }
    /////

    IPLAudioBuffer soundOut=(IPLAudioBuffer){PhSharedContext.stereoFormat, PhSharedContext.settings.frameSize, outFrame,NULL};
 // Positionals are always mono 

        IPLAudioBuffer soundIn=(IPLAudioBuffer){PhSharedContext.monoFormat, PhSharedContext.settings.frameSize,inFrame,NULL};
    
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


        if(ctx->convolutionEffect!=NULL){
            IPLAudioBuffer wetInBuffer=(IPLAudioBuffer){PhSharedContext.monoFormat, PhSharedContext.settings.frameSize,inFrame,NULL};
            iplSetDryAudioForConvolutionEffect(ctx->convolutionEffect->effect,
                                            source,
                                            wetInBuffer);
        }


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

        IPLAudioBuffer directSoundOut=(IPLAudioBuffer){PhSharedContext.monoFormat, PhSharedContext.settings.frameSize, PhSharedContext.auxMonoFrame,NULL};

        iplApplyDirectSoundEffect(ctx->directSoundEffect,
                                soundIn,
                                path,
                                ctx->directSoundEffectOptions,
                                directSoundOut);

    

        if(asHasFlag(settings, asource, HRTF)&&PhSharedContext.useBinauralRendererForSources){
            iplApplyBinauralEffect(ctx->binauralEffect,PhSharedContext.binauralRenderer, 
                            directSoundOut, direction, 
                            IPL_HRTFINTERPOLATION_NEAREST, soundOut);
        }else{
            iplApplyPanningEffect(ctx->panningEffect, PhSharedContext.binauralRenderer, directSoundOut, direction,soundOut);
        }
}



void phCalculateListenerCentricReverb(struct GlobalSettings *settings,struct Listener *listener,jfloat* frame){
     if(PhSharedContext.listenerCentricReverb !=NULL){
         IPLAudioBuffer rev_in = (IPLAudioBuffer){PhSharedContext.stereoFormat, PhSharedContext.settings.frameSize, frame, NULL};

         IPLVector3 *listenerPos = lsGetPosition(settings, listener);
         IPLVector3 *listenerUp = lsGetUp(settings, listener);
         IPLVector3 *listenerDirection = lsGetDirection(settings, listener);

         IPLSource listener_source;
         listener_source.position = (*listenerPos);
         listener_source.ahead = (*listenerDirection);
         listener_source.up = (*listenerUp);
         listener_source.right = cross(listener_source.ahead, listener_source.up); // not needed?
         listener_source.directivity = (IPLDirectivity){0.0f, 0.0f, NULL, NULL};

         iplSetDryAudioForConvolutionEffect(PhSharedContext.listenerCentricReverb,
                                            listener_source,
                                            rev_in);
    }
}
 
void phGetEnvFrame(struct GlobalSettings *settings,struct Listener *listener,jfloat *outFrame){
    IPLVector3 *listenerPos = lsGetPosition(settings, listener);
    IPLVector3 *listenerUp = lsGetUp(settings, listener);
    IPLVector3 *listenerDirection = lsGetDirection(settings, listener);

    IPLAudioBuffer mixedOut = (IPLAudioBuffer){PhSharedContext.ambisonicsFormatD, PhSharedContext.settings.frameSize, NULL, PhSharedContext.auxAmbisonicsFrame};
    iplGetMixedEnvironmentalAudio(PhSharedContext.environmentalRenderer,
        (*listenerPos),
        (*listenerDirection), 
        (*listenerUp),
        mixedOut);

    IPLAudioBuffer renderOut=(IPLAudioBuffer){PhSharedContext.stereoFormat, PhSharedContext.settings.frameSize, outFrame,NULL};
    iplApplyAmbisonicsBinauralEffect(PhSharedContext.ambisonicsBinauralEffect,PhSharedContext.binauralRenderer,
                mixedOut,renderOut);

}

/**
 * Mix multiple outputBuffers
 */
void phMixOutputBuffers(jfloat **input,jint offset,jint numInputs,jfloat *output){
    for (jint i = 0; i <  numInputs; i++) {
        PhSharedContext.mixerQueue[i].interleavedBuffer = input[offset+i];
    }
    IPLAudioBuffer renderOut=(IPLAudioBuffer){PhSharedContext.stereoFormat, PhSharedContext.settings.frameSize, output,NULL};
    iplMixAudioBuffers(numInputs, PhSharedContext.mixerQueue, renderOut);
}