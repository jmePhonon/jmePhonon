package com.jme3.phonon.thread;

import com.jme3.phonon.PhononUpdater;

public class PhononDummyExecutor implements PhononExecutor {
    public PhononDummyExecutor() { }
    public void startUpdate() { }
    public void stopUpdate() { }
    public void setUpdater(PhononUpdater u) { }
}