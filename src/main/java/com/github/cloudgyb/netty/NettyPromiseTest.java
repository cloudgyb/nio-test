package com.github.cloudgyb.netty;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultPromise;

import java.util.concurrent.ExecutionException;

/**
 * @author geng
 * @since 2023/2/13 15:36
 */
public class NettyPromiseTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        DefaultEventExecutor eventExecutors = new DefaultEventExecutor();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventExecutors);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                promise.setSuccess(1000);
            } catch (InterruptedException e) {
                promise.setFailure(e);
            }
        }).start();
        promise.addListener(future -> {
            Object object = future.get();
            System.out.println(Thread.currentThread().getName() + " " + object);
        });
        Integer integer = promise.get();
        System.out.println(Thread.currentThread().getName() + " " + integer);
        eventExecutors.shutdownGracefully();
    }
}
