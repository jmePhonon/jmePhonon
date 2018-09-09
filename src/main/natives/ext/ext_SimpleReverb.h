#ifndef __EXT_SIMPLE_REVERB__
#define __EXT_SIMPLE_REVERB__
#include <jni.h>
#include "Settings.h"

/** 
 * Simple reverb extension
 *  
 * This functionality is by default uninplemented.
 * 
 * Some code that implements this header must be written in order
 * to provide backward compatibility to jme's audio Environment class.
 * 
 * If the proper code is implemented, it must be enabled by defining INCLUDE_SIMPLE_REVERB
 * at compile time
 * 
 * eg. BUILD_ARGS="-DINCLUDE_SIMPLE_REVERB" gradle buildNatives
 * 
 */ 


void srInit(struct GlobalSettings *settings);
void srDestroy(struct GlobalSettings *settings);
void srSetEnvironment(struct GlobalSettings *settings,jfloat *data);
void srApplyReverb(struct GlobalSettings *settings, jfloat *inframe, jfloat *outframe);

#endif