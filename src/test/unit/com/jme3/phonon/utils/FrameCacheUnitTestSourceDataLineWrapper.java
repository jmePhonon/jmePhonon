package com.jme3.phonon.utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * FrameCacheUnitTestSourceDataLineWrapper
 * 
 * This class is not general purpose and it's written to work only within FrameCacheUnitTest 
 */
public class FrameCacheUnitTestSourceDataLineWrapper implements SourceDataLine {

    public byte bytes[];
    public int written;

    public FrameCacheUnitTestSourceDataLineWrapper(byte bytes[]) {
        this.bytes = bytes;
    }
    
    @Override
    public int available() {
        return bytes.length;
    }

    @Override
    public int write(byte[] b, int off, int len) {
        System.arraycopy(b, off, bytes, written, len);
        written += len;
        return len;
    }

 

    @Override
    public void drain() {

    }

    @Override
    public void flush() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public AudioFormat getFormat() {
        return null;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

   

    @Override
    public int getFramePosition() {
        return 0;
    }

    @Override
    public long getLongFramePosition() {
        return 0;
    }

    @Override
    public long getMicrosecondPosition() {
        return 0;
    }

    @Override
    public float getLevel() {
        return 0;
    }

    @Override
    public javax.sound.sampled.Line.Info getLineInfo() {
        return null;
    }

    @Override
    public void open() throws LineUnavailableException {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public Control[] getControls() {
        return null;
    }

    @Override
    public boolean isControlSupported(Type control) {
        return false;
    }

    @Override
    public Control getControl(Type control) {
        return null;
    }

    @Override
    public void addLineListener(LineListener listener) {

    }

    @Override
    public void removeLineListener(LineListener listener) {

    }

    @Override
    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {

    }

    @Override
    public void open(AudioFormat format) throws LineUnavailableException {

    }


    
}