package com.github.cloudgyb.http2;

public class Http2ClientApplication {
    public static void main(String[] args) {
        Http2Client http2 = new Http2Client("localhost", 8080);
        http2.send();
    }
}
