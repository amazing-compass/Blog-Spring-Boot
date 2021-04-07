# 一. 基于注解的IOC配置

注解配置和xml配置要实现的功能都是一样的，都是要降低程序间的耦合。只是配置的形式不一样。

在pom.xml内引入依赖：

使用到了spring-aop-5.0.2.RELEASE.jar这个包

```xml
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```

**使用注解形式实现xml配置的功能 :**

因为使用注解，所以需要告知spring要扫描的包，配置bean.xml：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://http://cdn.xiongliqian.com.springframework.org/schema/context"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans
        http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd
        http://http://cdn.xiongliqian.com.springframework.org/schema/context
        http://http://cdn.xiongliqian.com.springframework.org/schema/context/spring-context.xsd">

    <!--告知spring在创建容器时要扫描的包，配置所需要的标签不是在beans的约束中，而是一个名称为
    context名称空间和约束中-->
    <context:component-scan base-package="com.xsh" />
</beans>
```

## 1.1 创建对象的注解

它们的作用就和在XML配置文件中编写一个标签实现的功能是一样的

**@Component**:

- 作用：**用于把当前类对象存入spring容器中**

- 属性：

  value：用于指定bean的id。**当不写时，它的默认值是当前类名，且首字母改小写。**

  当只有一个属性时，可省略value,直接写双引号加内容，

  例如@Component(value="accountDao")可简写为@Component("accountDao")

- 用法：

  ```java
  package com.xsh.dao.impl;
  
  import com.xsh.dao.IAccountDao;
  import org.springframework.stereotype.Component;
  /**
   * 账户的接口实现类，模拟保存账户
   */
  @Component("accountDao")
  public class AccountDaoImpl implements IAccountDao {
  
      public  void saveAccount(){
          System.out.println("保存了账户");
      }
  }
  ```

  @Component("accountDao")相当于：

  ```xml
   <bean id="accountDao" class="com.xsh.dao.impl.AccountDaoImpl" />
  ```

以下三个注解它们的作用和属性与Component是一模一样。

- Controller：一般用在表现层
- Service：一般用在业务层
- Repository：一般用在持久层

它们三个是spring框架提供明确的MVC三层架构使用的注解，使三层对象更加清晰容易分辨

## 1.2 注入数据的注解

它们的作用就和在xml配置文件中的bean标签中写一个标签的作用是一样的

### 1.2.1 @Autowired

- 作用：
  - 自动按照类型注入。只要**容器中有唯一的一个bean对象类型和要注入的变量类型匹配**，就可以注入成功
  - 如果ioc容器中没有任何bean的类型和要注入的变量类型匹配，则报错。
- 出现位置：可以是变量上，也可以是方法上
- 细节： 在使用注解注入时，**set方法就不是必须的了**。

IAccountDao持久层接口：

```
public interface IAccountDao {
    void saveAccount();
}
```

在服务层自动注入持久层接口：

```java
package com.xsh.service;
/**
 * 在AccountService类注入数据类型为IAccountDao,变量名称为accountDao的数据
 */
@Service
public class AccountService {
    @Autowired
    private IAccountDao accountDao;

}
```

相当于：

```xml
<bean id="accountService" class="com.xsh.service.AccountService">
    <property name="accountDao" ref="IAccountDao"></property>
</bean>
<bean id="IAccountDao" class="com.xsh.dao.IAccountDao" />
```

当存在多个数据类型相同时，因为都实现了IAccountDao接口，所以都可以看成IAccountDao类型：

accountDao1：

```java
@Repository("accountDao1")
public class AccountDaoImpl implements IAccountDao{

    public  void saveAccount(){
        System.out.println("保存了账户1");
    }
}
```

accountDao2：

```java
@Repository("accountDao2")
public class AccountDaoImpl2  implements IAccountDao {
    public  void saveAccount(){
        System.out.println("保存了账户2");
    }
}
```

此时如果使用 **@Autowired**注解在服务层注入**private IAccountDao accountDao;**会报错

报错提示如下： expected single matching bean but found 2: accountDao1,accountDao2

因为accountDao1，accountDao2都是IAccountDao类型，所以**@Autowired**不知道注入哪一个

**修改方式一：修改自动注入时的变量名称**

```java
@Service
public class AccountService {
    /*当IAccountDao类型存在多个时，可以修改注入的变量名称实现自动注入*/
    @Autowired
    private IAccountDao accountDao1;

}
```

**修改方式二：使用@Qualifier注解指定注入的变量名称**

### 1.2.2 @Qualifier

作用：**在按照类型注入的基础之上再按照变量名称注入**。

它在给类成员注入时不能单独使用，需要结合@Autowired注解。但是在给方法参数注入时可以单独使用

属性： value：用于指定注入bean的id。

```java
@Service
public class AccountService {
    /*当IAccountDao类型存在多个时，可以使用@Qualifier注解指定注入的变量名称*/
    @Autowired
    @Qualifier("accountDao1")
    private IAccountDao accountDao;

}
```

### 1.2.3 @Resource

作用：直接按照bean的id注入。它可以独立使用 属性： **name：用于指定bean的id。**

```java
@Service
public class AccountService {
	@Resource(name = "accountDao2")
    private IAccountDao accountDao;
}
```

**以上三个注解注入都只能注入其他bean类型的数据，而基本类型和String类型无法使用上述注解实现。**

另外，**集合类型的注入只能通过XML来实现**。

### 1.2.4 @Value

作用：用于注入基本类型和String类型的数据，配合配置文件使用(.propertites和.yml) 属性： value：用于指定数据的值。它可以使用spring中SpEL(也就是spring的el表达式）

## 1.3 改变bean作用范围的注解

**@Scope**

作用：用于指定bean的作用范围，和在bean标签中使用scope属性实现的功能是一样的

属性：

```
value：指定范围的取值。
```

​	常用取值：singleton prototype

```java
@Scope("prototype")
public class AccountServiceImpl implements IAccountService {
    .......
}
```

## 1.4 生命周期相关注解

@PostConstruct：用于指定初始化方法

@PreDestroy ：用于指定销毁方法

它们的作用就和在bean标签中使用init-method和destroy-methode的作用是一样的

```java
    @PostConstruct
    public void  init(){
        System.out.println("初始化方法执行了");
    }

    @PreDestroy
    public void  destroy(){
        System.out.println("销毁方法执行了");
    }
```

## 1.5 配置文件注解

### @Configuration

- 作用：用于指定当前类是一个spring配置类，当创建容器时会从该类上加载注解。获取容器时需要使用@AnnotationApplicationContext(有@Configuration注解的类.class)。
- 属性： value:用于指定配置类的字节码

### @ComponentScan

- 作用：用于指定spring在初始化容器时要扫描的包。
- 属性： basePackages：用于指定要扫描的包。和该注解中的value属性作用一样。

```java
@Configuration 
@ComponentScan("com.xlq") 
public class SpringConfiguration { 
}
```

相当于：

```xml
<context:component-scan base-package="com.xlq"/>
```

### @Bean

- 作用： 该注解只能写在方法上，表明使用此方法创建一个对象，并且放入spring容器。
- 属性： name：给当前@Bean注解方法创建的对象指定一个名称(即bean的id）

```java
public class JdbcConfig {
    @Bean(name="dataSource") 
	public DataSource createDataSource() {
    	
	}
}
```

### @PropertySource

- 作用：用于加载.properties文件中的配置。例如我们配置数据源时，可以把连接数据库的信息写到properties配置文件中，就可以使用此注解指定properties配置文件的位置。
- 属性： value( )：用于指定properties文件位置。如果是在类路径下，需要写上classpath:

```java
@Configuration 
@PropertySource("classpath:jdbc.properties") 
public class JdbcConfig{ 
    
}
```

### @Import

- 作用：用于导入其他配置类，在引入其他配置类时，可以不用再写@Configuration注解。当然，写上也没问题。
- 属性： value({ })：用于指定其他配置类的字节码。

```java
@Configuration 
@ComponentScan(basePackages = "com.xlq") 
@Import({ JdbcConfig.class}) 
public class SpringConfiguration { 
    
}
```

# 二. 基于xml的IOC案例

**使用spring的IoC实现账户的CRUD**

## 2.1 创建数据库

```mysql
CREATE TABLE account (
  id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(40) DEFAULT NULL,
  money float DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=gbk
```

## 2.2 环境搭建与实现功能

新建一个maven项目

### 2.2.1 pom.xml导入依赖

使用dbutils下的QueryRunner执行SQL， 使用c3p0数据源

```xml
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>commons-dbutils</groupId>
            <artifactId>commons-dbutils</artifactId>
            <version>1.4</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.6</version>
        </dependency>

        <dependency>
            <groupId>c3p0</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.9.1.2</version>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
    </dependencies>
```

### 2.2.2 创建Account实体类

```java
package com.xlq.domain;

import java.io.Serializable;
public class Account implements Serializable {

    private Integer id;
    private String name;
    private Float money;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getMoney() {
        return money;
    }

    public void setMoney(Float money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", money=" + money +
                '}';
    }
}
```

### 2.2.3 定义账户的持久层接口

```java
package com.xlq.dao;

import com.xlq.domain.Account;
import java.util.List;

public interface IAccountDao {
    //查询所有
    List<Account> findAllAccount();

    //查询一个
    Account findAccountById(Integer accountId);

    //保存
    void saveAccount(Account account);

    //更新
    void updateAccount(Account account);

    //删除
    void deleteAccount(Integer acccountId);
}
```

### 2.2.4 账号持久层接口的实现类

```java
package com.xlq.dao.impl;

import com.xlq.dao.IAccountDao;
import com.xlq.domain.Account;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.util.List;

/**
 * 账户的持久层实现类
 */
public class AccountDaoImpl implements IAccountDao {

    private QueryRunner runner;

    public void setRunner(QueryRunner runner) {
        this.runner = runner;
    }

    @Override
    public List<Account> findAllAccount() {
        try{
            return runner.query("select * from account",new BeanListHandler<Account>(Account.class));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Account findAccountById(Integer accountId) {
        try{
            return runner.query("select * from account where id = ? ",new BeanHandler<Account>(Account.class),accountId);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAccount(Account account) {
        try{
            runner.update("insert into account(name,money)values(?,?)",account.getName(),account.getMoney());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateAccount(Account account) {
        try{
            runner.update("update account set name=?,money=? where id=?",account.getName(),account.getMoney(),account.getId());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAccount(Integer accountId) {
        try{
            runner.update("delete from account where id=?",accountId);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 2.2.5 账户的服务层接口

```java
package com.xlq.service;

import com.xlq.domain.Account;
import java.util.List;

public interface IAccountService {

    List<Account> findAllAccount();

    Account findAccountById(Integer accountId);

    void saveAccount(Account account);

    void updateAccount(Account account);
    
    void deleteAccount(Integer acccountId);
}
```

### 2.2.6 账户的服务层实现类

**service层调用dao层**

```java
package com.xlq.service.impl;

import com.xlq.dao.IAccountDao;
import com.xlq.domain.Account;
import com.xlq.service.IAccountService;

import java.util.List;

/**
 * 账户的业务层实现类
 */
public class AccountServiceImpl implements IAccountService{

    private IAccountDao accountDao;

    public void setAccountDao(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public List<Account> findAllAccount() {
        return accountDao.findAllAccount();
    }

    @Override
    public Account findAccountById(Integer accountId) {
        return accountDao.findAccountById(accountId);
    }

    @Override
    public void saveAccount(Account account) {
        accountDao.saveAccount(account);
    }

    @Override
    public void updateAccount(Account account) {
        accountDao.updateAccount(account);
    }

    @Override
    public void deleteAccount(Integer acccountId) {
        accountDao.deleteAccount(acccountId);
    }
}
```

### 2.2.7 bean.xml文件配置

在resources目录下新建一个bean.xml文件,注入需要的数据：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongliqian.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongliqian.com.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://http://cdn.xiongliqian.com.springframework.org/schema/beans
        http://http://cdn.xiongliqian.com.springframework.org/schema/beans/spring-beans.xsd">
    <!-- 配置Service -->
    <bean id="accountService" class="com.xsh.service.impl.AccountServiceImpl">
        <!-- 注入dao -->
        <property name="accountDao" ref="accountDao"></property>
    </bean>

    <!--配置Dao对象-->
    <bean id="accountDao" class="com.xsh.dao.impl.AccountDaoImpl">
        <!-- 注入QueryRunner -->
        <property name="runner" ref="runner"></property>
    </bean>

    <!--配置QueryRunner-->
    <bean id="runner" class="org.apache.commons.dbutils.QueryRunner" scope="prototype">
        <!--注入数据源-->
        <constructor-arg name="ds" ref="dataSource"></constructor-arg>
    </bean>

    <!-- 配置数据源 -->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <!--连接数据库的必备信息-->
        <property name="driverClass" value="com.mysql.jdbc.Driver"></property>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/xsh"></property>
        <property name="user" value="root"></property>
        <property name="password" value="123456"></property>
    </bean>
</beans>
```

## 2.3 测试IOC案例功能

```java
package com.xlq.test;

import com.xlq.domain.Account;
import com.xlq.service.IAccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * 使用Junit单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:bean.xml")
public class AccountServiceTest {

    @Autowired
    private  IAccountService as;

    @Test
    public void testFindAll() {
        List<Account> accounts = as.findAllAccount();
        for(Account account : accounts){
            System.out.println(account);
        }
    }

    @Test
    public void testFindOne() {
        Account account = as.findAccountById(1);
        System.out.println(account);
    }

    @Test
    public void testSave() {
        Account account = new Account();
        account.setName("test");
        account.setMoney(12345f);
        as.saveAccount(account);

    }

    @Test
    public void testUpdate() {
        Account account = as.findAccountById(2);
        account.setMoney(23456f);
        as.updateAccount(account);
    }

    @Test
    public void testDelete() {
        as.deleteAccount(4);
    }
}
```

# 三. 使用注解改造IOC案例

使用注解配置实现bean.xml文件的功能，将bean.xml文件去除,实现纯注解

```xml
    <!-- 配置Service -->
    <bean id="accountService" class="com.xlq.service.impl.AccountServiceImpl">
        <!-- 注入dao -->
        <property name="accountDao" ref="accountDao"></property>
    </bean>
```

可改造为：

```java
@Service("accountService")
public class AccountServiceImpl implements IAccountService{

    @Autowired
    private IAccountDao accountDao;
}
```

创建一个jdbc的配置文件：

```properties
jdbc.driver=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/xlq
jdbc.username=root
jdbc.password=123456
```

jdbcConfig：

```java
package config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import javax.sql.DataSource;

/**
 * 和spring连接数据库相关的配置类
 */
public class JdbcConfig {

    @Value("${jdbc.driver}")
    private String driver;

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    /**
     * 用于创建一个QueryRunner对象
     * @param dataSource
     * @return
     */
    @Bean(name="runner")
    @Scope("prototype")
    public QueryRunner createQueryRunner(@Qualifier("ds") DataSource dataSource){
        return new QueryRunner(dataSource);
    }

    /**
     * 创建数据源对象
     */
    @Bean(name="ds")
    public DataSource createDataSource(){
        try {
            ComboPooledDataSource ds = new ComboPooledDataSource();
            ds.setDriverClass(driver);
            ds.setJdbcUrl(url);
            ds.setUser(username);
            ds.setPassword(password);
            return ds;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
```

SpringConfiguration：

```java
package config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * 该类是一个配置类，它的作用和bean.xml是一样的
 */
//@Configuration
@ComponentScan("com.xlq")
@Import(JdbcConfig.class)
@PropertySource("classpath:jdbcConfig.properties")
public class SpringConfiguration {
    
}
```

获取注解配置的容器：

```java
ApplicationContext ac = new AnnotationConfigApplicationContext(SpringConfiguration.class);
```

# 四. Spring注解和XML的选择问题

- 注解的优势： 配置简单，维护方便（我们找到类，就相当于找到了对应的配置）。
- XML的优势： 修改时，不用改源码。不涉及重新编译和部署。

当Bean来自第三方时，使用xml；Bean的实现类由用户自己定义时，使用注解配置