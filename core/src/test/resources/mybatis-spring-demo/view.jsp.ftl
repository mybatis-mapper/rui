<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${it.comment}信息</title>
    <link href="${"$"}{pageContext.request.contextPath}/static/css/style.css" rel="stylesheet" type="text/css"/>
</head>
<body style="margin-top:50px;overflow: hidden;">
<form action="${"$"}{pageContext.request.contextPath}/${it.name.fieldName.s}" method="post">
    <table class="gridtable" style="width:800px;">
        <tr>
            <th colspan="4">${it.comment}信息 - [<a href="${"$"}{pageContext.request.contextPath}/${it.name.fieldName.s}">返回</a>]
            </th>
        </tr>
        <#list it.columns as column>
            <tr>
                <th><#if column.comment != "">${column.comment}<#else>${column.name.fieldName}</#if>：</th>
                <td><input type="text" name="${column.name.fieldName}"
                           value="${"$"}{${it.name.fieldName}.${column.name.fieldName}}"/></td>
            </tr>
        </#list>
        <tr>
            <td colspan="4"><input type="submit" value="保存"/></td>
        </tr>
    </table>
</form>
</body>
</html>
