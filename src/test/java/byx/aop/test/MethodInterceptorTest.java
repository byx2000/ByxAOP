package byx.aop.test;

import byx.aop.core.*;
import byx.aop.exception.TargetMethodException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static byx.aop.AOP.*;
import static byx.aop.core.MethodInterceptor.*;
import static byx.aop.core.MethodMatcher.*;

public class MethodInterceptorTest
{
    public interface A
    {
        void f1();
        void f2(int i, String s);
        int f2();
        String f3(int i, String s);
    }

    public static class AImpl implements A
    {
        @Override
        public void f1()
        {

        }

        @Override
        public void f2(int i, String s)
        {

        }

        @Override
        public int f2()
        {
            return 123;
        }

        @Override
        public String f3(int i, String s)
        {
            return s + " " + i;
        }
    }

    @Test
    public void testInterceptor()
    {
        boolean[] flag = new boolean[]{ false };
        MethodInterceptor interceptor = (signature, targetMethod, params) ->
        {
            flag[0] = true;
            return targetMethod.invoke(params);
        };
        A a = proxy(new AImpl(), interceptor);

        a.f1();
        assertTrue(flag[0]);

        flag[0] = false;
        a.f2(123, "hello");
        assertTrue(flag[0]);

        flag[0] = false;
        a.f2();
        assertTrue(flag[0]);

        flag[0] = false;
        a.f3(123, "hello");
        assertTrue(flag[0]);
    }

    @Test
    public void testWhen()
    {
        boolean[] flag = new boolean[]{ false };
        MethodInterceptor interceptor = (signature, targetMethod, params) ->
        {
            flag[0] = true;
            return targetMethod.invoke(params);
        };
        MethodMatcher matcher = withName("f2");
        A a = proxy(new AImpl(), interceptor.when(matcher));

        a.f1();
        assertFalse(flag[0]);

        a.f3(123, "hello");
        assertFalse(flag[0]);

        a.f2();
        assertTrue(flag[0]);

        flag[0] = false;
        a.f2(123, "hello");
        assertTrue(flag[0]);
    }

    @Test
    public void testThen()
    {
        String[] s = new String[]{ "" };
        MethodInterceptor interceptor1 = (signature, targetMethod, params) ->
        {
            s[0] += "1b";
            Object ret = targetMethod.invoke(params);
            s[0] += "1e";
            return ret;
        };
        MethodInterceptor interceptor2 = (signature, targetMethod, params) ->
        {
            s[0] += "2b";
            Object ret = targetMethod.invoke(params);
            s[0] += "2e";
            return ret;
        };
        A a = proxy(new AImpl(), interceptor1.then(interceptor2));

        a.f1();
        assertEquals("2b1b1e2e", s[0]);
    }

    @Test
    public void testWhenAndThen()
    {
        String[] s = new String[]{ "", "" };
        MethodInterceptor interceptor1 = (signature, targetMethod, params) ->
        {
            s[0] += "a";
            return targetMethod.invoke(params);
        };
        MethodMatcher matcher1 = withName("f1");
        MethodInterceptor interceptor2 = (signature, targetMethod, params) ->
        {
            s[1] += "b";
            return targetMethod.invoke(params);
        };
        MethodMatcher matcher2 = withName("f3");
        A a = proxy(new AImpl(), interceptor1.when(matcher1).then(interceptor2.when(matcher2)));

        a.f2();
        assertEquals("", s[0]);
        assertEquals("", s[1]);

        a.f2(123, "hello");
        assertEquals("", s[0]);
        assertEquals("", s[1]);

        a.f1();
        assertEquals("a", s[0]);
        assertEquals("", s[1]);

        a.f3(123, "hello");
        assertEquals("a", s[0]);
        assertEquals("b", s[1]);
    }

    @Test
    public void testInterceptParameters()
    {
        boolean[] flag = new boolean[]{ false };
        ParametersInterceptor interceptor = (signature, params) ->
        {
            flag[0] = true;
            assertEquals(params.length, 2);
            assertTrue(params[0] instanceof Integer);
            assertTrue(params[1] instanceof String);
            assertEquals(123, params[0]);
            assertEquals("hi", params[1]);
            return new Object[]{ 456, "abc" };
        };
        A a = proxy(new AImpl(), interceptParameters(interceptor).when(withName("f3")));

        String ret = a.f3(123, "hi");
        assertTrue(flag[0]);
        assertEquals("abc 456", ret);
    }

    @Test
    public void testInterceptReturnValue()
    {
        boolean[] flag = new boolean[]{ false };
        ReturnValueInterceptor interceptor = (signature, returnValue) ->
        {
            flag[0] = true;
            assertTrue(returnValue instanceof Integer);
            assertEquals(123, returnValue);
            return 456;
        };
        A a = proxy(new AImpl(), interceptReturnValue(interceptor).when(withName("f2").andReturnType(int.class)));

        int ret = a.f2();
        assertTrue(flag[0]);
        assertEquals(456, ret);
    }

    @Test
    public void testDelegateToProxy()
    {
        CharSequence s = proxy("hello", delegateTo(new Object()
        {
            public int length()
            {
                return 123;
            }

            public String toString()
            {
                return "hi";
            }
        }));

        assertEquals(123, s.length());
        assertEquals("hi", s.toString());
        assertEquals('e', s.charAt(1));
    }

    @Test
    public void testException1()
    {
        A a = proxy(new AImpl(), delegateTo(new Object()
        {
            public void f1() throws Exception
            {
                throw new Exception("???????????????Exception");
            }

            public String f3(int i, String s)
            {
                throw new RuntimeException("???????????????RuntimeException");
            }
        }));

        assertThrows(TargetMethodException.class, a::f1);
        assertThrows(TargetMethodException.class, () -> a.f3(123, "hello"));
    }

    @Test
    public void testException2()
    {
        CharSequence s = proxy("hello", delegateTo(new Object()
        {
            public int length() throws Exception
            {
                throw new Exception("???????????????Exception");
            }

            public String toString()
            {
                throw new RuntimeException("???????????????RuntimeException");
            }
        }));

        assertThrows(TargetMethodException.class, s::length);
        assertThrows(TargetMethodException.class, s::toString);
    }

    @Test
    public void testException3()
    {
        A a1 = new A()
        {
            @Override
            public void f1()
            {

            }

            @Override
            public void f2(int i, String s)
            {
                throw new RuntimeException("???????????????RuntimeException");
            }

            @Override
            public int f2()
            {
                return 0;
            }

            @Override
            public String f3(int i, String s)
            {
                return null;
            }
        };
        A a2 = proxy(a1, (signature, targetMethod, params) -> targetMethod.invoke(params));

        assertThrows(TargetMethodException.class, () -> a2.f2(123, "hello"));
    }
}
