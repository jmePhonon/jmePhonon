package com.jme3.phonon.thread;

import com.jme3.phonon.PhononUpdater;

public class PhononNativeExecutor implements PhononExecutor, Runnable {
    private PhononUpdater updater;
    private volatile boolean firstLoop=false;

    public PhononNativeExecutor() { }

    public void setUpdater(PhononUpdater u) {
        updater = u;
    }

    public void startUpdate() {
        startUpdateNative(this);
        while(!firstLoop){
			try{
				Thread.sleep(1);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
    }

    public void stopUpdate() { 
        stopUpdateNative();
    }
   
    @Override
    public void run() {
        updater.phononUpdate();
        firstLoop=true;
    }

    protected native void startUpdateNative(Runnable runnable);
    protected native void stopUpdateNative();
}