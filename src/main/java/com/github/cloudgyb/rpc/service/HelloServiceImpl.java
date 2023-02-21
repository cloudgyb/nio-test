package com.github.cloudgyb.rpc.service;

/**
 * @author geng
 * @since 2023/02/21 20:45:57
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
