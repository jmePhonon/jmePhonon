#ifndef __AUDIOSOURCE_LAYOUT__
#define __AUDIOSOURCE_LAYOUT__
        #include "com_jme3_phonon_memory_layout_AUDIOSOURCE_LAYOUT.h"
        /** 
         * Returns position of first byte of the field
         */
        #define asSourceFieldB(field) com_jme3_phonon_memory_layout_AUDIOSOURCE_LAYOUT_##field

        /**
         * Returns field length in bytes
         */
        #define asSourceFieldSizeB(field) com_jme3_phonon_memory_layout_AUDIOSOURCE_LAYOUT_##field##_fieldsize

        /**
         * Returns position of first 4bytes of the field
         */
        #define asSourceField(field) (asSourceFieldB(field)/4)

        /**
         * Returns field length 
         */
        #define asSourceFieldSize(field) (asSourceFieldSizeB(field)/4)
#endif