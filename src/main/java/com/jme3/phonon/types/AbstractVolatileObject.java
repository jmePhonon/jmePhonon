/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package com.jme3.phonon.types;


/**
 * AbstractVolatileObject
 */
public abstract class AbstractVolatileObject<A,B> implements VolatileObject<A,B>{

    public volatile boolean needUpdate=true;
    public volatile boolean needUpdateFinalization;
    private volatile Thread updateThread;
    private volatile Thread commitThread;
    private volatile boolean forcedCommit=false;
    private volatile  boolean forcedUpdate=false;

    @Override
    public void setUpdateNeeded() {
        needUpdate = true;
    }


    @Override
    public void commit(B out, int i) {
        assert forcedCommit||(commitThread==null&&(commitThread=Thread.currentThread())!=null)||Thread.currentThread()==commitThread:"Commiting from wrong thread "+Thread.currentThread()+" =/= "+updateThread;
        if(!forcedCommit&&!needUpdateFinalization) return;
        onCommit(out,i);
        needUpdateFinalization=false;
        forcedCommit=false;
    }
    


    public abstract void onCommit(B out, int i);

    @Override
    public void update(A v) {
        assert forcedUpdate||(updateThread==null&&(updateThread=Thread.currentThread())!=null)||Thread.currentThread()==updateThread:"Updating from wrong thread "+Thread.currentThread()+" =/= "+updateThread;
        if(!forcedUpdate&&(!needUpdate||needUpdateFinalization)) return;

        onUpdate(v);
        needUpdate=false;
        needUpdateFinalization=true;
        forcedUpdate=false;
    }
    

    /**
     * Tells the instance to ignore every check for the next commit
     * @return
     */
    public AbstractVolatileObject forceCommit() {
        forcedCommit=true;
        return this;
    }
    
     /**
     * Tells the instance to ignore every check for the next update
     * @return
     */
    public AbstractVolatileObject forceUpdate(){
        forcedUpdate=true;
        return this;
    }

    public abstract void onUpdate(A out);

}