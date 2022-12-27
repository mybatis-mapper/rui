package io.mybatis.rui.template.database.dialect;

import io.mybatis.rui.model.Table;
import io.mybatis.rui.template.database.Database;
import io.mybatis.rui.template.database.JdbcConnection;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JDBC 方式无法获取表的注释，这里补充额外的信息
 */
@Slf4j(topic = "Database")
public class MySQLDatabaseMetaData extends JDBCDatabaseMetaData {
  /**
   * 查询表信息
   */
  public static final String TABLE_SQL = "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME in ( %s )";

  @Override
  public Collection<Table> getTables(Database database) {
    Collection<Table> tables = super.getTables(database);
    log.debug("通过 SQL 获取表的注释信息");
    //有可能存在不同的 schema（JDBC时对应的是catalog的值），要分组处理
    Map<String, List<Table>> catalogMap = tables.stream().collect(Collectors.groupingBy(Table::getCatalog));
    catalogMap.forEach((catalog, tableList) -> {
      Map<String, Table> tableMap = tableList.stream().collect(Collectors.toMap(t -> t.getName().getOriginal().getO(), Function.identity()));
      String tableNames = tableMap.keySet().stream().map(s -> String.format("'%s'", s)).collect(Collectors.joining(","));
      //数据库连接信息
      JdbcConnection jdbcConnection = database.getJdbcConnection();
      //连接数据库，查表获取信息
      try {
        Connection connection = DriverManager.getConnection(jdbcConnection.getUrl(), jdbcConnection.getUser(), jdbcConnection.getPassword());
        String sql = String.format(TABLE_SQL, catalog, tableNames);
        log.debug("执行 SQL: " + sql);
        PreparedStatement tablePs = connection.prepareStatement(sql);
        ResultSet rs = tablePs.executeQuery();
        if (rs != null) {
          while (rs.next()) {
            String table = rs.getString("TABLE_NAME");
            String comment = rs.getString("TABLE_COMMENT");
            log.debug("表: " + table + " - " + comment);
            tableMap.get(table).setComment(comment);
          }
          rs.close();
        }
        connection.close();
      } catch (SQLException throwables) {
        throw new RuntimeException(throwables);
      }
    });
    return tables;
  }

}
