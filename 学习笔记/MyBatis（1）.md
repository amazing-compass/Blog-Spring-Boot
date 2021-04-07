# 一. MyBatis框架概述

## 1.1 框架的概念

框架（Framework）是整个或部分系统的可重用设计，表现为一组抽象构件及构件实例间交互的方法;另一种定义认为，框架是可被应用开发者定制的应用骨架。

简而言之，框架其实就是某种应用的半成品，就是一组组件，供你选用完成你自己的系统。

## 1.2 软件开发的分层重要性

框架的重要性在于它实现了部分功能，并且能够很好的将低层应用平台和高层业务逻辑进行了缓和。为了实现软件工程中的“高内聚、低耦合”。把问题划分开来各个解决，易于控制，易于延展，易于分配资源。MVC软件设计思想就是很好的分层思想。

## 1.3 MyBatis概述

- mybatis是一个优秀的基于java的持久层框架，它内部封装了jdbc，使开发者只需要关注sql语句本身，而不需要花费精力去处理加载驱动、创建连接、创建statement等繁杂的过程。
- mybatis通过xml或注解的方式将要执行的各种statement配置起来，并通过java对象和statement中sql的动态参数进行映射生成最终执行的sql语句，最后由mybatis框架执行sql并将结果映射为java对象并返回。
- **采用ORM思想解决了实体和数据库映射的问题**，对jdbc进行了封装，屏蔽了jdbc api底层访问细节，使我们不用与jdbc api打交道，就可以完成对数据库的持久化操作。

# 二. Mybatis框架快速入门

## 2.1 jdbc回顾

```java
public static void main(String[] args) { 
    Connection connection = null; 
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null; 
    try { 
        //加载数据库驱动 
        Class.forName("com.mysql.jdbc.Driver"); 
        //通过驱动管理类获取数据库链接 
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mybatis","root", "password"); 
        //定义sql语句 ?表示占位符 
        String sql = "select * from user where username = ?";
		//获取预处理
        statement preparedStatement = connection.prepareStatement(sql); 
        //设置参数，第一个参数为sql语句中参数的序号（从1开始），第二个参数为设置的参数值 		  
        preparedStatement.setString(1, "王五"); 
        //向数据库发出sql执行查询，查询出结果集 
        resultSet = preparedStatement.executeQuery(); 
        //遍历查询结果集 
        while(resultSet.next()){ 
            System.out.println(resultSet.getString("id")+" 				  
                   "+resultSet.getString("username")); 
        	}
        } catch (Exception e) { 
            e.printStackTrace(); 
        }finally{
            //释放资源
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } 
   }
```

## 2.2 jdbc问题分析

1、数据库链接创建、释放频繁造成系统资源浪费从而影响系统性能，如果使用数据库链接池可解决此问题。

2、Sql语句在代码中硬编码，造成代码不易维护，实际应用sql变化的可能较大，sql变动需要改变java代码。

3、使用preparedStatement向占有位符号传参数存在硬编码，因为sql语句的where条件不一定，可能多也可能少，修改sql还要修改代码，系统不易维护。

4、对结果集解析存在硬编码（查询列名），sql变化导致解析代码变化，系统不易维护，如果能将数据库记录封装成pojo对象解析比较方便。

## 2.3 环境搭建

创建一个maven项目

### 2.3.1 引入依赖

配置pom.xml文件：

```java
	//定义打包方式为jar
	<packaging>jar</packaging>

    <dependencies>
    	/*引入mybatis依赖*/
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.4.5</version>
        </dependency>
		/*引入mysql驱动依赖*/
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.12</version>
        </dependency>
		/*引入日志依赖*/
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.12</version>
        </dependency>
		/*引入单元测试依赖*/
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.10</version>
        </dependency>

    </dependencies>
```

### 2.3.2 数据库创建

```mysql
create database xsh;
USE xsh;
CREATE TABLE USER(
  id INT(11) NULL AUTO_INCREMENT,
  username VARCHAR(32) NOT NULL,
  birthday DATETIME DEFAULT NULL,
  sex CHAR(1) DEFAULT NULL,
  address VARCHAR(256) DEFAULT NULL,
  PRIMARY KEY(id))ENGINE=INNODB DEFAULT CHARSET=utf8;
```

### 2.3.3 创建实体类

在main—java下创建一个三级包com.mybatis.domain,然后在domain目录下新建User.java

实体类中属性需和数据库中字段对应，以此类作为查询参数和返回结果集

```java
package com.mybatis.domain;

import java.util.Date;

public class User {
    private Integer id;
    private String username;
    private Date birthday;
    private String sex;
    private String address;
    
    //idea按住alt+insert生成getter,setter,toString方法，此处已省略
}
```

### 2.3.4 新建IUserDao接口文件

在main—java下创建一个三级包com.mybatis.dao,在再该目录下新建IUserDao接口文件

在该接口中定义一个查询全部用户的方法findAll( )，返回值为List

```java
package com.mybatis.dao;

import com.mybatis.domain.User;
import java.util.List;

public interface IUserDao {
    List<User> findAll();
}
```

### 2.3.5 SqlMapConfig.xml文件

在resource下新建SqlMapConfig.xml文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!--    配置环境-->
    <environments default="mysql">
        <!--配置mysql环境-->
        <environment id="mysql">
            <!--配置事务类型-->
            <transactionManager type="jdbc"></transactionManager>
            <!--配置数据源(连接池)-->
            <dataSource type="POOLED">
                <!--配置连接数据库的四个基本信息-->
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/xsh?serverTimezone=UTC"/>
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>
    </environments>
    <!--指定映射配置文件的位置，映射配置文件指的是每个Dao独立的配置文件-->
    <!--resources下的文件,因为是目录结构，所以用/不能用.-->
    <mappers>
        <mapper resource="com/mybatis/dao/IUserDao.xml"/>
    </mappers>
</configuration>
```

因为pom.xml文件中使用的是8.0.12版本的mysql驱动，所以此处配置数据库连接信息时使用：

```
driver : com.mysql.cj.jdbc.Driver
url : jdbc:mysql://localhost:3306/xsh?serverTimezone=UTC
```

如果是5.x.x版本的mysql驱动，则为:

```
driver : com.mysql.jdbc.Driver
url : jdbc:mysql://localhost:3306/xsh
```

### 2.3.6 IUserDao.xml文件

在resource下新建目录com.mybatis.dao(**三级目录，要一个一个创建**),在该目录下新建IUserDao.xml文件

其中**resource下新建的目录必须与前面定义接口的目录一致，xml文件名要和定义的接口文件名相同才能完成一 对一映射**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.mybatis.dao.IUserDao">
<!--namespace指的是main-java目录下的IUserDao接口，这是个三级包结构，所以可以用.也可以用/-->
<!--配置查询所有-->
<!-- mybatis中的所有查询，都必须返回resultType或者resultMap的值-->
    <select id="findAll"
        resultType="com.mybatis.domain.User">
        select *from user;
    </select>
</mapper>
```

## 2.4 编写测试类

```java
package com.mybatis.test;

import com.mybatis.dao.IUserDao;
import com.mybatis.domain.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.BasicConfigurator;
import java.io.InputStream;
import java.util.List;

public class MybatisTest {
    public static void main(String[] args) throws Exception{
//  BasicConfigurator.configure();//自动快速地使用缺省Log4j环境。
        //1.读取配置文件
        InputStream in= Resources.getResourceAsStream("SqlMapConfig.xml");
        //2.创建SqlSessionFactory工厂模式
//      SqlSessionFactory factory=null;//SqlSessionFactory是接口，不能new，所以用SqlSessionFactoryBuilder
        SqlSessionFactoryBuilder builder=new SqlSessionFactoryBuilder();
        SqlSessionFactory factory=builder.build(in);
        //3.使用工厂生产一个SqlSession对象
        SqlSession session=factory.openSession();
        //4.使用SqlSession创建Dao接口的代理对象
        IUserDao userDao=session.getMapper(IUserDao.class);
        //5.使用代理对象执行方法
        List<User> users=userDao.findAll();
        for(User user:users){
            System.out.println(user);
        }
        //6.释放资源
        session.close();
        in.close();
    }
}
```

运行时当出现以下警告时：

log4j:WARN No appenders could be found for logger (org.apache.ibatis.logging.LogFactory). log4j:WARN Please initialize the log4j system properly.

说明缺少日志配置文件,可在resources根目录下添加添加log4j.properties文件

```properties
log4j.rootLogger=DEBUG, A1
log4j.appender.A1=org.apache.log4j.ConsoleAppender
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n
```

或者在运行的方法中添加：

```java
BasicConfigurator.configure();//自动快速地使用缺省Log4j环境。
```

# 三. 基于注解的MyBatis使用

## 3.1 在持久层接口中添加注解

```java
public interface IUserDao {
    @Select("select*from user")//添加注解
    List<User> findAll();
}
```

## 3.2 修改SqlMapConfig.xml

因为是使用注解，所以需要先将IUserDao.xml删除，同时修改SqlMapConfig.xml中mappers标签的配置。

```xml
    <!--如果是用注解来配置的话，此处应该使用class属性指定被注解的dao层全限定类名-->
    <mappers>
        <mapper class="com.mybatis.dao.IUserDao"  />
    </mappers>
```