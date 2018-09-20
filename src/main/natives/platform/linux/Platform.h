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

#define _XOPEN_SOURCE 700

#include <jni.h>
#include <stdio.h>
#include <pthread.h>
#include <time.h>


#define HAS_NATIVE_THREAD_SUPPORT 1

#define true 1
#define false 0


struct {
    pthread_t thread;
} PlatformThreadContext;

inline void plStartThread(void* (*updateFunction)()) {
    jint c = pthread_create(&PlatformThreadContext.thread, NULL, updateFunction, NULL);
    if (c) {
        printf("Error - pthread_create() return code: %d\n", c);
    }
}

inline void plStopThread() {
    if(pthread_join(PlatformThreadContext.thread, NULL) != 0) {
        perror("error joining native thread");
    }
}

inline void plSleep(){
  if (nanosleep((const struct timespec[]){{0, 1000000ll}}, NULL) < 0) {
        printf("Error can't sleep \n");
  }
}

#endif