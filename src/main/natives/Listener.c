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

