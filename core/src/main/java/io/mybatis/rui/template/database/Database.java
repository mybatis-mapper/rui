package io.mybatis.rui.template.database;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.mybatis.rui.model.Column;
import io.mybatis.rui.model.JavaType;
import io.mybatis.rui.model.JdbcType;
import io.mybatis.rui.model.Table;
import io.mybatis.rui.template.Context;
import io.mybatis.rui.template.Ref;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Getter
@Setter
@Slf4j(topic = "Database")
public class Database extends Ref<Database> {
  /**
   * 数据库连接信息
   */
  private JdbcConnection             jdbcConnection;
  /**
   * 要查询的表信息，支持 SQL 模糊查询: % _
   */
  private List<String>               tables;
  /**
   * 要查询的表信息，支持 SQL 模糊查询: % _
   * 在 tables 基础上支持更复杂的配置，代码处理中会将 tables 转换为 tableRules 一起处理
   */
  private List<TableRule>            tableRules;
  /**
   * 类型转换配置，可以覆盖默认值
   */
  private Map<JdbcType, String>      typeMap;
  /**
   * 关键字配置
   */
  private List<String>               keywords;
  /**
   * 关键字包装，使用 String.format 替换 %s, 例如 `%s`
   */
  private String                     keywordWrap;
  /**
   * 类型标签
   */
  private Map<String, Set<JdbcType>> typeTags;


  public Collection<Table> getTables(@NonNull Context context) {
    JdbcConnection jdbcConnection = getJdbcConnection();
    if (jdbcConnection == null) {
      log.debug("不存在数据库连接信息，跳过获取数据库信息");
      return null;
    }
    Collection<Table> tables;
    if (context.getDatabaseMetaData() != null) {
      tables = context.getDatabaseMetaData().getTables(this);
    } else {
      tables = jdbcConnection.getDialect().getTables(this);
    }
    for (Table table : tables) {
      for (Column column : table.getColumns()) {
        if (isKeyword(column.getName().getOriginal().getO())) {
          column.setKeyword(true);
        }
        column.addTags(typeTags);
        column.setJavaType(getJavaType(column.getJdbcType()));
      }
    }
    return tables;
  }

  /**
   * 获取 jdbcType 对应的 javaType
   *
   * @param jdbcType 数据库字段类型
   * @return
   */
  public JavaType getJavaType(@NonNull JdbcType jdbcType) {
    if (MapUtil.isNotEmpty(typeMap) && typeMap.containsKey(jdbcType)) {
      return JavaType.of(typeMap.get(jdbcType));
    }
    return jdbcType.javaType;
  }

  /**
   * 是否为关键字
   *
   * @param word
   * @return
   */
  public boolean isKeyword(String word) {
    return CollectionUtil.isNotEmpty(keywords) && keywords.contains(word);
  }

  /**
   * 包装关键字
   *
   * @param word
   * @return
   */
  public String wrapKeyword(String word) {
    if (StrUtil.isNotEmpty(keywordWrap) && isKeyword(word)) {
      return String.format(keywordWrap, word);
    }
    return word;
  }

  public JdbcConnection getJdbcConnection() {
    return jdbcConnection != null ? jdbcConnection.getR() : null;
  }

  /**
   * 获取表信息
   *
   * @return
   */
  public Set<TableRule> getRules() {
    Set<TableRule> rules = new LinkedHashSet<>();
    if (CollectionUtil.isNotEmpty(tableRules)) {
      rules.addAll(tableRules);
    }
    if (CollectionUtil.isNotEmpty(tables)) {
      for (String table : tables) {
        TableRule rule = new TableRule(table);
        rules.add(rule);
      }
    }
    rules.forEach(rule -> {
      if (StrUtil.isBlank(rule.getCatalog()) && StrUtil.isNotBlank(jdbcConnection.getCatalog())) {
        rule.setCatalog(jdbcConnection.getCatalog());
      }
      if (StrUtil.isBlank(rule.getSchema()) && StrUtil.isNotBlank(jdbcConnection.getSchema())) {
        rule.setSchema(jdbcConnection.getSchema());
      }
    });
    return rules;
  }
}
