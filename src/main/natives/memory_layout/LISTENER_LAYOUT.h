#ifndef __LISTENER_LAYOUT__
#define __LISTENER_LAYOUT__
        #include "com_jme3_phonon_memory_layout_LISTENER_LAYOUT.h"
        /** 
         * Returns position of first byte of the field
         */
        #define ListenerFieldB(field) com_jme3_phonon_memory_layout_LISTENER_LAYOUT_##field

        /**
         * Returns field length in bytes
         */
        #define ListenerFieldSizeB(field) com_jme3_phonon_memory_layout_LISTENER_LAYOUT_##field##_fieldsize

        /**
         * Returns position of first 4bytes of the field
         */
        #define ListenerField(field) (ListenerFieldB(field)/4)

        /**
         * Returns field length 
         */
        #define ListenerFieldSize(field) (ListenerFieldSizeB(field)/4)
#endif