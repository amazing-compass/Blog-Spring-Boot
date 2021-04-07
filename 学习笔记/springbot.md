# 一. springBoot概念

## 1.1 原有Spring优缺点分析

- **Spring的优点分析**

  Spring是Java企业版（Java Enterprise Edition，JEE，也称J2EE）的轻量级代替品。无需开发重量级的Enterprise JavaBean（EJB），Spring为企业级Java开发提供了一种相对简单的方法，通过依赖注入和面向切面编程，用简单的Java对象（Plain Old Java Object，POJO）实现了EJB的功能。

- **Spring的缺点分析**

  虽然Spring的组件代码是轻量级的，但它的配置却是重量级的。一开始，Spring用XML配置，而且是很多XML配置。Spring 2.5引入了基于注解的组件扫描，这消除了大量针对应用程序自身组件的显式XML配置。Spring 3.0引入了基于Java的配置，这是一种类型安全的可重构配置方式，可以代替XML。

  所有这些配置都代表了开发时的损耗。因为在思考Spring特性配置和解决业务问题之间需要进行思维切换，所以编写配置挤占了编写应用程序逻辑的时间。和所有框架一样，Spring实用，但与此同时它要求的回报也不少。

  除此之外，项目的依赖管理也是一件耗时耗力的事情。在环境搭建时，需要分析要导入哪些库的坐标，而且还需要分析导入与之有依赖关系的其他库的坐标，一旦选错了依赖的版本，随之而来的不兼容问题就会严重阻碍项目的开发进度。

## 1.2 SpringBoot的概述

- **SpringBoot解决上述Spring的缺点**

  SpringBoot对上述Spring的缺点进行的改善和优化，**基于约定优于配置的思想**，可以让开发人员不必在配置与逻辑业务之间进行思维的切换，全身心的投入到逻辑业务的代码编写中，从而大大提高了开发的效率，一定程度上缩短了项目周期。

## 1.3 SpringBoot的特点

- 为基于Spring的开发提供更快的入门体验
- 开箱即用，没有代码生成，也无需XML配置。同时也可以修改默认值来满足特定的需求
- 提供了一些大型项目中常见的非功能性特性，如嵌入式服务器、安全、指标，健康检测、外部配置等
- SpringBoot不是对Spring功能上的增强，而是提供了一种快速使用Spring的方式

## 1.4 SpringBoot的核心功能

- 起步依赖

  起步依赖本质上是一个Maven项目对象模型（Project Object Model，POM），定义了对其他库的传递依赖，这些东西加在一起即支持某项功能。

  简单的说，起步依赖就是将具备某种功能的坐标打包到一起，并提供一些默认的功能。

- 自动配置

  Spring Boot的自动配置是一个运行时（更准确地说，是应用程序启动时）的过程，考虑了众多因素，才决定Spring配置应该用哪个，不该用哪个。该过程是Spring自动完成的。

# 二.SpringBoot快速入门

创建一个普通的maven工程

## 2.1 在pom.xml中添加SpringBoot的起步依赖

SpringBoot要求，项目要继承SpringBoot的起步依赖spring-boot-starter-parent

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.0.1.RELEASE</version>
</parent>
```

SpringBoot要集成SpringMVC进行Controller的开发，所以项目要导入web的启动依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

## 2.2 编写SpringBoot引导类

要通过SpringBoot提供的引导类起步SpringBoot才可以进行访问

该启动类一定要和项目的其它文件放在同一个根目录内

```java
package com.springBoot.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MySpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class);
    }
}
```

## 2.3 编写Controller

```java
package com.springBoot.test.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class HelloWorldController {

    @ResponseBody
    @RequestMapping("/hello")
    public String HelloWorld() {
        return "hello world";
    }
}
```

## 2.4 测试项目启动

执行SpringBoot起步类的主方法

通过日志发现，Tomcat started on port(s): 8080 (http) with context path ' '

tomcat已经起步，端口监听8080，web应用的虚拟工程名称为空

所以可以在浏览器内直接输入 http://localhost:8080/hello访问项目

# 三.springBoot工程热部署

在开发过程中反复修改类、页面等资源，每次修改后都是需要重新启动才生效，这样每次启动都很麻烦，浪费了大量的时间，我们可以在修改代码后不重启就能生效，在 pom.xml 中添加如下配置就可以实现这样的功能，我们称之为热部署。

需要在pom.xml中引入热部署依赖：

```xml
<!--热部署配置-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>
```

还需要设置自动开启热部署功能

然后 Shift+Ctrl+Alt+/，选择Registry(注册)

勾选key值为compiler.automake.allow.when.app.running的选项再重新运行项目

# 四.使用idea快速创建SpringBoot项目

新建项目——选择Spring Initializr(spring初始化)——点击下一步——填写项目名字和选择java版本——下一步

选择springBoot需要的启动依赖再点击下一步

# 五.SpringBoot的配置文件

## 5.1 SpringBoot配置文件类型和作用

SpringBoot是基于约定的，所以很多配置都有默认值，但如果想使用自己的配置替换默认配置的话，就可以使用application.properties或者application.yml（application.yaml）进行配置。

SpringBoot默认会从Resources目录下加载application.properties或application.yml（application.yaml）文件

其中，**application.properties**文件是键值对类型的文件(key = value)

```properties
server.port=8888
```

## 5.2 application.yml配置文件

### 5.2.1 yml配置文件简介

YML文件格式是YAML (YAML Aint Markup Language)编写的文件格式，YAML是一种直观的能够被电脑识别的的数据数据序列化格式，并且容易被人类阅读，容易和脚本语言交互的，可以被支持YAML库的不同的编程语言程序导入，比如： C/C++, Ruby, Python, Java, Perl, C#, PHP等。YML文件是以数据为核心的，比传统的xml方式更加简洁。

### 5.2.2 yml配置文件的语法

#### (1) 配置普通数据

- 语法： key: value

```
name: xlq
```

- 注意：value之前有一个空格

#### (2) 配置对象数据(map类型)

- 语法：

  key:

  ```
    	key1: value1
  
    	key2: value2
  
    或者：  key: {key1: value1,key2: value2}
  ```

- 注意：key1前面的空格个数不限定，在yml语法中，相同缩进代表同一个级别

```yaml
person:  
	name: xlq
	age: 18

#person: {name: xsh,age: 18}
```

#### (3)配置数组（List、Set集合）数据

- 语法：

  key:

  ```
    - value1
  
    - value2
  ```

- value与之间的 - 之间存在一个空格

或者：key: [value1,value2]

```yaml
city:  
    - beijing  
    - tianjin  
    - shanghai  
    - chongqing
#city: [beijing,tianjin,shanghai,chongqing]
```

集合中的元素也可以是对象形式

```yaml
student:
  - name: zhangsan
    age: 18
    score: 100
  - name: lisi
    age: 28
    score: 88
  - name: wangwu
    age: 38
    score: 90
#student: [{name: zhangsan,age: 18,score: 100},{name: lisi,age: 28,score: 88},{name: wangwu,age: 38,score: 90}]
```

## 5.3 获取配置文件的值

### 5.3.1 @Value注解

可以通过@Value注解将配置文件中的值映射到一个Spring管理的Bean的字段上

```java
//注入普通数据类型
@Value("${name}")
private String name;

//注入map类型
@Value("${person.age}")
private int age;

//注入集合类型
@Value("${city[1]}")
private String city;

//注入集合中的元素是对象形式的值
@Value("${student[1].score}")
private int score;
```

### 5.3.2 @ConfigurationProperties注解

通过注解@ConfigurationProperties(prefix="配置文件中的key的前缀")可以将配置文件中的配置自动与实体进行映射**(需要对属性封装相应的set方法)**

适用于对象数据类型(map)

```yaml
person:
  name: xlq
  age: 18
@Controller
@ConfigurationProperties(prefix="person")
public class test2Controller {

    //属性名必须和配置文件中的key对应才能绑定成功
    private String name;
    private Integer age;
    public void setName(String name) {
        this.name = name;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    
    @ResponseBody
    @RequestMapping("hello2")
    public String test1(){
        return "hello,"+name+","+age;
    }
}
```

- 注意：使用@ConfigurationProperties方式可以进行配置文件与实体字段的自动映射，但需要字段必须提供set方法才可以，而使用@Value注解修饰的字段不需要提供set方法

# 六.SpringBoot整合其他技术

## 6.1 SpringBoot整合Mybatis

### 6.1.1添加Mybatis的起步依赖

```xml
<!--mybatis起步依赖-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.4.5</version>
</dependency>
```

### 6.1.2 添加数据库驱动坐标

```xml
<!-- MySQL连接驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

### 6.1.3 添加数据库连接信息

在application.properties中添加数据库的连接信息

```properties
#DB Configuration:
spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=password
```

### 6.1.4 编写代码

1. 编写domain实体类

   ```java
   public class Account {    
       private int id;    
       private String name;    
       private float money;
       //此处还需加入getter和setter方法
   }
   ```

2. 编写dao层接口(需加入@Mapper注解)

   注意：@Mapper标记该类是一个mybatis的mapper接口，可以被spring boot自动扫描到spring上下文中

   ```java
   @Mapper
   @Repository
   public interface AccountDao {    
       List<Account> findAll();
   }
   ```

3. 编写Controller

   ```java
   @Controller
   public class AccountController {
       @Autowired
       private AccountDao accountDao;
   
       @RequestMapping("account")
       @ResponseBody
       public List<Account> findAll(){
           List<Account> all = accountDao.findAll();
           return all;
       }
   }
   ```

4. 配置Mapper映射文件

   在src\main\resources\dao路径下加入AccountDao.xml配置文件

   ```xml
   <?xml version="1.0" encoding="utf-8" ?>
   <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
   <mapper namespace="com.springboot.test4_mybatis.dao.AccountDao">
       <select id="findAll" resultType="Account">
           select * from account
       </select>
   </mapper>
   ```

5. 在application.properties中添加mybatis的信息

   ```properties
   #spring集成Mybatis环境
   #domain别名扫描包
   mybatis.type-aliases-package=com.springboot.test4_mybatis.domain
   #加载Mybatis映射文件
   mybatis.mapper-locations=classpath:dao/*Dao.xml
   ```

### 6.1.5 访问浏览器

开启springBoot启动类并访问http://localhost:8080/account

可查出以下数据

```json
[{"id":1,"name":"aaa","money":800.0},{"id":2,"name":"bbb","money":2200.0},{"id":3,"name":"xlq","money":10000.0},{"id":5,"name":"hhh","money":200.0},{"id":14,"name":"uuuu","money":4000.0}]
```

## 6.2 SpringBoot整合Junit

### 6.2.1 添加Junit的起步依赖

```xml
<!--测试的起步依赖-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 6.2.2 编写测试类

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MySpringBootApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void test() {
        List<User> users = userMapper.queryUserList();
        System.out.println(users);
    }
}
```

其中，SpringRunner继承自SpringJUnit4ClassRunner，使用哪一个Spring提供的测试测试引擎都可以；

@SpringBootTest的属性指定的是引导类的字节码对象；

## 6.3 SpringBoot整合Spring Data JPA

### 6.3.1添加Spring Data JPA的起步依赖

```xml
<!-- springBoot JPA的起步依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

#### 6.3.2 添加数据库驱动依赖

```xml
<!-- MySQL连接驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

### 6.3.3 在application.properties中配置数据库和jpa的相关属性

```properties
#DB Configuration:
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xsh?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456

#JPA Configuration:
spring.jpa.database=MySQL
spring.jpa.show-sql=true
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming_strategy=org.hibernate.cfg.ImprovedNamingStrategy
```

### 6.3.4 创建domain和dao

```java
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private float money;
    //此处需加入toString,setter和getter方法... ...
}
public interface AccountDao extends JpaRepository<Account,Integer> {    
    List<Account> findAll();
}
```

### 6.3.5 测试是否整合成功

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Test5SpringdatajpaApplication.class)
public class Test5SpringdatajpaApplicationTests {

    @Autowired
    private AccountDao accountDao;
    @Test
    public void JpaTest() {
        List<Account> all = accountDao.findAll();
        System.out.println(all);
    }
}
```

## 6.4 Spring Boot 整合模版引擎

```xml
<!-- springMVC整合Freemarker -->
    <!-- 放在InternalResourceViewResolver的前面，优先找freemarker -->  
    <bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">  
        <property name="templateLoaderPath" value="/WEB-INF/views/templates"/>  
    </bean>  
    <bean id="viewResolver" class="org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver">  
        <property name="prefix" value=""/>  
        <property name="suffix" value=".ftl"/>  
        <property name="contentType" value="text/html; charset=UTF-8"/>
    </bean>
```