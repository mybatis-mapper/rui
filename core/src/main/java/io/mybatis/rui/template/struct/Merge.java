package io.mybatis.rui.template.struct;

import io.mybatis.rui.template.Context;

import java.util.Map;

/**
 * 合并文件内容
 */
public interface Merge {

  /**
   * 合并文件
   *
   * @param context   上下文
   * @param structure 合并的文件结构信息
   * @param params    参数
   * @param fileName  文件名
   * @param before    已存在的内容
   * @param after     新的内容
   * @return 返回合并后的内容
   */
  String merge(Context context, Structure structure, Map<String, Object> params, String fileName, String before, String after);

}
