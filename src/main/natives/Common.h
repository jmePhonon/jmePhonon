#ifndef __COMMONS_H__
#define __COMMONS_H__
#include <jni.h>
#include <math.h>
#include <stdlib.h>
#include <stdint.h>

#include "phonon.h"


#define true 1
#define false 0

#define bool error_use_jni_type
#define char error_use_jni_type
#define int error_use_jni_type
#define float error_use_jni_type
#define long error_use_jni_type
#define double error_use_jni_type
#define short error_use_jni_type
#define unsigned error_use_jni_type
#define signed error_use_jni_type


struct GlobalSettings{
    jint nOutputLines; // How many direct output lines
    jint nOutputChannels; // How many channels per line (1=mono, 2=stereo ..)
    jint bufferSize; // How many frames on the outputline buffer
    jint frameSize; // How many samples for each frame
    // jint outputFrameSize; // How many samples for each frame
    jint sampleRate; // Sound sampling rate ( eg 44100 )
    jboolean isPassthrough; // debugonly
        jint nSourcesPerLine;
};


// typedef struct {
//     jfloat x;
//     jfloat y;
//     jfloat z;
// } vec3;

// typedef struct {
//     jfloat x;
//     jfloat y;
//     jfloat z;
//     jfloat w;
// } qtr;

typedef IPLVector3 vec3;
typedef IPLQuaternion qtr;
typedef IPLDirectivity drt;

/** Adapted from jmonkeyengine's Quaternion.java */
static inline void qtrRotateVec3(qtr *q, vec3 *v, vec3 *store) {
    jfloat vx = v->x, vy = v->y, vz = v->z;
    if (vx == 0.f && vy == 0.f && vz == 0.f) {
        store->x = 0;
        store->y = 0;
        store->z = 0;
    } else {
        jfloat x = q->x;
        jfloat y = q->y;
        jfloat z = q->z;
        jfloat w = q->w;

        store->x = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x * vx + 2 * y * x * vy + 2 * z * x * vz - z * z * vx - y * y * vx;
        store->y = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w * z * vx - z * z * vy + w * w * vy - 2 * x * w * vz - x * x * vy;
        store->z = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w * y * vx - y * y * vz + 2 * w * x * vy - x * x * vz + w * w * vz;
    }
}
static inline jfloat qtrNorm(qtr *q) {
    jfloat x = q->x;
    jfloat y = q->y;
    jfloat z = q->z;
    jfloat w = q->w;

    return w * w + x * x + y * y + z * z;
}

static inline void qtrInverse(qtr *q, qtr *out) {
    jfloat x = q->x;
    jfloat y = q->y;
    jfloat z = q->z;
    jfloat w = q->w;
    jfloat norm = qtrNorm(q);
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

static inline void vec3SubVec3(vec3 *v1, vec3 *v2, vec3 *store) {
    jfloat x = v1->x, y = v1->y, z = v1->z;
    store->x = x - v2->x;
    store->y = y - v2->y;
    store->z = z - v2->z;
}

static inline void vec3Normalize(vec3 *v1, vec3 *store) {
    jfloat x = v1->x, y = v1->y, z = v1->z;
    jfloat length = x * x + y * y + z * z;
    if (length != 1.f && length != 0.f) {
        length = 1.0f / sqrtf(length);
        store->x = x * length;
        store->y = y * length;
        store->z = z * length;
    }
    store->x = x;
    store->y = y;
    store->z = z;
}
#endif 