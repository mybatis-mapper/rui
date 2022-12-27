package io.mybatis.rui.template.engine;

import io.mybatis.rui.template.DataEngine;
import org.mvel2.MVEL;

import java.util.Map;

/**
 * 默认的数据引擎
 */
public class Mvel2DataEngine implements DataEngine {
  @Override
  public Object eval(String expression, Map<String, Object> vars) {
    return MVEL.eval(expression, vars);
  }

  @Override
  public Boolean evalToBoolean(String expression, Map<String, Object> vars) {
    return MVEL.evalToBoolean(expression, vars);
  }
}
