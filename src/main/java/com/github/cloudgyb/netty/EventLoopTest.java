package com.github.cloudgyb.netty;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author geng
 * @since 2023/2/13 12:38
 */
public class EventLoopTest {
    public static void main(String[] args) {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup(2);
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        eventExecutors.next().execute(() -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println("执行！！！");
        });

        eventExecutors.shutdownGracefully().addListener(future -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println("Event Loop 关闭！");
        });

        GlobalEventExecutor.INSTANCE.execute(() -> System.out.println(Thread.currentThread().getName()));
    }
}
