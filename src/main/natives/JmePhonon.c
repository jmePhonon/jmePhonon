



#include "JmePhonon.h"

#include "types.h"


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

void phInit(struct GlobalSettings *settings) {
    iplCreateContext(NULL, NULL, NULL, &PhContext.context);
    PhContext.settings.samplingRate = settings->sampleRate;
    PhContext.settings.frameSize =settings->inputFrameSize;

    PhContext.hrtfParams.type = IPL_HRTFDATABASETYPE_DEFAULT;
    iplCreateBinauralRenderer(PhContext.context, PhContext.settings, PhContext.hrtfParams, &PhContext.binauralRenderer);     
        
    PhContext.inputFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhContext.inputFormat.channelLayout = IPL_CHANNELLAYOUT_MONO;
    PhContext.inputFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;

    PhContext.outputFormat.channelLayoutType = IPL_CHANNELLAYOUTTYPE_SPEAKERS;
    PhContext.outputFormat.channelLayout = IPL_CHANNELLAYOUT_STEREO;
    PhContext.outputFormat.channelOrder = IPL_CHANNELORDER_INTERLEAVED;

    iplCreateBinauralEffect(PhContext.binauralRenderer, PhContext.inputFormat, PhContext.outputFormat, &PhContext.binauralEffect);

    PhContext.inputBuffer.format = PhContext.inputFormat;
    PhContext.outputBuffer.format = PhContext.outputFormat;

    PhContext.inputBuffer.numSamples = PhContext.settings.frameSize ;
    PhContext.outputBuffer.numSamples = PhContext.settings.frameSize ;

    phUpdateListener(settings,0, 0, 0,     0, 0, 0, 0);
}


void phProcessFrame(struct GlobalSettings *settings,jfloat *inFrame, jfloat *outFrame){
    // for(jint i=0;i<PhContext.settings.frameSize;i++){
    //     outframe[i] = inframe[i];
    // }
    IPLVector3 direction;
    // phMultQtrVec(PhContext.listenerRotation, audiosourceposition,&direction
    // phSubVecVec(direction,PhContext.listenerPosition,&direction);
    direction.x = 1.0;
    direction.y = 1.0;
    direction.z = 1.0;
    

    PhContext.inputBuffer.interleavedBuffer = inFrame;
    PhContext.outputBuffer.interleavedBuffer = outFrame;

    
    iplApplyBinauralEffect(PhContext.binauralEffect, 
    PhContext.inputBuffer, direction, IPL_HRTFINTERPOLATION_NEAREST,  PhContext.outputBuffer);
}

void phDestroy(struct GlobalSettings *settings){

}

void phUpdateListener(struct GlobalSettings *settings,
jfloat wposx,jfloat wposy, jfloat wposz,
jfloat wrotx,jfloat wroty,jfloat wrotz,jfloat wrotw

){
    PhContext.listenerPosition.x = wposx;
    PhContext.listenerPosition.y = wposy;
    PhContext.listenerPosition.z = wposz;

    PhContext.listenerRotation.x = wrotx;
    PhContext.listenerRotation.y = wroty;
    PhContext.listenerRotation.z = wrotz;
    PhContext.listenerRotation.w = wrotw;

}