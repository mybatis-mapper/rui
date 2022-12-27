package ${package};

<#list it.importJavaTypes as javaType>
    import ${javaType};
</#list>

/**
* ${it.comment}
*/
public class ${it.name.className}QueryModel extends QueryModel {

private ${it.name.className} ${it.name.fieldName};

public ${it.name.className}QueryModel() {
this(new ${it.name.className}());
}

public ${it.name.className}QueryModel(${it.name.className} ${it.name.fieldName}) {
this.${it.name.fieldName} = ${it.name.fieldName};
}

<#list it.columns as column>
    /**
    * 获取${column.comment}
    *
    * @return ${column.name.fieldName} - ${column.comment}
    */
    public ${column.javaType} get${column.name.className}() {
    return ${it.name.fieldName}.get${column.name.className}();
    }

    /**
    * 设置${column.comment}
    *
    * @param ${column.name.fieldName} ${column.comment}
    */
    public void set${column.name.className}(${column.javaType} ${column.name.fieldName}) {
    ${it.name.fieldName}.set${column.name.className}(${column.name.fieldName});
    }

</#list>
}
