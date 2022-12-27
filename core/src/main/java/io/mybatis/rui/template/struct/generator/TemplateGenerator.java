package io.mybatis.rui.template.struct.generator;

import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import io.mybatis.rui.template.Context;
import io.mybatis.rui.template.struct.Generator;
import io.mybatis.rui.template.struct.Structure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

@Slf4j(topic = "Generator")
public class TemplateGenerator implements Generator {

  /**
   * 生成模板代码
   *
   * @param params
   */
  @Override
  public void generator(Context context, Structure structure, Map<String, Object> params) {
    //paramsIter 是为了解决当出现迭代时，避免同级修改 path 等变量导致冲突
    context.iterableDatas(structure, params, (paramsIter, item) -> {
      generatorTemplate(context, structure, paramsIter);
    });
  }

  protected void generatorTemplate(Context context, Structure structure, Map<String, Object> params) {
    structure.initParams(context, params);
    String path = getPath(params);
    String name;
    if (StrUtil.isNotEmpty(structure.getName())) {
      name = context.process(structure.getName(), params);
    } else {
      Assert.notEmpty(structure.getFile(), "name 和 file 必须指定一个值");
      //文件名，模板名，不支持模板参数
      name = structure.getFile();
    }
    File file = new File(path, name);
    switch (structure.getMode()) {
      case MERGE:
        //只有文件存在时才会合并文件，否则和 ONCE 一样
        if (file.exists()) {
          String before = context.readGen(file.getAbsolutePath());
          String after = genTemplate(context, structure, params);
          String merge = context.merge(structure, params, name, before, after);
          log.debug("合并已存在文件: " + name);
          context.writeStr(merge, file);
          break;
        }
      case ONCE:
        if (!file.exists()) {
          log.debug("初次创建文件: " + name);
          context.writeStr(genTemplate(context, structure, params), file);
        }
        break;
      case OVERRIDE:
      default:
        log.debug((file.exists() ? "覆盖已存在文件: " : "初次创建文件: ") + name);
        context.writeStr(genTemplate(context, structure, params), file);
        break;
    }
  }

  protected String genTemplate(Context context, Structure structure, Map<String, Object> params) {
    //如果只有文件名，就查找文件名对应的内容，不经过任何模板处理直接使用
    if (StrUtil.isEmpty(structure.getFile())) {
      try {
        //读取目标文件
        return context.readTemplate(structure.getName());
      } catch (NoResourceException ignore) {
        //找不到文件时就创建空文件
        return "";
      }
    } else {
      //模板内容
      return context.processTemplateOrFile(structure.getFile(), params, structure.getTimes());
    }
  }
}
