package io.mybatis.rui.template;

import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.core.util.StrUtil;
import io.mybatis.rui.template.database.DatabaseMetaData;
import io.mybatis.rui.template.engine.FreeMarkerTemplateEngine;
import io.mybatis.rui.template.engine.Mvel2DataEngine;
import io.mybatis.rui.template.struct.Merge;
import io.mybatis.rui.template.struct.Structure;
import io.mybatis.rui.template.struct.UserCodesMerge;
import io.mybatis.rui.template.struct.generator.StructGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Getter
@Setter
@Slf4j(topic = "Context")
public class Context {
  /**
   * 项目信息
   */
  private final Project             project;
  /**
   * 数据源 - all
   */
  private final Map<String, Object> dataMap;
  /**
   * 模板引擎实例
   */
  private       TemplateEngine      templateEngine;
  /**
   * 数据引擎
   */
  private       DataEngine          dataEngine;
  /**
   * 获取数据源信息
   */
  private       DatabaseMetaData    databaseMetaData;
  /**
   * 文件写入功能（可以读取写入后的文件）
   */
  private       GenFileSystem       genFileSystem      = GenFileSystem.DEFAULT;
  /**
   * 读取模板文件功能（相对模板文件的路径，不能读取写入后的文件）
   */
  private       TemplateFileSystem  templateFileSystem = TemplateFileSystem.DEFAULT;
  /**
   * 合并文件内容，暂时不考虑外部设置
   */
  private       Merge               merge              = new UserCodesMerge();
  /**
   * 结构生成器，暂时不考虑外部设置
   */
  private       StructGenerator     structGenerator    = new StructGenerator();

  Context(Project project) {
    this.project = project;
    this.dataMap = new LinkedHashMap<>();
  }

  /**
   * 实例化对象
   *
   * @param instanceClass
   * @param <T>
   * @return
   */
  public static <T> T newInstance(String instanceClass) {
    try {
      return newInstance(Class.forName(instanceClass));
    } catch (Exception e) {
      throw new RuntimeException("实例化[ " + instanceClass + " ]失败", e);
    }
  }

  /**
   * 实例化对象
   *
   * @param instanceClass
   * @param <T>
   * @return
   */
  public static <T> T newInstance(Class instanceClass) {
    try {
      return (T) instanceClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("实例化[ " + instanceClass + " ]失败", e);
    }
  }

  void initContext(Map<String, Object> params) {
    if (templateEngine == null) {
      if (StrUtil.isNotEmpty(project.getTemplateEngineClass())) {
        templateEngine = newInstance(project.getTemplateEngineClass());
      } else {
        templateEngine = new FreeMarkerTemplateEngine();
      }
    }
    if (dataEngine == null) {
      if (StrUtil.isNotEmpty(project.getDataEngineClass())) {
        dataEngine = newInstance(project.getDataEngineClass());
      } else {
        dataEngine = new Mvel2DataEngine();
      }
    }
    if (databaseMetaData == null && StrUtil.isNotEmpty(project.getDatabaseMetaDataClass())) {
      databaseMetaData = newInstance(project.getDatabaseMetaDataClass());
    }
    if (project.getDatabase() != null && project.getDatabase().getJdbcConnection() != null) {
      //避免反复获取
      if (!dataMap.containsKey("tables")) {
        addData("tables", project.getDatabase().getTables(this));
      }
    }
    //初始化文件系统（目前对VFS有用）
    String path = process(project.getPath(), params);
    log.debug("生成项目路径: " + path);
    genFileSystem.init(path);
    String templates = process(project.getTemplates(), params);
    log.debug("读取模板路径: " + templates);
    templateFileSystem.init(templates);
  }

  /**
   * 添加数据源
   *
   * @param name
   * @param dataSource
   */
  public void addData(String name, Object dataSource) {
    dataMap.put(name, dataSource);
  }

  /**
   * 读取模板资源文件
   *
   * @param resource
   * @return
   */
  public String readTemplate(String resource) {
    return templateFileSystem.readStr(resource);
  }

  /**
   * 读取生成的文件
   *
   * @param resource
   * @return
   */
  public String readGen(String resource) {
    return genFileSystem.readStr(resource);
  }

  /**
   * 写入文件
   *
   * @param content
   * @param file
   */
  public void writeStr(String content, File file) {
    genFileSystem.writeStr(content, file);
  }

  /**
   * 创建目录
   *
   * @param file
   */
  public void mkdirs(File file) {
    genFileSystem.mkdirs(file);
  }

  /**
   * 处理模板串
   *
   * @param template
   * @param params
   * @return
   */
  public String process(String template, Map<String, Object> params) {
    return process(template, params, 1);
  }

  /**
   * 处理模板串, 由于模板可能嵌套模板，因此可能需要执行多次才能全部处理干净
   *
   * @param template
   * @param params
   * @param times    执行几次
   * @return
   */
  public String process(String template, Map<String, Object> params, int times) {
    if (StrUtil.isEmpty(template)) {
      return template;
    }
    for (int i = 0; i < times; i++) {
      template = templateEngine.process(template, params);
    }
    return template;
  }

  /**
   * 处理模板内容或模板文件
   *
   * @param template
   * @param params
   * @return
   */
  public String processTemplateOrFile(String template, Map<String, Object> params) {
    return processTemplateOrFile(template, params, 1);
  }

  /**
   * 处理模板内容或模板文件
   *
   * @param template
   * @param params
   * @return
   */
  public String processTemplateOrFile(String template, Map<String, Object> params, int times) {
    //使用模板文件时不能有换行，不能多行文本
    if (!template.contains("\n")) {
      try {
        //读取目标文件
        template = readTemplate(template);
      } catch (NoResourceException ignore) {
        log.warn("没有找到 " + template + " 对应的模板文件，使用模板本身的值作为模板使用");
      }
    }
    if (StrUtil.isNotEmpty(template)) {
      return process(template, params, times);
    }
    return "";
  }

  /**
   * 根据表达式从参数中提取值
   *
   * @param expression 表达式
   * @param vars       参数
   * @return
   */
  public Object eval(String expression, Map<String, Object> vars) {
    return dataEngine.eval(expression, vars);
  }

  /**
   * 计算表达式的真假
   *
   * @param expression 表达式 不提供表达式时，默认 true
   * @param vars       参数
   * @return
   */
  public Boolean evalToBoolean(String expression, Map<String, Object> vars) {
    if (StrUtil.isEmpty(expression)) {
      return true;
    }
    return dataEngine.evalToBoolean(expression, vars);
  }

  /**
   * 迭代绑定的数据源
   *
   * @param params
   * @param consumer
   */
  public void iterableDatas(Structure structure, Map<String, Object> params, BiConsumer<Map<String, Object>, Object> consumer) {
    String iter = structure.getIter();
    if (StrUtil.isEmpty(iter)) {
      if (evalToBoolean(structure.getFilter(), params)) {
        consumer.accept(params, null);
      }
    } else {
      if (evalToBoolean(structure.getFilter(), params)) {
        Object datas = eval(iter, params);
        if (datas.getClass().isArray()) {
          datas = Arrays.asList((Object[]) datas);
        }
        if (datas instanceof Iterable) {
          params.put("iter", datas);
          Map<String, Object> paramsIter;
          for (Object it : (Iterable) datas) {
            //迭代过程中，params 中的 path 一直在修改，这会导致同级的内容变成递归嵌套
            //为了避免同级节点的数据出错，需要创建新的 params 避免冲突
            paramsIter = new HashMap<>(params);
            paramsIter.put(structure.getIterName(), it);
            if (evalToBoolean(structure.getItFilter(), paramsIter)) {
              consumer.accept(paramsIter, it);
            }
          }
        } else {
          throw new RuntimeException("iter: " + iter + " isn't iterable");
        }
      }
    }
  }

  /**
   * 生成代码结构
   *
   * @param context
   * @param structure
   * @param params
   */
  public void generator(Context context, Structure structure, Map<String, Object> params) {
    structGenerator.generator(context, structure, params);
  }

  /**
   * 合并生成的文件
   *
   * @param structure 合并的文件结构信息
   * @param params    参数
   * @param fileName  文件名
   * @param before    已存在的内容
   * @param after     新的内容
   * @return
   */
  public String merge(Structure structure, Map<String, Object> params, String fileName, String before, String after) {
    return merge.merge(this, structure, params, fileName, before, after);
  }

}
