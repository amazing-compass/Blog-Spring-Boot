# 一.spring概述

spring是分层的Java SE/EE应用full-stack轻量级开源框架，以IOC（Inverse Of Control:控制反转）和AOP（Aspect Orinted Programming：面向切面编程）为内核，提供了展现层Spring MVC和持久层JDBC以及业务层管理事务等众多的企业级应用技术，还能整合开源世界众多著名的第三方框架和类库，逐渐成为使用最多的Java EE企业应用开源框架。

## 1.1优势

**方便解耦，简化开发** 通过Spring提供的IOC容器，可以将对象间的依赖关系交友Spring进行控制，避免硬编码所造成的程序过度耦合。用户也不必再为单例模式类，属性文件解析等这些很底层的需求编写代码，可以更专注与上层的应用。

**AOP编程的支持** 通过Spring的AOP功能，方便进行面向切面的编程，许多不容易用传统OOP实现的功能可以通过AOP轻松应付。

**声明式事务的支持** 可以将我们从单调烦闷的事务管理代码中解脱出来，通过声明式方式灵活的进行事务的管理，提高开发效率和质量。

**方便程序的测试** 可以用非容器依赖的编程方式进行几乎所有的测试工作，测试不再是昂贵的操作，而是随手可做的事情。

**方便集成各种优秀框架** Spring可以降低各种框架的使用难度，提供了对各种优秀框架（Struts、Hibernate、Hessian、Quartz等）的直接支持。

**降低 JavaEE API 的使用难度** Spring对 JavaEE API（如 JDBC、JavaMail、远程调用等）进行了薄薄的封装层，使这些 API 的使用难度大为降低。

**Java 源码是经典学习范例** Spring的源代码设计精妙、结构清晰、匠心独用，处处体现着大师对Java 设计模式灵活运用以及对 Java技术的高深造诣。它的源代码无意是 Java 技术的最佳实践的范例。



## 1.2Spring的体系结构

![img](https://img2018.cnblogs.com/blog/480452/201903/480452-20190318225849216-2097896352.png)



# 二.程序的耦合和解耦

**（1）程序的耦合**

耦合：**程序间的依赖关系**

包括：

​	类之间的依赖

​	方法间的依赖

**（2）解耦：**

**降低程序间的依赖关系**

实际开发中：

​	应该做到：编译期不依赖，运行时才依赖。

解耦的思路：

​	第一步：使用反射来创建对象，而**避免使用new关键字**。

​	第二步：通过读取配置文件来获取要创建的对象全限定类名

**jdbc解耦例子:**

```java
package com.xlq.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class JdbcDemo1 {
    public static void main(String[] args) throws  Exception{
        //1.注册驱动
        //注册驱动时使用registerDriver注册，使用了new关键字，当驱动包缺失时，该程序编译期就会报错
		//DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        //而使用Class.forName注册驱动，括号内的参数只是一个字符串，所以当缺少驱动包，编译期不会报错，运行时才会抛出异常，这样就降低了程序间的依赖关系
        Class.forName("com.mysql.jdbc.Driver");

        //2.获取连接
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test","root","123456");
        //3.获取操作数据库的预处理对象
        PreparedStatement pstm = conn.prepareStatement("select * from account");
        //4.执行SQL，得到结果集
        ResultSet rs = pstm.executeQuery();
        //5.遍历结果集
        while(rs.next()){
            System.out.println(rs.getString("name"));
        }
        //6.释放资源
        rs.close();
        pstm.close();
        conn.close();
    }
}
```



# 三.使用spring解耦

新建一个maven项目

## 3.1在pom.xml文件中引入依赖

```xml
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.0.9.RELEASE</version>
        </dependency>
    </dependencies>
```

## 3.2定义一个HelloWorld方法

```java
public class HelloWorld {
    private String uname;
    public void setUname(String uname){
        this.uname=uname;
    }
    public void sayHello(){
        System.out.println("hello,"+uname);
    }
}
```

## 3.3在其他类中调用这个方法

### 3.3.1不适用spring

在不使用框架的时候，也就是平常的编程中，我们要调用sayHello这个方法，可以分为3步。

```java
public class Main {
    public static void main(String[] args) {
        //1.创建一个HelloWorld的实例对象
        HelloWorld hw=new HelloWorld();
        //2.设置实例对象的name属性
        hw.setUname("test");
        //3.调用对象的sayHello()方法
        hw.sayHello();
    }
}
```

这样因为使用了new关键字，所以是高耦合的

### 3.3.2使用spring

使用Spring，首先要在resources根目录下创建Spring的配置文件bean.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd">
   
    <!--把对象的创建交给spring来管理-->
    <bean id="test1" class="com.xlq.HelloWorld">
        <!--可使用property标签进行赋值,前提是uname要有setter方法-->
        <property name="uname" value="xlq"></property>
    </bean>
</beans>
```

调用sayHello方法:

```java
public class Main {
    public static void main(String[] args) {
        //1.创建一个spring的IOC容器ac,读取baen.xml配置文件
        ApplicationContext ac=new ClassPathXmlApplicationContext("baen.xml");
        //2.从IOC容器中获取Bean实例,test1为baen.xml中定义的bean标签id
        //因为getBean()方法返回值是Object类型，所以需要强转
        //HelloWorld hw=ac.getBean("test1",HelloWorld.class);
        HelloWorld hw=(HelloWorld) ac.getBean("test1");
        //3.调用sayHello()方法
        hw.sayHello();
    }
}
```

使用spring后，就没有使用new关键字创建对象，从而降低了程序间的耦合



# 四. BeanFactory和ApplicationContext

BeanFactory 才是 Spring 容器中的顶层接口，ApplicationContext 是它的子接口。

BeanFactory 和 ApplicationContext 的区别：**创建对象的时间点不一样**。

- AppicationContext：只要一读取配置文件，默认情况下就会创建对象。
- BeanFactory：什么使用什么时候创建对象



## 4.1ApplicationContext

单例对象适用， 它在构建核心容器时，创建对象采取的策略是采用**立即加载**的方式。

**ApplicationContext 接口的实现类**：

- ClassPathXmlApplicationContext ：

  ​	它是从类的根路径下加载配置文件，**推荐使用这种**

- FileSystemXmlApplicationContext ：

  ​	它是从磁盘路径上加载配置文件，配置文件可以在磁盘的任意位置。

- AnnotationConfigApplicationContext:

  ​	当我们使用注解配置容器对象时，需要使用此类来创建 spring 容器。它用来读取注解。

## 4.2 BeanFactory

多例对象适用, 它在构建核心容器时，创建对象采取的策略是采用**延迟加载**的方式

```java
public class Client {
   public static void main(String[] args) {
       Resource resource = new ClassPathResource("bean.xml");
       BeanFactory factory = new XmlBeanFactory(resource);
       HelloWorld hw=(HelloWorld) ac.getBean("test1");
       System.out.println(hw);
    }
}
```



# 五.spring中bean的细节

## 5.1 创建bean的三种方式

### 5.1.1 使用默认构造函数创建

在spring的配置文件中使用bean标签，配以id和class属性之后，**且没有其他属性和标签**时。采用的就是默认构造函数创建bean对象，此时如果类中没有默认构造函数**(默认的无参构造函数被重写为有参构造函数)**，则对象无法创建。

```xml
<bean id="accountService" class="com.xsh.service.impl.AccountServiceImpl"></bean>
```

### 5.1.2 使用普通工厂中的方法创建对象

（使用某个类中的方法创建对象，并存入spring容器）

```java
public class InstanceFactory {

    public IAccountService getAccountService(){
        return new AccountServiceImpl();
    }
}
```

factory-bean：用于指定实例工厂 bean 的 id

```xml
<bean id="instanceFactory" class="com.xsh.factory.InstanceFactory"></bean>
 <bean id="accountService" factory-bean="instanceFactory" factory-method="getAccountService"></bean>

```

### 5.1.3 使用工厂中的静态方法创建对象

（使用某个类中的静态方法创建对象，并存入spring容器)

```java
public class StaticFactory {

    public static IAccountService getAccountService(){
        return new AccountServiceImpl();
    }
}
```

factory-method：指定静态方法

```xml
<bean id="accountService" 
      class="com.xsh.factory.StaticFactory" 
      factory-method="getAccountService">
</bean>
```



## 5.2 bean的作用范围

scope：指定对象的作用范围。

- singleton : 默认值，单例的. 表示在整个bean容器中或者说是整个应用中只会有一个实例。
- prototype : 多例的. 表示每次从bean容器中都会获取到一个对应bean定义全新的实例。
- request : WEB 项目中,Spring 创建一个 Bean 的对象,将对象存入到 request 域中.
- session : WEB 项目中,Spring 创建一个 Bean 的对象,将对象存入到 session 域中.
- global session :WEB 项目中,应用在 Portlet 环境.如果没有 Portlet 环境那么globalSession 相当于 session.

**常用的有singleton 和prototype**

测试用的类:

```java
package example2;
public class Scope {
    private String name;
    public void setName(String name){
        this.name=name;
    }
    public void outName(){
        System.out.println("你的名字是"+name);
    }
}
```

### 5.2.1 singleton

Singleton是单例类型，就是在**创建容器时就同时自动创建了一个bean的对象**，不管你是否使用，他都存在了，**每次获取到的对象都是同一个对象**。

配置文件example_2.xml： 将Scope类设置为单例，并且设置name的缺省值为null

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="scope" class="example2.Scope" scope="singleton">
        <property name="name" value="null"></property>
    </bean>
</beans>
```

Main方法运行测试：

```java
package example2;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext scope_test=new ClassPathXmlApplicationContext("example_2.xml");
        Scope s1=(Scope) scope_test.getBean("scope");
        s1.setName("test");
        s1.outName();//输出test
        Scope s2=(Scope) scope_test.getBean("scope");
        s2.outName();//虽然s2实例没有设置name值，但因为是单例，所以也输出test
        System.out.println(s1==s2); //因为是单例，所以创建的都是同一个对象，输出为true
    }
}
```

### 5.2.2 prototype

Prototype是原型类型，**它在我们创建容器的时候并没有实例化**，而是当我们获取bean的时候才会去创建一个对象，而且我们**每次获取到的对象都不是同一个对象**。根据经验，对有状态的bean应该使用prototype作用域，而对无状态的bean则应该使用singleton作用域。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="scope" class="example2.Scope" scope="singleton">
        <property name="name" value="null"></property>
    </bean>
</beans>
```

Main方法运行测试：

```java
package example2;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext scope_test=new ClassPathXmlApplicationContext("example_2.xml");
        Scope s1=(Scope) scope_test.getBean("scope");
        s1.setName("xsh");
        s1.outName();//输出xsh
        Scope s2=(Scope) scope_test.getBean("scope");
        s2.outName();//,因为设置为多例，所以输出value默认值null
        System.out.println(s1==s2);//多例时，每次创建的对象都不同，所以输出false
    }
}
```

## 5.3 bean的生命周期

bean标签:

**init-method：指定初始化方法； destroy-method：制定销毁方法；**

Bean的生命周期可以表达为：

**Bean的定义——Bean的初始化——Bean的使用——Bean的销毁**

**单例对象：scope="singleton"**

一个应用只有一个对象的实例，它的作用范围就是整个引用。

生命周期：

- 对象出生：当应用加载，创建容器时，对象就被创建了。
- 对象活着：只要容器在，对象一直活着。
- 对象死亡：当应用关闭(close)，销毁容器时，对象就被销毁了。

**多例对象：scope="prototype"**

每次访问对象时，都会重新创建对象实例。

生命周期：

- 对象出生：当使用对象时，创建新的对象实例。
- 对象活着：只要对象在使用中，就一直活着。
- 对象死亡：当对象长时间不用时，且没有别的对象引用时，被java的垃圾回收器回收了。

### 5.3.1 单例和多例的生命周期

指定初始化方法和销毁方法,并定义对应的测试类：

```java
package example3;

public class LifeCycle{
    private String message;

    public void setMessage(String message){
        this.message  = message;
    }
    public void getMessage(){
        System.out.println("Your Message : " + message);
    }
    public void init(){
        System.out.println("Bean 初始化");
    }
    public void destroy(){
        System.out.println("Bean 销毁");
    }
}
```

Main方法:

```java
package example3;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        //因为ApplicationContext没有close方法，所以此处用ClassPathXmlApplicationContext(多态特性)
        //ApplicationContext ac = new ClassPathXmlApplicationContext("example_3.xml");
        ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("example_3.xml");
        LifeCycle lc=(LifeCycle) context.getBean("LC");
        lc.getMessage();
        //手动关闭容器，并且调用相关的 destroy 方法。
        context.close();
    }
}
```

**当bean为单例时：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="LC"
          class="example3.LifeCycle"
          scope="singleton" 
          init-method="init" 
          destroy-method="destroy">
        <property name="message" value="Hello World!"/>
    </bean>
</beans>
```

运行Main方法后，输出结果：

Bean 初始化 Your Message : Hello World! Bean 销毁

**当bean为多例时：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="LC"
          class="example3.LifeCycle"
          scope="prototype" 
          init-method="init" 
          destroy-method="destroy">
        <property name="message" value="Hello World!"/>
    </bean>
</beans>
```

运行Main方法后，输出结果：

Bean 初始化 Your Message : Hello World!

可以注意到当为多例时，既使运行了容器的close方法，但是也没有运行销毁方法；

说明当容器为多例时，容器关闭后并没有真正的销毁，而是**当对象长时间不用，且没有别的对象引用时，被java的垃圾回收器回收了。**

# 六. spring的依赖注入

依赖注入：Dependency Injection。它是spring框架核心ioc的具体实现。

IOC的作用： **降低程序间的耦合（依赖关系）**

程序在编写时，通过控制反转，把对象的创建交给了spring，但是代码中不可能出现没有依赖的情况。ioc解耦只是降低他们的依赖关系，但不会消除。例如：我们的业务层仍会调用持久层的方法。所以需要注入需要使用到的东西。

依赖注入能注入的数据有三类 ：

- 基本类型和String
- 其他bean类型（在配置文件中或者注解配置过的bean）
- 复杂类型/集合类型

## 6.1 构造函数注入

使用类中的构造函数，给成员变量赋值。注意，赋值的操作不是我们自己做的，而是通过配置的方式，让spring框架来为我们注入。

使用的标签:constructor-arg

标签出现的位置：bean标签的内部

标签中的属性

- type：用于指定要注入的数据的数据类型，该数据类型也是构造函数中某个或某些参数的类型
- index：用于指定要注入的数据给构造函数中指定索引位置的参数赋值。索引的位置是从0开始
- **name：用于指定给构造函数中指定名称的参数赋值 (常用的)**

=============以上三个用于指定给构造函数中哪个参数赋值=============

- value：用于提供基本类型和String类型的数据
- ref：用于指定其他的bean类型数据。它指的就是在spring的Ioc核心容器中出现过的bean对象

优势： 在获取bean对象时，注入数据是必须的操作，否则对象无法创建成功。

弊端： 改变了bean对象的实例化方式，使我们在创建对象时，如果用不到这些数据，也必须提供。

```java
package com.spring.service;

import java.util.Date;

/**
 * @describe:构造注入
 如果是经常变化的数据，并不适合用注入的方式
 */
public class AccountService {
    private String username;
    private int age;
    private Date birthday;
    //使用构造注入，类中需要提供一个对应参数列表的构造函数。
    public AccountService(String username, int age, Date birthday) {
        this.username = username;
        this.age = age;
        this.birthday = birthday;
    }
    public void saveAccount(){
        System.out.println("名字："+username+"，年龄："+age+"，生日："+birthday);
    }
}
```

bean.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd">
    <!--构造注入配置-->
    <bean id="AccountService" class="com.spring.service.AccountService">
        <constructor-arg name="username" value="test1"/>
        <constructor-arg name="age" value="18"/>
         <!-- ref指定要使用的对象 -->
        <constructor-arg name="birthday" ref="now"/>
    </bean>
     <!-- 配置一个日期对象 -->
    <bean id="now" class="java.util.Date"></bean>
</beans>
```

## 6.2 设值注入(set注入)

在类中提供需要注入成员的set方法。

```java
package com.spring.service;

import java.util.Date;

public class AccountService1 {
    private String username;
    private int age;
    private Date birthday;

    public void setUsername(String username) {
        this.username = username;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "AccountService1{" +
                "username='" + username + '\'' +
                ", age=" + age +
                ", birthday=" + birthday +
                '}';
    }
}
```

bean.xml：

使用**property**标签

```xml
<!--设值注入配置-->
<bean id="AccountService1" class="com.spring.service.AccountService1">
    <property name="username" value="test2"></property>
    <property name="age" value="19"></property>
    <property name="birthday" ref="now2"></property>
</bean>
<bean id="now2" class="java.util.Date"></bean>
```

## 6.3 p名称空间注入数据

本质还是设值注入，只是bean.xml中配置标签不一样

```xml
    <bean id="AccountService1" class="com.spring.service.AccountService1"
          p:username="username" 
          p:age="18" 
          p:birthday-ref="now2"/>
    <bean id="now2" class="java.util.Date"></bean>
```

## 6.4 复杂类型注入

```java
package com.spring.service;

import java.util.*;

/**
 * @describe:复杂类型的数据注入
 *          List 结构的:array,list,set
 *          Map  结构的:map,entry,props,prop
 */
public class AccountService2 {
    private String[] str;
    private List<String> stringList;
    private Set<String> stringSet;
    private Map<String,String> stringStringMap;
    private Properties myProps;

    public void setStr(String[] str) {
        this.str = str;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public void setStringSet(Set<String> stringSet) {
        this.stringSet = stringSet;
    }

    public void setStringStringMap(Map<String, String> stringStringMap) {
        this.stringStringMap = stringStringMap;
    }

    public void setMyProps(Properties myProps) {
        this.myProps = myProps;
    }
    public void  saveAccount(){
        System.out.println(Arrays.toString(str));
        System.out.println(stringList);
        System.out.println(stringSet);
        System.out.println(myProps);
        System.out.println(stringStringMap);
    }
}
```

bean.xml

```java
    <!--在注入集合数据时，只要结构相同，标签可以互换-->
    <bean id="AccountService2" class="com.spring.service.AccountService2">
        <property name="str">
            <array>
                <value>aaa</value>
                <value>bbb</value>
                <value>ccc</value>
            </array>
        </property>
        <property name="stringList">
            <list>
                <value>aaa</value>
                <value>bbb</value>
                <value>ccc</value>
            </list>
        </property>
        <property name="stringSet">
            <set>
                <value>aaa</value>
                <value>bbb</value>
                <value>ccc</value>
            </set>
        </property>
        <property name="myProps">
            <props>
                <prop key="test1">aaa</prop>
                <prop key="test2">bbb</prop>
                <prop key="test3">ccc</prop>
            </props>
        </property>
        <property name="stringStringMap">
            <map>
                <entry key="test3" value="ccc"></entry>
                <entry key="test2" value="bbb"></entry>
                <entry key="test1" value="aaa"></entry>
            </map>
        </property>
    </bean>
```

## 6.4 测试注入

```java
package com.spring.ui;

import com.spring.service.AccountService;
import com.spring.service.AccountService1;
import com.spring.service.AccountService2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Client {
    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("bean.xml");
        //1.构造注入
        AccountService as=(AccountService) context.getBean("AccountService");
        as.saveAccount();
        //2.设值注入
        AccountService1 as1=(AccountService1) context.getBean("AccountService1");
        System.out.println(as1.toString());
        //3.复杂类型注入(List,Set,Map,Properties)
        AccountService2 as2=(AccountService2) context.getBean("AccountService2");
        as2.saveAccount();
    }
}
```