package byx.aop.test;

import byx.aop.annotation.Before;
import byx.aop.annotation.WithName;
import byx.aop.exception.ByxAOPException;
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
    }

    public interface UserService {
        String login(String username, String password);
        String register(String username, String password);
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
    }
}
