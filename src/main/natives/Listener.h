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
#ifndef __LISTENER_H__
#define __LISTENER_H__
#include "Common.h"



struct Listener{

    /*internal use only*/
    vec3 _pos;
    vec3 _dir;
    vec3 _up;
    jfloat *_data; // Shared memory
} ;


/**
 * Create and initialize one Listener
 */
struct Listener *lsNew(struct GlobalSettings *settings,jfloat *data);


void lsDestroy(struct GlobalSettings *settings,struct Listener *ls);

/**
 * Return a vector3 containing the  listener position retrieved from the shared memory
 */
vec3 *lsGetPosition(struct GlobalSettings *settings,struct Listener *ls);

/**
 * Return an unit vector3 containing the  listener direction retrieved from the shared memory
 */
vec3 *lsGetDirection(struct GlobalSettings *settings,struct Listener *ls);

/**
 * Return an unit vector3 containing the  listener up axis retrieved from the shared memory
 */
vec3 *lsGetUp(struct GlobalSettings *settings,struct Listener *ls);

jfloat *lsGetVolume(struct GlobalSettings *settings, struct Listener *ls);

#endif