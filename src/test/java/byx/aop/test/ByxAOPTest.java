package byx.aop.test;

import byx.aop.annotation.After;
import byx.aop.annotation.Around;
import byx.aop.annotation.Before;
import byx.aop.annotation.WithName;
import byx.aop.exception.ByxAOPException;
import byx.util.proxy.core.TargetMethod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static byx.aop.ByxAOP.*;

public class ByxAOPTest {
    public static class IdLessThanZeroException extends RuntimeException {
        public IdLessThanZeroException() {
            super("id小于0");
        }
    }

    public static class StringEmptyException extends RuntimeException {
        public StringEmptyException(String name) {
            super(name + "为空");
        }
    }

    public static class MyException extends RuntimeException {

    }

    public static class User {
        private int id;
        private String username;
        private String password;
        private String nickname;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    public static class UserAdvice {
        @Before
        @WithName("setId")
        public void checkId(int id) {
            if (id < 0) {
                throw new IdLessThanZeroException();
            }
        }

        @Before
        @WithName("setUsername")
        public void checkUsername(String username) throws Exception {
            if (username == null || username.isEmpty()) {
                throw new Exception();
            }
        }

        @Before
        @WithName("setPassword")
        public String[] changePassword(String password) {
            if (password == null || password.isEmpty()) {
                throw new StringEmptyException("password");
            }
            return new String[]{"*".repeat(password.length())};
        }

        @Before
        @WithName("setNickname")
        public String[] checkNickname(String nickname) throws Exception {
            if (nickname == null || nickname.isEmpty()) {
                throw new Exception();
            }
            return new String[]{nickname};
        }

        @After
        @WithName("getId")
        public void afterGetId(int id) {
            if (id == 1002) {
                throw new MyException();
            }
        }

        @After
        @WithName("getUsername")
        public String afterGetUsername(String username) {
            if ("abcd".equals(username)) {
                return "hhhh";
            }
            return username;
        }
    }

    public static class UserAdvice2 {
        @Before
        @WithName("setId")
        public int checkId(int id) {
            return id;
        }
    }

    public interface UserService {
        String login(String username, String password);
        String register(String username, String password);
        String list(String username, String password);
        String get(String username, String password);
        String insert(String username, String password);
    }

    public static class UserServiceImpl implements UserService {
        @Override
        public String login(String username, String password) {
            return username + " " + password;
        }

        @Override
        public String register(String username, String password) {
            return username + " " + password;
        }

        @Override
        public String list(String username, String password) {
            return username + " " + password;
        }

        @Override
        public String get(String username, String password) {
            return username + " " + password;
        }

        @Override
        public String insert(String username, String password) {
            return username + " " + password;
        }
    }

    public static class UserServiceAdvice {
        @Before
        @WithName("login")
        public void checkLogin(String username, String password) {
            if (username == null || username.isEmpty()) {
                throw new StringEmptyException("username");
            }
            if (password == null || password.isEmpty()) {
                throw new StringEmptyException("password");
            }
        }

        @Before
        @WithName("register")
        public String[] changeRegister(String username, String password) {
            return new String[]{"ccc", "789"};
        }

        @Around
        @WithName("list")
        public String aroundList(TargetMethod targetMethod) {
            Object[] params = targetMethod.getParams();
            String ret = (String) targetMethod.invoke(params[0] + "x", params[1] + "0");
            ret += " ok";
            return ret;
        }

        @Around
        @WithName("get")
        public String aroundGet(TargetMethod targetMethod) {
            Object[] params = targetMethod.getParams();
            if (params[0] == null || params[1] == null) {
                throw new MyException();
            }
            return targetMethod.invokeWithOriginalParams() + " get";
        }

        @Around
        @WithName("insert")
        public String aroundInsert(TargetMethod targetMethod) throws Exception {
            if (targetMethod.getParams()[0] == null) {
                throw new Exception();
            }
            return targetMethod.invokeWithOriginalParams() + " insert";
        }
    }

    @Test
    public void testBefore() {
        User user = getAopProxy(new User(), new UserAdvice());

        user.setId(333);
        assertEquals(333, user.getId());
        assertThrows(IdLessThanZeroException.class, () -> user.setId(-5));
        assertEquals(333, user.getId());

        user.setUsername("abc");
        assertEquals("abc", user.getUsername());
        assertThrows(ByxAOPException.class, () -> user.setUsername(null));
        assertThrows(ByxAOPException.class, () -> user.setUsername(""));
        assertEquals("abc", user.getUsername());

        user.setPassword("123456");
        assertEquals("******", user.getPassword());
        assertThrows(StringEmptyException.class, () -> user.setPassword(null));
        assertThrows(StringEmptyException.class, () -> user.setPassword(""));
        assertEquals("******", user.getPassword());

        user.setNickname("byx");
        assertEquals("byx", user.getNickname());
        assertThrows(ByxAOPException.class, () -> user.setNickname(null));
        assertThrows(ByxAOPException.class, () -> user.setNickname(""));
        assertEquals("byx", user.getNickname());

        UserService userService = getAopProxy(new UserServiceImpl(), new UserServiceAdvice());

        assertEquals("aaa 123", userService.login("aaa", "123"));
        assertThrows(StringEmptyException.class, () -> userService.login("", "123"));
        assertThrows(StringEmptyException.class, () -> userService.login("aaa", null));

        assertEquals("ccc 789", userService.register("bbb", "456"));

        assertThrows(ByxAOPException.class, () -> getAopProxy(new User(), new UserAdvice2()));
    }

    @Test
    public void testAfter() {
        User user = getAopProxy(new User(), new UserAdvice());

        user.setId(1001);
        assertEquals(1001, user.getId());
        user.setId(1002);
        assertThrows(MyException.class, () -> user.getId());

        user.setUsername("aaa");
        assertEquals("aaa", user.getUsername());
        user.setUsername("abcd");
        assertEquals("hhhh", user.getUsername());
    }

    @Test
    public void testAround() {
        UserService userService = getAopProxy(new UserServiceImpl(), new UserServiceAdvice());

        assertEquals("aaax 1230 ok", userService.list("aaa", "123"));
        assertEquals("abc 123 get", userService.get("abc", "123"));
        assertThrows(MyException.class, () -> userService.get(null, "123"));
        assertThrows(MyException.class, () -> userService.get("abc", null));
        assertEquals("xxx yyy insert", userService.insert("xxx", "yyy"));
        assertThrows(ByxAOPException.class, () -> userService.insert(null, "yyy"));
    }
}