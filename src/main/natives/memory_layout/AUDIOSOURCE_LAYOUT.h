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

        #define asFlag(flag) (com_jme3_phonon_memory_layout_AUDIOSOURCE_LAYOUT_FLAG_##flag)
#endif