package com.github.cloudgyb.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 双线程的非阻塞服务器实现
 * 主线程负责处理请求的连接，<b>当有 io 事件发生时</b>将要处理的事件放到 EventLoop 中的 worker 线程处理请求的读写。
 * 弊端：请求连接比较多时，使用一个主线程接受连接的效率低，一个 worker 线程处理 IO 较慢。
 *
 * @author geng
 * @since 2023/2/7 10:49
 */
public class NioMultiThreadServer {
    private final static Logger logger = LoggerFactory.getLogger(NioMultiThreadServer.class);

    static class EventLoop implements Closeable {
        private final Thread thread;
        private volatile Selector selector;

        EventLoop() {
            this.thread = new Thread(EventLoop.this::run);
            try {
                this.selector = Selector.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.thread.start();
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
            logger.info("客户端（{}）连接已建立！%n", remoteAddress != null ? remoteAddress : "未知");
        }

        public void run() {
            logger.info(Thread.currentThread().getName() + " 开始监听io事件...");
            while (true) {
                try {
                    int n = this.selector.select();//当调用 selector.wakeup(); 时直接返回，返回值可能为 0
                    if (n == 0)
                        continue;
                } catch (ClosedSelectorException | IOException e) {
                    logger.error(Thread.currentThread().getName() + "Selector 出现异常！当前 EventLoop 退出!");
                    break;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                logger.info("处理读事件！" + keys.size());
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    handleIO(key);
                }
            }
            try {
                if (this.selector.isOpen())
                    this.selector.close();
            } catch (IOException ignore) {
            }
            logger.info(Thread.currentThread().getName() + " 停止监听io事件！");

        }

        @Override
        public void close() {
            thread.interrupt();
            try {
                selector.close();
            } catch (IOException ignore) {
            }
        }

        private void handleIO(SelectionKey key) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            SocketAddress remoteAddress = null;
            try {
                remoteAddress = socketChannel.getRemoteAddress();
            } catch (IOException ignore) {
            }
            ByteBuffer buff = ByteBuffer.allocate(10);
            ByteBuffer buff1 = ByteBuffer.allocate(5);
            long read;
            try {
                while ((read = socketChannel.read(new ByteBuffer[]{buff, buff1})) > 0) {
                    logger.info("读取到客户端（{}）" + read + "个字节！%n", remoteAddress);
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
                    logger.info("读取到客户端（{}）数据：" + sb + "%n", remoteAddress);
                    socketChannel.write(ByteBuffer.wrap(("回显：" + sb).getBytes(StandardCharsets.UTF_8)));
                }
                if (read == -1) { //通道被正常关闭，需要将该 key 从 selector 中移除（本质是将该 key 加入到 selector canceledKeys 集合中
                    //在下一轮 select() 中将 cancelledKeys 所有的 key 从 selector 的 keys 中移除。 ）
                    key.cancel();
                    socketChannel.close();
                    logger.info("客户端（{}）关闭连接！%n", remoteAddress);
                }
            } catch (IOException e) {
                try {
                    key.cancel();
                    logger.info("客户端（{}）读取抛出异常！{} %n", remoteAddress, e.getMessage());
                    socketChannel.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    static class EventLoopGroup implements Closeable {
        private final EventLoop[] eventLoopsGroup;
        private final AtomicInteger counter = new AtomicInteger(0);

        public EventLoopGroup() {
            this(0);
        }

        public EventLoopGroup(int threadN) {
            threadN = threadN == 0 ? Runtime.getRuntime().availableProcessors() : threadN;
            eventLoopsGroup = new EventLoop[threadN];
            for (int i = 0; i < threadN; i++) {
                eventLoopsGroup[i] = new EventLoop();
            }
        }

        private EventLoop next() {
            int next = counter.getAndIncrement();
            return eventLoopsGroup[next % eventLoopsGroup.length];
        }

        public void register(SocketChannel channel) throws IOException {
            next().register(channel);
        }

        @Override
        public void close() {
            Arrays.stream(eventLoopsGroup).forEach(EventLoop::close);
        }
    }


    public static void main(String[] args) throws IOException {

        ServerSocketChannel serverSocketChannel = null;
        Selector selector = null;
        EventLoopGroup eventLoopGroup = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            eventLoopGroup = new EventLoopGroup();
            registerShutdownHandler(eventLoopGroup, serverSocketChannel, selector);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 9090));
            logger.info("Server listen at:" + "localhost:" + 9090 + "....");
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    if (socketChannel == null) {
                        continue;
                    }
                    eventLoopGroup.register(socketChannel);
                }
            }
        } finally {
            logger.info("退出！");
            if (eventLoopGroup != null) {
                eventLoopGroup.close();
            }
            if (selector != null) {
                selector.close();
            }
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        }
    }

    private static void registerShutdownHandler(EventLoopGroup eventLoopGroup,
                                                ServerSocketChannel serverSocketChannel,
                                                Selector selector) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            eventLoopGroup.close();
            try {
                serverSocketChannel.close();
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.info("清理完成，退出！");
        }));
    }
}


