package io.mybatis.rui.template.struct;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.mybatis.rui.template.Context;
import io.mybatis.rui.template.Ref;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件夹，包，模板
 */
@Getter
@Setter
public class Structure extends Ref<Structure> {
  /**
   * 项目id
   */
  protected           String              id;
  /**
   * 文件名，当file不为空时，文件内容为file；
   * 当file为空的时候，name可以对应非模板文件，如果不存在对应的文件，文件内容为空。
   */
  protected           String              name;
  /**
   * 模板文件，当name不为空时，file是模板内容，当name为空时，file既是name，也是模板内容
   */
  protected           String              file;
  /**
   * 文件类型，可选
   * <p>
   * 默认类型根据以下字段优先级判断
   * <ol>
   *   <li>type指定值</li>
   *   <li>files(包含子目录)[DIR]</li>
   *   <li>content(内容)[TEMPLATE]</li>
   *   <li>template(模板)[TEMPLATE]</li>
   * </ol>
   */
  protected           Type                type;
  /**
   * 附加属性
   */
  protected           Map<String, String> attrs;
  /**
   * 是否启用当前结构，默认启用，禁用后会排除当前结构
   */
  protected           boolean             enabled  = true;
  /**
   * 模板被执行几次，默认1次
   */
  protected           int                 times    = 1;
  /**
   * 绑定的数据源，使用 mvel 表达式
   */
  protected           String              iter;
  /**
   * 迭代使用的名称
   */
  protected           String              iterName = "it";
  /**
   * 绑定数据过滤器，使用 mvel 表达式，过滤条件不满足时不执行当前的操作
   */
  protected           String              filter;
  /**
   * 迭代数据过滤器，当指定 {@link #iter} 时，对迭代的数据进行过滤
   */
  protected           String              itFilter;
  /**
   * 模式，默认覆盖 OVERRIDE，可选值参考 {@link Mode}
   */
  protected           Mode                mode     = Mode.OVERRIDE;
  /**
   * 父级
   */
  protected transient Structure           parent;
  /**
   * 子目录
   */
  protected           List<Structure>     files;

  /**
   * 初始化参数，需要在具体语境中初始化
   *
   * @param params
   */
  public void initParams(Context context, Map<String, Object> params) {
    //file中记录的都是原始值
    params.put("file", this);
    params.put("id", id);
    params.put("type", type);
    params.put("mode", mode);
    params.put("ref", getRef());
    //下面所有字段都是经过模板处理后的值
    if (CollectionUtil.isNotEmpty(this.attrs)) {
      Map<String, String> fileAttrs = new HashMap<>(this.attrs);
      params.put("attrs", fileAttrs);
      fileAttrs.keySet().forEach(key -> {
        String val = fileAttrs.get(key);
        if (StrUtil.isNotEmpty(val)) {
          fileAttrs.put(key, context.process(val, params));
        }
      });
    } else {
      params.put("attrs", Collections.emptyMap());
    }
    params.put("name", StrUtil.isNotEmpty(name) ? context.process(name, params) : "");
    params.put("file", StrUtil.isNotEmpty(file) ? context.process(file, params) : "");
  }

  /**
   * 计算文件类型
   *
   * @param parent
   */
  protected void autoStruct(Structure parent) {
    if (type != null) {
      //已经指定了类型，不需要自动处理
    } else if (files != null) {
      if (parent != null && (parent.type == Type.PACKAGE)) {
        type = Type.PACKAGE;
      } else {
        type = Type.DIR;
      }
    } else if (StrUtil.isNotEmpty(file)) {
      //模板优先级高于静态文件
      type = Type.TEMPLATE;
    } else if (StrUtil.isNotEmpty(name)) {
      type = Type.STATIC;
    } else {
      type = parent != null ? parent.type : Type.DIR;
    }
  }

  /**
   * 生成代码，主要处理数据（parent.parent..)
   *
   * @param context
   * @param parent
   * @param params
   */
  public void generator(Context context, Structure parent, Map<String, Object> params) {
    Map<String, Object> tempMap = params;
    //如果不是 parent 内部调用，就 clone params
    if (parent != null) {
      params = new HashMap<>();
      params.putAll(tempMap);
      params.put("parent", tempMap);
    }
    //计算文件类型
    autoStruct(parent);
    //生成自己的结构或代码
    context.generator(context, this, params);
  }

  /**
   * 获取子文件
   *
   * @return
   */
  public List<Structure> getFiles() {
    if (CollectionUtil.isNotEmpty(files)) {
      //可以引用其他文件
      return files.stream().map(Structure::getR).collect(Collectors.toList());
    }
    return files;
  }

  /**
   * 设置子级的时候，同时设置父级信息
   *
   * @param files 子文件
   */
  public void setFiles(List<Structure> files) {
    this.files = files;
    if (CollectionUtil.isNotEmpty(files)) {
      files.forEach(file -> file.setParent(this));
    }
  }

  /**
   * 获取属性值，当前不存在时默认向上逐级查找
   *
   * @param attr 属性名
   * @return
   */
  public String getAttr(String attr) {
    return getAttr(attr, null);
  }

  /**
   * 获取属性值，当前不存在时默认向上逐级查找
   *
   * @param attr         属性名
   * @param defaultValue 默认值
   * @return
   */
  public String getAttr(String attr, String defaultValue) {
    return getAttr(attr, defaultValue, true);
  }

  /**
   * 获取属性值，可以设置默认值和是否向上逐级查找
   *
   * @param attr         属性名
   * @param defaultValue 默认值
   * @param parents      递归查找上级
   * @return
   */
  public String getAttr(String attr, String defaultValue, boolean parents) {
    if (MapUtil.isNotEmpty(attrs) && attrs.containsKey(attr)) {
      String val = attrs.get(attr);
      if (val != null) {
        return val;
      }
    } else if (parents && parent != null) {
      return parent.getAttr(attr, defaultValue, parents);
    }
    return defaultValue;
  }

  /**
   * 设置子级的时候，同时设置父级信息
   *
   * @param files 子文件
   */
  public void addFiles(Structure... files) {
    if (this.files == null) {
      this.files = new ArrayList<>();
    }
    for (Structure file : files) {
      file.setParent(this);
    }
    this.files.addAll(Arrays.asList(files));
  }

  @Override
  public String toString() {
    return StrUtil.isNotEmpty(name) ? name : file;
  }
}
