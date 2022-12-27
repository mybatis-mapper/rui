package io.mybatis.rui.template.database;

import io.mybatis.rui.model.Table;
import io.mybatis.rui.template.database.dialect.JDBCDatabaseMetaData;
import io.mybatis.rui.template.database.dialect.MySQLDatabaseMetaData;

import java.util.Collection;

public enum Dialect {
  JDBC(JDBCDatabaseMetaData.DEFAULT),
  HSQLDB(JDBCDatabaseMetaData.DEFAULT),
  ORACLE(JDBCDatabaseMetaData.DEFAULT),
  DB2(JDBCDatabaseMetaData.DEFAULT),
  SQLSERVER(JDBCDatabaseMetaData.DEFAULT),
  MARIADB(new MySQLDatabaseMetaData()),
  MYSQL(new MySQLDatabaseMetaData());

  private final DatabaseMetaData databaseMetaData;

  Dialect(DatabaseMetaData databaseMetaData) {
    this.databaseMetaData = databaseMetaData;
  }

  public Collection<Table> getTables(Database database) {
    return databaseMetaData.getTables(database);
  }

}
