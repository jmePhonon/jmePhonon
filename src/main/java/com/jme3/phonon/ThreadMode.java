package com.jme3.phonon;

/**
 * ThreadMode
 */
public enum ThreadMode {
    JAVA(false,false),
    NATIVE(true,false),
    NATIVE_DECOUPLED(true, true);
    
    public final boolean isNative,isDecoupled;
    ThreadMode(boolean nnative,boolean decoupled){
        isNative=nnative;
        isDecoupled=decoupled;
    }
}