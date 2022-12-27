package ${package};

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

<#list it.importJavaTypes as javaType>
import ${javaType};
</#list>

/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
@Getter
@Setter
@Table(name = "${it.name}")
public class ${it.name.className} {
  <#list it.columns as column>
  <#if column.pk>
  @Id
  @GeneratedValue(generator = "JDBC")
  </#if>
  @Column(name = "${column.name}")
  private ${column.javaType} ${column.name.fieldName};

  </#list>
}
