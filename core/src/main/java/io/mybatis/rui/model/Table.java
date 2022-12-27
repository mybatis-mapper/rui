package io.mybatis.rui.model;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class Table {
  /**
   * 表名
   */
  private Name         name;
  /**
   * 备注
   */
  private Text         comment;
  private String       schema;
  private String       catalog;
  /**
   * 主键列信息，方便模板使用，当使用联合主键时，这里只会记录其中一个字段
   */
  private Column       pk;
  /**
   * 主键列信息，方便模板使用
   */
  private List<Column> pks;
  /**
   * 普通列信息，不包含主键字段，方便模板使用
   */
  private List<Column> normals;
  /**
   * 列信息
   */
  private List<Column> columns;

  public Table(@NonNull String name) {
    setName(name);
  }

  public Table(@NonNull String name, String comment) {
    setName(name);
    setComment(comment);
  }

  public Table(@NonNull Name name) {
    setName(name);
  }

  public Table(@NonNull Name name, String comment) {
    setName(name);
    setComment(comment);
  }

  public void addColumn(Column column) {
    if (CollectionUtil.isEmpty(this.columns)) {
      this.columns = new ArrayList<>();
    }
    this.columns.add(column);
    //处理主键
    if (column.isPk()) {
      //记录第一个主键字段
      if (pk == null) {
        pk = column;
      }
      if (pks == null) {
        pks = new ArrayList<>();
      }
      pks.add(column);
    } else {
      //普通字段
      if (normals == null) {
        normals = new ArrayList<>();
      }
      normals.add(column);
    }
  }

  /**
   * 获取导入 JavaType
   *
   * @return
   */
  public List<String> getImportJavaTypes() {
    return columns.stream().map(c -> c.getJavaType().getFullName())
        //排除 java.lang 和不含包名的类型（如 byte[]）
        .filter(type -> !type.startsWith("java.lang") && type.contains("."))
        .distinct().sorted().collect(Collectors.toList());
  }

  public void setName(String name) {
    this.name = Name.of(name);
  }

  public void setName(Name name) {
    this.name = name;
  }

  public void setComment(String comment) {
    this.comment = new Text(comment);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Table table = (Table) o;
    return name.equals(table.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name.toString();
  }
}
