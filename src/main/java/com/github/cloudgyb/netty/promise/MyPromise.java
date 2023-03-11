package com.github.cloudgyb.netty.promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;

public class MyPromise<T> {
    private volatile Object result;
    ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private final List<Consumer<T>> listeners = Collections.synchronizedList(new ArrayList<>());

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<MyPromise, Object> resultFieldUpdater =
            AtomicReferenceFieldUpdater.newUpdater(MyPromise.class, Object.class, "result");


    public void setSuccess(T res) {
        if (resultFieldUpdater.compareAndSet(this, null, res)) {
            notifyListeners();
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private void notifyListeners() {
        threadPool.submit(this::notifyListeners0);
    }

    private void notifyListeners0() {
        listeners.forEach(l -> {
            try {
                l.accept(get());
            } catch (InterruptedException ignore) {
            }
        });
    }

    @SuppressWarnings("unchecked")
    public T get() throws InterruptedException {
        while (result == null) {
            synchronized (this) {
                wait();
            }
        }
        return (T) result;
    }

    public MyPromise<T> addListener(Consumer<T> customizer) {
        listeners.add(customizer);
        return this;
    }

    public void close() {
        threadPool.shutdown();
    }
}
