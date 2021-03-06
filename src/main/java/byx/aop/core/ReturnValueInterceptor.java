package byx.aop.core;

/**
 * 返回值拦截器
 */
public interface ReturnValueInterceptor
{
    /**
     * 拦截
     * @param signature 方法签名
     * @param returnValue 原始返回值
     * @return 增强后的返回值
     */
    Object intercept(MethodSignature signature, Object returnValue);
}
