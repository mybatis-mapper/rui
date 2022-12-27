package io.mybatis.rui.template.struct.generator;

import io.mybatis.rui.template.Context;
import io.mybatis.rui.template.struct.Generator;
import io.mybatis.rui.template.struct.Structure;
import io.mybatis.rui.template.struct.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * 结构生成器
 */
public class StructGenerator implements Generator {
  private final Map<Type, Generator> structGeneratorMap;

  public StructGenerator() {
    structGeneratorMap = new HashMap<>();
    structGeneratorMap.put(Type.DIR, new DirGenerator());
    structGeneratorMap.put(Type.STATIC, new StaticGenerator());
    structGeneratorMap.put(Type.PACKAGE, new PackageGenerator());
    structGeneratorMap.put(Type.TEMPLATE, new TemplateGenerator());
  }

  /**
   * 生成代码
   *
   * @param params
   */
  @Override
  public void generator(Context context, Structure structure, Map<String, Object> params) {
    if (structure.isEnabled()) {
      structGeneratorMap.get(structure.getType()).generator(context, structure, params);
    }
  }

}
