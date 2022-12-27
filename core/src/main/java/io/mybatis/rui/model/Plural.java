package io.mybatis.rui.model;

import lombok.Getter;
import lombok.NonNull;
import org.atteo.evo.inflector.English;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单词的复数形式
 */

public class Plural {
  private static final Map<String, Plural> PLURAL_MAP = new ConcurrentHashMap<>();
  @Getter
  private final        String              o;
  @Getter
  private final        String              s;

  private Plural(String o) {
    this.o = o;
    this.s = English.plural(o);
  }

  /**
   * 创建 Name
   *
   * @param o
   * @return
   */
  public static Plural of(@NonNull String o) {
    if (!PLURAL_MAP.containsKey(o)) {
      synchronized (Plural.class) {
        if (!PLURAL_MAP.containsKey(o)) {
          PLURAL_MAP.put(o, new Plural(o));
        }
      }
    }
    return PLURAL_MAP.get(o);
  }

  @Override
  public String toString() {
    return o;
  }
}
