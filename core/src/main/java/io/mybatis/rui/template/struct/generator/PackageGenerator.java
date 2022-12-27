package io.mybatis.rui.template.struct.generator;

import io.mybatis.rui.model.Package;
import io.mybatis.rui.template.Context;
import io.mybatis.rui.template.struct.Generator;
import io.mybatis.rui.template.struct.Mode;
import io.mybatis.rui.template.struct.Structure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

@Slf4j(topic = "Generator")
public class PackageGenerator implements Generator {

  /**
   * 生成包
   *
   * @param params
   */
  @Override
  public void generator(Context context, Structure structure, Map<String, Object> params) {
    //paramsIter 是为了解决当出现迭代时，避免同级修改 path 等变量导致冲突
    context.iterableDatas(structure, params, (paramsIter, item) -> {
      generatorPackage(context, structure, paramsIter);
    });
  }

  protected void generatorPackage(Context context, Structure structure, Map<String, Object> params) {
    //将自己的内部信息放入 params
    structure.initParams(context, params);
    String path = getPath(params);
    //包名
    String name = structure.getName();
    //包名支持模板
    name = context.process(name, params);
    //包名转换为目录名
    String folderName = name.replaceAll("\\.", "/");
    File file = new File(path, folderName);
    //文件不存在或者为覆盖模式时，生成目录，反之，目录下面的文件也不会重复生成
    if (!file.exists() || structure.getMode() == Mode.OVERRIDE) {
      //创建多级目录结构
      log.debug((file.exists() ? "已存在包: " : "创建包: ") + name);
      context.mkdirs(file);
      //将当前路径记录下面，作为子级目录的上级供子级使用
      updatePath(params, file.getAbsolutePath());
      //初始化包名，设置当前包名
      Package.initPackage(params, name);
      //处理子级
      if (structure.getFiles() != null) {
        structure.getFiles().forEach(child -> {
          child.generator(context, structure, params);
        });
      }
    }

  }

}
