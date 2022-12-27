package io.mybatis.rui.template.engine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.mybatis.rui.template.TemplateEngine;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * 官方文档: http://www.kerneler.com/freemarker2.3.23/ref.html
 */
public class FreeMarkerTemplateEngine implements TemplateEngine {

  private static final Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

  static {
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
  }

  @Override
  public String process(String template, Map<String, Object> params) {
    try {
      Template tpl = new Template(template, new StringReader(template), cfg);
      StringWriter stringWriter = new StringWriter();
      tpl.process(params, stringWriter);
      return stringWriter.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
