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