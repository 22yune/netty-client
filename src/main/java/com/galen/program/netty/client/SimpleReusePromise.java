package com.galen.program.netty.client;

import io.netty.util.concurrent.Promise;

public class SimpleReusePromise<V> extends SimpleRepeatPromise<V> {
    @Override
    protected boolean checkRepeat(Promise<V> nowPromise, Promise<V> newPromise){
        if(!nowPromise.isDone()){
            return false;
        }
        return true;
    }
}
