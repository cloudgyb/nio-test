package org.example;

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

/**
 * 单线程的非阻塞服务器实现
 * 主线程负责处理请求的连接和 IO 事件的处理。
 * 弊端：请求连接比较多时，使用一个主线程接受连接的效率低，且当IO处理比较费时时，其他请求的 IO 事件将得不到处理
 *
 * @author geng
 * @since 2023/2/7 10:49
 */
public class NioSingleThreadServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        Selector selector = null;
        try (serverSocketChannel) {
            selector = Selector.open();
            registerShutdownHandler(serverSocketChannel, selector);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.bind(new InetSocketAddress("localhost", 9090));
            System.out.println("Server listen at:" + "localhost:" + 9090 + "....");
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buff = ByteBuffer.allocate(10);
                        ByteBuffer buff1 = ByteBuffer.allocate(5);
                        long read = channel.read(new ByteBuffer[]{buff, buff1});
                        SocketAddress remoteAddress = channel.getRemoteAddress();
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
                        Thread.sleep(2000);
                        channel.write(ByteBuffer.wrap(("回显：" + sb).getBytes(StandardCharsets.UTF_8)));
                    }
                    iterator.remove();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("退出！");
            if (selector != null) {
                selector.close();
            }
        }
    }

    private static void registerShutdownHandler(ServerSocketChannel serverSocketChannel,
                                                Selector selector) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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


