package byx.aop.test;

import byx.aop.annotation.Before;
import byx.aop.annotation.Filter;
import byx.aop.exception.ByxAOPException;
import byx.aop.exception.IllegalMethodSignatureException;
import org.junit.jupiter.api.Test;

import static byx.aop.ByxAOP.getAopProxy;
import static org.junit.jupiter.api.Assertions.*;

public class BeforeTest {
    private static boolean flag = false;

    public static class MyException1 extends Exception {}
    public static class MyException2 extends RuntimeException {}

    public static class A {
        public int f1() {
            return 1001;
        }

        public String f2(int i) {
            return "hello " + i;
        }

        public String f3(int i, int j) {
            return i + " " + j;
        }

        public int f4(int i, int j, int k) {
            return i + j + k;
        }

        public String f5(String a) {
            return a + " hello";
        }

        public String f6(String a, String b) {
            return a + " " + b;
        }
    }

    public static class Advice {
        @Before
        @Filter(name = "f1")
        public void g1() {
            flag = true;
        }

        @Before
        @Filter(name = "f2")
        public int[] f2(int i) {
            return new int[]{i + 1};
        }

        @Before
        @Filter(name = "f3")
        public void g3() {
            flag = true;
        }

        @Before
        @Filter(name = "f4")
        public int[] g4(int i, int j, int k) {
            return new int[]{i + 1, j + 1, k + 1};
        }

        @Before
        @Filter(name = "f5")
        public String[] g5(String a) {
            return new String[]{a + a};
        }

        @Before
        @Filter(name = "f6")
        public String[] g6(String a, String b) {
            return new String[]{b, a};
        }
    }

    public static class Advice2 {
        @Before
        @Filter(name = "f2")
        public int g1(int i) {
            return i;
        }
    }

    public static class Advice3 {
        @Before
        @Filter(name = "f3")
        public void g1() throws MyException1 {
            throw new MyException1();
        }

        @Before
        @Filter(name = "f4")
        public int[] g2(int i, int j, int k) {
            throw new MyException2();
        }
    }

    public static class Advice4 {
        @Before
        @Filter(name = "f2")
        public void beforeF2(int n) {
            System.out.println("before f2");
            System.out.println("n = " + n);
            assertEquals(100, n);
        }
    }

    @Test
    public void test1() {
        A a = getAopProxy(new A(), new Advice());

        flag = false;
        assertEquals(1001, a.f1());
        assertTrue(flag);

        assertEquals("hello 11", a.f2(10));

        flag = false;
        assertEquals("100 200", a.f3(100, 200));
        assertTrue(flag);

        assertEquals(15, a.f4(3, 4, 5));

        assertEquals("abcabc hello", a.f5("abc"));

        assertEquals("def abc", a.f6("abc", "def"));
    }

    @Test
    public void test2() {
        assertThrows(IllegalMethodSignatureException.class, () -> getAopProxy(new A(), new Advice2()));
    }

    @Test
    public void test3() {
        A a = getAopProxy(new A(), new Advice3());

        assertThrows(ByxAOPException.class, () -> a.f3(100, 200));

        assertThrows(MyException2.class, () -> a.f4(3, 4, 5));
    }

    @Test
    public void test4() {
        A a = getAopProxy(new A(), new Advice4());
        a.f2(100);
    }
}
