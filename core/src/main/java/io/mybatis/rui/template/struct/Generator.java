package io.mybatis.rui.template.struct;

import io.mybatis.rui.template.Context;

import java.util.Map;

public interface Generator {

  /**
   * 生成代码
   *
   * @param context
   * @param structure
   * @param params
   */
  void generator(Context context, Structure structure, Map<String, Object> params);

  /**
   * 获取代码生成路径
   *
   * @param params
   * @return
   */
  default String getPath(Map<String, Object> params) {
    return (String) params.get("path");
  }

  /**
   * 更新代码生成路径
   *
   * @param params
   * @param path
   */
  default void updatePath(Map<String, Object> params, String path) {
    params.put("path", path);
  }
}
