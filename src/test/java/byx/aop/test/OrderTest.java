package byx.aop.test;

import byx.aop.annotation.Around;
import byx.aop.annotation.Filter;
import byx.aop.annotation.Order;
import byx.util.proxy.core.TargetMethod;
import org.junit.jupiter.api.Test;

import static byx.aop.ByxAOP.getAopProxy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {
    public interface A {
        String f(String s);
    }

    public static class AImpl implements A {
        @Override
        public String f(String s) {
            return "f: " + s;
        }
    }

    public static class Advice {
        @Around
        @Filter(name = "f")
        @Order(1)
        public String apple(TargetMethod targetMethod) {
            System.out.println("before 1");
            Object ret = targetMethod.invokeWithOriginalParams();
            System.out.println("after 1");
            return ret + " 1";
        }

        @Around
        @Filter(name = "f")
        @Order(2)
        public String cat(TargetMethod targetMethod) {
            System.out.println("before 2");
            Object ret = targetMethod.invokeWithOriginalParams();
            System.out.println("after 2");
            return ret + " 2";
        }

        @Around
        @Filter(name = "f")
        @Order(3)
        public String banana(TargetMethod targetMethod) {
            System.out.println("before 3");
            Object ret = targetMethod.invokeWithOriginalParams();
            System.out.println("after 3");
            return ret + " 3";
        }

        public int add(int a, int b) {
            return a + b;
        }
    }

    @Test
    public void test() {
        A a = getAopProxy(new AImpl(), new Advice());
        assertEquals("f: hello 1 2 3", a.f("hello"));
    }
}
