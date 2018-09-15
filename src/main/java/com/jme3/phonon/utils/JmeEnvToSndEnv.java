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
package com.jme3.phonon.utils;

import com.jme3.audio.Environment;

/**
 * JmeEnvToSndEnv
 */
public class JmeEnvToSndEnv {

    public static float[] convert(Environment env) {

        // TODO: if possible, perform some sort of conversion for custom environments
        float envdata[] = new float[17];

        envdata[0] = 1;
        envdata[1] = .4f;
        envdata[2] = -.9f;
        envdata[3] = -10f;
        envdata[4] = 1.6f;
        envdata[5] = .7f;
        envdata[6] = 1.f;
        envdata[7] = -0f;
        envdata[8] = .27f;
        envdata[9] = .15f;
        envdata[10] = .7f;
        envdata[11] = 17000;
        envdata[12] = 500;
        envdata[13] = 7000;
        envdata[14] = 10000;
        envdata[15] = 3.2f;
        envdata[16] = 0.020f;
        
        if (env == null) {
            envdata[0] = -2;
        } else if (env == Environment.Closet) {
            envdata[0] = -1;
            envdata[1] = 7;            
        }else if (env == Environment.Dungeon) {
            envdata[0] = -1;
            envdata[1] = 6;            
        }else if (env == Environment.Garage) {
            envdata[0] = -1;
            envdata[1] = 13;            
        }else if (env == Environment.Cavern) {
            envdata[0] = -1;
            envdata[1] = 17;            
        }else if (env == Environment.AcousticLab) {
            envdata[0] = -1;
            envdata[1] = 14;            
        }

        return envdata;
    }
}