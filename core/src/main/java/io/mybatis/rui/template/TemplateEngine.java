package io.mybatis.rui.template;

import java.util.Map;

/**
 * 模板处理器
 */
public interface TemplateEngine {

  /**
   * 处理模板串
   *
   * @param template
   * @param params
   * @return
   */
  String process(String template, Map<String, Object> params);

}
