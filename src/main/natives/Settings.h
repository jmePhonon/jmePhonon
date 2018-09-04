#ifndef __COMMONS_H__
#define __COMMONS_H__
#include <jni.h>
struct GlobalSettings{
    jint nOutputLines; // How many direct output lines
    jint nOutputChannels; // How many channels per line (1=mono, 2=stereo ..)
    jint bufferSize; // How many frames on the outputline buffer
    jint inputFrameSize; // How many samples for each frame
    // jint outputFrameSize; // How many samples for each frame
    jint sampleRate; // Sound sampling rate ( eg 44100 )
    jboolean isPassthrough; // debugonly
};

#endif 