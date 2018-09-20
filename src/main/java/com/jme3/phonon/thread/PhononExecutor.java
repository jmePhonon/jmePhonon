package com.jme3.phonon.thread;

import com.jme3.phonon.PhononUpdater;

public interface PhononExecutor {
    public abstract void startUpdate();
    public abstract void stopUpdate();
    public abstract void setUpdater(PhononUpdater u);
}