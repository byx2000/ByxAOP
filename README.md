# ByxAOP——简易AOP框架

ByxAOP是一个简易AOP框架，实现了前置增强、后置增强、环绕增强等功能，支持注解方式使用。

## 使用示例

下面通过一个简单的例子来快速了解ByxAOP的使用。

`UserService`接口：

```java
public interface UserService {
    boolean login(String username, String password);
}
```

`UserServiceImpl`实现类：

```java
public class UserServiceImpl implements UserService {
    @Override
    public boolean login(String username, String password) {
        System.out.println("in login");
        return true;
    }
}
```

`UserServiceAdvice`增强类：

```java
public class UserServiceAdvice {
    @Around
    @WithName("login")
    public boolean aroundLogin(TargetMethod targetMethod) {
        System.out.println("before login");
        System.out.println("params: " + Arrays.toString(targetMethod.getParams()));
        boolean ret = (boolean) targetMethod.invokeWithOriginalParams();
        System.out.println("after login");
        System.out.println("return value: " + ret);
        return ret;
    }
}
```

主函数：

```java
public static void main(String[] args) {
    UserService userService = ByxAOP.getAopProxy(new UserServiceImpl(), new UserServiceAdvice());
    userService.login("aaa", "123");
}
```

控制台输出结果：

```
before login
params: [aaa, 123]
in login
after login
return value: true
```

从输出结果可以看到，`UserServiceImpl`的`login`方法被增强了。