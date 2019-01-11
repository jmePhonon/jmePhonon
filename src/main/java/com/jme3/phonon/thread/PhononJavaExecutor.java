package com.jme3.phonon.thread;

import com.jme3.phonon.PhononUpdater;

public class PhononJavaExecutor extends Thread implements PhononExecutor {
    private volatile boolean UPDATE_FLAG;
    private PhononUpdater updater;
    private volatile boolean firstLoop=false;
    public PhononJavaExecutor() {
        this.UPDATE_FLAG=true;
        setName("Phonon Java Thread");
        setPriority(Thread.MAX_PRIORITY);
        setDaemon(true);
    }

    public void setUpdater(PhononUpdater u) {
        updater = u;
    }

    public void startUpdate() {
        super.start();
        while(!firstLoop){
			try{
				Thread.sleep(1);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
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
            updater.phononUpdate();
            firstLoop=true;
		} while (UPDATE_FLAG);
    }
}