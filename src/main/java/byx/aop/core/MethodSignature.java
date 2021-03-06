package byx.aop.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 方法签名
 */
public interface MethodSignature
{
    /**
     * 获取方法名
     */
    String getName();

    /**
     * 获取返回值类型
     */
    Class<?> getReturnType();

    /**
     * 获取参数类型
     */
    Class<?>[] getParameterTypes();

    /**
     * 获取方法上的指定注解
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * 获取方法上的所有注解
     */
    Annotation[] getAnnotations();

    /**
     * 方法是否被某个注解标注
     */
    <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass);

    /**
     * 获取方法参数上的注解
     */
    Annotation[][] getParameterAnnotations();

    /**
     * 是否为public方法
     */
    boolean isPublic();

    /**
     * 是否为private方法
     */
    boolean isPrivate();

    /**
     * 是否为protected方法
     */
    boolean isProtected();

    /**
     * 创建MethodSignature
     */
    static MethodSignature of(Method method)
    {
        return new MethodSignature()
        {
            @Override
            public String getName()
            {
                return method.getName();
            }

            @Override
            public Class<?> getReturnType()
            {
                return method.getReturnType();
            }

            @Override
            public Class<?>[] getParameterTypes()
            {
                return method.getParameterTypes();
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return method.getAnnotation(annotationClass);
            }

            @Override
            public Annotation[] getAnnotations()
            {
                return method.getAnnotations();
            }

            @Override
            public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass)
            {
                return method.isAnnotationPresent(annotationClass);
            }

            @Override
            public Annotation[][] getParameterAnnotations()
            {
                return method.getParameterAnnotations();
            }

            @Override
            public boolean isPublic()
            {
                return Modifier.isPublic(method.getModifiers());
            }

            @Override
            public boolean isPrivate()
            {
                return Modifier.isPrivate(method.getModifiers());
            }

            @Override
            public boolean isProtected()
            {
                return Modifier.isProtected(method.getModifiers());
            }
        };
    }
}
