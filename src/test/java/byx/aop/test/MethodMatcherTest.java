package byx.aop.test;

import byx.aop.annotation.*;
import byx.util.proxy.core.TargetMethod;
import org.junit.jupiter.api.Test;
import static byx.aop.ByxAOP.*;
import static org.junit.jupiter.api.Assertions.*;

public class MethodMatcherTest {
    public static class A {
        public int f1(String s) {
            return s.length();
        }

        public String f1() {
            return "hello";
        }

        public String f2(int a, int b) {
            return a + " " + b;
        }

        public String f2(int a, String b) {
            return a + " " + b;
        }
    }

    public static class Advice {
        @Before
        @WithName("f1")
        @WithReturnType(int.class)
        @WithParameterTypes(String.class)
        public String[] g1(String s) {
            return new String[]{s + "x"};
        }

        @After
        @WithPattern(".1")
        @WithReturnType(String.class)
        public String g2(String ret) {
            return ret + " hi";
        }

        @Around
        @WithPattern(".*")
        @WithParameterTypes({int.class, int.class})
        @WithReturnType(String.class)
        public String g3(TargetMethod targetMethod) {
            Object[] params = targetMethod.getParams();
            return (String) targetMethod.invoke((int) params[0] + 1, (int) params[1] + 1);
        }

        @Replace
        @WithName("f2")
        @WithReturnType(String.class)
        @WithParameterTypes({int.class, String.class})
        public String g4(int a, String b) {
            return a + " " + b + " g4";
        }
    }

    @Test
    public void test() {
        A a = getAopProxy(new A(), new Advice());

        assertEquals(3, a.f1("hi"));
        assertEquals("hello hi", a.f1());
        assertEquals("4 5", a.f2(3, 4));
        assertEquals("12 abc g4", a.f2(12, "abc"));
    }
}
