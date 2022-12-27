package io.mybatis.rui.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class Column {
  /**
   * 列名
   */
  private Name                 name;
  /**
   * 备注
   */
  private Text                 comment;
  /**
   * 类型，对应java.sql.Types中的类型
   */
  private int                  type;
  /**
   * 类型名称
   */
  private String               typeName;
  /**
   * 数据库类型
   */
  private JdbcType             jdbcType;
  /**
   * Java类型
   */
  private JavaType             javaType;
  /**
   * 是否主键
   */
  private boolean              pk;
  /**
   * 是否可空
   */
  private boolean              nullable;
  /**
   * 是否为关键字
   */
  private boolean              keyword;
  /**
   * 字段长度
   */
  private int                  length;
  /**
   * 小数位数
   */
  private int                  scale;
  /**
   * 是否自增
   */
  private boolean              autoIncrement;
  /**
   * 可查询 - jdbc not support
   */
  private boolean              selectable;
  /**
   * 可插入 - jdbc not support
   */
  private boolean              insertable;
  /**
   * 可更新 - jdbc not support
   */
  private boolean              updatable;
  /**
   * 排序方式，默认空时不作为排序字段，只有手动设置 ASC 和 DESC 才有效 - jdbc not support
   */
  private String               orderBy;
  /**
   * 默认值 - jdbc not support
   */
  private String               defaultValue;
  /**
   * 列标签
   */
  private Map<String, Boolean> tags;

  public Column(@NonNull String name) {
    setName(name);
  }

  public Column(@NonNull String name, String comment) {
    setName(name);
    setComment(comment);
  }

  public Column(@NonNull Name name) {
    this.name = name;
  }

  public Column(@NonNull Name name, String comment) {
    this.name = name;
    setComment(comment);
  }

  public void setName(@NonNull String name) {
    this.name = Name.of(name);
  }

  public void setComment(String comment) {
    this.comment = new Text(comment);
  }

  public void setType(int type) {
    this.type = type;
    this.jdbcType = JdbcType.forCode(type);
  }

  public void addTags(Map<String, Set<JdbcType>> typeTags) {
    if (typeTags != null) {
      typeTags.forEach((tag, types) -> addTags(tag, types.contains(getJdbcType()) || types.contains(getJdbcType().toString())));
    }
  }

  public void addTags(String tag, Boolean contains) {
    if (tags == null) {
      tags = new HashMap<>();
    }
    tags.put(tag, contains);
  }

}
