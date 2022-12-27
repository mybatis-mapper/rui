package ${package};

import com.github.pagehelper.PageInfo;

import ${package.parent}.model.${it.name.className};
import ${package.parent}.service.${it.name.className}Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
* ${it.name} - ${it.comment}
*
* @author ${SYS['user.name']}
*/
@Controller
@RequestMapping("${it.name.fieldName.s}")
public class ${it.name.className}Controller {

@Autowired
private ${it.name.className}Service ${it.name.fieldName}Service;

private String page_list = "${it.name.noUnderlineCase}/index";
private String page_view = "${it.name.noUnderlineCase}/view";

private String redirect_list = "redirect:${it.name.fieldName.s}";

@RequestMapping
public ModelAndView getList(${it.name.className} ${it.name.fieldName},
@RequestParam(required = false, defaultValue = "1") int page,
@RequestParam(required = false, defaultValue = "10") int rows) {
ModelAndView result = new ModelAndView(page_list);
List<${it.name.className}> ${it.name.fieldName}List = ${it.name.fieldName}Service.selectPage(${it.name.fieldName}, page, rows);
result.addObject("pageInfo", new PageInfo<${it.name.className}>(${it.name.fieldName}List));
result.addObject("queryParam", ${it.name.fieldName});
result.addObject("page", page);
result.addObject("rows", rows);
return result;
}

@RequestMapping(value = "view", method = RequestMethod.GET)
public ModelAndView view(${it.name.className} ${it.name.fieldName}) {
ModelAndView result = new ModelAndView(page_view);
if (${it.name.fieldName}.getId() != null) {
${it.name.fieldName} = ${it.name.fieldName}Service.selectByKey(${it.name.fieldName}.getId());
}
result.addObject("${it.name.fieldName}", ${it.name.fieldName});
return result;
}

@RequestMapping(value = "save", method = RequestMethod.POST)
public ModelAndView save(${it.name.className} ${it.name.fieldName}) {
ModelAndView result = new ModelAndView(redirect_list);
if (${it.name.fieldName}.getId() != null) {
${it.name.fieldName}Service.updateAll(${it.name.fieldName});
} else {
${it.name.fieldName}Service.save(${it.name.fieldName});
}
return result;
}

@RequestMapping(value = "delete", method = RequestMethod.POST)
public String delete(Integer id) {
${it.name.fieldName}Service.delete(id);
return redirect_list;
}

}