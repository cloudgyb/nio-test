package com.github.cloudgyb.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author cloudgyb
 * @since 2025/3/15 16:53
 */
public class ByteBufferTest {
    /*public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8);
        printBuffer(byteBuffer);
        byteBuffer.putInt(1);
        printBuffer(byteBuffer);
        byteBuffer.flip();
        printBuffer(byteBuffer);
        int anInt = byteBuffer.getInt();
        System.out.println(anInt);
        printBuffer(byteBuffer);
        byteBuffer.clear();
        printBuffer(byteBuffer);
        byteBuffer.putInt(2);
        printBuffer(byteBuffer);
        byteBuffer.putInt(2);
        printBuffer(byteBuffer);
        byteBuffer.putInt(2);
        printBuffer(byteBuffer);
    }*/

    private static void printBuffer(ByteBuffer byteBuffer) {
        System.out.println("position:" + byteBuffer.position() + ",limit:" + byteBuffer.limit() + ",capacity:" + byteBuffer.capacity());
    }

    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048 * 100);
        ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(true, outputStream);
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world11111111111111111111".getBytes());
        chunkedOutputStream.write("hello world22222222222222222222222".getBytes());
        chunkedOutputStream.write("hello world44444444444444444".getBytes());
        chunkedOutputStream.write("hello world33333333333333333333333".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        chunkedOutputStream.write("hello world".getBytes());
        byte[] bytes = Files.readAllBytes(Path.of("C:\\Users\\Administrator\\Desktop\\1111.md"));
        chunkedOutputStream.write(bytes);
        chunkedOutputStream.close();
        System.out.println(outputStream.toString());
    }
}
