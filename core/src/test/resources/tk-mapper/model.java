package ${package};

import javax.persistence.*;

<#list it.importJavaTypes as javaType>
import ${javaType};
</#list>

/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
@Table(name = "${it.name}")
public class ${it.name.className} {
  <#-- 下面这段只是为了演示 tk.mapper 中的 generateColumnConsts 用法，实际不需要这种变量 -->
  <#if project.attrs.generateColumnConsts?? && project.attrs.generateColumnConsts == 'true'>
  <#list it.columns as column>
  public static final String ${column.name.uppercase} = "${column.name}";
  </#list>
  </#if>

  <#list it.columns as column>
  <#if column.pk>
  @Id
  @GeneratedValue(generator = "JDBC")
  </#if>
  @Column(name = "${column.name}")
  private ${column.javaType} ${column.name.fieldName};

  </#list>

  <#list it.columns as column>
  /**
   * 获取 ${column.comment}
   *
   * @return ${column.name.fieldName} - ${column.comment}
   */
  public ${column.javaType} get${column.name.className}() {
    return ${column.name.fieldName};
  }

  /**
   * 设置${column.comment}
   *
   * @param ${column.name.fieldName} ${column.comment}
   */
  public void set${column.name.className}(${column.javaType} ${column.name.fieldName}) {
    this.${column.name.fieldName} = ${column.name.fieldName};
  }

  </#list>
}
