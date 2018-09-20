package com.jme3.phonon.thread;

import com.jme3.phonon.PhononUpdater;

public class PhononJavaExecutor extends Thread implements PhononExecutor {
    private volatile boolean UPDATE_FLAG;
    private PhononUpdater updater;

    public PhononJavaExecutor() {
        this.UPDATE_FLAG = true;
    }

    public void setUpdater(PhononUpdater u) {
        updater = u;
    }

    public void startUpdate() {
        super.start(); 
    }

    public void stopUpdate() {
        UPDATE_FLAG = false;

        while(this.isAlive()) {
            try {
                Thread.sleep(1);
            } catch(Exception e) { }
        }
    }

    @Override
    public void run() {
		do {
			try {
				Thread.sleep(1);
			} catch (Exception e) { }

            updater.phononUpdate();

		} while (UPDATE_FLAG);
    }
}