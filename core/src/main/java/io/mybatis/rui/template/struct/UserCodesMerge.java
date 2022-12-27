package io.mybatis.rui.template.struct;

import cn.hutool.core.util.StrUtil;
import io.mybatis.rui.template.Context;
import org.yaml.snakeyaml.DumperOptions.LineBreak;

import java.util.Map;

/**
 * 合并文件内容，通过规则将用户添加的代码提取到原有的位置
 * <p>
 * 用户代码通过注释中的关键字进行包装，注释只能一行，有开始和结束，默认为：
 * <p>
 * USER CODES START
 * 写自己的代码
 * USER CODES END
 * <p>
 * 在不同的模板中需要用不同的注释方式，但是注释中需要有上述完整的内容，
 * 生成代码后会将原有代码中的手写部分原样拷贝到新生成的文件中。
 */
public class UserCodesMerge implements Merge {
  public static final String userCodesStartAttr = "userCodesStart";
  public static final String userCodesEndAttr   = "userCodesEnd";
  public static final String USER_CODES_START   = "USER CODES START";
  public static final String USER_CODES_END     = "USER CODES END";

  /**
   * 识别行分隔符
   *
   * @param sample
   * @return
   */
  public static String getLineSeparator(String sample) {
    if (StrUtil.isEmpty(sample)) {
      //使用系统换行符
    } else if (sample.indexOf("\r\n") > -1) {
      return "\r\n";
    } else if (sample.indexOf("\n") > -1) {
      return "\n";
    } else if (sample.indexOf("\r") > -1) {
      return "\r";
    }
    return LineBreak.getPlatformLineBreak().toString();
  }

  @Override
  public String merge(Context context, Structure structure, Map<String, Object> params, String fileName, String before, String after) {
    String start = structure.getAttr(userCodesStartAttr, USER_CODES_START);
    String end = structure.getAttr(userCodesEndAttr, USER_CODES_END);

    StringBuffer merge = new StringBuffer(before.length() + after.length());
    String bs = getLineSeparator(before);
    String as = getLineSeparator(after);
    //前后不相同时使用系统默认
    String separator = bs.equals(as) ? bs : System.getProperty("line.separator");
    String[] befores = before.split(bs);
    String[] afters = after.split(as);
    int aIndex = 0, bIndex = 0;
    while (aIndex < afters.length) {
      //新文件读到用户代码开始
      aIndex = readTo(merge, afters, aIndex, start, separator, true);
      //新生成的文件读到头时就要结束，否则会超出模板的整体范围
      //模板可以增加用户自定义块，但是生成的文件上不能新增自定义块，
      //重新生成时如果模板上没有和新增块对应的块，这个自定义块会被移除。
      if (aIndex == afters.length) {
        break;
      }
      //旧文件跳过用户代码前的部分
      bIndex = skipTo(befores, bIndex, start) + 1;
      //旧文件读取用户代码部分
      bIndex = readTo(merge, befores, bIndex, end, separator, false);
      //新文件跳过用户代码空白部分
      aIndex = skipTo(afters, aIndex, end);
    }
    return merge.toString();
  }

  /**
   * 读取指定的内容
   *
   * @param merge     拼接内容
   * @param lines     所有行
   * @param start     开始的的行号
   * @param to        查找到的模板字符串所在行
   * @param separator 行分隔符
   * @param includeTo 是否包含 to 行
   * @return
   */
  public int readTo(StringBuffer merge, String[] lines, int start, String to, String separator, boolean includeTo) {
    boolean contains;
    for (; start < lines.length; start++) {
      contains = lines[start].contains(to);
      if (!contains || includeTo) {
        if (merge.length() > 0) {
          merge.append(separator);
        }
        merge.append(lines[start]);
      }
      if (contains) {
        return start;
      }
    }
    return start;
  }

  /**
   * 跳过指定的内容
   *
   * @param lines
   * @param start
   * @param to
   * @return
   */
  public int skipTo(String[] lines, int start, String to) {
    for (; start < lines.length; start++) {
      if (lines[start].contains(to)) {
        return start;
      }
    }
    return start;
  }
}
