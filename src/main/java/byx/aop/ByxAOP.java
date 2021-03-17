package byx.aop;

import byx.aop.annotation.*;
import byx.aop.exception.ByxAOPException;
import byx.aop.exception.IllegalMethodSignatureException;
import byx.util.proxy.ProxyUtils;
import byx.util.proxy.core.MethodInterceptor;
import byx.util.proxy.core.MethodMatcher;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import static byx.util.proxy.core.MethodInterceptor.*;
import static byx.util.proxy.core.MethodMatcher.*;

/**
 * AOP工具类
 *
 * @author byx
 */
public class ByxAOP {
    /**
     * 存放一个增强方法的定义
     */
    private static class MethodInterceptorDefinition {
        private final Object advice;
        private final Method method;
        private final int order;

        private MethodInterceptorDefinition(Object advice, Method method) {
            this.advice = advice;
            this.method = method;
            if (method.isAnnotationPresent(Order.class)) {
                this.order = method.getAnnotation(Order.class).value();
            } else {
                this.order = 1;
            }
        }

        public int getOrder() {
            return order;
        }

        public MethodInterceptor build() {
            return getMethodInterceptor().when(getMethodMatcher());
        }

        private MethodInterceptor getMethodInterceptor() {
            if (method.isAnnotationPresent(Before.class)) {
                return processBefore();
            } else if (method.isAnnotationPresent(After.class)) {
                return processAfter();
            } else if (method.isAnnotationPresent(Around.class)) {
                return processAround();
            } else if (method.isAnnotationPresent(Replace.class)) {
                return processReplace();
            } else if (method.isAnnotationPresent(AfterThrowing.class)) {
                return processAfterThrowing();
            } else {
                return invokeTargetMethod();
            }
        }

        private MethodMatcher getMethodMatcher() {
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
            return matcher;
        }

        /**
         * 调用增强对象的方法
         * 增强对象的方法不允许抛出受检异常
         * 如果增强对象的方法抛出RuntimeException，则直接向外抛出
         */
        private Object callAdviceMethod(Object[] params) {
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
         * 解析Before注解
         */
        private MethodInterceptor processBefore() {
            if (method.getParameterCount() > 0) {
                if (!method.getReturnType().isArray()) {
                    throw new IllegalMethodSignatureException(method, Before.class);
                }
                return interceptParameters(params -> {
                    // 避免基本类型数组转换时的坑
                    Object ret = callAdviceMethod(params);
                    int len = Array.getLength(ret);
                    Object[] arr = new Object[len];
                    for (int i = 0; i < len; ++i) {
                        arr[i] = Array.get(ret, i);
                    }
                    return arr;
                });
            } else {
                return interceptParameters(params -> {
                    callAdviceMethod(new Object[]{});
                    return params;
                });
            }
        }

        /**
         * 解析After注解
         */
        private MethodInterceptor processAfter() {
            if (method.getParameterCount() == 0) {
                return interceptReturnValue(returnValue -> {
                    callAdviceMethod(new Object[]{});
                    return returnValue;
                });
            } else if (method.getParameterCount() == 1) {
                return interceptReturnValue(returnValue -> {
                    return callAdviceMethod(new Object[]{returnValue});
                });
            } else {
                throw new IllegalMethodSignatureException(method, After.class);
            }
        }

        /**
         * 解析Around注解
         */
        private MethodInterceptor processAround() {
            return targetMethod -> {
                return callAdviceMethod(new Object[]{targetMethod});
            };
        }

        /**
         * 解析Replace注解
         */
        private MethodInterceptor processReplace() {
            return targetMethod -> {
                return callAdviceMethod(targetMethod.getParams());
            };
        }

        private MethodInterceptor processAfterThrowing() {
            return interceptException(t -> {
                return callAdviceMethod(new Object[]{t});
            });
        }
    }

    /**
     * 获取AOP代理对象
     * @param target 目标对象
     * @param advice 目标对象增强对象
     * @param <T> 返回类型
     * @return 已增强的对象
     */
    public static <T> T getAopProxy(T target, Object advice) {
        MethodInterceptor interceptor = Arrays
                .stream(advice.getClass().getDeclaredMethods())
                .map(m -> new MethodInterceptorDefinition(advice, m))
                .sorted(Comparator.comparingInt(MethodInterceptorDefinition::getOrder))
                .map(MethodInterceptorDefinition::build)
                .reduce(invokeTargetMethod(), MethodInterceptor::then);
        return ProxyUtils.proxy(target, interceptor);
    }
}
