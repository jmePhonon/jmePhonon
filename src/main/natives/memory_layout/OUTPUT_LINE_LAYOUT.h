#ifndef __OUTPUT_LINE_LAYOUT__
#define __OUTPUT_LINE_LAYOUT__
        #include "com_jme3_phonon_memory_layout_OUTPUT_LINE_LAYOUT.h"
        /** 
         * Returns position of first byte of the field
         */
        #define olHeaderB(field) com_jme3_phonon_memory_layout_OUTPUT_LINE_LAYOUT_##field

        /**
         * Returns field length in bytes
         */
        #define olHeaderSizeB(field) com_jme3_phonon_memory_layout_OUTPUT_LINE_LAYOUT_##field##_fieldsize

        /**
         * Returns position of first 4bytes of the field
         */
        #define olHeader(field) (olHeaderB(field)/4)

        /**
         * Returns field length 
         */
        #define olHeaderSize(field) (olHeaderSizeB(field)/4)
#endif