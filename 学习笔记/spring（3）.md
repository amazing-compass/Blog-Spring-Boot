# 一. AOP 的相关概念

在软件业，AOP为Aspect Oriented Programming的缩写，意为：面向切面编程，**通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术**。AOP是OOP的延续，是软件开发中的一个热点，也是Spring框架中的一个重要内容，是函数式编程的一种衍生范型。**利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高了开发的效率**。

- AOP 的作用及优势

作用：**在程序运行期间，不修改源码对已有方法进行增强。**

优势： 1. 减少重复代码 2. 提高开发效率 3. 维护方便

- AOP 的实现方式

  使用动态代理技术

# 二. 转账案例

在 **spring(二) **章节账户的CRUD案例中，加入以下方法：

在IAccountDao和AccountDaoImpl中加入根据账户名称查询账号的接口和实现类

IAccountDao：

```java
/**
 * 根据名称查询账户接口
 * @param accountName
 * @return  如果有唯一的一个结果就返回，如果没有结果就返回null
 *          如果结果集超过一个就抛异常
 */
Account findAccountByName(String accountName);
```

AccountDaoImpl：

```java
    @Override
    public Account findAccountByName(String accountName) {
        try{
            List<Account> accounts = runner.query(connectionUtils.getThreadConnection(),"select * from account where name = ? ",new BeanListHandler<Account>(Account.class),accountName);
            if(accounts == null || accounts.size() == 0){
                return null;
            }
            if(accounts.size() > 1){
                throw new RuntimeException("结果集不唯一，数据有问题");
            }
            return accounts.get(0);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
```

在IAccountService和IAccountServiceImpl中加入转账的接口和实现类

IAccountService：

```java
    /**
     * 转账接口
     * @param sourceName        转出账户名称
     * @param targetName        转入账户名称
     * @param money             转账金额
     */
    void transfer(String sourceName,String targetName,Float money);
```

IAccountServiceImpl：

```java
    @Override
    public void transfer(String sourceName, String targetName, Float money) {
        System.out.println("转账案例的实现类");
            //1根据名称查询转出账户
            Account source = accountDao.findAccountByName(sourceName);
            //2根据名称查询转入账户
            Account target = accountDao.findAccountByName(targetName);
            //3转出账户减钱
            source.setMoney(source.getMoney()-money);
            //4转入账户加钱
            target.setMoney(target.getMoney()+money);
            //5更新转出账户
            accountDao.updateAccount(source);

	        // int i=1/0;

            //6更新转入账户
            accountDao.updateAccount(target);
    }
```

运行测试方法：

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:bean.xml")
public class AccountServiceTest {

    @Autowired
    private  IAccountService as;

    @Test
    public  void testTransfer(){
        as.transfer("aaa","bbb",100f);
    }

}
```

正常情况下可以实现转账功能，但异常情况出现时，因为要同时更新转入账户和转出账户，所以当中间出现异常时会导致后面的更新操作失败而前面的更新操作成功，这是不符合业务逻辑的；

如手动在两个更新操作间加入**int i=1/0;**来模拟异常

这是因为执行两次查询账户和两次更新账户时，四次访问数据库会创建四个不同的连接，当更新中途出现错误后不会影响之前的连接，所以前面的更新操作成功而后面的更新操作失败。

**问题解决方法**：

- 使用ThreadLocal对象把connection和当前线程绑定,从而使一个线程中只有一个能控制事务的对象
- 使用同一个事物来管理，在业务层来控制事务的提交和回滚

新加连接的工具类：

```java
package com.xsh.utils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 连接的工具类，它用于从数据源中获取一个连接，并且实现和线程的绑定
 */
public class ConnectionUtils {

    private ThreadLocal<Connection> tl = new ThreadLocal<Connection>();

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取当前线程上的连接
     * @return
     */
    public Connection getThreadConnection() {
        try{
            //1.先从ThreadLocal上获取
            Connection conn = tl.get();
            //2.判断当前线程上是否有连接
            if (conn == null) {
                //3.从数据源中获取一个连接，并且存入ThreadLocal中
                conn = dataSource.getConnection();
                tl.set(conn);
            }
            //4.如果不为空则直接返回当前线程上的连接，实现一个线程只有一个连接
            return conn;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 事物管理工具类TransactionManager中释放连接后并不是真正的关闭连接，而是把连接还回连接池中
     * 所以getThreadConnection方法内判断当前线程上是否有连接时，此时一定是有连接的，不过已经被close过	   * 还回连接池中不能使用了，所以TransactionManager.release()方法内释放连接后还需要把连接和线程解	  * 绑。
     */
    public void removeConnection(){
        tl.remove();
    }
}
```

新加事物管理工具类：

```java
package com.xsh.utils;

/**
 * 和事务管理相关的工具类，它包含了，开启事务，提交事务，回滚事务和释放连接
 */
public class TransactionManager {

    private ConnectionUtils connectionUtils;

    public void setConnectionUtils(ConnectionUtils connectionUtils) {
        this.connectionUtils = connectionUtils;
    }

    /**
     * 开启事务
     */
    public  void beginTransaction(){
        try {
            connectionUtils.getThreadConnection().setAutoCommit(false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 提交事务
     */
    public  void commit(){
        try {
            connectionUtils.getThreadConnection().commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 回滚事务
     */
    public  void rollback(){
        try {
            connectionUtils.getThreadConnection().rollback();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 释放连接
     */
    public  void release(){
        try {
            connectionUtils.getThreadConnection().close();//还回连接池中
            connectionUtils.removeConnection(); //把连接和线程解绑
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
```

修改service层的AccountServiceImpl方法：

```java
package com.xsh.service.impl;

import com.xsh.dao.IAccountDao;
import com.xsh.domain.Account;
import com.xsh.service.IAccountService;
import com.xsh.utils.TransactionManager;

import java.util.List;

/**
 * 账户的业务层实现类
 *
 * 事务控制应该都是在业务层
 */
public class AccountServiceImpl_OLD implements IAccountService{

    private IAccountDao accountDao;
    private TransactionManager txManager; //使用同一事物管理

    public void setTxManager(TransactionManager txManager) {//set方法方便bean.xml内注入
        this.txManager = txManager;
    }

    public void setAccountDao(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }


    @Override
    public void updateAccount(Account account) {
        try {
            //1.开启事务
            txManager.beginTransaction();
            //2.执行操作
            accountDao.updateAccount(account);
            //3.提交事务
            txManager.commit();
        }catch (Exception e){
            //4.回滚操作
            txManager.rollback();
        }finally {
            //5.释放连接
            txManager.release();
        }

    }


    @Override
    public void transfer(String sourceName, String targetName, Float money) {
        try {
            //1.开启事务
            txManager.beginTransaction();
            //2.执行操作

            //2.1根据名称查询转出账户
            Account source = accountDao.findAccountByName(sourceName);
            //2.2根据名称查询转入账户
            Account target = accountDao.findAccountByName(targetName);
            //2.3转出账户减钱
            source.setMoney(source.getMoney()-money);
            //2.4转入账户加钱
            target.setMoney(target.getMoney()+money);
            //2.5更新转出账户
            accountDao.updateAccount(source);

            int i=1/0;

            //2.6更新转入账户
            accountDao.updateAccount(target);
            //3.提交事务
            txManager.commit();

        }catch (Exception e){
            //4.回滚操作
            txManager.rollback();
            e.printStackTrace();
        }finally {
            //5.释放连接
            txManager.release();
        }


    }
}
```

这样当中途出现异常后，事物回滚，因为使用同一事物管理，所以都会回滚而不执行。

通过对业务层改造，已经可以实现事务控制了，但是由于我们添加了事务控制，也产生了一个新的问题：**业务层方法变得臃肿了，里面充斥着很多重复代码。并且业务层方法和事务控制方法耦合了。**

# 三. 动态代理

动态代理的特点

- 字节码随用随创建，随用随加载。
- 它与静态代理的区别也在于此。因为静态代理是字节码一上来就创建好，并完成加载。
- 装饰者模式就是静态代理的一种体现。
- 不修改代码的情况下对方法进行增强

动态代理常用的有两种方式

- 基于接口的动态代理
  - 提供者：JDK 官方的 Proxy 类。
  - 要求：被代理类最少实现一个接口
- 基于子类的动态代理
  - 提供者：第三方的 CGLib，如果报 asmxxxx 异常，需要导入 asm.jar。
  - 要求：被代理类不能用 final 修饰的类（最终类）。

## 3.1 基于接口的动态代理

```java
package com.xsh.proxy;

/**
 * 对生产厂家要求的接口
 */
public interface IProducer {

    /**
     * 销售
     * @param money
     */
    public void saleProduct(float money);

    /**
     * 售后
     * @param money
     */
    public void afterService(float money);
}
```

实现类：

```java
package com.xsh.proxy;

/**
 * 一个生产者,实现接口的销售和售后服务
 */
public class Producer implements IProducer{

    /**
     * 销售
     * @param money
     */
    public void saleProduct(float money){
        System.out.println("销售产品，并拿到钱："+money);
    }

    /**
     * 售后
     * @param money
     */
    public void afterService(float money){
        System.out.println("提供售后服务，并拿到钱："+money);
    }
}
```

使用动态代理：

```java
package com.xsh.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 模拟一个消费者
 */
public class Client {

    public static void main(String[] args) {
        //method.invoke()匿名内部类访问外部成员变量时，外部变量要求是最终的，也就是用final修饰
        final Producer producer = new Producer(); //最终类不能再创建子类

        /**
         *  如何创建代理对象：
         *      使用Proxy类中的newProxyInstance方法
         *  创建代理对象的要求：
         *      被代理类最少实现一个接口，如果没有则不能使用
         *  newProxyInstance方法的参数：
         *      ClassLoader：类加载器
         *          它是用于加载代理对象字节码的。和被代理对象使用相同的类加载器。固定写法。
         *      Class[]：字节码数组
         *          它是用于让代理对象和被代理对象有相同方法。固定写法。
         *      InvocationHandler：用于提供增强的代码
         *          它是让我们写如何代理。我们一般都是些一个该接口的实现类，通常情况下都是匿名内部类，但不是必须的。
         *          此接口的实现类都是谁用谁写。
         */
       IProducer proxyProducer = (IProducer) Proxy.newProxyInstance(producer.getClass().getClassLoader(),
                producer.getClass().getInterfaces(),
                new InvocationHandler() {
                    /**
                     * 作用：执行被代理对象的任何接口方法都会经过该方法
                     * 方法参数的含义
                     * @param proxy   代理对象的引用
                     * @param method  当前执行的方法
                     * @param args    当前执行方法所需的参数
                     * @return        和被代理对象方法有相同的返回值
                     * @throws Throwable
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //提供增强的代码
                        //1.定义返回值
                        Object returnValue = null;

                        //2.获取方法执行的参数，因为只有一个参数，所以写0
                        Float money = (Float)args[0];
                        //3.判断当前方法是不是销售
                        if("saleProduct".equals(method.getName())) {
                            returnValue = method.invoke(producer, money*0.8f);
                        }
                        return returnValue;
                    }
                });
        proxyProducer.saleProduct(10000f);
    }
}
```

**被代理类必须实现一个接口，否则就会报错**,为解决这个问题，可以使用基于子类的动态代理

## 3.2 基于子类的动态代理

需要引入jar包：

```xml
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>cglib</groupId>
            <artifactId>cglib</artifactId>
            <version>2.1_3</version>
        </dependency>
    </dependencies>
```

Producer：(未实现接口)

```java
package com.xsh.cglib;

/**
 * 一个生产者
 */
public class Producer {

    /**
     * 销售
     */
    public void saleProduct(float money){
        System.out.println("销售产品，并拿到钱："+money);
    }

    /**
     * 售后
     */
    public void afterService(float money){
        System.out.println("提供售后服务，并拿到钱："+money);
    }
}
```

使用基于子类的动态代理:

```java
package com.xsh.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 模拟一个消费者
 */
public class Client {

    public static void main(String[] args) {
        final Producer producer = new Producer();

        /**
         *  基于子类的动态代理：
         *      涉及的类：Enhancer
         *      提供者：第三方cglib库
         *  如何创建代理对象：
         *      使用Enhancer类中的create方法
         *  创建代理对象的要求：
         *      被代理类不能是最终类
         *  create方法的参数：
         *      Class：字节码
         *          它是用于指定被代理对象的字节码。
         *
         *      Callback：用于提供增强的代码
         *          它是让我们写如何代理。我们一般都是些一个该接口的实现类，通常情况下都是匿名内部类，但不是必须的。
         *          此接口的实现类都是谁用谁写。
         *          我们一般写的都是该接口的子接口实现类：MethodInterceptor
         */
        Producer cglibProducer = (Producer)Enhancer.create(producer.getClass(), new MethodInterceptor() {
            /**
             * 执行被代理对象的任何方法都会经过该方法
             * @param proxy
             * @param method
             * @param args
             *    以上三个参数和基于接口的动态代理中invoke方法的参数是一样的
             * @param methodProxy ：当前执行方法的代理对象
             * @return
             * @throws Throwable
             */
            @Override
            public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                //提供增强的代码
                Object returnValue = null;

                //1.获取方法执行的参数
                Float money = (Float)args[0];
                //2.判断当前方法是不是销售
                if("saleProduct".equals(method.getName())) {
                    returnValue = method.invoke(producer, money*0.8f);
                }
                return returnValue;
            }
        });
        cglibProducer.saleProduct(12000f);
    }
}
```

## 3.3 使用动态代理解决转账案例代码过多的问题

使用动态代理控制事物：

对IAccountService进行增强，添加事物的支持：

```java
package com.xsh.factory;

import com.xsh.service.IAccountService;
import com.xsh.utils.TransactionManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 用于创建Service的代理对象的工厂
 */
public class BeanFactory {

    private IAccountService accountService;

    private TransactionManager txManager;

    public void setTxManager(TransactionManager txManager) {
        this.txManager = txManager;
    }


    public final void setAccountService(IAccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 获取Service代理对象
     * @return
     */
    public IAccountService getAccountService() {
        return (IAccountService)Proxy.newProxyInstance(accountService.getClass().getClassLoader(),
                accountService.getClass().getInterfaces(),
                new InvocationHandler() {
                    /**
                     * 添加事务的支持
                     *invoke方法能拦截被代理对象中的所有方法
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        //当执行的方法名为test时，不执行增强；所以test为连接点但不是切入点
                        if("test".equals(method.getName())){
                            return method.invoke(accountService,args);
                        }

                        Object rtValue = null;
                        try {
                            //1.开启事务(前置通知)
                            txManager.beginTransaction();
                            //2.执行操作
                            rtValue = method.invoke(accountService, args);
                            //3.提交事务(后置通知)
                            txManager.commit();
                            //4.返回结果
                            return rtValue;
                        } catch (Exception e) {
                            //5.回滚操作(异常通知)
                            txManager.rollback();
                            throw new RuntimeException(e);
                        } finally {
                            //6.释放连接(最终通知)
                            txManager.release();
                        }
                    }
                });

    }
}
```

# 四. spring中的aop

通过配置的方式，实现转账案例

## 4.1 AOP 相关术语

- **Joinpoint( 连接点)：**

所谓连接点是指那些被拦截到的点。在 spring 中,这些点指的是方法,因为 spring 只支持方法类型的 连接点。

- **Pointcut( 切入点)：**

所谓切入点是指我们要对哪些 Joinpoint 进行拦截(加强)的定义

- **Advice( 通知/ 增强)：**

所谓通知是指拦截到 Joinpoint 之后所要做的事情就是通知。 通知的类型：前置通知,后置通知,异常通知,最终通知,环绕通知。

- **Introduction( 引介)：**

引介是一种特殊的通知在不修改类代码的前提下, Introduction 可以在运行期为类动态地添加一些方 法或 Field。

- **Target( 目标对象)：**

代理的目标对象

- **Weaving( 织入)：**

是指把增强应用到目标对象来创建新的代理对象的过程。 spring 采用动态代理织入，而 AspectJ 采用编译期织入和类装载期织入。

- **Proxy （代理）:**

一个类被 AOP 织入增强后，就产生一个结果代理类。

- **Aspect( 切面)：**

是切入点和通知（引介）的结合。

## 4.2 学习 spring 中的 AOP 要明确的事

- 开发阶段（我们做的） 编写核心业务代码（开发主线） 把公用代码抽取出来，制作成通知。（开发阶段最后再做） 在配置文件中，声明切入点与通知间的关系，即切面。
- 运行阶段（Spring 框架完成的） Spring 框架监控切入点方法的执行。一旦监控到切入点方法被运行，使用代理机制，动态创建目标对 象的代理对象，根据通知类别，在代理对象的对应位置，将通知对应的功能织入，完成完整的代码逻辑运行。

## 4.3 基于 XML的AOP 配置

在学习 spring 的 aop 时，采用模拟账户转账作为示例，并且把 spring 的 ioc 也一起应用进来。

### 4.3.1 环境搭建

新建一个maven工程，在pom.xml文件中导入下列依赖：

```xml
   <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.8.7</version>
        </dependency>
    </dependencies>
```

业务层接口：

```java
package com.xsh.service;

/**
 * 账户的业务层接口
 */
public interface IAccountService {

 	/* 模拟保存账户*/
   void saveAccount();

    /* 模拟更新账户*/
   void updateAccount(int i);

    /*删除账户*/
   int  deleteAccount();
}
```

业务层接口实现类：

```java
package com.xsh.service.impl;

import com.xsh.service.IAccountService;

/**
 * 账户的业务层实现类
 */
public class AccountServiceImpl implements IAccountService{

    @Override
    public void saveAccount() {
        System.out.println("执行了保存");
    }

    @Override
    public void updateAccount(int i) {
        System.out.println("执行了更新"+i);

    }

    @Override
    public int deleteAccount() {
        System.out.println("执行了删除");
        return 0;
    }
}
```

日志类,用于测试通知，模拟共用的代码块：

```java
package com.xsh.utils;

/**
 * 用于记录日志的工具类，它里面提供了公共的代码
 */
public class Logger {

    /**
     * 用于打印日志：计划让其在切入点方法执行之前执行（切入点方法就是业务层方法）
     */
    public  void printLog(){
        System.out.println("Logger类中的pringLog方法开始记录日志了。。。");
    }
}
```

bean.xml,导入aop的约束：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongsihao.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongsihao.com.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://http://cdn.xiongsihao.com.springframework.org/schema/aop"
       xsi:schemaLocation="http://http://cdn.xiongsihao.com.springframework.org/schema/beans
        http://http://cdn.xiongsihao.com.springframework.org/schema/beans/spring-beans.xsd
        http://http://cdn.xiongsihao.com.springframework.org/schema/aop
        http://http://cdn.xiongsihao.com.springframework.org/schema/aop/spring-aop.xsd">

    <!-- 配置srping的Ioc,把service对象配置进来-->
    <bean id="accountService" class="com.xsh.service.impl.AccountServiceImpl"></bean>

    <!-- 配置Logger类 -->
    <bean id="logger" class="com.xsh.utils.Logger"></bean>

    <!--配置AOP-->
    <aop:config>
        <!--配置切面 -->
        <aop:aspect id="logAdvice" ref="logger">
            <!-- 配置通知的类型，并且建立通知方法和切入点方法的关联-->
            <aop:before method="printLog" pointcut="execution(* com.xsh.service.impl.*.*(..))"></aop:before>
        </aop:aspect>
    </aop:config>

</beans>
```

### 4.3.2 AOPTest类运行测试

```java
package com.xsh.test;

import com.xsh.service.IAccountService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 测试AOP的配置
 */
public class AOPTest {

    public static void main(String[] args) {
        //1.获取容器
        ApplicationContext ac = new ClassPathXmlApplicationContext("bean.xml");
        //2.获取对象
        IAccountService as = (IAccountService)ac.getBean("accountService");
        //3.执行方法的同时也会执行配置的通知类
        as.saveAccount();
    }
}
```

### 4.3.3 bean.xml配置步骤解析

- 配置srping的Ioc,把service对象配置进来

```xml
 <bean id="accountService" class="com.xsh.service.impl.AccountServiceImpl"></bean>
```

- 把通知Bean也交给spring来管理

```xml
<!-- 配置Logger类 -->
<bean id="logger" class="com.xsh.utils.Logger"></bean>
```

- 使用aop:aspect标签表明配置切面

  id属性：是给切面提供一个唯一标识

  ref属性：是指定通知类bean的Id。

- 在aop:aspect标签的内部使用对应标签来配置通知的类型

我们现在示例是让printLog方法在切入点方法执行之前之前：所以是前置通知

**aop:before**：表示配置前置通知

​	method属性：用于指定类中哪个方法是前置通知

​	pointcut属性：用于指定**切入点表达式**，该表达式的含义指的是对业务层中哪些方法增强

- 切入点表达式

  关键字：execution(表达式)

  表达式： 访问修饰符 返回值 包名.包名.包名...类名.方法名(参数列表)

  ```java
  标准的表达式写法：
  		public void com.xsh.service.impl.AccountServiceImpl.saveAccount()
  访问修饰符可以省略
          void com.xsh.service.impl.AccountServiceImpl.saveAccount()
  返回值可以使用通配符，表示任意返回值
          * com.xsh.service.impl.AccountServiceImpl.saveAccount()
  包名可以使用通配符，表示任意包。但是有几级包，就需要写几个*.
          * *.*.*.*.AccountServiceImpl.saveAccount())
  包名可以使用..表示当前包及其子包
          * *..AccountServiceImpl.saveAccount()
  类名和方法名都可以使用*来实现通配
          * *..*.*()
  参数列表：
         可以直接写数据类型：
              基本类型直接写名称           int
              引用类型写包名.类名的方式   java.lang.String
          可以使用通配符表示任意类型，但是必须有参数
          可以使用..表示有无参数均可，有参数可以是任意类型
  全通配写法：
        * *..*.*(..)
  
  实际开发中切入点表达式的通常写法：
       切入到业务层实现类下的所有方法
       * com.xsh.service.impl.*.*(..)
  ```

  ```xml
      <!--配置AOP-->
  <aop:config>
          <!--配置切面 -->
       <aop:aspect id="logAdvice" ref="logger">
              <!-- 配置通知的类型，并且建立通知方法和切入点方法的关联-->
              <aop:before method="printLog" pointcut="execution(* com.xsh.service.impl.*.*(..))"></aop:before>
       </aop:aspect>
  </aop:config>
  ```

## 4.4 四种常用通知类型

修改测试用的Logger类：

```java
package com.xsh.utils;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 用于记录日志的工具类，它里面提供了公共的代码
 */
public class Logger {

    /**
     * 前置通知
     */
    public  void beforePrintLog(){
        System.out.println("前置通知Logger类中的beforePrintLog方法开始记录日志了。。。");
    }

    /**
     * 后置通知
     */
    public  void afterReturningPrintLog(){
        System.out.println("后置通知Logger类中的afterReturningPrintLog方法开始记录日志了。。。");
    }
    /**
     * 异常通知
     */
    public  void afterThrowingPrintLog(){
        System.out.println("异常通知Logger类中的afterThrowingPrintLog方法开始记录日志了。。。");
    }

    /**
     * 最终通知
     */
    public  void afterPrintLog(){
        System.out.println("最终通知Logger类中的afterPrintLog方法开始记录日志了。。。");
    }

    /**
     * 环绕通知
     * 问题：
     *      当我们配置了环绕通知之后，切入点方法没有执行，而通知方法执行了。
     * 分析：
     *      通过对比动态代理中的环绕通知代码，发现动态代理的环绕通知有明确的切入点方法调用，而我们的代码中没有。
     * 解决：
     *      Spring框架为我们提供了一个接口：ProceedingJoinPoint。该接口有一个方法proceed()，此方法就相当于明确调用切入点方法。
     *      该接口可以作为环绕通知的方法参数，在程序执行时，spring框架会为我们提供该接口的实现类供我们使用。
     *
     * spring中的环绕通知：
     *      它是spring框架为我们提供的一种可以在代码中手动控制增强方法何时执行的方式。
     *		通常情况下，环绕通知都是独立使用的.
     *		根据代码位置判断是什么通知
     */
    public Object aroundPrintLog(ProceedingJoinPoint pjp){
        Object rtValue = null;
        try{
            Object[] args = pjp.getArgs();//得到方法执行所需的参数

            System.out.println("Logger类中的aroundPrintLog方法开始记录日志了。。。前置");

            rtValue = pjp.proceed(args);//明确调用业务层方法（切入点方法）

            System.out.println("Logger类中的aroundPrintLog方法开始记录日志了。。。后置");

            return rtValue;
        }catch (Throwable t){
            System.out.println("Logger类中的aroundPrintLog方法开始记录日志了。。。异常");
            throw new RuntimeException(t);
        }finally {
            System.out.println("Logger类中的aroundPrintLog方法开始记录日志了。。。最终");
        }
    }
}
```

修改bean.xml：

- aop:pointcut标签：

配置切入点表达式 id属性用于指定表达式的唯一标识。expression属性用于指定表达式内容 此标签写在aop:aspect标签内部只能当前切面使用。 它还可以写在aop:aspect外面，此时就变成了所有切面可用

- pointcut-ref属性: 引用定义好的切入点表达式
- aop:before标签: 配置前置通知，在切入点方法执行之前执行
- aop:after-returning：配置后置通知：在切入点方法正常执行之后值。它和异常通知永远只能执行一个
- aop:after-throwing：配置异常通知：在切入点方法执行产生异常之后执行。它和后置通知永远只能执行一个
- aop:after ：配置最终通知：无论切入点方法是否正常执行它都会在其后面执行
- aop:around：配置环绕通知 详细的注释请看Logger类中

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongsihao.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongsihao.com.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://http://cdn.xiongsihao.com.springframework.org/schema/aop"
       xsi:schemaLocation="http://http://cdn.xiongsihao.com.springframework.org/schema/beans
        http://http://cdn.xiongsihao.com.springframework.org/schema/beans/spring-beans.xsd
        http://http://cdn.xiongsihao.com.springframework.org/schema/aop
        http://http://cdn.xiongsihao.com.springframework.org/schema/aop/spring-aop.xsd">

    <!-- 配置srping的Ioc,把service对象配置进来-->
    <bean id="accountService" class="com.xsh.service.impl.AccountServiceImpl"></bean>

    <!-- 配置Logger类 -->
    <bean id="logger" class="com.xsh.utils.Logger"></bean>

    <!--配置AOP-->
    <aop:config>
        <aop:pointcut id="pt1" expression="execution(* com.xsh.service.impl.*.*(..))"></aop:pointcut>
        <!--配置切面 -->
        <aop:aspect id="logAdvice" ref="logger">
            <!--<aop:before method="beforePrintLog" pointcut-ref="pt1" ></aop:before>
            <aop:after-returning method="afterReturningPrintLog" pointcut-ref="pt1"></aop:after-returning>
            <aop:after-throwing method="afterThrowingPrintLog" pointcut-ref="pt1"></aop:after-throwing>
            <aop:after method="afterPrintLog" pointcut-ref="pt1"></aop:after>-->

            <!-- 配置环绕通知,通常单独使用，详细的注释请看Logger类中-->
            <aop:around method="aroundPrintLog" pointcut-ref="pt1"></aop:around>
        </aop:aspect>
    </aop:config>

</beans>
```

## 4.5 基于注解的 AOP 配置

修改bean.xml：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://http://cdn.xiongsihao.com.springframework.org/schema/beans"
       xmlns:xsi="http://http://cdn.xiongsihao.com.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://http://cdn.xiongsihao.com.springframework.org/schema/aop"
       xmlns:context="http://http://cdn.xiongsihao.com.springframework.org/schema/context"
       xsi:schemaLocation="http://http://cdn.xiongsihao.com.springframework.org/schema/beans
        http://http://cdn.xiongsihao.com.springframework.org/schema/beans/spring-beans.xsd
        http://http://cdn.xiongsihao.com.springframework.org/schema/aop
        http://http://cdn.xiongsihao.com.springframework.org/schema/aop/spring-aop.xsd
        http://http://cdn.xiongsihao.com.springframework.org/schema/context
        http://http://cdn.xiongsihao.com.springframework.org/schema/context/spring-context.xsd">

    <!-- 配置spring创建容器时要扫描的包-->
    <context:component-scan base-package="com.xsh"></context:component-scan>

    <!-- 配置spring开启注解AOP的支持 -->
    <aop:aspectj-autoproxy></aop:aspectj-autoproxy>
</beans>
```

使用注解替换xml配置完成功能：

```java
package com.xsh.service.impl;

import com.xsh.service.IAccountService;
import org.springframework.stereotype.Service;

/**
 * 账户的业务层实现类
 */
@Service("accountService")
public class AccountServiceImpl implements IAccountService{

    @Override
    public void saveAccount() {
        System.out.println("执行了保存");
        int i=1/0;
    }

    @Override
    public void updateAccount(int i) {
        System.out.println("执行了更新"+i);

    }

    @Override
    public int deleteAccount() {
        System.out.println("执行了删除");
        return 0;
    }
}
```

Logger类：

- @Component：把当前类交给spring管理
- @Aspect：表示当前类是一个切面类
- @Pointcut：配置切入点表达式,类名就是id,**通知类注解必须指定使用切入点表达式**
- @Before ：前置通知
- @AfterReturning ：后置通知
- @AfterThrowing ：异常通知
- @After：最终通知
- @Around :环绕通知，通常单独使用

**使用通知类注解方式时，当出现异常，spring的执行顺序为先执行前置通知，再执行业务，再执行最终通知，最后才执行异常通知，正确的顺序应该是最终通知在异常通知后；而使用@Around环绕通知则为正确的执行顺序，所以当使用注解方式时一般使用环绕通知**

```java
package com.xsh.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 用于记录日志的工具类，它里面提供了公共的代码
 */
@Component("logger")
@Aspect
public class Logger {

    @Pointcut("execution(* com.itheima.service.impl.*.*(..))")
    private void pt1(){}

//    @Before("pt1()")
    public  void beforePrintLog(){
        System.out.println("前置通知Logger类中的beforePrintLog方法开始记录日志了。。。");
    }

//    @AfterReturning("pt1()")
    public  void afterReturningPrintLog(){
        System.out.println("后置通知Logger类中的afterReturningPrintLog方法开始记录日志了。。。");
    }

//    @AfterThrowing("pt1()")
    public  void afterThrowingPrintLog(){
        System.out.println("异常通知Logger类中的afterThrowingPrintLog方法开始记录日志了。。。");
    }

//    @After("pt1()")
    public  void afterPrintLog(){
        System.out.println("最终通知Logger类中的afterPrintLog方法开始记录日志了。。。");
    }

    @Around("pt1()")
    public Object aroundPringLog(ProceedingJoinPoint pjp){
        Object rtValue = null;
        try{
            Object[] args = pjp.getArgs();//得到方法执行所需的参数

            System.out.println("Logger类中的aroundPringLog方法开始记录日志了。。。前置");

            rtValue = pjp.proceed(args);//明确调用业务层方法（切入点方法）

            System.out.println("Logger类中的aroundPringLog方法开始记录日志了。。。后置");

            return rtValue;
        }catch (Throwable t){
            System.out.println("Logger类中的aroundPringLog方法开始记录日志了。。。异常");
            throw new RuntimeException(t);
        }finally {
            System.out.println("Logger类中的aroundPringLog方法开始记录日志了。。。最终");
        }
    }
}
```

## 4.6 不使用bean.xml的基于注解的AOP配置

```xml
<!-- 配置spring创建容器时要扫描的包-->
<context:component-scan base-package="com.xsh"></context:component-scan>

<!-- 配置spring开启注解AOP的支持 -->
<aop:aspectj-autoproxy></aop:aspectj-autoproxy>
```

可修改为:

```java
@Configuration
@ComponentScan(basePackages="com.xsh")
@EnableAspectJAutoProxy
public class SpringConfiguration {
    
}
```