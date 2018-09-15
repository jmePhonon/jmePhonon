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
#include "Listener.h"
#include "memory_layout/LISTENER_LAYOUT.h"


struct Listener *lsNew(struct GlobalSettings *settings,jfloat *data) {
    struct Listener *out = malloc(sizeof(struct Listener));
    out->_pos.x = 0;
    out->_pos.y = 0;
    out->_pos.z = 0;

    out->_dir.x = 0;
    out->_dir.y = 0;
    out->_dir.z = 0;

    out->_up.x = 0;
    out->_up.y = 0;
    out->_up.z = 0;

    out->_data = data;
    return out;
}

void lsDestroy(struct GlobalSettings *settings,struct Listener* ls){
    free(ls);
}

vec3 *lsGetPosition(struct GlobalSettings *settings,struct Listener *ls) {
    ls->_pos.x = ls->_data[ListenerField(POSX)];
    ls->_pos.y = ls->_data[ListenerField(POSY)];
    ls->_pos.z = ls->_data[ListenerField(POSZ)];
    return &ls->_pos;
}

vec3 *lsGetDirection(struct GlobalSettings *settings,struct Listener *ls) {
    ls->_dir.x = ls->_data[ListenerField(DIRX)];
    ls->_dir.y = ls->_data[ListenerField(DIRY)];
    ls->_dir.z = ls->_data[ListenerField(DIRZ)];
    return &ls->_dir;
}

vec3 *lsGetUp(struct GlobalSettings *settings,struct Listener *ls) {
    ls->_up.x = ls->_data[ListenerField(UPX)];
    ls->_up.y = ls->_data[ListenerField(UPY)];
    ls->_up.z = ls->_data[ListenerField(UPZ)];
    return &ls->_up;
}

jfloat *lsGetVolume(struct GlobalSettings *settings,struct Listener *ls){
    return &ls->_data[ListenerField(VOLUME)];
}

