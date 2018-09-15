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
#ifndef __NATIVE_UPDATE__
#define __NATIVE_UPDATE__
#include <jni.h>

#define _XOPEN_SOURCE 700

#include <stdio.h>

#include <jni.h>
#include <pthread.h>
#include <time.h>
// #include "types.h"
#include "platform/linux/NativeUpdate.h"

#define HAS_NATIVE_THREAD_SUPPORT 1

#define true 1
#define false 0

struct {
    JavaVM *vm;
    JNIEnv *env;
    pthread_t thread;
    jobject renderer;
    struct timespec tp;    
    jboolean decoupled;
    jboolean initialized;
    jlong startTime;
    jlong endTime;

    void (*updateFunc)(JNIEnv*, jobject);
} ThreadContext;

#define timespec2ns(x) (x.tv_sec * 1000000000LL + x.tv_nsec)
#define CLOCK_TYPE CLOCK_MONOTONIC_COARSE

void *nuLoop() {
    if (!ThreadContext.initialized) {
            JavaVMAttachArgs thread_arg;
            thread_arg.version = JNI_VERSION_1_6;
            thread_arg.name = "Phonon Native Thread";
            thread_arg.group = NULL;

            (*ThreadContext.vm)->AttachCurrentThreadAsDaemon(ThreadContext.vm, (void **)&ThreadContext.env, &thread_arg);
            
            ThreadContext.initialized = true;
    }

    while (1) {

        if (nanosleep((const struct timespec[]){{0, 1000000ll}}, NULL) < 0) {
            printf("Error can't sleep \n");
        }

        ThreadContext.updateFunc(ThreadContext.env, ThreadContext.renderer);

        if(!ThreadContext.decoupled){
            jclass class = (*ThreadContext.env)->GetObjectClass(ThreadContext.env, ThreadContext.renderer);
            jmethodID mid = (*ThreadContext.env)->GetMethodID(ThreadContext.env, class, "runDecoder", "()V");  
            (*ThreadContext.env)->CallVoidMethod(ThreadContext.env, ThreadContext.renderer, mid);      
        }             
    }
    return NULL;
}

void nuInit(JNIEnv *env, jobject *renderer, jboolean decoupled,void (*uf)(JNIEnv*, jobject)) {
    printf("Initialize pthread\n");
    ThreadContext.updateFunc = uf;
    ThreadContext.decoupled = decoupled;
    ThreadContext.initialized = false;
    ThreadContext.startTime = 0;
    ThreadContext.endTime = 0;

    (*env)->GetJavaVM(env, &ThreadContext.vm);
    ThreadContext.renderer = (*env)->NewGlobalRef(env, (*renderer));
    jint c = pthread_create(&ThreadContext.thread, NULL, nuLoop, NULL);
    if (c) {
        printf("Error - pthread_create() return code: %d\n", c);
    }
}
#endif