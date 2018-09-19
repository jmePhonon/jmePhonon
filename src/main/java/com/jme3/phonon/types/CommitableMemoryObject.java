package com.jme3.phonon.types;

/**
 * CommitableMemoryObject
 * This is used internally to update direct buffers in a thread safe manner.
 * The producer will call `update` and store the update into temporary thread safe variables,
 * the consumer will call `commit` to write the temporary variables into its own memory.
 */

public abstract class CommitableMemoryObject {
    private Thread updateThread;
    private Thread commitThread;
    private volatile boolean forcedCommit=false;
    private volatile  boolean forcedUpdate=false;

    public final void update(float tpf) {
        assert forcedUpdate||(updateThread==null&&(updateThread=Thread.currentThread())!=null)|| Thread.currentThread()==updateThread;
        onUpdate(tpf);
        forcedUpdate=false;
    }

    public final void commit(float tpf) {       
        assert forcedCommit||(commitThread==null&&(commitThread=Thread.currentThread())!=null)||Thread.currentThread()==commitThread;
        onCommit(tpf);
        forcedCommit=false;
    }
    
   
  /**
     * Tells the instance to ignore every check for the next commit
     * @return
     */
    public CommitableMemoryObject forceCommit() {
        forcedCommit=true;
        return this;
    }
    
   /**
     * Tells the instance to ignore every check for the next update
     * @return
     */
    public CommitableMemoryObject forceUpdate(){
        forcedUpdate=true;
        return this;
    }


    public abstract void onUpdate(float tpf);
    public abstract void onCommit(float tpf);

}