package org.example;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 多线程的非阻塞服务器实现
 * 主线程负责处理请求的连接，<b>当有 io 事件发生时</b>将要处理的事件放到线程池中处理请求的读写。
 * 弊端：请求连接比较多时，使用一个主线程接受连接的效率低
 *
 * @author geng
 * @since 2023/2/7 10:49
 */
public class NioMultiThreadServer {
    static class RequestIOHandler extends Thread {
        private final SelectionKey key;
        private final SocketChannel socketChannel;
        private SocketAddress remoteAddress = null;

        RequestIOHandler(SelectionKey key) {
            System.out.println(key);
            this.key = key;
            this.socketChannel = (SocketChannel) key.channel();
            try {
                this.remoteAddress = socketChannel.getRemoteAddress();
            } catch (IOException ignore) {
            }
        }

        @Override
        public void run() {
            ByteBuffer buff = ByteBuffer.allocate(10);
            ByteBuffer buff1 = ByteBuffer.allocate(5);
            long read;
            try {
                while ((read = socketChannel.read(new ByteBuffer[]{buff, buff1})) > 0) {
                    System.out.printf("读取到客户端（%s）" + read + "个字节！%n", remoteAddress);
                    StringBuilder sb = new StringBuilder();
                    buff.flip();
                    buff1.flip();
                    for (int i = 0; i < buff.limit(); i++) {
                        sb.append((char) buff.get());
                    }
                    buff.clear();
                    for (int i = 0; i < buff1.limit(); i++) {
                        sb.append((char) buff1.get());
                    }
                    buff1.clear();
                    System.out.printf("读取到客户端（%s）数据：" + sb + "%n", remoteAddress);
                    socketChannel.write(ByteBuffer.wrap(("回显：" + sb).getBytes(StandardCharsets.UTF_8)));
                }
                if (read == -1) { //通道被正常关闭，需要将该 key 从 selector 中移除（本质是将该 key 加入到 selector canceledKeys 集合中
                    //在下一轮 select() 中将 cancelledKeys 所有的 key 从 selector 的 keys 中移除。 ）
                    key.cancel();
                }
            } catch (IOException e) {
                try {
                    key.cancel();
                    System.out.printf("客户端（%s）读取抛出异常！%s %n", remoteAddress, e.getMessage());
                    socketChannel.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    static class EventLoop extends Thread implements Closeable {
        private final ExecutorService threadPool;
        private volatile Selector selector;

        EventLoop(int threadN) {
            this.threadPool = new ThreadPoolExecutor(threadN, threadN,
                    0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
            try {
                this.selector = Selector.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.start();
        }

        public void register(SocketChannel channel) throws IOException {
            channel.configureBlocking(false);
            channel.register(this.selector, SelectionKey.OP_READ);
            SocketAddress remoteAddress = null;
            try {
                remoteAddress = channel.getRemoteAddress();
            } catch (IOException ignore) {
            }
            this.selector.wakeup();
            System.out.printf("客户端（%s）连接已建立！%n", remoteAddress != null ? remoteAddress : "未知");
        }

        @Override
        public void run() {
            try {
                System.out.println("开始监听io事件...");
                while (true) {
                    int n = this.selector.select();//当调用 selector.wakeup(); 时直接返回，返回值可能为 0
                    if (n == 0)
                        continue;
                    Set<SelectionKey> keys = selector.selectedKeys();
                    System.out.println("处理读事件！" + keys.size());
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        try {
                            threadPool.submit(new RequestIOHandler(key));
                        } catch (Exception ignore) {
                        }
                    }
                    keys.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("停止监听io事件！");
            }
            System.out.println("----------");
        }

        public void close() throws IOException {
            threadPool.shutdownNow();
            selector.close();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = null;
        Selector selector = null;
        EventLoop eventLoop = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            eventLoop = new EventLoop(2);
            registerShutdownHandler(eventLoop, serverSocketChannel, selector);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 9090));
            System.out.println("Server listen at:" + "localhost:" + 9090 + "....");
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    if (socketChannel == null)
                        continue;
                    eventLoop.register(socketChannel);
                    iterator.remove();
                }
            }
        } finally {
            System.out.println("退出！");
            if (eventLoop != null) {
                eventLoop.close();
            }
            if (selector != null) {
                selector.close();
            }
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        }
    }

    private static void registerShutdownHandler(EventLoop eventLoop,
                                                ServerSocketChannel serverSocketChannel,
                                                Selector selector) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                eventLoop.close();
            } catch (IOException ignore) {
            }
            try {
                serverSocketChannel.close();
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("清理完成，退出！");
        }));
    }
}


