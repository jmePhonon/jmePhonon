package com.jme3.phonon;

class PhononJavaThread extends Thread {
    private volatile boolean UPDATE_FLAG = true;

    public PhononJavaThread(Runnable runnable) {
        super(runnable);
    }

    public void stopUpdate() {
        UPDATE_FLAG = false;	
    }

    public boolean isUpdating() {
        return UPDATE_FLAG;
    }
}