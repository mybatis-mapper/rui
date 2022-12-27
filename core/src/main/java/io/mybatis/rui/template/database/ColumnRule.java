package io.mybatis.rui.template.database;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class ColumnRule extends Rule {
  /**
   * 忽略/排除字段
   */
  private boolean ignore;

  public ColumnRule() {
  }

  public ColumnRule(@NonNull String name) {
    super(name);
  }
}
