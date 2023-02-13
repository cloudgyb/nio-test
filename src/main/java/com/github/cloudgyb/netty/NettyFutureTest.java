package com.github.cloudgyb.netty;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.Future;

import java.util.concurrent.ExecutionException;

/**
 * @author geng
 * @since 2023/2/13 15:10
 */
public class NettyFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        DefaultEventLoop eventExecutors = new DefaultEventLoop();
        Future<Integer> future = eventExecutors.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName());
            return 100;
        });

        Integer res = future.get();

        System.out.println(res);
        future.addListener(future1 -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println(future1.get());
        });
        eventExecutors.shutdownGracefully();
    }
}
