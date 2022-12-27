/**
 * Copyright 2009-2018 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mybatis.rui.model;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * javaType refer JavaTypeResolverDefaultImpl
 *
 * @author Clinton Begin
 */
public enum JdbcType {
  /*
   * This is added to enable basic support for the
   * ARRAY data type - but a custom type handler is still required
   */
  ARRAY(Types.ARRAY, Object.class),
  BIT(Types.BIT, Boolean.class),
  TINYINT(Types.TINYINT, Byte.class),
  SMALLINT(Types.SMALLINT, Short.class),
  INTEGER(Types.INTEGER, Integer.class),
  BIGINT(Types.BIGINT, Long.class),
  FLOAT(Types.FLOAT, Double.class),
  REAL(Types.REAL, Float.class),
  DOUBLE(Types.DOUBLE, Double.class),
  NUMERIC(Types.NUMERIC, BigDecimal.class),
  DECIMAL(Types.DECIMAL, BigDecimal.class),
  CHAR(Types.CHAR, String.class),
  VARCHAR(Types.VARCHAR, String.class),
  LONGVARCHAR(Types.LONGVARCHAR, String.class),
  DATE(Types.DATE, Date.class),
  TIME(Types.TIME, Date.class),
  TIMESTAMP(Types.TIMESTAMP, Date.class),
  BINARY(Types.BINARY, "byte[]"),
  VARBINARY(Types.VARBINARY, "byte[]"),
  LONGVARBINARY(Types.LONGVARBINARY, "byte[]"),
  NULL(Types.NULL, Object.class),
  OTHER(Types.OTHER, Object.class),
  BLOB(Types.BLOB, "byte[]"),
  CLOB(Types.CLOB, String.class),
  BOOLEAN(Types.BOOLEAN, Boolean.class),
  CURSOR(-10), // Oracle
  UNDEFINED(Integer.MIN_VALUE + 1000),
  NVARCHAR(Types.NVARCHAR, String.class), // JDK6
  NCHAR(Types.NCHAR, String.class), // JDK6
  NCLOB(Types.NCLOB, String.class), // JDK6
  STRUCT(Types.STRUCT, Object.class),
  JAVA_OBJECT(Types.JAVA_OBJECT, Object.class),
  DISTINCT(Types.DISTINCT, Object.class),
  REF(Types.REF, Object.class),
  DATALINK(Types.DATALINK, Object.class),
  ROWID(Types.ROWID), // JDK6
  LONGNVARCHAR(Types.LONGNVARCHAR, String.class), // JDK6
  SQLXML(Types.SQLXML), // JDK6
  DATETIMEOFFSET(-155), // SQL Server 2008
  TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE, "java.time.OffsetTime"), // JDBC 4.2 JDK8
  TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE, "java.time.OffsetDateTime"); // JDBC 4.2 JDK8

  private static final Map<Integer, JdbcType> codeLookup = new HashMap<>();

  static {
    for (JdbcType type : JdbcType.values()) {
      codeLookup.put(type.TYPE_CODE, type);
    }
  }

  public final int      TYPE_CODE;
  public       JavaType javaType;

  JdbcType(int code) {
    this.TYPE_CODE = code;
  }

  JdbcType(int code, Class type) {
    this.TYPE_CODE = code;
    this.javaType = JavaType.of(type);
  }

  JdbcType(int code, String type) {
    this.TYPE_CODE = code;
    this.javaType = JavaType.of(type);
  }

  public static JdbcType forCode(int code) {
    return codeLookup.get(code);
  }

}
