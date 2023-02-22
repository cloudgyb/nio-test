package com.github.cloudgyb.rpc.service;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author geng
 * @since 2023/02/22 11:53:24
 */
public class ServiceFactory {
    private final static Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
    private static final Map<Class<?>, Object> serviceInstances = new HashMap<>();
    private static final String DEFAULT_SCAN_BASE_PACKAGE = ServiceFactory.class.getPackageName();

    @SuppressWarnings("unchecked")
    public static <T> T getServiceInstance(Class<T> tClass) {
        return (T) serviceInstances.get(tClass);
    }

    public static void scanService() {
        scanService(DEFAULT_SCAN_BASE_PACKAGE);
    }

    public static void scanService(String basePackage) {
        logger.info("开始扫描包{}下的 service...", basePackage);
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(RPCService.class);
        services.forEach(s -> {
            Class<?>[] interfaces = s.getInterfaces();
            for (Class<?> i : interfaces) {
                try {
                    Constructor<?> constructor = s.getConstructor();
                    serviceInstances.put(i, constructor.newInstance());
                } catch (NoSuchMethodException ignore) {

                } catch (InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        logger.info("包{}下共扫描到的 {} 个 service...", basePackage, serviceInstances.size());
    }
}
