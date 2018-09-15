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
#ifndef __EXT_SIMPLE_REVERB__
#define __EXT_SIMPLE_REVERB__
#include "Common.h" 


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
jboolean srHasValidEnvironment(struct GlobalSettings *settings);
void srApplyReverb(struct GlobalSettings *settings, jfloat *inframe, jfloat *outframe);

#endif