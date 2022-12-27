package io.mybatis.rui.model;

import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java 类型
 */
@Getter
public class JavaType {
  private static final Map<String, JavaType> JAVA_TYPE_MAP = new ConcurrentHashMap<>();

  private final String original;
  private final String simpleName;
  private final String fullName;

  private JavaType(@NonNull Class type) {
    this.original = type.getName();
    this.fullName = type.getName();
    this.simpleName = type.getSimpleName();
  }

  private JavaType(@NonNull String original) {
    this.original = original;
    this.fullName = original;
    this.simpleName = original.substring(original.lastIndexOf(".") + 1);
  }

  public static JavaType of(Class type) {
    String javaType = type.getName();
    if (!JAVA_TYPE_MAP.containsKey(javaType)) {
      synchronized (JavaType.class) {
        if (!JAVA_TYPE_MAP.containsKey(javaType)) {
          JAVA_TYPE_MAP.put(javaType, new JavaType(type));
        }
      }
    }
    return JAVA_TYPE_MAP.get(javaType);
  }

  public static JavaType of(String javaType) {
    if (!JAVA_TYPE_MAP.containsKey(javaType)) {
      synchronized (JavaType.class) {
        if (!JAVA_TYPE_MAP.containsKey(javaType)) {
          JAVA_TYPE_MAP.put(javaType, new JavaType(javaType));
        }
      }
    }
    return JAVA_TYPE_MAP.get(javaType);
  }

  @Override
  public String toString() {
    return simpleName;
  }
}
