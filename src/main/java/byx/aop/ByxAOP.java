package byx.aop;

import byx.aop.annotation.After;
import byx.aop.annotation.Around;
import byx.aop.annotation.Before;
import byx.aop.annotation.WithName;
import byx.aop.exception.ByxAOPException;
import byx.util.proxy.ProxyUtils;
import byx.util.proxy.core.MethodInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static byx.util.proxy.core.MethodInterceptor.*;
import static byx.util.proxy.core.MethodMatcher.withName;

public class ByxAOP {
    public static <T> T getAopProxy(T target, Object advice) {
        Class<?> adviceClass = advice.getClass();
        MethodInterceptor interceptor = invokeTargetMethod();
        for (Method method : adviceClass.getDeclaredMethods()) {
            method.setAccessible(true);

            MethodInterceptor temp = null;

            // Before
            if (method.isAnnotationPresent(Before.class)) {
                // 无返回值
                if (method.getReturnType() == void.class) {
                    temp = interceptParameters(params -> {
                        try {
                            method.invoke(advice, params);
                            return params;
                        } catch (IllegalAccessException e) {
                            throw new ByxAOPException("无法调用方法：" + method, e);
                        } catch (InvocationTargetException e) {
                            Throwable targetException = e.getTargetException();
                            if (targetException instanceof RuntimeException) {
                                throw (RuntimeException)targetException;
                            }
                            else {
                                throw new ByxAOPException("增强方法不能抛出受检异常：" + method);
                            }
                        }
                    });
                }
                // 返回值为数组
                else if (method.getReturnType().isArray()) {
                    temp = interceptParameters(params -> {
                        try {
                            return (Object[]) method.invoke(advice, params);
                        } catch (IllegalAccessException e) {
                            throw new ByxAOPException("无法调用方法：" + method, e);
                        } catch (InvocationTargetException e) {
                            Throwable targetException = e.getTargetException();
                            if (targetException instanceof RuntimeException) {
                                throw (RuntimeException)targetException;
                            }
                            else {
                                throw new ByxAOPException("增强方法不能抛出受检异常：" + method);
                            }
                        }
                    });
                }
                // 出错
                else {
                    throw new ByxAOPException("被@Before注解的方法要么无返回值，要么返回数组：" + method);
                }
            }
            else if (method.isAnnotationPresent(After.class)) {
                temp = interceptReturnValue(returnValue -> {
                    try {
                        return method.invoke(advice, returnValue);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            else if (method.isAnnotationPresent(Around.class)) {
                temp = targetMethod -> {
                    try {
                        return method.invoke(advice, targetMethod);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            if (temp != null) {
                if (method.isAnnotationPresent(WithName.class)) {
                    temp = temp.when(withName(method.getAnnotation(WithName.class).value()));
                }

                interceptor = interceptor.then(temp);
            }
        }
        return ProxyUtils.proxy(target, interceptor);
    }
}
