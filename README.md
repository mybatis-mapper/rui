# 睿Rui

一个使用和配置简单，功能全面的代码生成器。

## 配置文件

配置文件格式为 yaml，配置内容分为两大部分，`database` 和 `files`，分别对应数据库配置信息和代码生成的项目结构。

```yaml
name: tk-mapper-demo # 项目文件名，如果是已经存在的项目，需要 path + name 为项目的路径
path: ${SYS['user.dir']}/target/ # 项目所在路径
templates: tk-mapper # 模板文件所在路径（相对 classpath 或者 path）
database: # 1. 数据库配置
  jdbcConnection:
    dialect: MYSQL # 默认为 JDBC，当前只提供了一个 MYSQL 的特殊实现
    driver: com.mysql.jdbc.Driver # 数据库驱动
    url: jdbc:mysql://localhost:3306/test?useSSL=false # 数据库连接地址
    user: root # 账户
    password: root # 密码
  tables: # 要获取的表名，可以指定多个，支持模糊匹配
    - '%'
  keywordWrap: "`%s`" # 关键字包装形式，其中 %s 代表了表字段名称
  # 数据库关键字
  keywords: # 关键字列表，手动指定
    - name
    - order
    - desc
attrs: # 附加属性，方便额外设置信息，通过 parent.parent...attrs.属性名 可以逐级向上使用属性
  basePackage: tk.mybatis.mapper.demo
  generateColumnConsts: true
files: # 子目录（包）
  - name: src/main # 默认为目录 DIR，目录名可以写多级
    files: # 子目录
      - name: java
        files:
          - name: '${project.attrs.basePackage}' #包名，可以多级点隔开
            type: PACKAGE # 包，可以多层
            files: # 子包
              - name: mapper
                files:
                  - name: '${it.name.className}Mapper.java' # 文件名，下面配置 iter 循环表，it 为其中一个表
                    file: mapper.java # 模板文件，相对顶部 templates 配置的目录（可以 ../../向上查找）
                    iter: tables # 可选配置，针对指定的数据（mvel2表达式取值）进行循环，会输出多个目录或文件（也可以用于目录和包）
                    # iterFilter: mvel2 表达式，对数据进行过滤，满足的会执行
              - name: model
                files:
                  - name: '${it.name.className}.java'
                    file: model.java
                    iter: tables
      - name: resources
        files:
          - name: mappers
            files:
              - name: '${it.name.className}Mapper.xml'
                iter: tables
                file: mapper.xml
```

## 完整配置示例

```yaml
name: tk-mapper-demo # 项目文件名，如果是已经存在的项目，需要 path + name 为项目的路径
path: ${SYS['user.dir']}/target/ # 项目所在路径
templates: tk-mapper # 模板文件所在路径（相对 classpath 或者 path）
#templateEngineClass: '' # 模板引擎配置，默认实现 FreeMarkerTemplateEngine
#dataEngineClass: '' # 数据表达式引擎配置，默认实现 Mvel2DataEngine
#databaseMetaDataClass: '' # 数据源获取数据配置，默认值和 database.jdbcConnection.dialect 有关，可以覆盖默认实现
attrs: # 附加属性，方便额外设置信息，通过 parent.parent...attrs.属性名 可以逐级向上使用属性
  basePackage: tk.mybatis.mapper.demo
  generateColumnConsts: true
database: # 1. 数据库配置，配置结构参考 io.mybatis.rui.template.database.Database
  jdbcConnection:
    dialect: MYSQL # 默认为 JDBC，当前只提供了一个 MYSQL 的特殊实现
    driver: com.mysql.jdbc.Driver # 数据库驱动
    url: jdbc:mysql://localhost:3306/test?useSSL=false # 数据库连接地址
#    catalog: '' # 数据库名称
#    schema: '' # 数据库名称
    user: root # 账户
    password: root # 密码
  tables: # 要获取的表名，可以指定多个，支持模糊匹配
    - 'sys%'
    - 'user_info'
    - 'user_role'
  tableRules: # 比 tables 优先级更高，包含相同表时优先使用这里的配置，可以针对表和列进行更复杂的配置
    - name: 'sys%' # 表名，支持模糊匹配的_和%
      search: 'sys_' # 支持正则，用于搜索替换
      replace: '' # 将表名中的 sys_ 部分替换为空，相当于去掉前缀，使用 String.replaceAll(search, replace) 实现
#      catalog: '' # 数据库名称
#      schema: '' # 数据库名称
      ignoreColumnsByRegex: '^(create_time|update_time|create_by|update_by)$' # 忽略的列，正则表达式
      columnRules: # 列配置
        - name: 'sname' # 列名，精确匹配，区分大小写
          search: '^s' # 支持正则，用于搜索替换
          replace: '' # 将表名中的 sys_ 部分替换为空，相当于去掉前缀，使用 String.replaceAll(search, replace) 实现
          ignore: false # 忽略列
        - name: 'saddress' # 列名，精确匹配，区分大小写
          search: '^s' # 支持正则，用于搜索替换
          replace: '' # 将表名中的 sys_ 部分替换为空，相当于去掉前缀，使用 String.replaceAll(search, replace) 实现
          ignore: false # 忽略列
  typeMap: # 类型转换配置，覆盖默认值，默认配置请看 io.mybatis.rui.model.JdbcType
    TINYINT: java.lang.Byte
    VARCHAR: java.lang.String
  keywordWrap: "`%s`" # 关键字包装形式，其中 %s 代表了表字段名称
  # 数据库关键字
  keywords: # 关键字列表，手动指定，没有默认值
    - name
    - order
    - desc
  # 根据类型对字段类型打标签, 可以通过 column.tags.TAG 的 true/false 来判断当前列是否有该标签
  # 主要对列进行分类，方便后续模板中的使用
  typeTags:
    datetime: # 给下面的jdbcType类型打上 datetime 标签
      - DATE
      - TIME
      - TIMESTAMP
files: # 子目录（包）, 配置结构参考 io.mybatis.rui.template.struct.Structure
  - name: src/main # 默认为目录 DIR，目录名可以写多级
    enabled: true # 当前级别配置是否生效，默认 true 生效
    type: DIR # 类型为目录，默认为 DIR 目录，一般只有第一层 package 时需要设置 PACKAGE 类型
    times: 1 # 表达式执行次数，1 为默认值，表示只用参数执行一次，当有嵌套的参数时，需要执行多次才能变成最终的值，
             # 例如一个表达式执行完是一个新的表达式，还需要再执行一次才能变成具体值
    files: # 子目录
      - name: java
        files:
          - name: '${project.attrs.basePackage}' #包名，可以多级点隔开
            type: PACKAGE # 类型为java的包，只有第一层包必须指定，否则无法区分是目录还是包
            files: # 子包
              - name: mapper
                files:
                  - name: '${it.name.className}Mapper.java' # 文件名，下面配置 iter 循环表，it 为其中一个表
                    file: mapper.java # 模板文件，相对顶部 templates 配置的目录（可以 ../../向上查找）
                    iter: tables # 可选配置，针对指定的数据（mvel2表达式取值）进行循环，会输出多个目录或文件（也可以用于目录和包）
                    # iterName: 'it' # 默认为 it，是 name 和模板中可以使用的迭代对象名
                    # iterFilter: mvel2 表达式，对迭代数据进行过滤，满足的会执行
                    # filter: tables != null # 只有满足条件，当前配置才会执行 iter 的迭代
                    # mode: OVERRIDE # 默认为 OVERRIDE，如果文件已经存在，是否覆盖，OVERRIDE 覆盖，
                                     # ONCE 只生成一次，存在就不执行，MERGE 合并已存在和新生成的内容
              - name: model
                files:
                  - name: '${it.name.className}.java'
                    file: model.java
                    iter: tables
      - name: resources
        files:
          - name: mappers
            files:
              - name: '${it.name.className}Mapper.xml'
                iter: tables
                file: mapper.xml
```

## 模板文件

上面代码生成器配置中用到了下面几个模板。

### `mapper.java`

```java
package ${package};

import io.mybatis.mapper.Mapper;

/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
public interface ${it.name.className}Mapper extends Mapper<${it.name.className}, Long> {

}
```

这个模板生成一个继承了 tk.Mapper 接口的接口。例如：
```java
package tk.mybatis.mapper.demo.mapper;

import io.mybatis.mapper.Mapper;

/**
 * user - 用户
 *
 * @author lzh
 */
public interface UserMapper extends Mapper<User, Long> {

}
```

### `model.java`

```java
package ${package};

import javax.persistence.*;

<#list it.importJavaTypes as javaType>
import ${javaType};
</#list>

/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
@Table(name = "${it.name}")
public class ${it.name.className} {
  <#-- 下面这段只是为了演示 tk.mapper 中的 generateColumnConsts 用法，实际不需要这种变量 -->
  <#if project.attrs.generateColumnConsts?? && project.attrs.generateColumnConsts == 'true'>
  <#list it.columns as column>
  public static final String ${column.name.uppercase} = "${column.name}";
  </#list>
  </#if>

  <#list it.columns as column>
  <#if column.pk>
  @Id
  @GeneratedValue(generator = "JDBC")
  </#if>
  @Column(name = "${column.name}")
  private ${column.javaType} ${column.name.fieldName};

  </#list>

  <#list it.columns as column>
  /**
   * 获取 ${column.comment}
   *
   * @return ${column.name.fieldName} - ${column.comment}
   */
  public ${column.javaType} get${column.name.className}() {
    return ${column.name.fieldName};
  }

  /**
   * 设置${column.comment}
   *
   * @param ${column.name.fieldName} ${column.comment}
   */
  public void set${column.name.className}(${column.javaType} ${column.name.fieldName}) {
    this.${column.name.fieldName} = ${column.name.fieldName};
  }

  </#list>
}
```
上面模板生成了实体类，实体类改为模板生成后会更灵活，使用更方便。

上面模板的一个示例如下：

```java
package tk.mybatis.mapper.demo.model;

import javax.persistence.*;


/**
 * user - 用户
 *
 * @author lzh
 */
@Table(name = "user")
public class User {
  public static final String ID = "id";
  public static final String USER_NAME = "user_name";
  public static final String USER_AGE = "user_age";
  public static final String ADDRESS = "address";

  @Id
  @GeneratedValue(generator = "JDBC")
  @Column(name = "id")
  private Long id;

  @Column(name = "user_name")
  private String userName;

  @Column(name = "user_age")
  private Integer userAge;

  @Column(name = "address")
  private String address;


  /**
   * 获取 主键
   *
   * @return id - 主键
   */
  public Long getId() {
    return id;
  }

  /**
   * 设置主键
   *
   * @param id 主键
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * 获取 用户名
   *
   * @return userName - 用户名
   */
  public String getUserName() {
    return userName;
  }

  /**
   * 设置用户名
   *
   * @param userName 用户名
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * 获取 年龄
   *
   * @return userAge - 年龄
   */
  public Integer getUserAge() {
    return userAge;
  }

  /**
   * 设置年龄
   *
   * @param userAge 年龄
   */
  public void setUserAge(Integer userAge) {
    this.userAge = userAge;
  }

  /**
   * 获取 地址
   *
   * @return address - 地址
   */
  public String getAddress() {
    return address;
  }

  /**
   * 设置地址
   *
   * @param address 地址
   */
  public void setAddress(String address) {
    this.address = address;
  }

}
```

### `mapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${project.attrs.basePackage}.mapper.${it.name.className}Mapper">
  <resultMap id="baseResultMap" type="${project.attrs.basePackage}.model.${it.name.className}">
    <#list it.columns as column>
    <#if column.pk>
    <id property="${column.name.fieldName}" column="${column.name}" jdbcType="${column.jdbcType}"/>
    <#else>
    <result property="${column.name.fieldName}" column="${column.name}" jdbcType="${column.jdbcType}"/>
    </#if>
    </#list>
  </resultMap>
</mapper>
```

生成的一个示例如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="tk.mybatis.mapper.demo.mapper.UserMapper">
  <resultMap id="baseResultMap" type="tk.mybatis.mapper.demo.model.User">
    <id property="id" column="id" jdbcType="BIGINT"/>
    <result property="userName" column="user_name" jdbcType="VARCHAR"/>
    <result property="userAge" column="user_age" jdbcType="INTEGER"/>
    <result property="address" column="address" jdbcType="VARCHAR"/>
  </resultMap>
</mapper>
```

## 使用方式

### 方式一：引入依赖，写代码调用

```xml
<dependency>
  <groupId>io.mybatis.rui</groupId>
  <artifactId>rui-core</artifactId>
  <version>1.1.0</version>
</dependency>
```

在代码中调用生成器：
```java
Project.load("tk-mapper/generator-demo.yaml").generate();
```

### 方式二：可执行Jar包

示例：

```bash
java -cp mysql-connector-java-5.1.49.jar:rui-cli.jar \
 -Dorg.slf4j.simpleLogger.defaultLogLevel=trace \
 io.mybatis.rui.cli.Main -p project.yaml
```
>`-cp`需要包含数据库驱动和代码生成器。


详细可以参数如下：
```
  Options:
    -p, --project
      代码生成器YAML配置文件
    -o, --output
      输出目录，默认使用配置文件中的 path，为空时使用当前执行目录
    -T, --templates
      模板文件路径，默认和YAML相同位置，或者为当前执行目录的相对位置
    --jdbc.dialect
      数据库方言
      Possible Values: [JDBC, HSQLDB, ORACLE, DB2, SQLSERVER, MARIADB, MYSQL]
    --jdbc.driver
      数据库驱动
    --jdbc.url
      数据库URL
    --jdbc.user
      数据库用户
    --jdbc.password
      数据库密码
    -t, --tables
      要获取的表名，支持模糊匹配(%)，多个表名用逗号隔开，指定该值后会覆盖配置文件中的值
    -A, -attrs
      项目附加属性，会覆盖项目下的 attrs 配置
      Syntax: -Akey=value
      Default: {}
    -l, --log
      日志级别
      Default: debug
    -h, --help
      显示帮助信息
```
