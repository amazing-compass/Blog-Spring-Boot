# 一. 使用xml配置方式完成CRUD

```java
package com.mybatis.test;

import com.mybatis.dao.IUserDao;
import com.mybatis.domain.QueryVo;
import com.mybatis.domain.User;
import com.sun.xml.internal.bind.v2.model.core.ID;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * @describe: mybatis的CRUD测试类
 */
public class MybatisTest {
    private InputStream in;
    private SqlSession session;
    private IUserDao userDao;
    public static void main(String[] args){
        
    }
    @Before   //用于在测试方法执行之前执行的方法
    public void init() throws Exception {
        //  BasicConfigurator.configure();//自动快速地使用缺省Log4j环境。
        //1.读取配置文件
        in= Resources.getResourceAsStream("SqlMapConfig.xml");
        //2.创建SqlSessionFactory工厂模式
//      SqlSessionFactory factory=null;//SqlSessionFactory是接口，不能new，所以用SqlSessionFactoryBuilder
        SqlSessionFactoryBuilder builder=new SqlSessionFactoryBuilder();
        SqlSessionFactory factory=builder.build(in);
        //3.使用工厂生产一个SqlSession对象
        session=factory.openSession();//在openSession()中填入参数true,则系统会自动提交事务而不用在@After方法中手动提交
        //4.使用SqlSession创建Dao接口的代理对象
        userDao=session.getMapper(IUserDao.class);
    }
    
    /*查询所有*/
    @Test
    public void testfindAll() throws Exception{
        //5.使用代理对象执行方法
        List<User> users=userDao.findAll();
        for(User user:users){
            System.out.println(user);
        }
    }
    
    @After   //用于在测试方法执行之后执行的方法
    public void destroy() throws Exception {
        //因为系统默认为Setting autocommit to false on JDBC Connection，所以需要手动提交事务,执行增删改的时候才不会被回滚事务
        session.commit();
        //6.释放资源
        session.close();
        in.close();
    }
}
```

## 1.1 根据ID查询

在持久层接口中添加findById方法：

```java
/** 
 * 根据id查询 
 * @param userId 
 * @return 
 */ 
User findById(Integer userId);
```

在用户的映射配置文件中配置：

```xml
    <!--根据id查询用户配置-->
    <!--因为根据id查询需要传入id,所以parameterType="int",查询后需要返回查询的数据，所以也要指定resultType-->
    <select id="findById" parameterType="int" resultType="com.mybatis.domain.User">
        <!--#{}表示占位符,由于数据类型是基本类型int，所以此处可以随意写。-->
        select*from user where id=#{iii};
    </select>
```

在测试类中添加方法：

```java
/*根据id查询用户信息*/
@Test
public void testFindById() throws Exception{
    User a=userDao.findById(4);
    System.out.println(a);
}
```

## 1.2 插入用户

在持久层接口中添加insertUser方法：

```java
/*插入数据*/
void insertUser(User user);
```

在用户的映射配置文件中配置：

```xml
<!--插入用户配置-->
<insert id="insertUser" parameterType="com.mybatis.domain.User">
    <!--插入数据后，获取插入数据的id(id为系统自增)-->
    <!--keyProperty为实体类中对应的属性名，keyColumn为数据库中对应的字段名，order值为 AFTER表示在插入之后进行获取操作-->
    <selectKey keyProperty="id" keyColumn="id" resultType="int" order="AFTER" >
        select last_insert_id();
    </selectKey>
	<!--values内所填入的参数名,当属性名和get方法后的名字不一样，以get方法后的名字为主-->
    insert into user(username,birthday,sex,address)
    values(#{username},#{birthday},#{sex},#{address});
</insert>
```

在测试类中添加方法：

```java
/*保存用户(插入用户)*/
@Test
public void testInsert() throws IOException {
    User user=new User();
    user.setUsername("mybatis");
    user.setAddress("test");
    user.setSex("男");
    user.setBirthday(new Date());
    System.out.println("执行插入之前的user:"+user);//插入之前id为null
    //使用代理对象执行方法
    userDao.insertUser(user);
    System.out.println("执行插入之后的user:"+user);//插入之后可获取id
}
```

## 1.3 用户更新

在持久层接口中添加updateUser方法：

```java
void updateUser(User user);
```

在用户的映射配置文件中配置：

```xml
<!--更新用户配置-->
<update id="updateUser" parameterType="com.mybatis.domain.User">
    update user set username=#{username},birthday=#{birthday},sex=#{sex},address=#{address} where id=#{id};
</update>
```

在测试类添加方法：

```java
/*更新用户*/
@Test
public void testUpdate() throws IOException {
    User user=new User();
    user.setId(12);//指定已存在的id更新该id的信息;
    user.setUsername("mybatisUpdate");
    user.setAddress("testUpdate");
    user.setSex("男");
    user.setBirthday(new Date());
    //使用代理对象执行方法
    userDao.updateUser(user);
}
```

## 1.4 用户删除

在持久层接口中添加deleteUser方法：

```java
/*根据id删除*/
void deleteUser(Integer id);
```

在用户的映射配置文件中配置：

```xml
<!--删除用户配置-->
<!--因为是根据id删除,参数是id，所以填入parameterType="int"-->
<!--#{}只有一个参数可以随意起名，占位符-->
<delete id="deleteUser" parameterType="int">
    delete from user where id=#{u123id};
</delete>
```

在测试类添加方法：

```java
@Test
public void testDelete() throws IOException {
    //使用代理对象执行方法
    userDao.deleteUser(14);
}
```

## 1.5 用户模糊查询

在持久层接口中添加findByName方法：

```java
/*根据名称模糊查询用户信息*/
/*因为是模糊查询，返回的结果不止一个，所以返回值为List*/
List<User> findByName(String username);
```

在用户的映射配置文件中配置：

```xml
<!--根据username模糊查询用户信息配置-->
<!--如果选择在配置文件中加%的方法，那么大括号内一定要是value，因为源码内就是value-->
<!--推荐使用在外部加%的方法而不在配置文件中的SQL语句中加，因为在SQL中语句加%使用的是字符串拼接的方法；在外部加%使用的是预处理的方法，可防止SQL注入-->
<!--在xml文件内不能用/**/注释-->
<select id="findByName" parameterType="String" resultType="com.mybatis.domain.User">
    select*from user where username like #{uname};
    <!--select*from user where username like '%{value}%';-->
</select>
```

在测试类添加方法：

```java
/*根据username模糊查询用户信息*/
@Test
public void testFindByName() throws Exception{
    //使用代理对象执行方法
    List<User> a=userDao.findByName("%米%");//因为模糊查询要%value%，所以此处要加%，也可以在配置文件的SQL语句内加
    for(User user:a){
        System.out.println(user);
    }
}
```

## 1.6 查询使用聚合函数

在持久层接口中添加getUserCount方法：

```java
/*获取用户记录的条数*/
int getUserCount();
```

在用户的映射配置文件中配置：

```xml
<!--获取用户信息的记录条数配置-->
<!--因为只返回一个数字，所以resultType="int"-->
<select id="getUserCount" resultType="int">
    select count(*) from user;
</select>
```

在测试类添加方法：

```java
/*获取用户信息条数*/
@Test
public void testGetUserCount() throws Exception{
    //使用代理对象执行方法
    int count=userDao.getUserCount();
    System.out.println("用户信息条数为:"+count);
}
```

## 1.7 使用pojo包装对象

开发中通过pojo传递查询条件 ，查询条件是综合的查询条件，不仅包括用户查询条件还包括其它的查询条件（比如将用户购买商品信息也作为查询条件），这时可以使用包装对象传递输入参数，将需要的参数定义成一个新的实体类。

定义一个新的实体类QueryVo：

```java
public class QueryVo{
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
```

在持久层接口中添加findUserByVo方法：

```java
/*根据queryVo中的条件查询用户*/
List<User> findUserByVo(QueryVo vo);
```

在用户的映射配置文件中配置：

获取用户名称:

- 类中的写法: user.getUsername( );
- OGNL表达式写法: user.username

```xml
<!--根据queryVo的条件查询用户-->
<!--使用OGNL表达式获取username-->
<select id="findUserByVo" parameterType="com.mybatis.domain.QueryVo" resultType="com.mybatis.domain.User">
    select*from user where username like #{user.username};
</select>
```

在测试类添加方法：

```java
/*测试使用QueryVo作为查询条件*/
@Test
public void testFindByQueryVo() throws Exception{
    QueryVo vo=new QueryVo();
    User user=new User();
    user.setUsername("%杨%");
    vo.setUser(user);
    List<User> a=userDao.findUserByVo(vo);
    for (User u:a){
        System.out.println(u);
    }
}
```

# 二. 配置文件解析

## 2.1 parameterType配置参数

parameterType指定sql语句执行的参数类型。

**基本类型和String可以直接写类型名称**，也可以使用包名.类名的方式，例如：java.lang.String。

实体类类型，目前只能使用全限定类名。 究其原因，是mybaits在加载时已经把常用的数据类型注册了别名，从而我们在使用时可以不写包名，而我们的是实体类并没有注册别名，所以必须写全限定类名。

可使用typeAliases标签在xml配置文件中自定义别名

可以在mybatis源码中org.apache.ibatis.type.TypeAliasRegistry类下查看有哪些类型已经注册了别名

## 2.2 Mybatis的输出结果封装

### (1) resultType结果类型

**resultType指定sql语句执行完毕后返回的数据类型。**

使用resultType，实体类中的属性名称必须和查询语句中的数据库列名保持一致，否则无法实现封装。

当实体类属性和数据库表的列名已经不一致，如实体类中的属性名为userId,数据库对应的字段名为user_id,则无法使用resultType完成封装,需要使用resultMap。

### (2) resultMap结果类型

resultMap标签可以建立查询的列名和实体类的属性名称不一致时建立对应关系。从而实现封装。

- 定义resultMap

  ```xml
  <!--resultMap中的id属性唯一可任意取,作为引用的值-->
  <resultMap id="unique" type="com.mybatis.domain.User">
      <!--首先，使用id标签配置主键字段的对应-->
      <!--property指定类中的属性名，column指定对应的数据库字段名-->
      <id property="id" column="userId"></id>
      <!--result标签配置非主键字段的对应-->
      <result property="username"" column="userName"></result>
  </resultMap>
  ```

- 使用resultMap

  ```xml
  <!--unique为定义resultMap的id值-->
  <select id="findAll"  resultMap="unique">
      select *from user;
  </select>
  ```

## 2.3 SqlMapConfig.xml配置文件

### (1)SqlMapConfig.xml中配置的内容和顺序

```xml
-properties（属性） 
	--property 
-settings（全局配置参数） 
	--setting 
-typeAliases（类型别名） 
	--typeAliase 
	--package 
-typeHandlers（类型处理器） 
-objectFactory（对象工厂） 
-plugins（插件） 
-environments（环境集合属性对象） 
	--environment（环境子属性对象） 
		---transactionManager（事务管理） 
		---dataSource（数据源） 
-mappers（映射器）
	--mapper 
	--package
```

### (2) properties（属性）

在使用properties标签配置时，可以采用两种方式指定属性配置。

- 第一种：

```xml
<properties>  
	<property name="driver" value="com.mysql.cj.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/xsh?serverTimezone=UTC"/>
    <property name="username" value="root"/>
    <property name="password" value="123456"/>
</properties> 
```

- 第二种

  在classpath下定义db.properties文件

  ```properties
  jdbc.driver=com.mysql.cj.jdbc.Driver 
  jdbc.url=jdbc:mysql://localhost:3306/xsh?serverTimezone=UTC
  jdbc.username=root 
  jdbc.password=123456
  ```

  在SqlMapConfig.xml引入properties：

  ```xml
  <properties 	 url="file:///D:/java/mybatisCRUD/src/main/resources/jdbcConfig.properties"> 
  </properties>
  ```

  使用：

  ```xml
  <dataSource type="POOLED"> 
      <property name="driver" value="${jdbc.driver}"/> 
      <property name="url" value="${jdbc.url}"/> 
      <property name="username" value="${jdbc.username}"/> 
      <property name="password" value="${jdbc.password}"/> 
  </dataSource>
  ```

其中连接池dataSource的type取值：

- POOLED: 池的，在池中获取一个连接，用完之后还回去
- UNPOOL: 并没有池的思想(每次用都创建一个新的连接来用)
- JNDI:采用服务器提供的JNDI技术实现来获取DataSource对象，不同服务器所能拿到的DataSource是不一样的

### (3) typeAliases(类型别名)

自定义别名来使用实体类

```xml
在SqlMapConfig.xml中配置： 
<typeAliases> 
    <!-- 单个别名定义 --> 
    <typeAlias alias="user" type="com.itheima.domain.User"/> 
    <!-- 批量别名定义，扫描整个包下的类，别名为类名（首字母大写或小写都可以）,可定义多个--> 
    <package name="com.mybatis.domain"/> 
    <package name="其它包"/> 
</typeAliases>
```

### (4) mappers（映射器）

- 使用相对于类路径的资源，映射单个

  ```xml
  <mapper resource="com/mybatis/dao/IUserDao.xml" />
  ```

- 使用mapper接口类路径，不用写对应的映射文件,搭配注解开发使用

  ```xml
  <mapper class="com.mybatis.dao.UserDao"/> 
  ```

- 注册指定包下的所有mapper接口

  ```xml
  <package name="com.mybatis.dao"/> 
  ```

  此种方法要求mapper接口名称和mapper映射文件名称相同，且mapper映射文件与mapper接口所在文件目录相同，映射文件存在resources下，实现一 一对应。