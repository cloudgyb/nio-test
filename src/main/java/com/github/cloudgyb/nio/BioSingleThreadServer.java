package com.github.cloudgyb.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 单线程的服务器是实现
 * 接受 tcp 连接和处理 io 请求均在主线程中进行。
 * 弊端：当 tcp 连接未断开之前，无法接受新的连接请求也无法处理新的请求。
 *
 * @author geng
 * @since 2023/2/7 10:49
 */
public class BioSingleThreadServer {
    public static void main(String[] args) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(10);
        ByteBuffer buff1 = ByteBuffer.allocate(5);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        try (serverSocketChannel) {
            serverSocketChannel.bind(new InetSocketAddress("localhost", 9090));
            System.out.println("Server listen at:" + "localhost:" + 9090 + "....");
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null)
                    continue;
                long read;
                while ((read = socketChannel.read(new ByteBuffer[]{buff, buff1})) != -1) {
                    System.out.println("读取到" + read + "个字节！");
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
                    System.out.println("接收到数据：" + sb);
                }
                //关闭连接
                System.out.println("关闭连接：" + socketChannel.getRemoteAddress());
                socketChannel.close();
            }
        } finally {
            System.out.println("退出！");
        }
    }
}
