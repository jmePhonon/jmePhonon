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