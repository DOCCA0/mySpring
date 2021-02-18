# **手写简易spring框架**

运行效果：

![image.png](pic/image.png)


项目分为2个大板块：IOC和handlerMapping，分别实现控制反转和把url和类或方法关联起来。项目会用到servlet，因为涉及到web，如果只关注最核心的springIoc的地方，是可以不用servlet这个包的。



## IOC

### 容器：

```java
//保存application.properties里面所有的配置，主要是scan-package=com.myDemo（我需要扫描的包）
private Properties properties=new Properties();
//上面properties中的所有类（没有被@MyController、@MyService注释的类也在其中）
private List<String> beanNameList=new ArrayList<String>();
//key:保存所有@MyController、@MyService的类的类名;value:保存所有类的实例
private Map<String,Object> iocMap=new HashMap<String, Object>();
```

### 初始化：

```java
//1,application.properties里面所有的配置
doLoadConfig("application.properties");
//2,扫描上述配置下的所有类，并且全部加入beanNameList
doScanner(properties.getProperty("scan-package"));
//3,将上述beanNameList中的被@MyController,@MyService注释类全部加入iocMap
doInstance();
//4,如果某属性被@MyAutowired注释，在iocMap中找到这个类，然后给它赋值
doAutowired();

```



## handlerMapping

```java
//1,存路径到方法的映射，例如：/index映射到com.myDemo.controller.hello()这个方法上
Map<String, Method> handlerMapping = new HashMap<String, Method>();
//2,通过反射机制执行这个方法
protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ……
            method.invoke(iocMap.get(toLowerFirstCase(method.getDeclaringClass().getSimpleName())),req,resp);
        ……
    }
```



## 重要地方说明

