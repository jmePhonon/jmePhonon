#include "platform/linux/NativeUpdate.h"

struct {
    pthread_t thread;
} PlatformThreadContext;

void nuInit(void* (*updateFunction)()) {
    jint c = pthread_create(&PlatformThreadContext.thread, NULL, updateFunction, NULL);
    if (c) {
        printf("Error - pthread_create() return code: %d\n", c);
    }
}

void nuSleep() {
    if (nanosleep((const struct timespec[]){{0, 1000000ll}}, NULL) < 0) {
        printf("Error can't sleep \n");
    }
}

void nuStop() {
    if(pthread_join(PlatformThreadContext.thread, NULL) != 0) {
        perror("error joining native thread");
    }
}