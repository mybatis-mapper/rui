package ${package};

import javax.persistence.*;

<#list it.importJavaTypes as javaType>
    import ${javaType};
</#list>

/**
* ${it.comment}
*/
@Table(name = "${it.name}")
public class ${it.name.className} {
<#list it.columns as column>
    /**
    * ${column.comment}
    */
    <#if column.pk>
        @Id
        @GeneratedValue(generator = "JDBC")
    </#if>
    @Column(name = "${column.name}")
    private ${column.javaType} ${column.name.fieldName};

</#list>

<#list it.columns as column>
    /**
    * 获取${column.comment}
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
