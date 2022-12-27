package io.mybatis.rui.template.database;

import io.mybatis.rui.template.Ref;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JdbcConnection extends Ref<JdbcConnection> {
  private Dialect dialect = Dialect.JDBC;
  private String  driver;
  private String  url;
  /**
   * 全局默认值，优先级低于 {@link TableRule}
   */
  private String  catalog;
  /**
   * 全局默认值，优先级低于 {@link TableRule}
   */
  private String  schema;
  private String  user;
  private String  password;

}
