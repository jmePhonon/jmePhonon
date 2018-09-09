#ifndef __AUDIOSOURCE_LAYOUT__
#define __AUDIOSOURCE_LAYOUT__
        #include "com_jme3_phonon_memory_layout_AUDIOSOURCE_LAYOUT.h"
        /** 
         * Returns position of first byte of the field
         */
        #define phSourceFieldB(field) com_jme3_phonon_memory_layout_AUDIOSOURCE_LAYOUT_##field

        /**
         * Returns field length in bytes
         */
        #define phSourceFieldSizeB(field) com_jme3_phonon_memory_layout_AUDIOSOURCE_LAYOUT_##field##_fieldsize

        /**
         * Returns position of first 4bytes of the field
         */
        #define phSourceField(field) (phSourceFieldB(field)/4)

        /**
         * Returns field length 
         */
        #define phSourceFieldSize(field) (phSourceFieldSizeB(field)/4)
#endif