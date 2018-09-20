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

#include <jni.h>
#include <pthread.h>


#if defined(__linux__)
    #include "platform/linux/Platform.h"    
#endif

#include "com_jme3_phonon_thread_PhononNativeExecutor.h"


struct {
    JavaVM *vm;
    JNIEnv *env;
    struct timespec tp;
    jboolean initialized;
    volatile jboolean loop;
    jlong startTime;
    jlong endTime;

    jobject loopObject;
    jclass loopClass;
} ThreadContext;

inline void pntCallJMethod(char* name) {
    (*ThreadContext.env)->CallVoidMethod(ThreadContext.env, ThreadContext.loopObject, 
        (*ThreadContext.env)->GetMethodID(ThreadContext.env, ThreadContext.loopClass, name, "()V"));   
}

inline void pntInitializeContext() {
    #ifdef HAS_NATIVE_THREAD_SUPPORT    
        JavaVMAttachArgs thread_arg;
        thread_arg.version = JNI_VERSION_1_6;
        thread_arg.name = "Phonon Native Thread";
        thread_arg.group = NULL;

        (*ThreadContext.vm)->AttachCurrentThreadAsDaemon(ThreadContext.vm, (void **) &ThreadContext.env, &thread_arg);
        
        ThreadContext.loopClass = (*ThreadContext.env)->GetObjectClass(ThreadContext.env, ThreadContext.loopObject);

        ThreadContext.initialized = true;
        ThreadContext.loop = true;
    #endif
}

void* pntLoop() {
    #ifdef HAS_NATIVE_THREAD_SUPPORT
        if (!ThreadContext.initialized) {
            pntInitializeContext();
        }

        while (ThreadContext.loop) {
            plSleep();
            pntCallJMethod("run");
        }
    #endif 
    return NULL;
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_thread_PhononNativeExecutor_startUpdateNative(JNIEnv *env, jobject obj, jobject loopObj) {
    #ifdef HAS_NATIVE_THREAD_SUPPORT    
        printf("Initializing native thread\n");
        ThreadContext.initialized = false;
        ThreadContext.startTime = 0;
        ThreadContext.endTime = 0;
        (*env)->GetJavaVM(env, &ThreadContext.vm);
        ThreadContext.loopObject = (*env)->NewGlobalRef(env, loopObj);
        plStartThread(pntLoop);
    #endif
}

JNIEXPORT void JNICALL Java_com_jme3_phonon_thread_PhononNativeExecutor_stopUpdateNative(JNIEnv* env, jobject object) {
    #ifdef HAS_NATIVE_THREAD_SUPPORT    
        ThreadContext.loop = false;
        plStopThread();
    #endif
}

