package com.github.cloudgyb.netty.promise;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        MyPromise<Integer> promise = new MyPromise<>();
        promise.addListener(System.out::println)
                .addListener(System.out::println);
        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            promise.setSuccess(12);
        }).start();
        new Thread(() -> {
            try {
                Integer integer = promise.get();
                System.out.println(Thread.currentThread().getName() + integer);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        Integer integer = promise.get();
        System.out.println(Thread.currentThread().getName() + integer);

        promise.close();
    }
}
