package io.mybatis.rui.template;

import java.util.Map;

/**
 * 数据引擎
 */
public interface DataEngine {
  /**
   * 根据表达式从参数中提取值
   *
   * @param expression 表达式
   * @param vars       参数
   * @return
   */
  Object eval(String expression, Map<String, Object> vars);

  /**
   * 计算表达式的真假
   *
   * @param expression 表达式
   * @param vars       参数
   * @return
   */
  Boolean evalToBoolean(String expression, Map<String, Object> vars);
}
