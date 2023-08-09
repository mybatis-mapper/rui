package io.mybatis.rui.cli;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.mybatis.rui.cli.parameter.DialectParameter;
import io.mybatis.rui.cli.parameter.DriverParameter;
import io.mybatis.rui.cli.parameter.LevelParameter;
import io.mybatis.rui.cli.parameter.ProjectParameter;
import io.mybatis.rui.template.Project;
import io.mybatis.rui.template.database.Database;
import io.mybatis.rui.template.database.Dialect;
import io.mybatis.rui.template.database.JdbcConnection;
import org.slf4j.event.Level;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 启动类，支持参数覆盖
 *
 * @author liuzh
 */
public class Main {

  @Parameter(names = {"-p", "--project"}, description = "代码生成器YAML配置文件", converter = ProjectParameter.class, order = 0)
  private Project project;

  @Parameter(names = {"-o", "--output"}, description = "输出目录，默认使用配置文件中的 path，输出目录如果带 .zip 后缀，就会将生成的代码导出为压缩包", order = 1)
  private String outputPath;

  @Parameter(names = {"-T", "--templates"}, description = "模板文件路径，默认和YAML相同位置，或者为当前执行目录的相对位置", order = 2)
  private String templates;

  @Parameter(names = {"--jdbc.dialect"}, description = "数据库方言", validateWith = DialectParameter.class, converter = DialectParameter.class, order = 11)
  private Dialect jdbcDialect;
  @Parameter(names = {"--jdbc.driver"}, description = "数据库驱动", validateWith = DriverParameter.class, converter = DriverParameter.class, order = 12)
  private Class   jdbcDriver;
  @Parameter(names = {"--jdbc.url"}, description = "数据库URL", order = 13)
  private String  jdbcUrl;
  @Parameter(names = {"--jdbc.user"}, description = "数据库用户", order = 14)
  private String  jdbcUser;
  @Parameter(names = {"--jdbc.password"}, description = "数据库密码", password = true, order = 15)
  private String  jdbcPassword;

  @Parameter(names = {"-t", "--tables"}, description = "要获取的表名，支持模糊匹配(%)，多个表名用逗号隔开，指定该值后会覆盖配置文件中的值", order = 20)
  private List<String> tables;

  @DynamicParameter(names = {"-A", "-attrs"}, description = "项目附加属性，会覆盖项目下的 attrs 配置", order = 30)
  private Map<String, String> attrs = new HashMap<>();

  @Parameter(names = {"--level"}, description = "日志级别, 默认 trace，必须作为第一个参数才能生效", validateWith = LevelParameter.class, converter = LevelParameter.class, order = 40)
  private Level level;

  @Parameter(names = {"-h", "--help"}, help = true, description = "显示帮助信息", order = Integer.MAX_VALUE)
  private boolean help;

  private JCommander commander;

  public static void main(String[] args) {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    Main main = new Main();
    main.commander = JCommander.newBuilder().addObject(main).build();
    main.commander.setProgramName("java -cp rui-cli.jar:jdbc-driver.jar io.mybatis.rui.cli.Main");
    main.commander.parse(args);
    if (main.help) {
      main.commander.usage();
    } else {
      main.init();
      main.generate();
    }
  }

  /**
   * 从标准输入获取配置文件
   *
   * @return
   */
  protected Project loadFromStdIn() {
    try {
      if (System.in.available() > 0) {
        return new Yaml(new Constructor(Project.class, new LoaderOptions())).load(System.in);
      }
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * 初始化参数
   */
  protected void init() {
    if (project == null) {
      project = loadFromStdIn();
      if (project == null) {
        System.err.println("无法获取代码生成器配置，请通过参数或标准输入方式提供代码生成器配置文件");
        commander.usage();
        System.exit(-1);
      }
    }
    if (StrUtil.isNotBlank(outputPath) && !outputPath.toLowerCase().endsWith(".zip")) {
      project.setPath(outputPath);
    }
    if (StrUtil.isNotBlank(templates)) {
      project.setTemplates(templates);
    }
    if (attrs.size() > 0) {
      Map<String, String> projectAttrs = project.getAttrs();
      if (projectAttrs == null) {
        project.setAttrs(attrs);
      } else {
        projectAttrs.putAll(attrs);
      }
    }
    //数据库信息
    if (jdbcDialect != null) {
      setJdbcInfo(jdbcConnection -> jdbcConnection.setDialect(jdbcDialect));
    }
    if (jdbcDriver != null) {
      setJdbcInfo(jdbcConnection -> jdbcConnection.setDriver(jdbcDriver.getName()));
    }
    if (jdbcUrl != null) {
      setJdbcInfo(jdbcConnection -> jdbcConnection.setUrl(jdbcUrl));
    }
    if (jdbcUser != null) {
      setJdbcInfo(jdbcConnection -> jdbcConnection.setUser(jdbcUser));
    }
    if (jdbcPassword != null) {
      setJdbcInfo(jdbcConnection -> jdbcConnection.setPassword(jdbcPassword));
    }
    if (CollectionUtil.isNotEmpty(tables)) {
      Database database = project.getDatabase();
      if (database == null) {
        throw new ParameterException("无法获取数据库连接信息, tables 设置无效");
      }
      database.setTables(tables);
      database.setTableRules(null);
    }
  }

  /**
   * 设置 jdbc 信息
   *
   * @param jdbcConnectionConsumer
   */
  private void setJdbcInfo(Consumer<JdbcConnection> jdbcConnectionConsumer) {
    Database database = project.getDatabase();
    if (database == null) {
      database = new Database();
      project.setDatabase(database);
    }
    JdbcConnection jdbcConnection = database.getJdbcConnection();
    if (jdbcConnection == null) {
      jdbcConnection = new JdbcConnection();
      database.setJdbcConnection(jdbcConnection);
    }
    jdbcConnectionConsumer.accept(jdbcConnection);
  }

  protected void generate() {
    //压缩包时
    if (StrUtil.isNotBlank(outputPath) && outputPath.toLowerCase().endsWith(".zip")) {
      project.preview().syncDisk(FileUtil.file(outputPath));
    } else {
      //输出到目录
      project.generate();
    }
  }

}
