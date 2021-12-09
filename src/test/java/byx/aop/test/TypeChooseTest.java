package byx.aop.test;

import byx.aop.ByxAOP;
import byx.aop.annotation.After;
import byx.aop.annotation.Filter;
import byx.util.proxy.ProxyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TypeChooseTest {
    public interface A {
        String f();
    }

    public static class A1 implements A {
        @Override
        public String f() {
            return "A1";
        }
    }

    public static class A2 implements A {
        @Override
        public String f() {
            return "A2";
        }
    }

    public static class Advice {
        @After
        @Filter(name = "f", returnType = String.class)
        public String afterF(String ret) {
            return ret + " afterF";
        }
    }

    @Test
    public void test() {
        A1 a1 = ByxAOP.getAopProxy(new A1(), ProxyType.BYTE_BUDDY, new Advice());
        assertEquals("A1 afterF", a1.f());
        A2 a2 = ByxAOP.getAopProxy(new A2(), ProxyType.BYTE_BUDDY, new Advice());
        assertEquals("A2 afterF", a2.f());
        A a11 = ByxAOP.getAopProxy(new A1(), ProxyType.JDK, new Advice());
        assertEquals("A1 afterF", a11.f());
        A a22 = ByxAOP.getAopProxy(new A2(), ProxyType.JDK, new Advice());
        assertEquals("A2 afterF", a22.f());
    }
}
