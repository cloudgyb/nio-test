package com.github.cloudgyb.http2;

public class Http2ServerApplication {
    public static void main(String[] args) {
        Http2Server http2Server = new Http2Server("localhost", 8080);
        http2Server.start();
        System.out.println("Main 线程退出！");
    }
}
