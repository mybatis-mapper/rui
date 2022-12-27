package io.mybatis.rui.template.engine;

import io.mybatis.rui.template.TemplateEngine;
import org.mvel2.templates.TemplateRuntime;

import java.util.Map;

/**
 * 官方文档: http://mvel.documentnode.com/
 */
public class Mvel2TemplateEngine implements TemplateEngine {

  @Override
  public String process(String template, Map<String, Object> params) {
    return (String) TemplateRuntime.eval(template, params);
  }

}
