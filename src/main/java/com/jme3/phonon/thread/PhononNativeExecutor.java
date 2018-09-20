package com.jme3.phonon.thread;

import com.jme3.phonon.PhononUpdater;

public class PhononNativeExecutor implements PhononExecutor, Runnable {
    private PhononUpdater updater;

    public PhononNativeExecutor() { }

    public void setUpdater(PhononUpdater u) {
        updater = u;
    }

    public void startUpdate() {
        startUpdateNative(this);
    }

    public void stopUpdate() { 
        stopUpdateNative();
    }
   
    @Override
    public void run() {
        updater.phononUpdate();
    }

    protected native void startUpdateNative(Runnable runnable);
    protected native void stopUpdateNative();
}