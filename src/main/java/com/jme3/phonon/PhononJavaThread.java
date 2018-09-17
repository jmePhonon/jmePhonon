package com.jme3.phonon;

abstract class PhononJavaThread extends Thread {
    private volatile boolean UPDATE_FLAG = true;

    public void stopUpdate() {
        UPDATE_FLAG = false;	
    }

    public boolean isUpdating() {
        return UPDATE_FLAG;
    }
}