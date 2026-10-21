package com.library.infrastructure.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class TimingProxy {

    private TimingProxy(){}

    @SuppressWarnings("unchecked")
    public static <T> T wrap(Class<T> type, T target)
    {
        String name = type.getSimpleName();

        InvocationHandler handler = (proxy, method, args) ->{
            long start = System.nanoTime();
            try{
                return method.invoke(target, args);
            } catch (InvocationTargetException e)
            {
                throw e.getCause();
            } finally {
                long micros = (System.nanoTime() - start) / 1_000;
                System.out.printf("   [%s.%s] %.2f ms%n", name, method.getName(), micros / 1000.0);
            }
        };

        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }

}
