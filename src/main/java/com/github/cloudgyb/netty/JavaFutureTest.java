package com.github.cloudgyb.netty;

import java.util.concurrent.*;

/**
 * @author geng
 * @since 2023/2/13 14:44
 */
public class JavaFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        Future<Integer> res = threadPool.submit(() -> {
            Thread.sleep(1000);
            return 12;
        });
        Integer integer = res.get();
        System.out.println(integer);
        threadPool.shutdown();
    }
}
