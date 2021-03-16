package byx.aop.test;

import byx.aop.annotation.After;
import byx.aop.annotation.WithName;
import byx.aop.exception.ByxAOPException;
import org.junit.jupiter.api.Test;

import static byx.aop.ByxAOP.getAopProxy;
import static org.junit.jupiter.api.Assertions.*;

public class AfterTest {
    private static boolean flag = false;

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
        @WithName("f1")
        public int g1(int retVal) {
            assertEquals(1001, retVal);
            return 2002;
        }

        @After
        @WithName("f2")
        public String g2(String retVal) {
            return retVal + " hello";
        }

        @After
        @WithName("f3")
        public void g3() {
            flag = true;
        }

        @After
        @WithName("f4")
        public void g4() {
            flag = true;
        }
    }

    public static class Advice2 {
        @After
        @WithName("f1")
        public void g(int a, String b) {

        }
    }

    @Test
    public void test() {
        A a = getAopProxy(new A(), new Advice());

        assertEquals(2002, a.f1());

        assertEquals("14 hello", a.f2(13));

        flag = false;
        assertEquals(5, a.f3(4));
        assertTrue(flag);

        flag = false;
        a.f4();
        assertTrue(flag);

        assertThrows(ByxAOPException.class, () -> getAopProxy(new A(), new Advice2()));
    }
}
