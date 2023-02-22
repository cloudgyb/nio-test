package com.github.cloudgyb.rpc.service;

import java.lang.annotation.*;

/**
 * RPC 服务标记注解
 * 被该注解标记的 Service 实现类将被作为 RPC 服务类
 *
 * @author geng
 * @since 2023/02/22 13:54:06
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RPCService {
}
