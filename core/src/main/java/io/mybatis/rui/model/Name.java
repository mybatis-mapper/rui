package io.mybatis.rui.model;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 名字，可以获取名字的各种形式
 */
@Getter
public class Name {
  /**
   * 缓存相同名称
   */
  private static final Map<String, Name> NAME_MAP = new ConcurrentHashMap<>();
  /**
   * 全小写形式，例如 USER_ROLE => user_role, USER => user
   */
  private final        Plural            lowercase;
  /**
   * 全大写形式，例如 user_role => USER_ROLE, user => USER
   */
  private final        Plural            uppercase;
  /**
   * 下划线形式，例如 UserRole => user_role, USER => USER
   */
  private final        Plural            underlineCase;
  /**
   * 无下划线形式，例如 UserRole => userrole, USER => USER
   */
  private final        Plural            noUnderlineCase;
  /**
   * 大写下划线形式，例如 UserRole => USER_ROLE，user => USER
   */
  private final        Plural            upperUnderlineCase;
  /**
   * 大写下划线形式，例如 UserRole => USERROLE，user => USER
   */
  private final        Plural            upperNoUnderlineCase;
  /**
   * 驼峰形式，例如 USER_ROLE => userRole, USER = user
   */
  private final        Plural            camelCase;
  /**
   * 首字母大写驼峰形式，例如 user_role => UserRole，user => User，USER => User
   */
  private final        Plural            upperCamelCase;
  /**
   * 类名形式，同 {@link #upperCamelCase}
   */
  private final        Plural            className;
  /**
   * 字段形式，同 {@link #camelCase}
   */
  private final        Plural            fieldName;
  /**
   * 默认使用的名字，可以是替换后的名字
   */
  private final        Plural            name;
  /**
   * 原始值，例如数据库表名 t_order0，替换为 order，此时 original 为 t_order0，name 为 order（多用于表分片和逻辑表名）
   */
  private              Plural            original;

  /**
   * 允许修改名称，例如去掉前缀
   *
   * @param original 原始值，如 sys_user
   * @param name     使用值，如 user
   */
  private Name(@NonNull String original, @NonNull String name) {
    this(name);
    this.original = Plural.of(original);
  }

  /**
   * 名称
   *
   * @param original
   */
  private Name(String original) {
    this.name = Plural.of(original);
    this.original = Plural.of(original);
    //表名中可能存在空格（不规范）
    String temp = original.replaceAll("\\s", "");
    this.lowercase = Plural.of(temp.toLowerCase());
    this.uppercase = Plural.of(temp.toUpperCase());
    this.underlineCase = Plural.of(StrUtil.toUnderlineCase(temp));
    this.noUnderlineCase = Plural.of(this.underlineCase.toString().replaceAll("_", ""));
    this.upperUnderlineCase = Plural.of(this.underlineCase.toString().toUpperCase());
    this.upperNoUnderlineCase = Plural.of(this.upperUnderlineCase.toString().replaceAll("_", ""));
    this.camelCase = Plural.of(StrUtil.toCamelCase(temp.toLowerCase()));
    this.upperCamelCase = Plural.of(StrUtil.upperFirst(this.camelCase.toString()));
    this.className = this.upperCamelCase;
    this.fieldName = this.camelCase;
  }

  /**
   * 创建 Name
   *
   * @param name
   * @return
   */
  public static Name of(@NonNull String name, @NonNull String override) {
    String key = name + "#" + override;
    if (!NAME_MAP.containsKey(key)) {
      synchronized (Name.class) {
        if (!NAME_MAP.containsKey(key)) {
          NAME_MAP.put(key, new Name(name, override));
        }
      }
    }
    return NAME_MAP.get(key);
  }

  /**
   * 创建 Name
   *
   * @param name
   * @return
   */
  public static Name of(@NonNull String name) {
    if (!NAME_MAP.containsKey(name)) {
      synchronized (Name.class) {
        if (!NAME_MAP.containsKey(name)) {
          NAME_MAP.put(name, new Name(name));
        }
      }
    }
    return NAME_MAP.get(name);
  }

  @Override
  public String toString() {
    return name.toString();
  }
}
