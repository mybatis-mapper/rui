package io.mybatis.rui.template.database.dialect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.pooled.DbConfig;
import cn.hutool.db.ds.pooled.PooledDataSource;
import cn.hutool.db.meta.TableType;
import io.mybatis.rui.model.Column;
import io.mybatis.rui.model.Name;
import io.mybatis.rui.model.Table;
import io.mybatis.rui.template.database.Database;
import io.mybatis.rui.template.database.DatabaseMetaData;
import io.mybatis.rui.template.database.JdbcConnection;
import io.mybatis.rui.template.database.TableRule;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 使用 JDBC 方式获取表和列信息
 */
@Slf4j(topic = "Database")
public class JDBCDatabaseMetaData implements DatabaseMetaData {
  public static final DatabaseMetaData DEFAULT = new JDBCDatabaseMetaData();

  /**
   * 创建数据源
   *
   * @param jc
   * @return
   */
  private PooledDataSource createDataSource(JdbcConnection jc) {
    DbConfig config = new DbConfig();
    config.setDriver(jc.getDriver());
    config.setUrl(jc.getUrl());
    config.setUser(jc.getUser());
    config.setPass(jc.getPassword());
    config.setInitialSize(1);
    config.setMaxActive(1);
    return new PooledDataSource(config);
  }

  @Override
  public Collection<Table> getTables(Database database) {
    //使用 Hutool 获取
    JdbcConnection jc = database.getJdbcConnection();
    log.debug("获取数据库信息");
    @Cleanup PooledDataSource ds = createDataSource(jc);
    Map<String, TableRule> tableRuleMap = new LinkedHashMap<>();
    for (TableRule rule : database.getRules()) {
      List<String> tables = DatabaseMetaUtil.getTables(ds,
          StrUtil.isNotBlank(rule.getCatalog()) ? rule.getCatalog() : jc.getCatalog(),
          StrUtil.isNotBlank(rule.getSchema()) ? rule.getSchema() : jc.getSchema(),
          rule.getName(), TableType.TABLE);
      String fullTableName;
      for (String table : tables) {
        fullTableName = rule.getFullTableName(table);
        if (!tableRuleMap.containsKey(fullTableName)) {
          tableRuleMap.put(fullTableName, rule);
        }
      }
    }
    List<Table> tables = new ArrayList<>(tableRuleMap.size());
    //获取表的详细信息（列信息）
    tableRuleMap.forEach((fullTableName, tableRule) -> {
      log.debug("获取表: " + fullTableName);
      //后续使用简单表名
      String tableName = tableRule.getSimpleTableName(fullTableName);
      cn.hutool.db.meta.Table tableMeta = DatabaseMetaUtil.getTableMeta(ds, tableRule.getCatalog(), tableRule.getSchema(), tableName);
      //使用 tableRule 处理表
      Table table = new Table(Name.of(tableName, tableRule.getReplaceName(tableName)), tableMeta.getComment());
      table.setCatalog(tableMeta.getCatalog());
      table.setSchema(tableMeta.getSchema());
      for (cn.hutool.db.meta.Column c : tableMeta.getColumns()) {
        //不排除列的情况下添加列
        String columnName = c.getName();
        if (!tableRule.ignoreColumn(columnName)) {
          log.trace("记录列: " + columnName);
          Column column = new Column(Name.of(database.wrapKeyword(columnName), tableRule.getColumnReplaceName(columnName)), c.getComment());
          column.setType(c.getType());
          column.setTypeName(c.getTypeName());
          column.setNullable(c.isNullable());
          column.setPk(c.isPk());
          column.setLength(c.getSize());
          column.setScale(c.getDigit());
          column.setAutoIncrement(c.isAutoIncrement());
          table.addColumn(column);
        } else {
          log.trace("排除列: " + columnName);
        }
      }
      tables.add(table);
    });
    return tables;
  }
}
