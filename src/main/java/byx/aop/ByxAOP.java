package byx.aop;

import byx.aop.annotation.*;
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

            if (method.isAnnotationPresent(Before.class)) {
                temp = processBefore(method, advice);
            } else if (method.isAnnotationPresent(After.class)) {
                temp = processAfter(method, advice);
            } else if (method.isAnnotationPresent(Around.class)) {
                temp = processAround(method, advice);
            } else if (method.isAnnotationPresent(Replace.class)) {
                temp = processReplace(method, advice);
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

    private static Object callAdviceMethod(Method method, Object advice, Object[] params) {
        try {
            return method.invoke(advice, params);
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
    }

    private static MethodInterceptor processBefore(Method method, Object advice) {
        if (method.getReturnType() == void.class) {
            return interceptParameters(params -> {
                callAdviceMethod(method, advice, params);
                return params;
            });
        }
        else if (method.getReturnType().isArray()) {
            return interceptParameters(params -> {
                return (Object[]) callAdviceMethod(method, advice, params);
            });
        }
        else {
            throw new ByxAOPException("被@Before注解的方法要么无返回值，要么返回数组：" + method);
        }
    }

    private static MethodInterceptor processAfter(Method method, Object advice) {
        if (method.getReturnType() == void.class) {
            return interceptReturnValue(returnValue -> {
                callAdviceMethod(method, advice, new Object[]{returnValue});
                return returnValue;
            });
        }
        else {
            return interceptReturnValue(returnValue -> {
                return callAdviceMethod(method, advice, new Object[]{returnValue});
            });
        }
    }

    private static MethodInterceptor processAround(Method method, Object advice) {
        return targetMethod -> {
            return callAdviceMethod(method, advice, new Object[]{targetMethod});
        };
    }

    private static MethodInterceptor processReplace(Method method, Object advice) {
        return targetMethod -> {
            return callAdviceMethod(method, advice, targetMethod.getParams());
        };
    }
}
