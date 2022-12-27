package io.mybatis.rui.template.database.dialect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.DbRuntimeException;
import cn.hutool.db.DbUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.Table;
import cn.hutool.db.meta.TableType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.db.meta.MetaUtil.getCataLog;
import static cn.hutool.db.meta.MetaUtil.getSchema;

public class DatabaseMetaUtil {

  /**
   * 获得所有表名
   *
   * @param ds        数据源
   * @param catalog   目录
   * @param schema    表数据库名，对于Oracle为用户名，MySQL为数据库名
   * @param tableName 表名
   * @param types     表类型
   * @return 表名列表
   * @since 3.3.1
   */
  public static List<String> getTables(DataSource ds, String catalog, String schema, String tableName, TableType... types) {
    final List<String> tables = new ArrayList<>();
    Connection conn = null;
    try {
      conn = ds.getConnection();
      // catalog和schema获取失败默认使用null代替
      if (null == catalog) {
        catalog = getCataLog(conn);
      }
      if (null == schema) {
        schema = getSchema(conn);
      }

      final DatabaseMetaData metaData = conn.getMetaData();
      try (ResultSet rs = metaData.getTables(catalog, schema, tableName, Convert.toStrArray(types))) {
        if (null != rs) {
          String table;
          while (rs.next()) {
            table = rs.getString("TABLE_NAME");
            if (StrUtil.isNotBlank(table)) {
              tables.add(table);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new DbRuntimeException("Get tables error!", e);
    } finally {
      DbUtil.close(conn);
    }
    return tables;
  }

  /**
   * 获得表的元信息
   *
   * @param ds        数据源
   * @param catalog   目录
   * @param schema    表数据库名，对于Oracle为用户名，MySQL为数据库名
   * @param tableName 表名
   * @return Table对象
   */
  public static Table getTableMeta(DataSource ds, String catalog, String schema, String tableName) {
    final Table table = Table.create(tableName);
    Connection conn = null;
    try {
      conn = ds.getConnection();

      // catalog和schema获取失败默认使用null代替
      if (null == catalog) {
        catalog = getCataLog(conn);
      }
      table.setCatalog(catalog);
      if (null == schema) {
        schema = getSchema(conn);
      }
      table.setSchema(schema);
      final DatabaseMetaData metaData = conn.getMetaData();

      // 获得表元数据（表注释）
      try (ResultSet rs = metaData.getTables(catalog, schema, tableName, new String[]{TableType.TABLE.value()})) {
        if (null != rs) {
          if (rs.next()) {
            table.setComment(rs.getString("REMARKS"));
          }
        }
      }

      // 获得主键
      try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
        if (null != rs) {
          while (rs.next()) {
            table.addPk(rs.getString("COLUMN_NAME"));
          }
        }
      }

      // 获得列
      try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, null)) {
        if (null != rs) {
          while (rs.next()) {
            table.setColumn(Column.create(table, rs));
          }
        }
      }
    } catch (SQLException e) {
      throw new DbRuntimeException("Get columns error!", e);
    } finally {
      DbUtil.close(conn);
    }

    return table;
  }
}
