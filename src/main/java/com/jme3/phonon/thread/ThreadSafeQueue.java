package com.jme3.phonon.thread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ThreadSafeQueue
 */
public class ThreadSafeQueue implements Runnable{
    private final CopyOnWriteArrayList<Runnable> QUEUE=new CopyOnWriteArrayList<Runnable>();
    private Method getArray;
    private Method removeRange;
    
    public ThreadSafeQueue(){
        super();
        // TODO find a better solution
        try{
            getArray=CopyOnWriteArrayList.class.getDeclaredMethod("getArray");
            removeRange=CopyOnWriteArrayList.class.getDeclaredMethod("removeRange",Integer.TYPE,Integer.TYPE);
            getArray.setAccessible(true);
            removeRange.setAccessible(true);
        }catch(Exception e){
            
            e.printStackTrace();
            System.exit(1);
        }
    }


    protected Object callMethod(Object obj,Method m, Object... args) {
        try{
            return m.invoke(obj,args);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
            return null;
		}
    }

    public void enqueue(Runnable r) {
        QUEUE.add(r);
    }
    
    @Override
    public void run() {
        if(QUEUE.size()==0) return;
        Object arr[]=(Object[])callMethod(QUEUE,getArray);
        if(arr.length==0) return;
        for(int i=0;i<arr.length;i++){
            ((Runnable)arr[i]).run();
        }
        callMethod(QUEUE,removeRange,0,arr.length);
    }
}