package byx.aop.test;

import byx.aop.annotation.After;
import byx.aop.annotation.Filter;
import byx.aop.exception.ByxAOPException;
import byx.aop.exception.IllegalMethodSignatureException;
import org.junit.jupiter.api.Test;

import static byx.aop.ByxAOP.getAopProxy;
import static org.junit.jupiter.api.Assertions.*;

public class AfterTest {
    private static boolean flag = false;

    public static class MyException1 extends Exception {}
    public static class MyException2 extends RuntimeException {}

    public static class A {
        public int f1() {
            return 1001;
        }

        public String f2(int a) {
            return String.valueOf(a + 1);
        }

        public int f3(int a) {
            return a + 1;
        }

        public void f4() {

        }
    }

    public static class Advice {
        @After
        @Filter(name = "f1")
        public int g1(int retVal) {
            assertEquals(1001, retVal);
            return 2002;
        }

        @After
        @Filter(name = "f2")
        public String g2(String retVal) {
            return retVal + " hello";
        }

        @After
        @Filter(name = "f3")
        public void g3() {
            flag = true;
        }

        @After
        @Filter(name = "f4")
        public void g4() {
            flag = true;
        }
    }

    public static class Advice2 {
        @After
        @Filter(name = "f1")
        public void g(int a, String b) {

        }
    }

    public static class Advice3 {
        @After
        @Filter(name = "f1")
        public void g1(int a) throws MyException1 {
            throw new MyException1();
        }

        @After
        @Filter(name = "f3")
        public void g2() {
            throw new MyException2();
        }
    }

    @Test
    public void test1() {
        A a = getAopProxy(new A(), new Advice());

        assertEquals(2002, a.f1());

        assertEquals("14 hello", a.f2(13));

        flag = false;
        assertEquals(5, a.f3(4));
        assertTrue(flag);

        flag = false;
        a.f4();
        assertTrue(flag);
    }

    @Test
    public void test2() {
        assertThrows(IllegalMethodSignatureException.class, () -> getAopProxy(new A(), new Advice2()));
    }

    @Test
    public void test3() {
        A a = getAopProxy(new A(), new Advice3());

        assertThrows(ByxAOPException.class, () -> a.f1());

        assertThrows(MyException2.class, () -> a.f3(123));
    }
}
