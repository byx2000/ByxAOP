package byx.aop;

import byx.aop.annotation.*;
import byx.aop.exception.ByxAOPException;
import byx.util.proxy.ProxyUtils;
import byx.util.proxy.core.MethodInterceptor;
import byx.util.proxy.core.MethodMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static byx.util.proxy.core.MethodInterceptor.*;
import static byx.util.proxy.core.MethodMatcher.*;

/**
 * AOP工具类
 *
 * @author byx
 */
public class ByxAOP {
    /**
     * 获取AOP代理对象
     * @param target 目标对象
     * @param advice 目标对象增强对象
     * @param <T> 返回类型
     * @return 已增强的对象
     */
    public static <T> T getAopProxy(T target, Object advice) {
        Class<?> adviceClass = advice.getClass();
        MethodInterceptor interceptor = null;

        for (Method method : adviceClass.getDeclaredMethods()) {
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

            if (temp == null) {
                continue;
            }

            MethodMatcher matcher = all();

            if (method.isAnnotationPresent(WithName.class)) {
                matcher = matcher.and(withName(method.getAnnotation(WithName.class).value()));
            }
            if (method.isAnnotationPresent(WithPattern.class)) {
                matcher = matcher.and(withPattern(method.getAnnotation(WithPattern.class).value()));
            }
            if (method.isAnnotationPresent(WithReturnType.class)) {
                matcher = matcher.and(withReturnType(method.getAnnotation(WithReturnType.class).value()));
            }
            if (method.isAnnotationPresent(WithParameterTypes.class)) {
                matcher = matcher.and(withParameterTypes(method.getAnnotation(WithParameterTypes.class).value()));
            }

            temp = temp.when(matcher);

            if (interceptor == null) {
                interceptor = temp;
            } else {
                interceptor = interceptor.then(temp);
            }
        }
        return interceptor == null ? target : ProxyUtils.proxy(target, interceptor);
    }

    /**
     * 调用增强对象的方法
     * 增强对象的方法不允许抛出受检异常
     * 如果增强对象的方法抛出RuntimeException，则直接向外抛出
     */
    private static Object callAdviceMethod(Method method, Object advice, Object[] params) {
        try {
            method.setAccessible(true);
            return method.invoke(advice, params);
        } catch (IllegalAccessException e) {
            throw new ByxAOPException("无法调用方法：" + method, e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else {
                throw new ByxAOPException("增强方法不能抛出受检异常：" + method);
            }
        }
    }

    /**
     * 解析@Before注解
     */
    private static MethodInterceptor processBefore(Method method, Object advice) {
        if (method.getReturnType() == void.class) {
            return interceptParameters(params -> {
                callAdviceMethod(method, advice, params);
                return params;
            });
        } else if (method.getReturnType().isArray()) {
            return interceptParameters(params -> {
                return (Object[]) callAdviceMethod(method, advice, params);
            });
        } else {
            throw new ByxAOPException("被@Before注解的方法要么无返回值，要么返回数组：" + method);
        }
    }

    /**
     * 解析@After注解
     */
    private static MethodInterceptor processAfter(Method method, Object advice) {
        if (method.getReturnType() == void.class) {
            return interceptReturnValue(returnValue -> {
                callAdviceMethod(method, advice, new Object[]{returnValue});
                return returnValue;
            });
        } else {
            return interceptReturnValue(returnValue -> {
                return callAdviceMethod(method, advice, new Object[]{returnValue});
            });
        }
    }

    /**
     * 解析@Around注解
     */
    private static MethodInterceptor processAround(Method method, Object advice) {
        return targetMethod -> {
            return callAdviceMethod(method, advice, new Object[]{targetMethod});
        };
    }

    /**
     * 解析@Replace注解
     */
    private static MethodInterceptor processReplace(Method method, Object advice) {
        return targetMethod -> {
            return callAdviceMethod(method, advice, targetMethod.getParams());
        };
    }
}
