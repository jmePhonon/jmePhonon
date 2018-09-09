#ifndef __LISTENER_LAYOUT__
#define __LISTENER_LAYOUT__
        #include "com_jme3_phonon_memory_layout_LISTENER_LAYOUT.h"
        /** 
         * Returns position of first byte of the field
         */
        #define phListenerFieldB(field) com_jme3_phonon_memory_layout_LISTENER_LAYOUT_##field

        /**
         * Returns field length in bytes
         */
        #define phListenerFieldSizeB(field) com_jme3_phonon_memory_layout_LISTENER_LAYOUT_##field##_fieldsize

        /**
         * Returns position of first 4bytes of the field
         */
        #define phListenerField(field) (phListenerFieldB(field)/4)

        /**
         * Returns field length 
         */
        #define phListenerFieldSize(field) (phListenerFieldSizeB(field)/4)
#endif