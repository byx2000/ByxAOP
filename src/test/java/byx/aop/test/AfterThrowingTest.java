package byx.aop.test;

import byx.aop.annotation.AfterThrowing;
import byx.aop.annotation.WithName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static byx.aop.ByxAOP.*;

public class AfterThrowingTest {
    public static class MyException1 extends Exception {
        public MyException1(String msg) {
            super(msg);
        }
    }
    public static class MyException2 extends RuntimeException {
        public MyException2(String msg) {
            super(msg);
        }
    }

    public static class A {
        public int f1(String s) throws MyException1 {
            if ("hello".equals(s)) {
                throw new MyException1("exception 1");
            }
            return 1001;
        }

        public int f2(String s) {
            if ("hi".equals(s)) {
                throw new MyException2("exception 2");
            }
            return 2002;
        }
    }

    public static class Advice {
        @AfterThrowing
        @WithName("f1")
        public int g1(MyException1 e) {
            assertEquals("exception 1", e.getMessage());
            return 3003;
        }

        @AfterThrowing
        @WithName("f2")
        public int g2(MyException2 e) {
            assertEquals("exception 2", e.getMessage());
            return 4004;
        }
    }

    @Test
    public void test() throws MyException1 {
        A a = getAopProxy(new A(), new Advice());

        assertEquals(1001, a.f1("abc"));
        assertEquals(3003, a.f1("hello"));

        assertEquals(2002, a.f2("def"));
        assertEquals(4004, a.f2("hi"));
    }
}
