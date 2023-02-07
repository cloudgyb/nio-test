package org.example;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

/**
 * 多线程的阻塞服务器实现
 * 主线程负责处理请求的连接，使用线程池处理请求的读写。
 * 弊端：对于 tcp 长连接一个线程负责一个连接，当 io 阻塞时线程被阻塞，该连接未断开之前无法处理其他请求，很难支持并发连接数很高的场景。
 *
 * @author geng
 * @since 2023/2/7 10:49
 */
public class BioMultiThreadServer {
    static class RequestHandler extends Thread implements Closeable {
        private final SocketChannel socketChannel;
        private SocketAddress remoteAddress = null;

        public RequestHandler(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
            try {
                this.remoteAddress = socketChannel.getRemoteAddress();
            } catch (IOException ignore) {
            }
            System.out.printf("客户端（%s）连接已建立！%n", remoteAddress != null ? remoteAddress : "未知");
        }

        @Override
        public void run() {
            ByteBuffer buff = ByteBuffer.allocate(10);
            ByteBuffer buff1 = ByteBuffer.allocate(5);
            long read;
            try {
                while ((read = socketChannel.read(new ByteBuffer[]{buff, buff1})) != -1) {
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
                    System.out.printf("读取到客户端（%s）数据：" + sb + "%n", remoteAddress);
                    socketChannel.write(ByteBuffer.wrap(("回显：" + sb).getBytes(StandardCharsets.UTF_8)));
                }
                //关闭连接
                System.out.printf("客户端（%s）关闭连接！%n", remoteAddress);
                socketChannel.close();
            } catch (IOException e) {
                try {
                    System.out.printf("客户端（%s）读取抛出异常！%s %n", remoteAddress, e.getMessage());
                    socketChannel.close();
                } catch (IOException ignore) {
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (socketChannel != null) {
                socketChannel.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = new ThreadPoolExecutor(5, 10,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        registerShutdownHandler(threadPool, serverSocketChannel);
        try (serverSocketChannel) {
            serverSocketChannel.bind(new InetSocketAddress("localhost", 9090));
            System.out.println("Server listen at:" + "localhost:" + 9090 + "....");
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null)
                    continue;
                threadPool.submit(new BioMultiThreadServer.RequestHandler(socketChannel));
            }
        } finally {
            System.out.println("退出！");
            threadPool.shutdownNow();
        }
    }

    private static void registerShutdownHandler(ExecutorService threadPool, ServerSocketChannel serverSocketChannel) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            threadPool.shutdownNow();
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("清理完成，退出！");
        }));
    }
}


