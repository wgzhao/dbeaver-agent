package com.dbeaver.agent.interceptor;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 通用拦截器，可通过构造参数指定返回值。
 * 用于替代返回固定值的各类拦截器。
 */
public class GenericInterceptor {
    private final Object returnValue;

    public GenericInterceptor(Object returnValue) {
        this.returnValue = returnValue;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @SuperCall Callable<?> callable) {
        return returnValue;
    }
}

