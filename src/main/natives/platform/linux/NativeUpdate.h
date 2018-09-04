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

struct {
    JavaVM *vm;
    JNIEnv *env;
    pthread_t thread;
    jobject renderer;
    jboolean useNativeClock;
    jlong timeDelta;
    struct timespec tp;
    void (*updateFunc)(JNIEnv*, jobject);
} ThreadContext;

#define timespec2ns(x) (x.tv_sec * 1000000000LL + x.tv_nsec)
#define CLOCK_TYPE CLOCK_REALTIME_COARSE

void *nuLoop() {
    while (1) {
        jlong startTime = 0;
        if (ThreadContext.useNativeClock) {
            clock_gettime(CLOCK_TYPE, &ThreadContext.tp);
            startTime = timespec2ns(ThreadContext.tp);
        }
        JavaVMAttachArgs thread_arg;
        thread_arg.version = JNI_VERSION_1_6;
        thread_arg.name = "Phonon Native Thread";
        thread_arg.group = NULL;

        (*ThreadContext.vm)->AttachCurrentThreadAsDaemon(ThreadContext.vm, (void **)&ThreadContext.env, &thread_arg);

        // Java_com_jme3_phonon_PhononRenderer_updateNative(ThreadContext.env, ThreadContext.renderer);
        ThreadContext.updateFunc(ThreadContext.env, ThreadContext.renderer);

        jclass class = (*ThreadContext.env)->GetObjectClass(ThreadContext.env, ThreadContext.renderer);
        jmethodID mid = (*ThreadContext.env)->GetMethodID(ThreadContext.env, class, "runDecoder", "()V");

        (*ThreadContext.env)->CallVoidMethod(ThreadContext.env, ThreadContext.renderer, mid);

        if (ThreadContext.useNativeClock) {
            clock_gettime(CLOCK_TYPE, &ThreadContext.tp);
            jlong endTime = timespec2ns(ThreadContext.tp);

            jlong sleeptime = ThreadContext.timeDelta - (endTime - startTime);
            if (sleeptime > 0) {

                if (nanosleep((const struct timespec[]){{0, sleeptime}}, NULL) < 0) {
                    printf("Error can't sleep \n");
                }
            }
        }
    }
    return NULL;
}

void nuInit(JNIEnv *env, jobject *renderer, jboolean useNativeClock, jdouble sdelta, void (*uf)(JNIEnv*, jobject)) {
    printf("Initialize pthread\n");
    ThreadContext.useNativeClock = useNativeClock;
    ThreadContext.timeDelta = 1000000000LL * sdelta;
    ThreadContext.updateFunc = uf;

    (*env)->GetJavaVM(env, &ThreadContext.vm);
    ThreadContext.renderer = (*env)->NewGlobalRef(env, (*renderer));
    jint c = pthread_create(&ThreadContext.thread, NULL, nuLoop, NULL);
    if (c) {
        printf("Error - pthread_create() return code: %d\n", c);
    }
}
#endif