package io.mybatis.rui.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.mybatis.rui.VFS;
import io.mybatis.rui.template.database.Database;
import io.mybatis.rui.template.struct.Structure;
import io.mybatis.rui.template.struct.Type;
import io.mybatis.rui.template.vfs.VFSGenFileSystem;
import io.mybatis.rui.template.vfs.VFSTemplateFileSystem;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 顶层项目
 */
@Getter
@Setter
@Slf4j(topic = "Project")
public class Project extends Structure {
  /**
   * 项目根路径
   */
  protected         String   path;
  /**
   * 模板相对 classpath 路径
   */
  private           String   templates;
  /**
   * 代码数据源
   */
  private           Database database;
  /**
   * 模板引擎配置
   */
  private           String   templateEngineClass;
  /**
   * 数据引擎配置
   */
  private           String   dataEngineClass;
  /**
   * 数据源获取数据配置
   */
  private           String   databaseMetaDataClass;
  /**
   * 封装一些方法便于调用
   */
  private transient Context  context;

  public Project() {
    //设置类型
    setType(Type.DIR);
    context = new Context(this);
  }

  /**
   * 从资源文件加载
   *
   * @param resource
   * @return
   */
  public static Project load(@NonNull String resource) {
    return load(resource, Project.class);
  }

  @Override
  public void initParams(Context context, Map<String, Object> params) {
    super.initParams(context, params);
    params.put("templates", context.process(templates, params));
    params.put("path", context.process(path, params));
  }

  /**
   * 处理模板路径，当使用可执行 jar 包运行时，资源文件可以在任何位置，也可能用到绝对路径
   *
   * @param params
   */
  protected void initPath(Map<String, Object> params) {
    String basedir = System.getProperty("user.dir");
    log.debug("执行程序路径: " + basedir);
    //执行的路径
    params.put("basedir", basedir);
    log.debug("basedir变量: " + basedir);
    //如果没有设置生成文件路径，就生成到当前执行的路径下
    if (StrUtil.isEmpty(path)) {
      path = basedir;
      log.debug("设置path: " + basedir);
    }
    //如果通过资源文件创建的 Project，可以通过该文件的路径来计算模板其他文件的路径
    if (StrUtil.isNotEmpty(getRef())) {
      String path = getRef();
      basedir = FileUtil.file(path).getParent();
      params.put("yamlDir", basedir);
      log.debug("yamlDir变量: " + basedir);
    }
    //模板路径
    if (StrUtil.isEmpty(templates)) {
      templates = basedir;
      log.debug("设置templates: " + templates);
    } else if (basedir.lastIndexOf(templates) > -1) {
      templates = basedir.substring(0, basedir.lastIndexOf(templates) + templates.length());
      log.debug("设置templates: " + templates);
    }
  }

  /**
   * 生成代码
   */
  public void generate() {
    Map<String, Object> params = new HashMap<>();
    params.put("id", id);
    params.put("project", this);
    params.put("context", context);
    params.put("database", database);
    params.put("SYS", System.getProperties());
    traceLogMap("SYS", System.getProperties());
    params.put("ENV", System.getenv());
    traceLogMap("ENV", System.getenv());
    //处理路径相关的默认值
    initPath(params);
    //每次生成代码时，初始化 context
    context.initContext(params);
    //获取所有数据信息
    params.putAll(context.getDataMap());
    //生成代码
    generator(context, null, params);
  }

  /**
   * 使用指定的模板进行生成（仍然是 project.yaml 中配置的模板）
   *
   * @param template 指定模板目录或者模板压缩文件
   */
  public void generate(File template) {
    VFS vfs = VFS.load(template);
    VFSTemplateFileSystem templateFileSystem = new VFSTemplateFileSystem(vfs);
    TemplateFileSystem old = context.getTemplateFileSystem();
    context.setTemplateFileSystem(templateFileSystem);
    generate();
    context.setTemplateFileSystem(old);
  }

  /**
   * 预览代码，返回虚拟文件结构
   *
   * @return 虚拟文件结构
   */
  public VFS preview() {
    VFSGenFileSystem writeFileSystem = new VFSGenFileSystem();
    GenFileSystem old = context.getGenFileSystem();
    context.setGenFileSystem(writeFileSystem);
    generate();
    context.setGenFileSystem(old);
    return writeFileSystem.getVfs();
  }

  /**
   * 输出 map 可用参数信息
   *
   * @param variable
   * @param map
   */
  private void traceLogMap(String variable, Map map) {
    if (log.isTraceEnabled()) {
      log.trace(variable + "可用参数:");
      map.forEach((k, v) -> {
        log.trace(variable + "['" + k + "'] = " + v);
      });
    }
  }

  /**
   * 添加数据源
   *
   * @param name
   * @param dataSource
   */
  public void addDataSource(String name, Iterable dataSource) {
    context.addData(name, dataSource);
  }

  public Database getDatabase() {
    return database != null ? database.getR() : null;
  }

}
