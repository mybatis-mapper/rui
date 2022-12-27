# README.md

## 环境变量

JAVA_HOME: ${r"$"}{ENV.JAVA_HOME} - ${ENV.JAVA_HOME!""}

JAVA_HOME: ${r"$"}{ENV['JAVA_HOME']} - ${ENV['JAVA_HOME']!""}

|ENV|Value|
|---|-----|

<#list ENV?keys as key>
|${key}|${ENV[key]}|
</#list>

## 系统属性

user.dir: ${r"$"}{SYS['user.dir']} - ${SYS['user.dir']}

|SYS|Value|
|---|-----|

<#list SYS?keys as key>
|${key}|${SYS[key]}|
</#list>

## 数据库表信息

<#list tables as it>

### ${it.name} - ${it.comment}

|字段名|注释|jdbcType|javaType|主键|可空|
|---|---|---|---|:---:|:---:|

<#list it.columns as column>
|${column.name}|${column.comment}|${column.jdbcType}|${column.javaType}|${column.pk?string('Y', 'N')
}|${column.nullable?string('Y', 'N')}|
</#list>

</#list>