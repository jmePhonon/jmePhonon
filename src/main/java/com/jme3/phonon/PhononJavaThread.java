package com.jme3.phonon;

abstract class PhononJavaThread extends Thread {
    private volatile boolean UPDATE_FLAG;

    public void stopUpdate() {
        UPDATE_FLAG = false;	
    }

    protected boolean keepUpdating() {
        return UPDATE_FLAG;
    }
}