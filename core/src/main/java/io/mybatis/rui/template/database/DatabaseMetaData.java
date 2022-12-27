package io.mybatis.rui.template.database;

import io.mybatis.rui.model.Table;

import java.util.Collection;

public interface DatabaseMetaData {

  /**
   * 获取表的信息
   *
   * @param database
   * @return
   */
  Collection<Table> getTables(Database database);

}
