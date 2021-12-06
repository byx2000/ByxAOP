package byx.aop.test;

import byx.aop.ByxAOP;
import byx.aop.annotation.Before;
import byx.aop.annotation.Filter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MultiAopTest {
    public static int c1 = 0, c2 = 0;

    public static class A {
        public void f() {
            c1++;
            System.out.println("f");
        }

        public void g() {
            c2++;
            System.out.println("g");
        }
    }

    public static class Advice1 {
        @Before
        @Filter(name = "f")
        public void beforeF() {
            c1++;
            System.out.println("before f");
        }
    }

    public static class Advice2 {
        @Before
        @Filter(name = "g")
        public void beforeG() {
            c2++;
            System.out.println("before g");
        }
    }

    @Test
    public void test() {
        A a = new A();
        a = ByxAOP.getAopProxy(a, new Advice1(), new Advice2());
        a.f();
        a.g();

        assertEquals(2, c1);
        assertEquals(2, c2);
    }
}
