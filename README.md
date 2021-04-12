# ByxAOP——简易AOP框架

ByxAOP是一个简易AOP框架，基于JDK动态代理和CGLIB动态代理，支持前置增强、后置增强、环绕增强、异常增强四种增强类型。

## Maven引入

```xml
<repositories>
     <repository>
         <id>byx-maven-repo</id>
         <name>byx-maven-repo</name>
         <url>https://gitee.com/byx2000/maven-repo/raw/master/</url>
     </repository>
 </repositories>

<dependencies>
   <dependency>
      <groupId>byx.aop</groupId>
      <artifactId>byx-aop</artifactId>
      <version>1.0.0</version>
   </dependency>
</dependencies>
```

## API文档

[API文档](http://byx2000.gitee.io/javadoc/ByxAOP-1.0.0-javadoc/)

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

## 创建AOP代理类

使用`ByxAOP`中的静态方法`getAopProxy`来创建AOP代理类：

```java
public static <T> T getAopProxy(T target, Object advice);
```

* `target`是目标对象，即被增强的对象
* `advice`是目标对象的增强类，其中包含了若干方法，每个方法都表示对目标对象中特定方法的增强和拦截，这些信息通过注解来配置


## 拦截类型注解

ByxAOP支持如下拦截类型注解：

|注解|说明|
|---|---|
|`@Before`|前置增强，可以拦截和修改方法参数|
|`@After`|后置增强，可以拦截和修改方法返回值|
|`@Around`|环绕增强，可以自定义拦截方式|
|`@Replace`|替换目标方法的实现|
|`@AfterThrowing`|拦截方法异常|

### @Before

该注解用于对目标方法进行前置增强。

目标方法：

```java
public int target(int a, String b) {
    ...
}
```

前置增强有三种使用方式：

1. 在目标方法调用之前执行自定义操作

    ```java
    @Before
    public void beforeTarget() {
        // 自定义操作...
    }
    ```

2. 获取目标方法的参数并执行自定义操作，但不修改参数值

    ```java
    @Before
    public void beforeTarget(int n) {
        // n为传入目标方法的参数值
        // 自定义操作...
    }
    ``

3. 拦截目标方法的参数，并可修改目标方法的参数

    ```java
    @Before
    public Object[] beforeTarget(int a, String b) {
        // 自定义操作...

        // 返回修改后的方法参数数组
        return new Object[]{...};
    }
    ```

### @After

该注解用于对目标方法进行后置增强。

目标方法：

```java
public String target(String a, String b) {
    ...
}
```

后置增强有三种使用方式：

1. 在目标方法返回之后执行自定义操作

    ```java
    @After
    public void afterTarget() {
        // 自定义操作...
    }
    ```

2. 接收目标方法的返回值并执行自定义操作，但不修改返回值

    ```java
    @After
    public void afterTarget(int retVal) {
        // retVal为目标方法的返回值
        // 自定义操作...
    }
    ```

3. 拦截目标方法的返回值，并可修改目标方法的返回值

    ```java
    @After
    public String afterTarget(String retVal) {
        // 自定义操作...

        // 返回一个新的返回值
        return ...;
    }
    ```

### @Around

该注解用于对目标方法进行环绕增强。用户可以通过环绕增强实现自定义的拦截操作，其它所有增强类型都能用环绕增强实现。被`@Around`注解的方法需要接收一个`TargetMethod`类型的参数，返回一个值作为目标方法的返回值。`TargetMethod`类的方法如下：

|方法|说明|
|---|---|
|`MethodSignature getSignature()`|获取目标方法签名|
|`Object[] getParams()`|获取目标方法的原始参数数组|
|`Object invoke(Object... params)`|使用指定参数调用目标方法|
|`Object invokeWithOriginalParams()`|使用原始参数调用目标方法|

目标方法：

```java
public String target(int a, String b) {
    ...
}
```

环绕增强方法：

```java
@Around
public String aroundTarget(TargetMethod targetMethod) {
    // 获取目标方法的签名
    MethodSignature signature = targetMethod.getSignature();
    // 获取目标方法的原始参数
    Object[] params = targetMethod.getParams();

    // 自定义拦截操作，可以通过targetMethod的invoke方法来调用目标方法
    // ...

    // 返回值作为目标方法的最终返回值
    return ...;
}
```

### @Replace

该注解用于替换目标方法的实现，被`@Replace`注解的方法必须与目标方法具有完全相同的签名。

目标方法：

```java
public String target(int a, String b) {
    ...
}
```

`Replace`增强方法：

```java
@Replace
public String replaceTarget(int a, String b) {
    // 替换target方法的实现
    // ...
}
```

### @AfterThrowing

该注解用于拦截目标方法抛出的异常。被`@AfterThrowing`注解的增强方法需要接收一个异常类作为参数，其返回值作为目标方法发生异常后的返回值。

目标方法：

```java
public String target(int a, String b) {
    ...
    throw new MyException(...);
    ...
}
```

异常拦截方法：

```java
@AfterThrowing
public String handleException(MyException e) {
    // 处理异常
    // ...
    return "exception";
}
```

## 方法匹配器注解

使用`@Filter`注解用来指定目标对象中哪些方法需要被增强。

如果不指定`@Filter`，则默认拦截所有方法。

下面的代码定义了一个前置增强，它作用于目标对象中所有方法名为`f1`、返回值类型为`int`、参数类型为`String`的方法：

```java
@Before
@Filter(name = "f1", returnType = int.class, parameterTypes = String.class)
public String[] g1(String s) {
    return new String[]{s + "x"};
}
```

下面的代码对所有以`list`开头的方法做了统一异常处理：

```java
@Around
@Filter(pattern = "list(.*)")
public Object handleException(TargetMethod targetMethod) {
    try {
        return targetMethod.invokeWithOriginalParams();
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}
```

## 关于多重AOP代理

ByAOP支持用多个拦截器拦截同一个方法，这是利用多重AOP代理实现的。在默认情况下，这些拦截方法执行的顺序是随机的。

举个例子，假设有以下目标对象：

```java
public class A {
    public void f() {
        return ...;
    }
}
```

以及以下增强类：

```java
public static class Advice {
    @Around
    @Filter(name = "f")
    public String apple(TargetMethod targetMethod) {
        System.out.println("before 1");
        Object ret = targetMethod.invokeWithOriginalParams();
        System.out.println("after 1");
        return ret;
    }

    @Around
    @Filter(name = "f")
    public String cat(TargetMethod targetMethod) {
        System.out.println("before 2");
        Object ret = targetMethod.invokeWithOriginalParams();
        System.out.println("after 2");
        return ret;
    }

    @Around
    @Filter(name = "f")
    public String banana(TargetMethod targetMethod) {
        System.out.println("before 3");
        Object ret = targetMethod.invokeWithOriginalParams();
        System.out.println("after 3");
        return ret;
    }
}
```

在增强类`Advice`中，为目标方法`f`指定了3个拦截方法，我们用下面的代码来创建一个AOP代理对象：

```java
A a = ByxAOP.getAopProxy(new A(), new Advice());
```

当我们调用`a.f()`时，`f`方法会被`Advice`中的三个拦截方法分别拦截，不过这些拦截方法的执行顺序是随机的，并不是按照拦截方法定义的顺序。在我的机器上，执行结果为：

```
before 1
before 3
before 2
after 2
after 3
after 1
```

如果需要指定多重代理的顺序，可以使用`@Order`注解，并传入一个用于指定顺序的整数。修改后的`Advice`类如下：

```java
public static class Advice {
    @Around
    @Filter(name = "f")
    @Order(1)
    public String apple(TargetMethod targetMethod) {
        ...
    }

    @Around
    @Filter(name = "f")
    @Order(2)
    public String cat(TargetMethod targetMethod) {
        ...
    }

    @Around
    @Filter(name = "f")
    @Order(3)
    public String banana(TargetMethod targetMethod) {
        ...
    }
}
```

此时，如果再次执行上面的操作，就会发现，拦截方法执行的顺序与我们用`Order`声明的一致：

```
before 3
before 2
before 1
after 1
after 2
after 3
```

请注意：

* 如果不指定Order值，则Order值默认为1
* Order值小的拦截方法比Order值大的拦截方法先执行
* 如果多个拦截方法的Order值相等，则它们之间的执行顺序仍然是随机的