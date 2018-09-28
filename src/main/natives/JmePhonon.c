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


        IPLAudioFormat outputFormat;
        IPLAudioBuffer outputBuffer;

   
        IPLhandle scene;

        IPLhandle environment;
        IPLhandle environmentalRenderer;

        IPLHrtfParams defaultHrtfParams;
        IPLhandle binauralRenderer;

        IPLAudioBuffer *mixerQueue;

        IPLhandle sceneMesh;

    } PhSharedContext; // This context is shared between every source

    struct PhContext { //nb for each source we need to create a new PhContext    
        IPLhandle binauralEffect;
        IPLhandle directSoundEffect;
        IPLDirectSoundEffectOptions directSoundEffectOptions;
    };




/**
 * Allocates one PhContext for the audioSource
 */
void phInitializeSource(struct GlobalSettings *settings, struct AudioSource *audioSource){
    struct PhContext *context = malloc(sizeof(struct PhContext));

    // TODO make this configurable
    /*context->directSoundEffectOptions.applyDistanceAttenuation = true;
    context->directSoundEffectOptions.applyAirAbsorption = true;
    context->directSoundEffectOptions.applyDirectivity = true;
    context->directSoundEffectOptions.directOcclusionMode = IPL_DIRECTOCCLUSION_TRANSMISSIONBYFREQUENCY;*/

    // Direct sound
    iplCreateDirectSoundEffect(PhSharedContext.environmentalRenderer, 
    PhSharedContext.monoFormat,PhSharedContext.monoFormat, &context->directSoundEffect);

    iplCreateBinauralEffect(PhSharedContext.binauralRenderer, PhSharedContext.monoFormat, PhSharedContext.outputFormat, &context->binauralEffect);
    audioSource->phononContext = context;
    PhSharedContext.sceneMesh = NULL;
}

/**
 * Deallocates the PhContext of the audioSource
 */
void phDestroySource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    struct PhContext *context = audioSource->phononContext;

    iplDestroyBinauralEffect(&context->binauralEffect);
    iplDestroyDirectSoundEffect(&context->directSoundEffect);
    
    free(audioSource->phononContext);
}


void phFlushSource(struct GlobalSettings *settings,struct AudioSource *audioSource){
    struct PhContext *context = audioSource->phononContext;

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
    PhSharedContext.simulationSettings.irDuration = GET_SETTINGS_DOUBLE(jSettings, settingsClass, "irDuration");
    PhSharedContext.simulationSettings.ambisonicsOrder = GET_SETTINGS_INT(jSettings, settingsClass, "ambisonicsOrder");
    PhSharedContext.simulationSettings.maxConvolutionSources = GET_SETTINGS_INT(jSettings, settingsClass, "maxConvolutionSources");
    PhSharedContext.simulationSettings.bakingBatchSize = GET_SETTINGS_INT(jSettings, settingsClass, "bakingBatchSize");

    //////////////////

    iplCreateContext(NULL, NULL, NULL, &PhSharedContext.context);
    PhSharedContext.settings.samplingRate = settings->sampleRate;
    PhSharedContext.settings.frameSize = settings->frameSize;
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
    iplCreateScene(PhSharedContext.context, NULL /*compute device*/, PhSharedContext.simulationSettings, nMaterials,
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
    NULL,
    &PhSharedContext.environment);

    // Environmental renderer
    iplCreateEnvironmentalRenderer(PhSharedContext.context, PhSharedContext.environment,
                                   PhSharedContext.settings, PhSharedContext.monoFormat,
                                   NULL, NULL, &PhSharedContext.environmentalRenderer);

    PhSharedContext.defaultHrtfParams.type = IPL_HRTFDATABASETYPE_DEFAULT;
    iplCreateBinauralRenderer(PhSharedContext.context, PhSharedContext.settings, PhSharedContext.defaultHrtfParams, 
    &PhSharedContext.binauralRenderer);

    PhSharedContext.mixerQueue = malloc(sizeof(IPLAudioBuffer)*mixerQueueSize);
    for(jint i=0;i<mixerQueueSize;i++){
        PhSharedContext.mixerQueue[i].format = PhSharedContext.outputFormat;
        PhSharedContext.mixerQueue[i].numSamples = PhSharedContext.settings.frameSize ;        
    }
}

void phDestroy(struct GlobalSettings *settings){
    iplDestroyBinauralRenderer(&PhSharedContext.binauralRenderer);
    iplDestroyEnvironmentalRenderer(&PhSharedContext.environmentalRenderer);
    iplDestroyEnvironment(&PhSharedContext.environment);
    iplDestroyScene(&PhSharedContext.scene);
    if(PhSharedContext.sceneMesh !=NULL)
        phDestroySceneMesh(settings);
    iplDestroyContext(&PhSharedContext.context);
    iplCleanup();
    free(PhSharedContext.mixerQueue);
    free(PhSharedContext.auxMonoFrame);
    // free(PhSharedContext.materials);
}

void phProcessFrame(struct GlobalSettings *settings,struct Listener *listener,struct AudioSource *asource,jfloat *inFrame, jfloat *outFrame){
    // for(jint i=0;i<PhSharedContext.settings.frameSize;i++){
    //     outframe[i] = inframe[i];
    // }
    struct PhContext *ctx=asource->phononContext;
    if(ctx==NULL){
        printf("FIXME: PhContext is null for this source?\n");
        return;
    }
    
    ((struct PhContext*) asource->phononContext)->directSoundEffectOptions.applyDirectivity = asHasFlag(settings, asource, DIRECTIONAL);
    ((struct PhContext*) asource->phononContext)->directSoundEffectOptions.applyDistanceAttenuation = asHasFlag(settings, asource, POSITIONAL);
    ((struct PhContext*) asource->phononContext)->directSoundEffectOptions.applyAirAbsorption = asHasFlag(settings, asource, AIRABSORPTION);

    ((struct PhContext*) asource->phononContext)->directSoundEffectOptions.directOcclusionMode = asGetDirectOcclusionMode(settings, asource);

    IPLSource source;
    source.position = (*asGetSourcePosition(settings, asource));
    source.ahead = (*asGetSourceDirection(settings, asource));
    source.up = (*asGetSourceUp(settings, asource));
    source.right = (*asGetSourceRight(settings, asource));
    source.directivity = (*asGetSourceDirectivity(settings, asource));

    ///>>??????
    ///

    jfloat sourceRadius = 1;

    IPLVector3 *listenerPos = lsGetPosition(settings, listener);
    IPLVector3 *listenerUp = lsGetUp(settings, listener);
    IPLVector3 *listenerDirection = lsGetDirection(settings, listener);

    IPLVector3 direction = iplCalculateRelativeDirection(source.position, (*listenerPos),
                                                        (*listenerDirection), (*listenerUp));

    PhSharedContext.monoBuffer1.interleavedBuffer = inFrame;
    PhSharedContext.monoBuffer2.interleavedBuffer = PhSharedContext.auxMonoFrame;

    // Find direct path
    IPLDirectSoundPath path = iplGetDirectSoundPath(PhSharedContext.environment,
                                                    (*listenerPos),
                                                    (*listenerDirection),
                                                    (*listenerUp),
                                                    source,
                                                    sourceRadius, //only for IPL_DIRECTOCCLUSION_VOLUMETRIC
                                                    IPL_DIRECTOCCLUSION_TRANSMISSIONBYFREQUENCY,
                                                    IPL_DIRECTOCCLUSION_VOLUMETRIC);

    //  path.distanceAttenuation *= 1.9;

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